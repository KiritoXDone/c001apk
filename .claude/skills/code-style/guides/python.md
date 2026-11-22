# Python 命名

| 标识符 | 写法 | 示例 |
|---|---|---|
| 模块、函数、方法、参数、局部变量、属性 | snake_case | `archive_io`、`read_entry`、`entry_count` |
| 类与异常类型 | CapWords，异常通常以 Error 结尾 | `ArchiveEntry`、`ArchiveReadError` |
| 模块级常量 | 大写下划线 | `MAX_ENTRIES` |
| 非公开成员 | 单下划线前缀 | `_buffer_size` |

布尔查询可写 `is_empty`、`has_entries`，普通布尔状态可用 `enabled`。实例方法首参用 `self`，类方法首参用 `cls`。异步函数按实际动作命名，如 `async def read_entry(...)`。双端下划线名称用于语言规定的协议，如 `__iter__`。

## 容易混用的名称

- 变量名与关键字冲突时可用尾部单下划线，如 `class_`。双前导下划线会触发类内名称改写，普通内部成员通常只需单下划线。
- 函数参数可能被当作关键字传入，重命名公共参数要检查关键字调用。数据类的字段名还可能用于构造、模式匹配与字典转换。
- 参数表示实例集合时用复数如 `entries`，表示数量时用 `entry_count`。名称中保留行业缩写，类可写 `HTTPError`，函数可写 `parse_http_header`。
