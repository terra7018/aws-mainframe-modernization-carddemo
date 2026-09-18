# データ辞書

以下の offset は各レコード内の **0-based byte offset**、length は PIC の
表示形式での長さです。`X(n)` は英数字、`9(n)` は符号なし zoned
decimal、`S9(n)V99` は EBCDIC overpunch を含む signed zoned decimal
（長さ `n+2`）、`COMP-3` は packed decimal として扱います。`FILLER` も
レコード長を再現するため掲載しています。意味はフィールド名とソースの
利用箇所から短く記載しています。

## CVACT01Y.cpy — ACCOUNT-RECORD (300)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| ACCT-ID | 9(11) | 0 | 11 | zoned decimal / 口座 ID、primary key |
| ACCT-ACTIVE-STATUS | X(01) | 11 | 1 | alphanumeric / 有効状態 |
| ACCT-CURR-BAL | S9(10)V99 | 12 | 12 | signed zoned / 現在残高 |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 24 | 12 | signed zoned / credit limit |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 36 | 12 | signed zoned / cash credit limit |
| ACCT-OPEN-DATE | X(10) | 48 | 10 | alphanumeric / 開設日 |
| ACCT-EXPIRAION-DATE | X(10) | 58 | 10 | alphanumeric / 有効期限（原綴り） |
| ACCT-REISSUE-DATE | X(10) | 68 | 10 | alphanumeric / 再発行日 |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 78 | 12 | signed zoned / current cycle credit |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 90 | 12 | signed zoned / current cycle debit |
| ACCT-ADDR-ZIP | X(10) | 102 | 10 | alphanumeric / 郵便番号 |
| ACCT-GROUP-ID | X(10) | 112 | 10 | alphanumeric / account group |
| FILLER | X(178) | 122 | 178 | reserved / 未使用領域 |

## CVACT02Y.cpy — CARD-RECORD (150)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| CARD-NUM | X(16) | 0 | 16 | alphanumeric / card number |
| CARD-ACCT-ID | 9(11) | 16 | 11 | zoned decimal / 紐付く口座 ID |
| CARD-CVV-CD | 9(03) | 27 | 3 | zoned decimal / CVV |
| CARD-EMBOSSED-NAME | X(50) | 30 | 50 | alphanumeric / 刻印名 |
| CARD-EXPIRAION-DATE | X(10) | 80 | 10 | alphanumeric / 有効期限 |
| CARD-ACTIVE-STATUS | X(01) | 90 | 1 | alphanumeric / 有効状態 |
| FILLER | X(59) | 91 | 59 | reserved / 未使用領域 |

## CVACT03Y.cpy — CARD-XREF-RECORD (50)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| XREF-CARD-NUM | X(16) | 0 | 16 | alphanumeric / card number key |
| XREF-CUST-ID | 9(09) | 16 | 9 | zoned decimal / customer ID |
| XREF-ACCT-ID | 9(11) | 25 | 11 | zoned decimal / account ID |
| FILLER | X(14) | 36 | 14 | reserved / 未使用領域 |

## CVCUS01Y.cpy — CUSTOMER-RECORD (500)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| CUST-ID | 9(09) | 0 | 9 | zoned decimal / customer ID |
| CUST-FIRST-NAME | X(25) | 9 | 25 | alphanumeric / 名 |
| CUST-MIDDLE-NAME | X(25) | 34 | 25 | alphanumeric / ミドルネーム |
| CUST-LAST-NAME | X(25) | 59 | 25 | alphanumeric / 姓 |
| CUST-ADDR-LINE-1 | X(50) | 84 | 50 | alphanumeric / 住所1 |
| CUST-ADDR-LINE-2 | X(50) | 134 | 50 | alphanumeric / 住所2 |
| CUST-ADDR-LINE-3 | X(50) | 184 | 50 | alphanumeric / 住所3 |
| CUST-ADDR-STATE-CD | X(02) | 234 | 2 | alphanumeric / 州コード |
| CUST-ADDR-COUNTRY-CD | X(03) | 236 | 3 | alphanumeric / 国コード |
| CUST-ADDR-ZIP | X(10) | 239 | 10 | alphanumeric / 郵便番号 |
| CUST-PHONE-NUM-1 | X(15) | 249 | 15 | alphanumeric / 電話番号1 |
| CUST-PHONE-NUM-2 | X(15) | 264 | 15 | alphanumeric / 電話番号2 |
| CUST-SSN | 9(09) | 279 | 9 | zoned decimal / SSN |
| CUST-GOVT-ISSUED-ID | X(20) | 288 | 20 | alphanumeric / 政府発行 ID |
| CUST-DOB-YYYY-MM-DD | X(10) | 308 | 10 | alphanumeric / 生年月日 |
| CUST-EFT-ACCOUNT-ID | X(10) | 318 | 10 | alphanumeric / EFT account |
| CUST-PRI-CARD-HOLDER-IND | X(01) | 328 | 1 | alphanumeric / primary cardholder indication |
| CUST-FICO-CREDIT-SCORE | 9(03) | 329 | 3 | zoned decimal / FICO score |
| FILLER | X(168) | 332 | 168 | reserved / 未使用領域 |

