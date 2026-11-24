# 构建与发布

## 环境

- Python 3.12 及以上；便携 JDK 21 由预构建脚本准备，也可通过 `JAVA_HOME` 指定 JDK 21。
- Android SDK：`platforms;android-34`、`platforms;android-35`、`platforms;android-37.1` 和 `build-tools;36.0.0`。
- 仓库自带的 Gradle Wrapper，依赖版本见 [版本目录](../gradle/libs.versions.toml)。

通过 `ANDROID_HOME` 或根目录 `local.properties` 中的 `sdk.dir` 指定 SDK 路径。例如将以下占位符替换为本机实际路径，配置文件不入库：

```properties
sdk.dir=<本机 Android SDK 路径>
```

安装 SDK 组件：

```sh
sdkmanager 'platforms;android-34' 'platforms;android-35' \
  'platforms;android-37.1' 'build-tools;36.0.0'
```

请使用包含完整历史的 Git 克隆。`versionCode` 由 `git rev-list HEAD --count` 生成；源码 ZIP 缺少 `.git`，无法直接按当前配置构建，浅克隆则会产生不正确的版本号。

## 本地构建

各系统统一使用 Python 脚本。脚本从自身位置定位仓库，不依赖当前工作目录；以下命令在仓库根目录执行：

```sh
python scripts/prebuild.py
python scripts/build.py doctor
python scripts/build.py build-apk --build-type debug
python scripts/build.py build-apk --build-type release
```

工具链优先使用 `build/jdk/` 中的 JDK 21，其次使用 `JAVA_HOME` 中的 JDK 21；均不可用时自动下载 Temurin 并校验 SHA-256。可执行 `python scripts/prebuild.py --portable` 主动准备便携 JDK。下载缓存保存在 `build/tmp/`，不会覆盖已有工具链，也不修改系统 Java 配置。

产物路径：

| 类型 | APK 路径 |
| --- | --- |
| 调试安装包 | `build/apk/debug/c001apk-debug-arm64-v8a-<版本名>.<版本号>.apk` |
| 发布安装包 | `build/apk/release/c001apk-release-arm64-v8a-<版本名>.<版本号>.apk` |
| 模块中间产物与报告 | `build/gradle/<模块>/` |
| 项目 Gradle 缓存 | `build/cache/` |

脚本从 Gradle 的 `output-metadata.json` 获取实际版本和产物名，支持版本参数覆盖。每个构建类型独立保存；同版本、同类型重新构建会更新对应安装包，旧版本保留。原始 APK 位于 `build/gradle/app/outputs/apk/<类型>/`。Room 生成的数据库结构位于 `build/gradle/app/schemas/`。

需要其他任务时使用 `python scripts/build.py gradle <任务及参数>`。`clean` 只清理模块输出，不清理便携工具链和本地记录；不要删除整个 `build/` 来重新编译。

调试构建和发布构建均启用 R8 与资源裁剪，共用 `app/proguard-rules.pro`。排查混淆后的堆栈时，在 `build/gradle/app/outputs/mapping/<variant>/` 中查找该次构建的映射文件。

版本名默认取自 [app/build.gradle.kts](../app/build.gradle.kts)，可通过 `--app-version x.y.z` 指定。构建并发用 `--jobs N` 指定，默认 12，范围 1～64；透传为 Gradle `--max-workers=N`。旧 `--max-workers N` 作为入口别名保留，Gradle 任务透传时把额度参数放在任务之前：

```sh
python scripts/build.py build-apk --build-type debug --jobs 12
python scripts/build.py gradle --jobs 12 :app:lintDebug
```

应用包名为 `com.example.c001apk.compose`，最低 Android 版本为 7.0（API 24），仅包含 ARM64 原生库。

## 签名

未配置发布密钥时，本地构建使用调试密钥签名，不能覆盖安装本项目发布的正式 APK。自行分发时应使用固定的发布密钥，后续更新沿用相同签名。

通过以下环境变量配置签名，也可在 `local.properties` 中使用去掉 `C001APK_` 前缀后的同名键：

```text
C001APK_KEYSTORE_PATH
C001APK_KEYSTORE_PASSWORD
C001APK_KEY_ALIAS
C001APK_KEY_PASSWORD
```

配置后执行：

```sh
python scripts/build.py build-apk --build-type release --require-release-signing
```

`requireReleaseSigning` 会在未配置发布密钥时终止构建。密钥文件和签名凭据不要提交到仓库。

## 测试与校验

```sh
python scripts/build.py gradle :app:testDebugUnitTest :media-support:testDebugUnitTest \
  :mojito:testDebugUnitTest :app:lintDebug
python -B -m unittest discover -s tools -p 'test_*.py'
```

