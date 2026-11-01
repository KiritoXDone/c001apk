# 构建与发布

## 环境

- Python 3.12 及以上；便携 JDK 21 由预构建脚本准备，也可通过 `JAVA_HOME` 指定 JDK 21。
- Android SDK：`platforms;android-34`、`platforms;android-35`、`platforms;android-37.0` 和 `build-tools;36.0.0`。
- 仓库自带的 Gradle Wrapper，依赖版本见 [版本目录](../gradle/libs.versions.toml)。

通过 `ANDROID_HOME` 或根目录 `local.properties` 中的 `sdk.dir` 指定 SDK 路径。例如将以下占位符替换为本机实际路径，配置文件不入库：

```properties
sdk.dir=<本机 Android SDK 路径>
```

安装 SDK 组件：

```sh
sdkmanager 'platforms;android-34' 'platforms;android-35' \
  'platforms;android-37.0' 'build-tools;36.0.0'
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
| 关闭优化的发布包 | `build/apk/release-no-r8/` |
| 模块中间产物与报告 | `build/gradle/<模块>/` |
| 项目 Gradle 缓存 | `build/cache/` |

脚本从 Gradle 的 `output-metadata.json` 获取实际版本和产物名，支持版本参数覆盖。每个构建类型独立保存；同版本、同类型重新构建会更新对应安装包，旧版本保留。原始 APK 位于 `build/gradle/app/outputs/apk/<类型>/`。Room 生成的数据库结构位于 `build/gradle/app/schemas/`。

需要其他任务时使用 `python scripts/build.py gradle <任务及参数>`。`clean` 只清理模块输出，不清理便携工具链和本地记录；不要删除整个 `build/` 来重新编译。

调试构建不启用 R8 和资源裁剪，发布构建默认开启。排查优化相关问题时使用 `python scripts/build.py build-apk --build-type release-no-r8`。

版本名默认取自 [app/build.gradle.kts](../app/build.gradle.kts)，可通过 `--app-version x.y.z` 指定；并行线程数可用 `--max-workers 2` 限制。应用包名为 `com.example.c001apk.compose`，最低 Android 版本为 7.0（API 24），仅包含 ARM64 原生库。

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

部分磁盘读写测试依赖覆盖式文件重命名。在 Windows 上，现有 DataStore 与图片缓存实现可能因此失败；应查看具体测试报告，并在 Linux 环境复核，不能跳过用例后声称全部通过。各模块报告位于 `build/gradle/<模块>/reports/`。

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

[Android 工作流](../.github/workflows/android.yml) 支持手动构建。Release 构建需要配置以下仓库 Secrets：

| Secret | 内容 |
| --- | --- |
| `KEYSTORE_BASE64` | 发布密钥库文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 签名密钥别名 |
| `KEY_PASSWORD` | 签名密钥密码 |
| `SIGNING_CERT_SHA256` | 预期签名证书的 SHA-256 摘要 |

构建完成后可从该次运行的 Artifacts 下载产物。Fork 需要配置自己的签名，自签名 APK 无法覆盖安装本项目正式版。

## 发布版本

1. 更新 `app/build.gradle.kts` 中的默认版本、`.github/workflows/android.yml` 中的版本默认值，以及 `CHANGELOG.md` 对应版本的更新记录。
2. 提交代码；因提交会增加 `versionCode`，最终发布包必须从该提交重新构建。说明待推送内容并取得确认后，推送代码及对应的 `vMAJOR.MINOR.PATCH` 标签。
3. 等待 [Publish Release 工作流](../.github/workflows/release.yml) 完成。

工作流从标签读取版本号，检出该标签的源码，执行单元测试、Lint、Release 构建和 APK 签名校验。通过后使用对应更新记录创建 GitHub Release，一并发布 APK、JSON 校验报告和 `SHA256SUMS`。

公开 APK 文件名为 `c001apk_<版本名>.<版本号>.apk`，与本地构建的命名格式不同。

若因 CI 环境问题失败，可手动运行 `Publish Release` 并填写原标签。源码仍从该标签检出，无需移动标签；如果 Release 已存在，重试会更新其说明和附件。

## Fork 的更新来源

应用内更新入口位于“设置 → 关于 → 应用更新”。更新仓库在构建时指定，应用内不提供自定义来源：

```sh
python scripts/build.py build-apk --build-type release --update-repository owner/repo
```

本地构建默认读取 `KiritoXDone/c001apk`，CI 默认使用当前 GitHub 仓库。应用检查正式 Release，支持自动检查、手动检查和跳过版本，APK 下载由浏览器处理。
