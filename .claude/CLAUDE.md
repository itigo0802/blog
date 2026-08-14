# プロジェクト概要

Spring Frameworkの理解を深めるための学習用アプリ。会員制ブログ(認証系)を実装する。
前段階として TODOアプリ (itigo0802/todo-app) をJavaで作成済み。今回はその次のステップとして、
認証・認可を含むより実務に近い構成に挑戦する。

## 学習目的

- Spring Securityによる認証・認可の実装パターンを理解する
- MyBatis(XML方式)によるSQLベースのデータアクセスに慣れる
- ロールベースのアクセス制御、CSRF対策など実務で必要な知識を身につける

## 学習方針

### 前提知識
- TODOアプリ(itigo0802/todo-app)はSpringを使わない素のJavaで実装済み。CRUDや基本設計の考え方はそこで経験済みだが、Spring自体(DI、MVC、Bean管理など)は触ったことはあるものの理解は浅い
- Spring Securityも同様に「用語は知っているが自分の手で実装した経験は薄い」レベル。`SecurityFilterChain`や`DaoAuthenticationProvider`などSpring/Spring Securityの基礎概念は、該当箇所の実装時に簡潔に説明する

### 実装の役割分担
学習効果が高い(=このプロジェクトの核心である)部分は人間が書き、定型的な部分はClaudeが用意する。

| 領域 | 誰が書くか | 理由 |
|---|---|---|
| `SecurityConfig`(認証・認可の中核設定) | 人間 | 学習目的そのもの |
| 権限判定ロジック(投稿者本人 or 管理者のチェックなど) | 人間 | ロールベースアクセス制御の要 |
| MyBatis: JOINや動的SQL(`<if>`/`<where>`など)を使う複雑なクエリ | 人間 | MyBatisならではの学びどころ |
| MyBatis: 単純CRUD(1テーブルへの基本INSERT/SELECT/UPDATE/DELETE) | Claude | 定型作業で学習効果が薄い |
| Thymeleafテンプレート(HTML/CSS)、domain/Mapperインターフェースの雛形 | Claude | Spring本体の学習と直接関係が薄いボイラープレート |

### 進め方
- 人間が書く範囲は、Claudeが先に雛形・呼び出し元・関連コードを用意し、`TODO(human)`を残して実装を依頼する
- 詰まった場合は、いきなり完成コードを渡さず、まず考慮すべき観点(ヒント)を提示する。それでも進まない場合は解説付きで実装する

## プロジェクト情報

| 項目 | 値 |
|---|---|
| Group | com.itigotti |
| Artifact / Package | blog / com.itigotti.blog |
| Java | 21 |

## 技術スタック

| 項目 | 選定内容 | 備考 |
|---|---|---|
| ビルドツール | Maven | Spring Initializrで生成 |
| 言語 | Java | 17 or 21 |
| フレームワーク | Spring Boot 4.0.7 | MyBatis対応が4.0.x系までのため4.1.0から変更 |
| Web | Spring Web (MVC) | |
| View | Thymeleaf (SSR) | RESTは採用しない。formLoginとの親和性・CSRFトークンの自動埋め込みを優先 |
| データアクセス | MyBatis (XML方式) | JPAは不採用。SQLを直接記述する |
| DB | H2 (インメモリ) | 学習用途のため。起動が速くイテレーションしやすい |
| 認証 | Spring Security | フォームログイン(セッション方式)。JWT/OAuth2は検討の上、当面は見送り(下記「実装時の注意点」参照) |
| バリデーション | Spring Validation | `@Valid` |
| その他 | Lombok | POJOのgetter/setter等のボイラープレート削減 |

## ディレクトリ構成方針

```
src/main/java/.../
├── domain/          # POJO (User, Post, Comment)
├── mapper/          # MyBatis Mapperインターフェース
├── service/         # ビジネスロジック
├── controller/       # MVCコントローラー(Thymeleafビュー名を返却。RESTは採用しない)
└── config/          # SecurityConfig等

src/main/resources/
├── mapper/          # MyBatis XML (UserMapper.xml, PostMapper.xml...)
├── templates/       # Thymeleafテンプレート (login.html, post-list.html...)
├── schema.sql       # DDL
└── application.yml
```

