# 数盟原生库越界与回退地址修复

> 历史记录：当前版本已改用[纯 Kotlin 注册实现](DIGITAL_UNION.md)，不再打包此原生库。下列补丁、工具和验证记录对应旧版本，可在 `v1.4.8` 标签中查阅。

## 来源与范围

来源是用户提供的 `c001apk_1.4.2_libdu_atoifix.apk`，SHA-256 为 `7ef9b2c2b5307bf2bd68de44d36e07e9d985e9c5210974dce27932696f412d9b`。与原版 1.4.2 相比，DEX 和 Manifest 相同，只有原生库及 APK 签名条目改变。未采用该 APK 的签名。

ARM64 `libdu.so` 大小仍为 724768 字节，只有 21 字节不同，范围为文件偏移/虚拟地址 `0x86048` 至 `0x86063`，共七条指令。原始 SHA-256 为 `3f37818d7aa8af50ad13956cfb3acfd72a447f721b77d3216043f710d57044ea`；修复后为 `f03cb88122cfb3ad23e3554f93d8f98cede1625c661711428badb80dcb9c7be5`。

## 修复原理

原生函数 `0x85fe8` 按冒号拆分配置项 `url`。原实现把冒号后的内容连同 NUL 复制到 8 字节栈缓冲区，再交给 `atoi`；没有检查复制长度。紧邻缓冲区的是栈保护值，错误会触发 `0x860e4` 的 `__stack_chk_fail`，对应报告中的崩溃地址。

补丁直接对原字符串的冒号后缀调用 `atoi`，消除中间栈复制；原有栈保护检查、主机提取及其他原生代码保持不变。

本项目额外在 JNI 边界将 HTTP(S) 基础地址规范化为 `host:port`。例如 `https://global-auni.checktrustworthiness.com` 转换为 `global-auni.checktrustworthiness.com:443`，而不是仅删除协议后隐式使用 SDK 默认端口。非根路径、查询参数、片段、凭据、IPv6 字面量和非法端口不传入原生库。已有原生 `host[:port]` 配置继续支持。

默认地址和回退地址分别在新进程中查询。SDK 原生线程不能由协程取消；主进程在超时、异常或取消后清理旧进程，再决定是否切换地址。仅最终有效 ID 写入应用设置。

## 可复现验证

```sh
# 验证仓库中的二进制已包含补丁。
python3 tools/patch_libdu_atoi.py app/src/main/jniLibs/arm64-v8a/libdu.so --check
# 对原始库副本应用补丁；拒绝未知版本，重复应用无副作用。
python3 tools/patch_libdu_atoi.py /path/to/original-libdu.so
python3 -m unittest discover -s tools -p 'test_*.py'
```

`DigitalUnionEndpointTest` 覆盖地址规范化与非法输入；`DigitalUnionQueryTest` 覆盖有界轮询、超时、异常、取消和后续请求恢复默认地址；`DigitalUnionProcessTest` 覆盖真实进程死亡及卡死后的自动回退。

真实回退测试需在独立测试包内显式运行，且清空该测试包数据，防止本地 native ID 缓存掩盖地址问题。不要清空用户正式应用的数据：

```sh
adb shell am instrument -w -e smidFallbackNetwork true \
  -e class com.example.c001apk.compose.ui.DigitalUnionNetworkTest#fallbackEndpointGeneratesAnIdWithoutPrimary \
  <verification-package>.test/androidx.test.runner.AndroidJUnitRunner
```

原生补丁修复已定位的栈复制；不代表消除第三方二进制中的所有问题，因此仍保留独立进程隔离。
