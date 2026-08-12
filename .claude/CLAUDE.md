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
| 認証 | Spring Security | フォームログイン → 将来的にJWT/OAuth2へ拡張予定 |
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

1. Spring Initializrでプロジェクト作成(上記依存関係を選択)
2. pom.xmlに`spring-boot-starter-thymeleaf`を追加、DDL(schema.sql)とapplication.ymlの設定(application.propertiesから変換)
3. User関連のMapper(XML)実装 → `UserDetailsService`実装
4. `SecurityConfig`でフォームログイン・PasswordEncoder(BCrypt)を設定
5. Post/CommentのCRUD実装(MyBatis XML)
6. 認可制御の追加(投稿者本人チェックなど、SpEL or Service層)
7. エラーハンドリング(未ログイン→ログイン画面、権限なし→403)

## 実装時の注意点(過去の議論より)

- `map-underscore-to-camel-case: true` を設定し、DBのスネークケースとJavaのキャメルケースを自動変換する
- 「投稿者本人 or 管理者」の判定は `@PreAuthorize` のSpELだけでは不十分な場合があり、Service層でのチェックも検討する
- User-Post間は最初は単方向(Post→User)にし、双方向による複雑化を避ける
- PasswordEncoderは必ずBean登録し、平文パスワード保存を避ける
- MyBatisには遅延ロードがないため、関連データ取得はJOINまたは複数クエリで明示的に行う
- Lombokの`@Data`は関連エンティティ(User⇔Postなど)を持つクラスに付けると`equals`/`hashCode`/`toString`で無限ループになりうるため、domainクラスでは`@Getter`/`@Setter`など個別アノテーションを基本とする
- BANされたユーザー(enabled=false)のログイン試行は`DisabledException`となり、`UserDetailsService`側で明示的にハンドリングしないとログイン画面に汎用エラーしか出せない点に注意

## コーディング方針

- MyBatis MapperはXML方式で記述する(アノテーション方式は使わない)
- SQLはMapper XML内に集約し、Javaコード内に直書きしない