## データモデル

- **User**: id, username, email(unique), password(BCryptハッシュ), role(USER/ADMIN), enabled(boolean, デフォルトtrue, BAN時にfalse。`UserDetails.isEnabled()`にそのまま対応), created_at
- **Post**: id, title, content, author_id(→User), created_at, updated_at
- **Comment**: id, content, post_id(→Post), author_id(→User), created_at

## 機能・権限設計

| 機能 | 権限 |
|---|---|
| 記事一覧・詳細閲覧 | 誰でも可 |
| 会員登録・ログイン | 誰でも可 |
| 記事投稿・コメント投稿 | ログイン済みユーザー |
| 記事編集・削除 | 投稿者本人 または 管理者 |
| コメント削除(編集は不可) | 投稿者本人 または 管理者 |
| ユーザーBAN | 管理者のみ |

## 実装の進め方(推奨順序)

- [x] 1. Spring Initializrでプロジェクト作成(上記依存関係を選択)
- [x] 2. pom.xmlに`spring-boot-starter-thymeleaf`を追加、DDL(schema.sql)とapplication.ymlの設定(application.propertiesから変換)
- [x] 3. User関連のMapper(XML)実装 → `UserDetailsService`実装
- [x] 4. `SecurityConfig`でフォームログイン・PasswordEncoder(BCrypt)を設定
  - 動作確認用に`AuthController`/`RegisterForm`/`UserService`/`login.html`/`register.html`も作成済み
  - curlで登録→ログイン成功/失敗→CSRF拒否まで手動テスト済み
  - `/error`と`/h2-console/**`をpermitAllにする追加対応が必要だった(下記の注意点を参照)
- [x] 5. Post/CommentのCRUD実装(MyBatis XML)
  - 役割分担通り: 単純CRUD(insert/update/deleteById)はClaude、投稿者usernameをJOINで取得するfindAll/findById/findByPostIdは人間が実装
  - Post: 一覧・詳細・新規作成・編集・削除。Comment: 投稿詳細ページでの一覧表示・投稿・削除
  - curlで登録→ログイン→投稿作成→一覧/詳細表示→コメント投稿→編集→コメント削除→ログアウト→匿名での削除拒否(CSRF token不在時403、有効token時は/loginへ302)まで一通り手動テスト済み
  - `Post`/`Comment`ドメインに`authorUsername`という非永続フィールドを追加し、JOIN結果のみそこに詰める設計にした(insert/updateでは使わない)
  - Post/Comment編集・削除は現時点では「ログイン済みなら誰でも可能」。投稿者本人/管理者チェックはStep6で追加する(コード中に`// 投稿者本人/管理者チェックはStep6で追加する`とコメントあり)
- [x] 6. 認可制御の追加(投稿者本人チェックなど、SpEL or Service層)
  - `security.AuthorizationService#canModify(resourceAuthorId, principal)`に「投稿者本人 or 管理者」判定を集約し、Post/Commentの両方から利用(重複防止)。SpELではなくService層でのチェックを採用
  - `PostService.update/delete`・`CommentService.delete`で判定し、falseなら`AccessDeniedException`をthrow → Spring Securityの`ExceptionTranslationFilter`が捕捉して自動的に403へ変換される(カスタムハンドラ未設定でも動作する)
  - `CustomUserDetails`に`getRole()`を追加(既存の`getId()`と合わせて判定に使用)
  - 実装時のハマりポイント: `principal.getId() == resourceAuthorId`と`Long`同士を`==`で比較するとバグる(`Long`のキャッシュは`-128〜127`のみ。id=200なら本人でも`false`になる)。`.equals()`で修正。jshellで`Long a=200L; Long b=200L; a==b`が`false`になることを実演して確認した
  - curlでの本人/非所有者テストに加え、管理者ケースは`AuthorizationService`を直接実行するスタンドアロンJavaプログラムで確認(H2コンソールへcurlでログインするより簡便なため)。3パターン(管理者は他人の投稿もOK、id=200の本人はOK、他人はNG)とも想定通り
  - GET `/posts/{id}/edit`自体はSecurityConfig上「認証済みなら誰でも」到達可能(所有者チェックはService層のみ)。つまり非所有者でも編集フォームは開けるが、実際の更新はサーバー側で拒否される、という設計になっている
