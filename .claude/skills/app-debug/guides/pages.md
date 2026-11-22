# 页面导航

页面导航直接调用应用的 `AppNavigator`（`Miuix NavController` 封装），走与界面按钮相同的入栈/出栈路径，保留转场、预测返回与状态恢复。

## 页面与命令

```text
python .codex/skills/app-debug/scripts/run.py page.open --destination settings --wait settled
python .codex/skills/app-debug/scripts/run.py page.back --wait settled --json
```

`--destination` 取值与对应页面：

| 取值 | 页面 |
| --- | --- |
| `main` | 首页（回到根） |
| `settings` | 设置 |
| `about` | 关于 |
| `license` | 开源许可证 |
| `accounts` | 账号管理 |
| `local-follows` | 本地关注 |
| `tab-editor` | 首页板块编辑 |
| `navigation-order` | 导航顺序 |
| `params` | 接口参数 |
| `content-filters` | 内容屏蔽 |
| `twilight-rules` | 暮色规则 |
| `device-import` | 数盟设备导入 |
| `login` | 登录 |

`page.back` 先调用导航控制器出栈；栈内已无页面时回退到 Activity 的返回分发。

## 就绪条件与返回值

- `page.open` 返回 `ok`、`action`、`destination`；导航在语义树变化前即返回，因此默认 `--wait settled`。
- `page.back` 返回 `ok`、`action`、`popped`；`popped: false` 表示已到根页面，由系统返回处理。
- `settled` 以语义节点坐标连续两次采样一致为准，上限 5 s；页面数据加载不影响该判定，需要数据就绪时继续用 `control.list` 核对目标文本或 `--wait frame` 观察骨架屏。

以「目标页面标题出现」作为验收依据，例如设置页核对 `设置` 标题与各分组的文案。观察转场时按 [动画](animations.md) 设置倍率，用 `--wait frame` 后立即 [截图](screenshots.md)。

## 结构说明

主 Activity 持有单个 `navigator` 字段（手机布局）；宽屏分栏由 `detailNavigator` 承担详情，本通道只操作主导航。需要验证详情栏时，用控件点击进入 feed，不使用 `page.open`。
