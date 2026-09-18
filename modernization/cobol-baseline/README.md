# CBTRN02C GnuCOBOL baseline

This directory contains the reproducible GnuCOBOL harness used to produce the
Java port's golden files. Generated indexed files and build products are
ignored; the committed COBOL source tools are under `tools/`.

このディレクトリは、`app/cbl/CBTRN02C.cbl` を GnuCOBOL 3.1.2.0
で実行した再現可能なベースラインです。作成時の環境は Ubuntu 22.04.5
で、GnuCOBOL は `sudo apt-get install -y gnucobol` で導入しました。

## コンパイラ

```text
cobc (GnuCOBOL) 3.1.2.0
```

アプリケーションのコンパイルコマンドは次のとおりです。

```bash
cobc -x -I ../../app/cpy -fsign=EBCDIC \
  -o build/CBTRN02C \
  ../../app/cbl/CBTRN02C.cbl
```

`-fsign=EBCDIC` は GnuCOBOL 3.1.2 の `cobc --help` にある
`-fsign=[ASCII|EBCDIC]` を使用しています。これにより ASCII サンプルの
EBCDIC overpunch (`{`, `}`, `A`--`R`) を display signed numeric として
解釈できます。

## 入力変換

`CBTRN02C` の VSAM 相当ファイルは GnuCOBOL の indexed file に変換しました。
`tools/load_*.cob` は LINE SEQUENTIAL の固定長テキストを読み、次のキーで
indexed file を作成します。

| ファイル | レコード長 | キー |
| --- | ---: | --- |
| TRANFILE | 350 | `X(16)`、offset 0 |
| XREFFILE | 50 | `X(16)`、offset 0 |
| ACCTFILE | 300 | `9(11)`、offset 0 |
| TCATBALF | 50 | `9(11)+X(2)+9(4)`、offset 0 |

TRANFILE は空の入力から作成しました。`dailytran.txt` は 350 バイト固定長
レコードのため、改行を除去して `inputs/dailytran.bin` を作成しました。
実行時の DD 割り当ては次の形式です。

```bash
DD_DALYTRAN=.../inputs/dailytran.bin \
DD_TRANFILE=.../inputs/tranfile.idx \
DD_XREFFILE=.../inputs/xreffile.idx \
DD_DALYREJS=.../outputs/dalyrejs.dat \
DD_ACCTFILE=.../inputs/acctfile.idx \
DD_TCATBALF=.../inputs/tcatbalf.idx \
.../build/CBTRN02C
```

GnuCOBOL は `ASSIGN TO DALYTRAN` などを `DD_DALYTRAN` 環境変数で解決します。

## 実行結果

実行 stdout は `outputs/cbtrn02c.stdout`、戻り値は
`outputs/exit_code.txt`、430 バイト固定長の reject file は
`outputs/dalyrejs.dat` に保存しています。

```text
TRANSACTIONS PROCESSED :000000300
TRANSACTIONS REJECTED  :000000038
exit=4
```

reject trailer は各 430 バイトレコードの offset 350 から 4 バイトを
読みました。結果は次のとおりです（ファイル上のゼロ埋めコードも併記）。

| reason | 件数 |
| --- | ---: |
| 100 (`0100`) | 0 |
| 101 (`0101`) | 0 |
| 102 (`0102`) | 38 |
| 103 (`0103`) | 0 |
| 109 (`0109`) | 0 |

実行中に作成された TCATBAL レコードは stdout の
`TCATBAL record not found for key ... Creating.` 行に記録されています。
実行後の indexed file の固定長ダンプは
`outputs/tranfile.txt`、`outputs/acctfile.txt`、`outputs/tcatbalf.txt` にあり、
いずれもキーでソート済みです。GnuCOBOL の LINE SEQUENTIAL writer が末尾の
空白を省略するため、スクリプトは dumper の各行をレコード長（350/300/50）
まで右 padding して golden 比較用の固定長に戻します。件数は順に 262、50、
100 です。

`TRAN-PROC-TS` は実行時に `FUNCTION CURRENT-DATE` から作られるため、
実行ごとに変わります。`CVTRA05Y` のレイアウトから計算した 0-based offset
は 304、長さ 26 です（`TRAN-ORIG-TS` は offset 278）。baseline の実行では
例として `2026-09-18-08.34.39.240000` が入りました。資料上で offset 293
と記載される場合は、ソースの copybook レイアウトと照合してください。

## 再実行

リポジトリを `/home/ubuntu/repos/carddemo` 以外に置く場合は
`CARDDEMO_REPO` を指定します。スクリプトは入力を再構築し、loader と
アプリを再コンパイルし、indexed file と出力を削除して最初から実行します。

```bash
./run_baseline.sh
# reject があるため終了コードは 4
```