- [x] 7. エラーハンドリング(未ログイン→ログイン画面、権限なし→403)
  - `templates/error/403.html`・`error/404.html`を追加。Spring Bootの`DefaultErrorViewResolver`が`/error`到達時にステータスコードから自動でテンプレートを探す規約に乗るだけで、403側は追加のJavaコード不要だった
  - `exception.GlobalExceptionHandler`(`@ControllerAdvice`)を追加し、`NoSuchElementException`(投稿/コメント未検出)を404として扱うようにした。`AccessDeniedException`はあえてハンドラ対象に含めていない(`@ControllerAdvice`はDispatcherServlet内、Spring Securityの自動403変換はServlet Filter層で動くため、横取りすると自動変換と競合する)
  - 実装のポイント: `@ExceptionHandler`メソッドは戻り値のビュー名だけでは200 OKになってしまうため、`HttpServletResponse#setStatus()`で明示的に404を設定する必要がある。例外メッセージ(内部IDを含む)は画面には汎用メッセージのみ表示し、詳細はログにのみ残す設計にした
  - ログレベルの検討: 「存在しないURLへのアクセス」は日常的に起こりうる正常な事象なので`ERROR`ではなく`WARN`を採用(ERRORでアラート連携している場合に無駄な通知が飛ぶのを避けるため)
  - curlでの検証時、403ページがJSON応答になって見えたことがあったが、これはcurlのデフォルト`Accept: */*`がSpring Bootの`BasicErrorController`のコンテントネゴシエーションでHTML側に倒れなかっただけで、`Accept: text/html`を明示すれば独自テンプレートが返ることを確認済み(実ブラウザは常にtext/htmlを明示するため実害なし)
  - 未ログイン→ログイン画面はStep4のformLoginの仕組みで既に動作済み(今回は回帰がないことのみ再確認)
- [x] 8. ユーザーBAN機能(管理者のみ、`機能・権限設計`表に記載済みだが未実装だった項目)
  - `UserMapper.findAll/updateEnabled`(単純CRUD、Claude) → `UserService.ban/unban`(Claude雛形+人間が自己BAN防止ガードを実装) → `AdminController`(`/admin/users`一覧・BAN・BAN解除、Claude) → `templates/admin/user-list.html`(Claude)、という一連の流れ
  - `/admin/**`は`SecurityConfig`に元からStep4時点で`hasRole("ADMIN")`が設定済みだった(未使用のまま残っていた設定)ので、今回はSecurityConfig自体の変更は不要だった
  - 自己BAN防止(`id.equals(principal.getId())`なら拒否)は人間が実装。`.equals()`比較を使う判断はStep6の学びがそのまま活きた
  - 「拒否」の表現として独自例外`SelfBanException`(`RuntimeException`継承)を人間が初めて自作。`GlobalExceptionHandler`に`@ExceptionHandler(SelfBanException.class)`を追加し400 Bad Requestへ変換、`error/400.html`を新設(Claude、既存の403/404ハンドラと同じパターン踏襲)
  - チェック例外/非チェック例外の違いを解説: 業務エラーを非チェック例外(`RuntimeException`系)に統一するのはSpringのお作法(`DataAccessException`が`SQLException`をラップする例と同じ)。既存の`AccessDeniedException`/`NoSuchElementException`もこの流儀に合わせている
  - 動作確認はcurlではなくブラウザ自動化(claude-in-chrome)で実施。理由: H2コンソールがフレーム構成+JS依存でcurlでの操作が煩雑なため(Step6の注意点参照)。ブラウザでH2コンソールにJDBC URL`jdbc:h2:mem:blogdb`で接続し`UPDATE users SET role='ADMIN' WHERE username=...`を実行してテスト用管理者を作成
    - H2コンソールへの接続時、`login.jsp`から`Connect`をクリックすると`login.do`に遷移するだけでSQL実行フレームが無い状態になることがあった。`/h2-console/frame.jsp?jsessionid=...`に直接遷移すると、スキーマツリー+SQL入力欄+結果表示が揃った正しいフレームで操作できた
  - 5パターン確認: ①管理者が他人をBAN→一覧が「BAN中」に ②管理者が自分自身をBAN→`SelfBanException`経由で400ページ ③BAN中ユーザーのログイン試行→`/login?error`へリダイレクト(汎用メッセージなのは既知の制限、注意点参照) ④BAN解除→「有効」に復帰 ⑤ロールがUSERのままだとnavに「ユーザー管理」リンクが出ない(`sec:authorize="hasRole('ADMIN')"`)こともあわせて確認
  - ブラウザ操作中、ログインフォームにChromeが過去の別セッションの認証情報(実際のメールアドレス)を自動入力する場面があった。フォームの値をアプリ側で明示的に上書きしてから送信することで対応(実運用コードの自動テストでも同様の配慮が必要になる場面)