本地单元测试使用同版本的 DataStore JVM 核心，避免安卓桩的版本号触发不支持覆盖文件的旧版重命名分支；应用与设备测试继续使用安卓实现。图片缓存通过 Okio 执行同目录原子替换，兼容最低 API 24，磁盘读写测试不按操作系统跳过。各模块报告位于 `build/gradle/<模块>/reports/`。

校验正式 APK，替换为实际文件路径及预期签名证书的 SHA-256 摘要：

```sh
python tools/verify_apk.py build/apk/release/<实际安装包文件名>.apk \
  --sdk "$ANDROID_HOME" \
  --certificate-sha256 'EXPECTED_CERTIFICATE_SHA256'
```

工具检查包名、最低 Android 版本、ABI、必需资源、原生库压缩、签名和 ZIP 对齐，并输出 JSON 报告。

ZIP 的 16 千字节对齐不等于原生库的内存页兼容性。仍需在目标设备实际启动；系统提示兼容模式时，应记录相关库名，不能据此声称已经完整支持 16 千字节内存页。

设备测试及需要显式启用的网络测试见 [miuix 改造说明](MIUIX_MIGRATION.md)。已有测试结果保存在[验证记录](VERIFICATION.md)中。

## GitHub Actions

[Build Release 工作流](../.github/workflows/build-release.yml) 支持 `main` 手动发布，以及 push 中应用版本变化后的自动发布。Release 构建需要配置以下仓库 Secrets：

| Secret | 内容 |
| --- | --- |
| `KEYSTORE_BASE64` | 发布密钥库文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 签名密钥别名 |
| `KEY_PASSWORD` | 签名密钥密码 |
| `SIGNING_CERT_SHA256` | 预期签名证书的 SHA-256 摘要 |

构建完成后可从该次运行的 Artifacts 下载产物。Fork 需要配置自己的签名，自签名 APK 无法覆盖安装本项目正式版。

## 发布版本

1. 更新 `app/build.gradle.kts` 中 `appVersion` 的默认版本，并在 [.github/CHANGELOG.md](../.github/CHANGELOG.md) 顶部追加 `## <版本> — <日期>` 和对应更新记录。
2. 提交修改，并按当前授权推送到 `main`。工作流检查 push 前后及全部新增提交中的实际应用版本；版本变化会自动构建，无需提前创建或推送标签。同文件中仅修改依赖或构建配置会跳过。合并提交仅沿用任一父提交的版本时，不单独计为版本更新；分支中实际修改版本的提交仍会检查。
3. 等待 `Build Release` 完成预检查、签名构建与发布；也可在 Actions 页面选择该工作流，在 `main` 上点击 `Run workflow` 手动发布当前提交。

需要重新构建已有版本、验证 CI 而不发布时，手动运行时勾选 `build_only`。该模式保留源码版本，执行相同构建与 APK 校验并上传 Artifacts，跳过标签检查和发布，不修改已有 Release。

预检查只读取元数据和日志，固定本次提交 SHA，保存 `release-plan-<run_id>`。构建沿用当前 Android ARM64、JDK 21、SDK 与正式签名，执行现有单元测试、Lint、Release 构建和 APK 校验；`versionCode` 来自该提交的完整 Git 历史。发布前核对文件清单、大小和 SHA-256，以同一 SHA 创建 `vMAJOR.MINOR.PATCH` 标签，先完整上传草稿，再公开并标记为最新版本。

公开附件为 `c001apk-v<版本>-android-arm64.apk`、对应 `.apk.json` 校验报告、`.sha256` 校验和与 `.manifest.json` 文件清单，保存在本次运行的 `packages-android-arm64` artifact 中。

若运行失败，在原运行页面重跑会恢复已保存的发布计划与草稿。计划丢失或过期时发起新运行。已公开的同名标签与同一 SHA 会跳过构建并保留原附件；同名标签指向其他 SHA 时失败，需更新源码版本后重新发布。首次分支 push 或缺少比较历史时自动跳过，按提示在 `main` 手动运行。

## Fork 的更新来源

应用内更新入口位于“设置 → 关于 → 应用更新”。更新仓库在构建时指定，应用内不提供自定义来源：

```sh
python scripts/build.py build-apk --build-type release --update-repository owner/repo
```

本地构建默认读取 `KiritoXDone/c001apk`，CI 默认使用当前 GitHub 仓库。应用检查正式 Release，支持自动检查、手动检查和跳过版本，APK 下载由浏览器处理。
