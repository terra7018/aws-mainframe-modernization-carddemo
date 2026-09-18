# モダナイゼーション評価

## 主なリスク

### データ型と符号

- `S9(n)V99` は COBOL の display numeric であり、GnuCOBOL baseline では
  `-fsign=EBCDIC` を使って overpunch（`{`、`}`、`A`–`R` など）を解釈した。
  Java/REST の decimal に移す際は、符号の nibble と小数点位置を明示的に
  変換し、単なる ASCII 文字列置換にしない。
- `COMP-3`/packed decimal は `CVEXPORT.cpy` の export balance/amount や
  `CBTRN03C` の counters などに存在し、`COMP` binary は各プログラムの
  status/counter にも使われる。byte length は PIC の桁数と異なるため、
  DB/RDB への移行時は scale、precision、負数をテストする。
- `PIC -ZZZ,ZZZ,ZZZ.ZZ` など edited numeric は presentation format であり、
  永続化する値と帳票表示を分離する。
- 固定長レコードは field 名の typo も契約の一部である。例えば
  `ACCT-EXPIRAION-DATE` はソースの綴りを保持する。offset/length を変える
  と既存ファイルと互換性がなくなる。

### VSAM から RDB

`ACCTFILE`、`XREFFILE`、`TRANFILE`、`TCATBALF` は KSDS の key と
固定長 layout に依存する。RDB 化では次のように自然キーと履歴を分離する。

| VSAM | 初期 RDB 案 | 注意点 |
|---|---|---|
| ACCTFILE | `account` | account ID の numeric/zero padding、残高更新の atomicity |
| XREFFILE | `card_account` | card number unique、customer/account FK |
| TRANFILE | `posted_transaction` | transaction ID unique、processing timestamp と source timestamp |
| TCATBALF | `transaction_category_balance` | `(account_id, type_cd, category_cd)` 複合 unique key |

まず byte-for-byte の landing table を用意し、業務テーブルへの変換を別段階
にする。符号・日付・空白埋めを変換する前に、元レコードを保持できるように
する。

### CICS から REST

CICS program は COMMAREA/work area、AID、mapset、XCTL による画面状態を
共有する。直接 REST endpoint に置き換えるのではなく、最初に
`COACTVWC`/`COACTUPC` の read/update 境界と認証・認可を分離する。transaction
ID、画面の error message、戻り先を API の status/error model に写像し、
二重送信、optimistic locking、監査 ID を追加する。

### JCL から scheduler

JCL は dataset の DISP、GDG/一時データ、COND、SORT、IDCAMS の順序で
依存関係を表現している。scheduler DAG へ変換する際は、単に EXEC PGM
だけを移さず、入力 dataset、正常終了条件、reject file、再実行時の
idempotency を明示する。`POSTTRAN` の return code 4 は reject を含む
業務結果であり、scheduler の技術的 failure と同一視しない。

## 推奨する増分

1. **観測可能な batch 境界を作る**: まず `CBTRN02C` を compile/run できる
   harness に固定し、inputs、return code、reject trailer、indexed dump を
   golden artifact として保存する。
2. **計算ロジックを分離する**: `CBACT04C` の interest/fee 計算を、VSAM
   read/write から分離した pure calculation として再実装する。元 COBOL と
   同一の zoned/packed decimal rounding を比較する。
3. **read-only adapter**: XREFFILE/ACCTFILE/TCATBALF/TRANFILE を最初は
   RDB の read adapter から提供し、posting writer は一つに保つ。
4. **posting の切替**: account balance、category balance、transaction の
   3 更新を一つの transaction boundary で実行し、旧 COBOL と shadow write
   の結果を比較する。
5. **CICS/REST を段階移行**: read-only account/card view、更新 API、
   user/admin 機能の順で切り替える。最後まで CICS transaction を rollback
   用の参照実装として残す。

## Golden-file equivalence testing

同じ fixed-width input と初期 dataset snapshot に対して COBOL と新実装を
実行し、次を比較します。

- return code、processed/rejected count、reason 100–103/109 の件数
- reject record の 350-byte payload と 80-byte trailer（timestamp は比較時に
  正規化）
- ACCTFILE の残高・cycle credit/debit
- TCATBALF の複合 key と balance
- TRANFILE の field-by-field 内容（`TRAN-PROC-TS` は形式・存在のみ比較）
- sorted key の一意性、record count、欠損/重複

最初の fixture は GnuCOBOL baseline の 300 transaction とし、card not found、
account not found、overlimit、expired、TCATBAL new-key、負数 amount、
EBCDIC overpunch を個別に追加する。入力、初期 snapshot、変換設定、比較
結果を同じ artifact ID で保管すれば、RDB/CICS/scheduler の変更を一つずつ
検証できる。