- [x] 9. 自動テスト(JUnit + Mockito)の導入
  - pom.xmlは追加設定不要だった。Spring Boot 4.xでは旧来の`spring-boot-starter-test`が`spring-boot-starter-security-test`/`spring-boot-starter-webmvc-test`/`spring-boot-starter-validation-test`などに分割されており、Initializr生成時点でJUnit5・AssertJ・Mockito・MockMvcがすでに`test`スコープで入っていた
  - `AuthorizationServiceTest`: `canModify()`は依存を持たない純粋ロジックなので`@SpringBootTest`でコンテナを起動せず`new AuthorizationService()`で直接テスト。投稿者本人/管理者/他人/`Long`境界値(id=200、Step6の`==`バグの再発防止)の4パターンを人間が実装
  - `UserServiceTest`: `UserService`は`UserMapper`(DB)に依存するため`@ExtendWith(MockitoExtension.class)`+`@Mock`で初めてMockitoを使用。他人BAN成功(Claudeがサンプル実装)/自己BAN時の`SelfBanException`と`updateEnabled()`が呼ばれないこと(`verify(..., never())`、人間が実装)の2パターン
  - ハマりポイント: `assertThatThrownBy(() -> userService.ban(...))`の直前に、ラムダに包まない素の`userService.ban(...)`呼び出しを書き残してしまい、その場で例外が飛んでテスト自体がErrorになった。`() -> ...`(ラムダ)は「後で実行される処理」であり、ラムダの外に書いたコードは通常通りその場で即時評価される、という違いを実演して確認した
  - 全7件(既存の`BlogApplicationTests`含む)がパスすることを確認してからコミット
- [x] 10. MockMvcによる結合テストの追加(`PostControllerIntegrationTest`)
  - Step9の単体テスト(Service層のみ、`AuthorizationService`は直接new、`UserService`はMockitoでMapperをモック)と異なり、`@SpringBootTest`+`@AutoConfigureMockMvc`で実際のSecurityFilterChain・実DB(H2)を経由させ、SecurityConfig(認証・CSRF)込みで検証する構成にした
  - テストデータは`@Sql`スクリプトではなく、`UserMapper`/`PostMapper`を直接`@Autowired`して`@BeforeEach`で投入する方式を採用。MyBatisのSqlSessionはSpring管理のトランザクションに乗るため、テストメソッドに`@Transactional`を付けるだけで各テスト後に自動ロールバックされ、DBリセット用の後始末コードが不要になった
  - Spring Boot 4.xで`@AutoConfigureMockMvc`のパッケージが`org.springframework.boot.test.autoconfigure.web.servlet`から`org.springframework.boot.webmvc.test.autoconfigure`に変更されていた(webmvc関連がstarter分割された影響)
  - 骨格(MockMvcセットアップ、3ユーザー+投稿1件のテストデータ、`principal(User)`ヘルパー)と動作確認用の2テスト(一覧の匿名閲覧・未認証POSTの`/login`リダイレクト)はClaudeが用意し、認可・CSRFの中核4パターン(投稿者本人の編集成功/他人の編集403/管理者による他人の投稿削除成功/CSRFトークンなしは認証済みでも403)を人間が実装
  - 実装時のハマりポイント: `PostController.edit()`の実際のリダイレクト先(`redirect:/posts/{id}`、詳細ページ)を`redirectedUrl("/posts")`(一覧ページ)と書き間違えて1回失敗。アサーションの期待値は実装を見ながら合わせるのではなく、仕様(編集後は詳細ページに戻る)から導くべき、という気づきを得た
  - CSRFトークンなしのテストで、`CsrfFilter`がSpring Securityのフィルタチェーン中で認可判定(`FilterSecurityInterceptor`)より手前に位置するため、認証済みでもCSRFトークンが無ければ403になる(401やログインリダイレクトにはならない)ことを実地で確認
  - 全13件(Step9までの7件+今回の6件)がパスすることを確認してからコミット
