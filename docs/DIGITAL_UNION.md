# 数盟 ID 注册

注册有效执行路径采用 [People-11/shuzilm_id 的 DuClient.kt](https://github.com/People-11/shuzilm_id/blob/03118bc4dd2fbf4807f971c839391742c181da9b/sample/DuClient.kt)，参考提交 `03118bc4dd2fbf4807f971c839391742c181da9b`。仅调整包名并填入用户提供的注册配置，省略未调用的分析辅助函数与注释字段；有效字段、编码、网络调度、超时和返回判断与仓库一致。

注册 API_KEY 使用用户提供的固定初始化值，不与旧 SDK 资源文件中的同名 RSA 公钥混用。

## 注册实现

- `DuClient` 使用 Android SDK / `org.json` 和 `HttpURLConnection` 构造一次 mdna 报告。
- 使用同一份请求路径与载荷，并发请求 `https://auni.telecome.cn` 与 `https://global-auni.checktrustworthiness.com`，采用先成功返回的结果。
- 每个连接的连接、读取超时均为 30 秒。JSON 请求按仓库执行 zlib 压缩和 XOR，响应按仓库执行 XOR 解码。
- 返回规则沿用仓库：`err` 为 0，优先读取 `n_a`，仅当它为空时读取 `cdd`，排除空值和全零字符串。未增加 ID 格式白名单、固定长度限制或额外报告字段。
- 仅发送仓库启用的精简字段。未启用的环境检查、证书、hosts、周边网络、其他 SDK 标识与后续上报代码没有接入。

旧的 `libdu.so`、JNI 包装层、PackageManager 包装、私有注册服务和 IPC 查询流程均已移除。没有增加运行时权限。

## 应用调用层

`DigitalUnionClient` 只用 IO 协程执行仓库的阻塞查询；原库网络逻辑不变。`DigitalUnionRepository` 负责应用已有 ID 的复用、写入与并发保护，返回失败时保留原值。启动页只等待本地账号恢复，不等待数盟注册；获取失败仍可进入首页并通过设置手动填写。

默认酷安协议版本为 `16.7.0 / 2610291`，`X-Api-Version` 为 `16`。修改版本字段时，协议设置与 User-Agent 中的版本段原子更新，并在后台重新调用注册。待刷新状态跨重启保存；手动填写 ID 会取消自动刷新，迟到结果不能覆盖新设置。已知的旧内置默认值升级，其他自定义版本保留。

仓库本身未发送其注释中的 `EV2` / `Zrs` 宿主版本字段，本项目也不额外加入。版本修改后的“同步更新 ID”是重新调用同一注册流程，不保证服务端分配不同的设备 ID。

## 验证

`DuCodecTest` 校验仓库编码向量，`ProtocolVersionTest` 与 `IdentityWriteGuardTest` 校验版本联动、持久化与迟到写入保护。真实网络测试只在独立测试包中显式开启：

```sh
adb shell am instrument -w -e smidNetwork true -e smidFallbackNetwork true \
  -e class com.example.c001apk.compose.ui.DigitalUnionNetworkTest \
  <verification-package>.test/androidx.test.runner.AndroidJUnitRunner
```

备用地址测试通过测试侧反射调用仓库原有的私有报告函数，正式代码没有为测试添加网络分支。测试不输出设备 ID，不清空正式应用数据。历史原生补丁见 [历史记录](DIGITAL_UNION_NATIVE_PATCH.md)，相关源码和工具保存在旧标签中。
