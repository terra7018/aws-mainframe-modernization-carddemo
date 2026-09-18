#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="${CARDDEMO_REPO:-$(cd "$ROOT/../.." && pwd)}"
INPUTS="$ROOT/inputs"
OUTPUTS="$ROOT/outputs"
TOOLS="$ROOT/tools"
BUILD="$ROOT/build"

mkdir -p "$INPUTS" "$OUTPUTS" "$TOOLS" "$BUILD"
rm -f "$INPUTS"/acctfile.idx "$INPUTS"/xreffile.idx \
      "$INPUTS"/tcatbalf.idx "$INPUTS"/tranfile.idx
rm -f "$OUTPUTS"/*

cp "$REPO/app/data/ASCII/acctdata.txt" "$INPUTS/acctdata.txt"
cp "$REPO/app/data/ASCII/cardxref.txt" "$INPUTS/cardxref.txt"
cp "$REPO/app/data/ASCII/tcatbal.txt" "$INPUTS/tcatbal.txt"
tr -d '\r\n' < "$REPO/app/data/ASCII/dailytran.txt" > "$INPUTS/dailytran.bin"
: > "$INPUTS/tranfile.empty"

cobc -x -I "$REPO/app/cpy" -fsign=EBCDIC \
    -o "$BUILD/CBTRN02C" "$REPO/app/cbl/CBTRN02C.cbl"
for src in "$TOOLS"/load_*.cob "$TOOLS"/dump_*.cob; do
    cobc -x -fsign=EBCDIC -o "${src%.cob}" "$src"
done

INPUTFILE="$INPUTS/acctdata.txt" OUTPUTFILE="$INPUTS/acctfile.idx" \
    "$TOOLS/load_acct"
INPUTFILE="$INPUTS/cardxref.txt" OUTPUTFILE="$INPUTS/xreffile.idx" \
    "$TOOLS/load_xref"
INPUTFILE="$INPUTS/tcatbal.txt" OUTPUTFILE="$INPUTS/tcatbalf.idx" \
    "$TOOLS/load_tcat"
INPUTFILE="$INPUTS/tranfile.empty" OUTPUTFILE="$INPUTS/tranfile.idx" \
    "$TOOLS/load_tran"

set +e
DD_DALYTRAN="$INPUTS/dailytran.bin" \
DD_TRANFILE="$INPUTS/tranfile.idx" \
DD_XREFFILE="$INPUTS/xreffile.idx" \
DD_DALYREJS="$OUTPUTS/dalyrejs.dat" \
DD_ACCTFILE="$INPUTS/acctfile.idx" \
DD_TCATBALF="$INPUTS/tcatbalf.idx" \
    "$BUILD/CBTRN02C" > "$OUTPUTS/cbtrn02c.stdout" 2>&1
rc=$?
set -e
printf 'exit=%s\n' "$rc" > "$OUTPUTS/exit_code.txt"

INPUTFILE="$INPUTS/tranfile.idx" OUTPUTFILE="$OUTPUTS/tranfile.txt" \
    "$TOOLS/dump_tran"
INPUTFILE="$INPUTS/acctfile.idx" OUTPUTFILE="$OUTPUTS/acctfile.txt" \
    "$TOOLS/dump_acct"
INPUTFILE="$INPUTS/tcatbalf.idx" OUTPUTFILE="$OUTPUTS/tcatbalf.txt" \
    "$TOOLS/dump_tcat"
sort -o "$OUTPUTS/tranfile.txt" "$OUTPUTS/tranfile.txt"
sort -o "$OUTPUTS/acctfile.txt" "$OUTPUTS/acctfile.txt"
sort -o "$OUTPUTS/tcatbalf.txt" "$OUTPUTS/tcatbalf.txt"

python3 - "$OUTPUTS" <<'PY'
from pathlib import Path
import sys

out = Path(sys.argv[1])
for name, length in (("tranfile.txt", 350), ("acctfile.txt", 300), ("tcatbalf.txt", 50)):
    path = out / name
    records = [line.rstrip("\r\n") for line in path.read_text().splitlines()]
    path.write_text("".join(record.ljust(length) + "\n" for record in records))
PY

python3 - "$OUTPUTS" <<'PY'
from collections import Counter
from pathlib import Path
import sys

out = Path(sys.argv[1])
raw = (out / "dalyrejs.dat").read_bytes()
records = len(raw) // 430
reasons = Counter(
    raw[i * 430 + 350:i * 430 + 354].decode("ascii", "replace")
    for i in range(records)
)
with (out / "reject_reason_counts.txt").open("w") as f:
    for code in ("0100", "0101", "0102", "0103", "0109"):
        f.write(f"{code} {reasons.get(code, 0)}\n")
with (out / "record_counts.txt").open("w") as f:
    for name in ("tranfile.txt", "acctfile.txt", "tcatbalf.txt"):
        f.write(f"{name} {sum(1 for _ in (out / name).open())}\n")
    f.write(f"dalyrejs.dat {records}\n")
PY

exit "$rc"