## CVTRA01Y.cpy — TRAN-CAT-BAL-RECORD (50)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| TRANCAT-ACCT-ID | 9(11) | 0 | 11 | zoned decimal / account key |
| TRANCAT-TYPE-CD | X(02) | 11 | 2 | alphanumeric / transaction type |
| TRANCAT-CD | 9(04) | 13 | 4 | zoned decimal / category |
| TRAN-CAT-BAL | S9(09)V99 | 17 | 11 | signed zoned / category balance |
| FILLER | X(22) | 28 | 22 | reserved / 未使用領域 |

## CVTRA02Y.cpy — DIS-GROUP-RECORD (50)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| DIS-ACCT-GROUP-ID | X(10) | 0 | 10 | alphanumeric / account group |
| DIS-TRAN-TYPE-CD | X(02) | 10 | 2 | alphanumeric / transaction type |
| DIS-TRAN-CAT-CD | 9(04) | 12 | 4 | zoned decimal / category |
| DIS-INT-RATE | S9(04)V99 | 16 | 6 | signed zoned / interest rate |
| FILLER | X(28) | 22 | 28 | reserved / 未使用領域 |

## CVTRA03Y.cpy — TRAN-TYPE-RECORD (60)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| TRAN-TYPE | X(02) | 0 | 2 | alphanumeric / transaction type |
| TRAN-TYPE-DESC | X(50) | 2 | 50 | alphanumeric / type description |
| FILLER | X(08) | 52 | 8 | reserved / 未使用領域 |

## CVTRA04Y.cpy — TRAN-CAT-RECORD (60)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| TRAN-TYPE-CD | X(02) | 0 | 2 | alphanumeric / type key |
| TRAN-CAT-CD | 9(04) | 2 | 4 | zoned decimal / category key |
| TRAN-CAT-TYPE-DESC | X(50) | 6 | 50 | alphanumeric / category description |
| FILLER | X(04) | 56 | 4 | reserved / 未使用領域 |

## CVTRA05Y.cpy — TRAN-RECORD (350) / CVTRA06Y.cpy — DALYTRAN-RECORD (350)

両レコードは同一の byte layout です。`TRAN-*` は post 後の master、
`DALYTRAN-*` は daily input の名前です。

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| TRAN-ID / DALYTRAN-ID | X(16) | 0 | 16 | alphanumeric / transaction ID |
| TRAN-TYPE-CD / DALYTRAN-TYPE-CD | X(02) | 16 | 2 | alphanumeric / type |
| TRAN-CAT-CD / DALYTRAN-CAT-CD | 9(04) | 18 | 4 | zoned decimal / category |
| TRAN-SOURCE / DALYTRAN-SOURCE | X(10) | 22 | 10 | alphanumeric / source |
| TRAN-DESC / DALYTRAN-DESC | X(100) | 32 | 100 | alphanumeric / description |
| TRAN-AMT / DALYTRAN-AMT | S9(09)V99 | 132 | 11 | signed zoned / amount |
| TRAN-MERCHANT-ID / DALYTRAN-MERCHANT-ID | 9(09) | 143 | 9 | zoned decimal / merchant ID |
| TRAN-MERCHANT-NAME / DALYTRAN-MERCHANT-NAME | X(50) | 152 | 50 | alphanumeric / merchant name |
| TRAN-MERCHANT-CITY / DALYTRAN-MERCHANT-CITY | X(50) | 202 | 50 | alphanumeric / merchant city |
| TRAN-MERCHANT-ZIP / DALYTRAN-MERCHANT-ZIP | X(10) | 252 | 10 | alphanumeric / merchant ZIP |
| TRAN-CARD-NUM / DALYTRAN-CARD-NUM | X(16) | 262 | 16 | alphanumeric / card number |
| TRAN-ORIG-TS / DALYTRAN-ORIG-TS | X(26) | 278 | 26 | alphanumeric / source timestamp |
| TRAN-PROC-TS / DALYTRAN-PROC-TS | X(26) | 304 | 26 | alphanumeric / processing timestamp |
| FILLER | X(20) | 330 | 20 | reserved / 未使用領域 |

