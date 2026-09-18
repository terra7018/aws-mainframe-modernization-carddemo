# CardDemo 調査ドキュメント

## 目次

1. [プログラム一覧](01-program-inventory.md)
2. [呼び出しグラフ](02-call-graph.md)
3. [データ辞書](03-data-dictionary.md)
4. [CBTRN02C バッチ設計](04-batch-posting-CBTRN02C.md)
5. [モダナイゼーション評価](05-modernization-assessment.md)

CardDemo は、顧客・口座・カード・取引を扱うクレジットカード業務の
サンプルアプリケーションです。COBOL を中心に、CICS オンライン画面、
VSAM KSDS、JCL バッチ、BMS map、CICS の CSD 定義で構成され、取引の
日次取込、残高更新、レポート生成を行います。リポジトリには VSAM/MQ 版
のほか、IMS/DB2/MQ 認証サブアプリケーションと DB2 取引種別サブアプリ
ケーションも含まれます。

本資料は、`app/cbl` のヘッダー・`COPY`・`FILE-CONTROL`・`CALL`/`EXEC CICS`、
`app/cpy` の PIC 定義、`app/jcl` の EXEC/DD、`app/csd` とサブアプリの
CSD を実際に読み取って作成しました。データ辞書の offset/length は PIC
句からプログラムで計算し、CBTRN02C の実行結果は
`/home/ubuntu/carddemo-baseline` の GnuCOBOL ベースラインを使用しています。