- [x] 11. MockMvcによる結合テストの追加(`AdminControllerIntegrationTest`)
  - `/admin/**`への`hasRole("ADMIN")`制限(SecurityConfig)は、Step8実装時ブラウザでの手動確認のみで自動テストが無かった箇所。PostControllerIntegrationTestのService層判定(`AuthorizationService.canModify()`)と違い、SecurityConfig自体の設定はMockMvc結合テストでしか自動検証できない点が今回のポイント
  - 骨格と動作確認用の2テスト(未ログイン→リダイレクト、一般ユーザー→403)はClaudeが用意し、管理者によるBAN機能の3パターン(管理者は閲覧可/他人をBANすると対象がenabled=falseになる/自分自身をBANしようとすると400)を人間が実装
  - 2つ目のテスト(他人をBAN)は、それまでのテストと違いHTTPレスポンスの検証だけでなく、POST後に`userMapper.findByUsername(...).isEnabled()`でDBの状態変化まで確認する点が新要素だった
  - 全18件(Step10までの13件+今回の5件)がパスすることを確認してからコミット
- [x] 12. MockMvcによる結合テストの追加(`CommentControllerIntegrationTest`)
  - PostControllerIntegrationTestと全く同じパターン(投稿者本人による削除成功/他人による削除403/管理者による削除成功/CSRFトークンなしは403)の横展開。人間がパターンを覚えた直後だったため4パターンとも一度で実装できた
  - コメントがぶら下がる記事自体の投稿者(`admin`)と、コメントの投稿者(`owner`)をあえて別人にしてテストデータを設計。同一人物にすると「本当に`comment.authorId`を見て判定しているか」を区別できなくなるため
  - 実装時のハマりポイント: 「管理者は他人のコメントでも削除できる」テストで、URLの末尾`/delete`を書き忘れて`POST /posts/{postId}/comments/{commentId}`を叩いてしまい、意図しない404で失敗(Range for response status value 404 expected:REDIRECTION but was:CLIENT_ERROR)。他の3パターンとURLを見比べて自己修正した
  - 全24件(Step11までの18件+今回の6件)がパスすることを確認してからコミット

## 実装時の注意点(過去の議論より)