## CVTRA07Y.cpy — report layouts

| Record / Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| REPORT-NAME-HEADER / REPT-SHORT-NAME | X(38) | 0 | 38 | report short name |
| REPORT-NAME-HEADER / REPT-LONG-NAME | X(41) | 38 | 41 | report long name |
| REPORT-NAME-HEADER / REPT-DATE-HEADER | X(12) | 79 | 12 | date-range label |
| REPORT-NAME-HEADER / REPT-START-DATE | X(10) | 91 | 10 | report start date |
| REPORT-NAME-HEADER / FILLER | X(04) | 101 | 4 | literal ` to ` |
| REPORT-NAME-HEADER / REPT-END-DATE | X(10) | 105 | 10 | report end date |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-TRANS-ID | X(16) | 0 | 16 | transaction ID |
| TRANSACTION-DETAIL-REPORT / FILLER | X(01) | 16 | 1 | separator |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-ACCOUNT-ID | X(11) | 17 | 11 | account ID |
| TRANSACTION-DETAIL-REPORT / FILLER | X(01) | 28 | 1 | separator |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-TYPE-CD | X(02) | 29 | 2 | transaction type |
| TRANSACTION-DETAIL-REPORT / FILLER | X(01) | 31 | 1 | separator |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-TYPE-DESC | X(15) | 32 | 15 | type description |
| TRANSACTION-DETAIL-REPORT / FILLER | X(01) | 47 | 1 | separator |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-CAT-CD | 9(04) | 48 | 4 | category |
| TRANSACTION-DETAIL-REPORT / FILLER | X(01) | 52 | 1 | separator |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-CAT-DESC | X(29) | 53 | 29 | category description |
| TRANSACTION-DETAIL-REPORT / FILLER | X(01) | 82 | 1 | separator |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-SOURCE | X(10) | 83 | 10 | source |
| TRANSACTION-DETAIL-REPORT / FILLER | X(04) | 93 | 4 | spacing |
| TRANSACTION-DETAIL-REPORT / TRAN-REPORT-AMT | -ZZZ,ZZZ,ZZZ.ZZ | 97 | 15 | edited numeric / formatted amount |
| TRANSACTION-DETAIL-REPORT / FILLER | X(02) | 112 | 2 | spacing |
| TRANSACTION-HEADER-2 | X(133) | 0 | 133 | separator line |
| REPORT-PAGE-TOTALS / REPT-PAGE-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | 97 | 15 | edited numeric / page total |
| REPORT-ACCOUNT-TOTALS / REPT-ACCOUNT-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | 97 | 15 | edited numeric / account total |
| REPORT-GRAND-TOTALS / REPT-GRAND-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | 97 | 15 | edited numeric / grand total |

## CVCRD01Y.cpy — CC-WORK-AREAS

これは固定長 entity record ではなく、CICS の画面/COMMAREA work area
です。`REDEFINES` された numeric 名は同じ byte offset を共有します。

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| CCARD-AID | X(5) | 0 | 5 | alphanumeric / AID |
| CCARD-NEXT-PROG | X(8) | 5 | 8 | alphanumeric / 次 program |
| CCARD-NEXT-MAPSET | X(7) | 13 | 7 | alphanumeric / 次 mapset |
| CCARD-NEXT-MAP | X(7) | 20 | 7 | alphanumeric / 次 map |
| CCARD-ERROR-MSG | X(75) | 27 | 75 | alphanumeric / error message |
| CCARD-RETURN-MSG | X(75) | 102 | 75 | alphanumeric / return message |
| CC-ACCT-ID / CC-ACCT-ID-N | X(11) / 9(11) | 177 | 11 | account ID（表示/数値 view） |
| CC-CARD-NUM / CC-CARD-NUM-N | X(16) / 9(16) | 188 | 16 | card number（表示/数値 view） |
| CC-CUST-ID / CC-CUST-ID-N | X(09) / 9(9) | 204 | 9 | customer ID（表示/数値 view） |

