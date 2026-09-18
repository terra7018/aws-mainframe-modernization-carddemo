# CBTRN02C Java 21 port

This project ports the `CBTRN02C` COBOL daily-transaction posting flow to
dependency-free Java 21. JUnit 5 tests compare the result with deterministic
GnuCOBOL golden files in `src/test/resources/golden/`.

## Build and test

Use Java 21 explicitly when more than one JDK is installed:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn -q test
```

## CLI sample run

The CLI uses the same DD environment variable names as the COBOL program.
`DD_DALYTRAN` may contain newline-delimited 350-character records or raw
350-byte records. A run writes the reject file and fixed-width store dumps to
the other DD paths.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
rm -rf target/cli && mkdir -p target/cli
cp ../../app/data/ASCII/cardxref.txt target/cli/cardxref.txt
cp ../../app/data/ASCII/acctdata.txt target/cli/acctfile.txt
cp ../../app/data/ASCII/tcatbal.txt target/cli/tcatbal.txt
: > target/cli/tranfile.txt
set +e
DD_DALYTRAN=../../app/data/ASCII/dailytran.txt \
DD_TRANFILE=target/cli/tranfile.txt \
DD_XREFFILE=target/cli/cardxref.txt \
DD_DALYREJS=target/cli/dalyrejs.dat \
DD_ACCTFILE=target/cli/acctfile.txt \
DD_TCATBALF=target/cli/tcatbal.txt \
java -cp target/classes com.carddemo.posttran.Main
rc=$?
set -e
test "$rc" = 4
```

For a clean text-store CLI run, first convert the baseline input stores with
the committed COBOL harness, then use the resulting indexed paths as the
`XREFFILE`, `ACCTFILE`, and `TCATBALF` inputs. The Java CLI's store loader
expects fixed-width text; the simplest equivalence check is performed by the
JUnit test, which loads the repository ASCII files directly.

## Golden comparison command

After `mvn -q test`, the following command runs the same comparison through the
CLI using text stores and compares outputs with the goldens. The processing
timestamp is runtime-generated, so the comparison masks that 26-byte column
before `cmp`.

```bash
rm -rf target/cli && mkdir -p target/cli
cp ../../app/data/ASCII/cardxref.txt target/cli/cardxref.txt
cp ../../app/data/ASCII/acctdata.txt target/cli/acctfile.txt
cp ../../app/data/ASCII/tcatbal.txt target/cli/tcatbal.txt
: > target/cli/tranfile.txt
DD_DALYTRAN=../../app/data/ASCII/dailytran.txt \
DD_TRANFILE=target/cli/tranfile.txt \
DD_XREFFILE=target/cli/cardxref.txt \
DD_DALYREJS=target/cli/dalyrejs.dat \
DD_ACCTFILE=target/cli/acctfile.txt \
DD_TCATBALF=target/cli/tcatbal.txt \
java -cp target/classes com.carddemo.posttran.Main > target/cli/stdout.txt || test "$?" = 4
python3 - <<'PY'
from pathlib import Path
src = Path("target/cli/tranfile.txt")
rows = [line[:304] + " " * 26 + line[330:] for line in src.read_text().splitlines()]
Path("target/cli/tranfile.masked.txt").write_text("\n".join(rows) + "\n")
PY
cmp target/cli/dalyrejs.dat src/test/resources/golden/dalyrejs.dat
cmp target/cli/tranfile.masked.txt src/test/resources/golden/tranfile.txt
cmp target/cli/acctfile.txt src/test/resources/golden/acctfile.txt
cmp target/cli/tcatbal.txt src/test/resources/golden/tcatbal.txt
cmp target/cli/stdout.txt src/test/resources/golden/stdout.txt
```

## COBOL-to-Java mapping

| COBOL paragraph | Java |
|---|---|
| `1500-VALIDATE-TRAN` / `1500-A-LOOKUP-XREF` / `1500-B-LOOKUP-ACCT` | `PostTransactionJob.run` validation |
| `2000-POST-TRANSACTION` | `DailyTransaction.asPosted` and `PostTransactionJob.run` |
| `2500-WRITE-REJECT-REC` | `PostTransactionJob.reject` |
| `2700-UPDATE-TCATBAL` | `TranCatBalance` plus `PostTransactionJob.run` |
| `2800-UPDATE-ACCOUNT-REC` | `Account.post` |
| `2900-WRITE-TRANSACTION-FILE` | `IndexedStore<Transaction>.write` |
| `Z-GET-DB2-FORMAT-TIMESTAMP` | `PostTransactionJob` timestamp formatter |
| `FILE-CONTROL` indexed files | `IndexedStore` |
