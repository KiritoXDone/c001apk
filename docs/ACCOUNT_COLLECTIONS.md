# 账号收藏与动态操作

本轮接口以用户提供的 Reqable `api.coolapk.com_2026_10_24_18_44_30.har` 核对。抓包仅在仓库外临时读取；源码、提交和交付文件不包含抓包、Cookie、token、真实收藏夹 ID 或动态 ID。

- 点击详情菜单的“收藏”打开账号收藏夹；未登录时提示登录，不自动改为本地收藏。
- 长按同一项添加或取消本地收藏，提示结果；两套收藏独立。
- 自己的收藏夹列表内，动态展开菜单提供“取消收藏”：仅把当前收藏夹 ID 放入 `cancelId`，不修改其他收藏夹；服务端确认后就地移除条目，失败保留，操作期间禁用重复点击。其他用户的收藏夹不显示此入口。
- `GET /v6/collection/list`：`uid` 为空，`id` 为当前动态，`type=feed`、`showDefault=1`、`page`，后续页带 `lastItem`。使用 `isBeCollected` 回填勾选状态，支持多选和分页。
- `POST /v6/collection/addItem`：表单 `id` 为选中的收藏夹 ID（逗号分隔），`cancelId` 为原勾选后取消的 ID，`targetId` 为动态 ID，`type=feed`。以响应 `data.collect` 确认成功，不做本地假成功或失败回退。
- `POST /v6/collection/create`：multipart 字段 `title`、`isOpen`、`description`、`pic`、`sourceId`；后三项为空。新建窗口可选择公开状态，默认私密；创建成功后勾选新收藏夹，按“保存”提交收藏关系。
- 账号 revision 与请求代次检查防止账号切换、关闭窗口或切换动态后回写旧状态。保存期间禁用重复操作。
- 自己的动态显示“删除”，经确认后调用已有 `POST /v6/feed/deleteFeed?id=…`。仅服务端确认删除成功后关闭详情，并通知已打开的动态列表移除该条目；失败保留详情。

本轮未由助手执行真实账号收藏、新建收藏夹或删除动态。按用户要求只构建、安装和通知，远程写入与界面验收由用户操作。
