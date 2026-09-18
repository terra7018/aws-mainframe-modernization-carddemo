# 呼び出し・データフロー

## CICS 画面遷移

ソース中の `EXEC CICS XCTL` と、`CDEMO-TO-PROGRAM` /
`CDEMO-MENU-OPT-PGMNAME` に設定するリテラルを基にした図です。動的な
メニュー選択は候補を一つのノードにまとめています。`RETURN` は CICS
transaction を再開するための制御であり、下図の XCTL エッジとは区別
しています。

```mermaid
flowchart TD
    S[COSGN00C<br/>signon CC00] -->|admin| A[COADM01C<br/>admin menu CA00]
    S -->|regular| M[COMEN01C<br/>main menu CM00]
    A --> AU[COACTUPC<br/>account update CAUP]
    A --> AV[COACTVWC<br/>account view CAVW]
    A --> CL[COCRDLIC<br/>card list CCLI]
    A --> CD[COCRDSLC<br/>card detail CCDL]
    A --> CU0[COUSR00C<br/>user list CU00]
    A --> CU1[COUSR01C<br/>user add CU01]
    A --> CU2[COUSR02C<br/>user update CU02]
    A --> CU3[COUSR03C<br/>user delete CU03]
    M --> AU
    M --> AV
    M --> CL
    M --> CD
    M --> CU[COBIL00C<br/>bill payment CB00]
    M --> TL[COTRN00C<br/>transaction list CT00]
    M --> RP[CORPT00C<br/>report request CR00]
    TL -->|detail option| TD[COTRN01C<br/>transaction detail CT01]
    TD -->|back| TL
    AU -->|return target| M
    AV -->|return target| M
    CD -->|return target| M
    CU0 --> CU2
    CU0 --> CU3
    RP -->|TDQ JOBS| B[batch report jobs]
```

`COACTUPC`、`COACTVWC`、`COCRDSLC`、`COCRDUPC` などは
`CDEMO-FROM-PROGRAM` または `CDEMO-TO-PROGRAM` を使って呼出元へ戻る
ため、実行時の遷移先は画面操作で変わります。`COSGN00C` はソース中で
`COADM01C` または `COMEN01C` を明示的に XCTL します。CICS transaction
と program の対応は `app/csd/CARDDEMO.CSD` の定義に従っています。

## バッチとデータセット

初期データを indexed VSAM にロードする JCL、日次 posting、後続処理の
関係です。IDCAMS の `REPRO` は図では `load` と表記しています。

```mermaid
flowchart LR
    acctps[ACCTDATA.PS] -->|ACCTFILE.jcl REPRO| acctvs[(ACCTDATA.VSAM.KSDS)]
    cardps[CARDDATA.PS] -->|CARDFILE.jcl REPRO| cardvs[(CARDDATA.VSAM.KSDS)]
    custps[CUSTDATA.PS] -->|CUSTFILE.jcl REPRO| custvs[(CUSTDATA.VSAM.KSDS)]
    xrefps[CARDXREF.PS] -->|XREFFILE.jcl REPRO| xrefvs[(CARDXREF.VSAM.KSDS)]
    tcatps[TCATBALF.PS] -->|TCATBALF.jcl REPRO| tcatvs[(TCATBALF.VSAM.KSDS)]
    traninit[DALYTRAN.PS.INIT] -->|TRANFILE.jcl REPRO| tranvs[(TRANSACT.VSAM.KSDS)]

    daily[DALYTRAN.PS] --> post[POSTTRAN<br/>CBTRN02C]
    tranvs --> post
    xrefvs --> post
    acctvs --> post
    tcatvs --> post
    post -->|更新| tranvs
    post -->|更新| acctvs
    post -->|更新/作成| tcatvs
    post --> rejects[DALYREJS]

    tranvs --> bkp[TRANBKP / COMBTRAN]
    bkp --> transort[TRANREPT<br/>SORT]
    transort --> report[CBTRN03C]
    xrefvs --> report
    typ[(TRANTYPE.VSAM)] --> report
    cat[(TRANCATG.VSAM)] --> report
    report --> tranreport[TRANREPT]

    tcatvs --> interest[INTCALC<br/>CBACT04C]
    xrefvs --> interest
    acctvs --> interest
    disc[(DISCGRP.VSAM)] --> interest
    interest --> systran[SYSTRAN]

    systran --> stmt[CREASTMT<br/>CBSTM03A]
    tranvs --> stmt
    acctvs --> stmt
    custvs --> stmt
    stmt --> statements[STMTFILE / HTMLFILE]
```

`TRANREPT.jcl` は `TRANSACT.VSAM.KSDS` をバックアップし SORT した後、
`CBTRN03C` を実行します。`CREASTMT.JCL` は SORT/IDCAMS の前処理後に
`CBSTM03A` を実行し、プログラム内で `CBSTM03B` を CALL します。