- `map-underscore-to-camel-case: true` を設定し、DBのスネークケースとJavaのキャメルケースを自動変換する
- 「投稿者本人 or 管理者」の判定は `@PreAuthorize` のSpELだけでは不十分な場合があり、Service層でのチェックも検討する(Step6では`AuthorizationService`にService層で実装した)
- ユーザーIDなど`Long`型の値をIDで比較するときは`==`ではなく`.equals()`を使う。`Long`のオートボクシングキャッシュは`-128〜127`のみのため、範囲外の値では`==`が意図せず`false`になる(自分のリソースなのに他人扱いされるバグにつながる)
- User-Post間は最初は単方向(Post→User)にし、双方向による複雑化を避ける
- PasswordEncoderは必ずBean登録し、平文パスワード保存を避ける
- MyBatisには遅延ロードがないため、関連データ取得はJOINまたは複数クエリで明示的に行う
- Lombokの`@Data`は関連エンティティ(User⇔Postなど)を持つクラスに付けると`equals`/`hashCode`/`toString`で無限ループになりうるため、domainクラスでは`@Getter`/`@Setter`など個別アノテーションを基本とする
- BANされたユーザー(enabled=false)のログイン試行は`DisabledException`となり、`UserDetailsService`側で明示的にハンドリングしないとログイン画面に汎用エラーしか出せない点に注意
- `authorizeHttpRequests`はデフォルトで`FORWARD`/`ERROR`ディスパッチにも適用される。Spring Bootの404などは内部的に`/error`へフォワードされるため、`/error`を`permitAll`しないと「本来公開のはずのページなのに未ログイン時だけログイン画面に飛ばされる」という紛らわしい挙動になる
- H2コンソール(`/h2-console/**`)を使うには、`permitAll`に加えて①`headers().frameOptions().sameOrigin()`(デフォルトの`X-Frame-Options: DENY`だとiframe表示できない)②`csrf().ignoringRequestMatchers("/h2-console/**")`(H2コンソール自身のフォームはCSRFトークンを付与しない)の2点が追加で必要
- `users`/`posts`/`comments`は全テーブルに`created_at`列があるため、JOINしたSELECTで`ORDER BY created_at`のようにテーブル修飾を省略すると曖昧になり得る。H2ではSELECT句に出したテーブルの列で解決されエラーにはならなかったが(実機確認済み)、DBエンジン依存の挙動なので`ORDER BY posts.created_at`のように明示するのが望ましい
- Thymeleafで`sec:authorize`を使うには`thymeleaf-extras-springsecurity6`をpom.xmlに追加する必要がある(Spring Boot本体には含まれない)
- curlでエラーページ(`/error`経由のもの)を検証するとき、`Accept`ヘッダを省略するとcurlは`*/*`を送るが、Spring Bootの`BasicErrorController`はそれだとJSON応答を返すことがある。`templates/error/<status>.html`が使われているかを確認したいときは`-H "Accept: text/html"`を明示すること(実ブラウザは常にtext/htmlを明示するのでアプリ側の実害はない)
- `@ControllerAdvice`の`@ExceptionHandler`はDispatcherServlet内(Spring MVCの中)で例外を横取りする。Spring Securityの`AccessDeniedException`自動403変換はServlet Filter層(`ExceptionTranslationFilter`)で動くため、`@ExceptionHandler(AccessDeniedException.class)`を書くとその自動変換を奪ってしまう。棲み分けが必要な場合は対象の例外クラスを分けること
- 編集・削除フォームを`sec:authorize="isAuthenticated()"`で非表示にしていても、それはUI上の話でしかない。未ログインユーザーが直接POSTを叩いた場合の挙動もcurlで確認する価値がある(本アプリでは想定通り、有効なCSRFトークンがあれば`/login`へ302リダイレクトされ、実際には削除されないことを確認済み)
- 業務エラー用の独自例外は`RuntimeException`(非チェック例外)を継承するのが定石。チェック例外(`Exception`直下、`RuntimeException`の子孫でないもの)は`throws`宣言をコンパイラが強制するため、層を跨ぐたびに関係ない中間層まで宣言が伝播してしまう。Spring自身も`SQLException`(チェック例外)を`DataAccessException`(非チェック例外)でラップし直している
- H2コンソール(`/h2-console/**`)はフレーム構成のページ。`login.jsp`→`Connect`後に遷移する`login.do`だけではSQL実行用フレームが無い場合があるため、ブラウザで直接操作するときは`/h2-console/frame.jsp?jsessionid=...`(スキーマツリー+SQL入力欄+結果表示が揃ったフレーム)に遷移すると確実
- ログインフォームなどをブラウザ自動化で操作する際、Chromeが別セッションの保存済み認証情報を自動入力することがある。送信前にフォームの値をコード側で明示的に上書きし、実際に何を送信しているか確認してから進めるべき
- JWT/OAuth2への拡張は、Step8完了時点で検討し、当面見送りと判断した。理由: ①JWTの強み(ステートレス、セッション不要)はフロント/バックエンドが別ドメインのSPA・モバイル構成で活きるものであり、今のThymeleaf SSR構成に導入すると`formLogin`+セッションの単純さ・CSRF自動対応を手放すだけの「退化的な変更」になりやすい ②OAuth2ログイン(Googleなど)はコードの外側でプロバイダーへのアプリ登録(client id/secret発行)が必要になり、コーディング学習の範囲を超える準備コストが発生する。将来SPA + REST APIへ作り替える段階になれば、その時点で再検討する

## コーディング方針

- MyBatis MapperはXML方式で記述する(アノテーション方式は使わない)
- SQLはMapper XML内に集約し、Javaコード内に直書きしない