## CUSTREC.cpy — CUSTOMER-RECORD (500)

`CUSTREC` は `CVCUS01Y` と同じ byte layout ですが、生年月日のフィールド名
だけが異なります。

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| CUST-ID | 9(09) | 0 | 9 | zoned decimal / customer ID |
| CUST-FIRST-NAME | X(25) | 9 | 25 | alphanumeric / 名 |
| CUST-MIDDLE-NAME | X(25) | 34 | 25 | alphanumeric / ミドルネーム |
| CUST-LAST-NAME | X(25) | 59 | 25 | alphanumeric / 姓 |
| CUST-ADDR-LINE-1 | X(50) | 84 | 50 | alphanumeric / 住所1 |
| CUST-ADDR-LINE-2 | X(50) | 134 | 50 | alphanumeric / 住所2 |
| CUST-ADDR-LINE-3 | X(50) | 184 | 50 | alphanumeric / 住所3 |
| CUST-ADDR-STATE-CD | X(02) | 234 | 2 | alphanumeric / 州コード |
| CUST-ADDR-COUNTRY-CD | X(03) | 236 | 3 | alphanumeric / 国コード |
| CUST-ADDR-ZIP | X(10) | 239 | 10 | alphanumeric / 郵便番号 |
| CUST-PHONE-NUM-1 | X(15) | 249 | 15 | alphanumeric / 電話番号1 |
| CUST-PHONE-NUM-2 | X(15) | 264 | 15 | alphanumeric / 電話番号2 |
| CUST-SSN | 9(09) | 279 | 9 | zoned decimal / SSN |
| CUST-GOVT-ISSUED-ID | X(20) | 288 | 20 | alphanumeric / 政府発行 ID |
| CUST-DOB-YYYYMMDD | X(10) | 308 | 10 | alphanumeric / 生年月日 |
| CUST-EFT-ACCOUNT-ID | X(10) | 318 | 10 | alphanumeric / EFT account |
| CUST-PRI-CARD-HOLDER-IND | X(01) | 328 | 1 | alphanumeric / primary cardholder indication |
| CUST-FICO-CREDIT-SCORE | 9(03) | 329 | 3 | zoned decimal / FICO score |
| FILLER | X(168) | 332 | 168 | reserved / 未使用領域 |

## CSUSR01Y.cpy — SEC-USER-DATA (80)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| SEC-USR-ID | X(08) | 0 | 8 | alphanumeric / user ID |
| SEC-USR-FNAME | X(20) | 8 | 20 | alphanumeric / first name |
| SEC-USR-LNAME | X(20) | 28 | 20 | alphanumeric / last name |
| SEC-USR-PWD | X(08) | 48 | 8 | alphanumeric / password field |
| SEC-USR-TYPE | X(01) | 56 | 1 | alphanumeric / user type |
| SEC-USR-FILLER | X(23) | 57 | 23 | reserved / 未使用領域 |

## COSTM01.CPY — TRNX-RECORD (350)

| Field | PIC | offset | length | type / 意味 |
|---|---|---:|---:|---|
| TRNX-CARD-NUM | X(16) | 0 | 16 | alphanumeric / card number key |
| TRNX-ID | X(16) | 16 | 16 | alphanumeric / transaction ID key |
| TRNX-TYPE-CD | X(02) | 32 | 2 | alphanumeric / type |
| TRNX-CAT-CD | 9(04) | 34 | 4 | zoned decimal / category |
| TRNX-SOURCE | X(10) | 38 | 10 | alphanumeric / source |
| TRNX-DESC | X(100) | 48 | 100 | alphanumeric / description |
| TRNX-AMT | S9(09)V99 | 148 | 11 | signed zoned / amount |
| TRNX-MERCHANT-ID | 9(09) | 159 | 9 | zoned decimal / merchant ID |
| TRNX-MERCHANT-NAME | X(50) | 168 | 50 | alphanumeric / merchant name |
| TRNX-MERCHANT-CITY | X(50) | 218 | 50 | alphanumeric / merchant city |
| TRNX-MERCHANT-ZIP | X(10) | 268 | 10 | alphanumeric / merchant ZIP |
| TRNX-ORIG-TS | X(26) | 278 | 26 | alphanumeric / source timestamp |
| TRNX-PROC-TS | X(26) | 304 | 26 | alphanumeric / processing timestamp |
| FILLER | X(20) | 330 | 20 | reserved / 未使用領域 |
