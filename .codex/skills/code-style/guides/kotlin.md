# Kotlin 命名

| 标识符 | 写法 | 示例 |
|---|---|---|
| 类、接口、对象、类型别名 | PascalCase | `ArchiveEntry`、`EntryReader` |
| 函数、属性、参数、局部变量 | camelCase | `readEntry`、`entryCount` |
| 编译期常量 | 大写下划线 | `const val MAX_ENTRIES` |
| 包名 | 小写，按层级分段 | `org.example.archive` |

布尔属性用 `isEmpty`、`hasEntries` 等。普通只读属性仍用 `camelCase`。挂起函数按操作命名，如 `suspend fun readEntry()`。回调参数描述事件，如 `onEntryRead`。重写函数、Java 互操作成员及框架回调沿用既有 API 签名。

## 容易混用的名称

- 给公开只读属性提供私有可变存储时，可配对使用 `entries` 和 `_entries`。普通私有属性按项目常规命名。
- 函数参数支持命名调用；公共参数从 `entryCount` 改名时要核对 Kotlin 调用方。JVM 上的属性还会生成访问器，检查 `isReady` 等布尔属性的 getter 以及既有 Java 调用。
- 可失败的查找或转换可以按返回行为命名，例如 `findEntry` 或 `toEntryOrNull`。运算符函数使用语言规定的名称，如 `operator fun contains(...)`。
