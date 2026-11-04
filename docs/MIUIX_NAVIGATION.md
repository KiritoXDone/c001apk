# Miuix navigation

The application uses `miuix-nav` 0.9.4 for page navigation, entry state and ViewModel ownership, page transitions, and predictive back. `AppNavHost` uses the library's `NavDisplay`, `NavTransitions.MiuixDefault`, and device corner radius. There is no application-owned pointer recognizer or page transition curve.

## Routes and state

- `AppDestination` contains typed, serializable page arguments. URLs, query text, nullable values, and update items are passed directly rather than encoded into route strings.
- Destination screens must not URL-decode these arguments again. Post text and search terms retain literal `%`, `+`, and `%20`; WebView and carousel URLs retain their encoded components. External-link query decoding happens once in `CoolapkLink`. Cookie usernames use the separate cookie codec, with malformed values preserved for display; Markdown fragments reject invalid escapes without crashing.
- Post and article long presses open `CopyTextScreen`; their Markdown and raw-text renderers do not also register inline text selection. Selection belongs to the copy page and requires a new gesture there. Standalone content such as release notes opts into inline selection explicitly.
- `AppNavEntry` adds a unique visit ID used as the native content key. Returning to an entry restores its state; revisiting the same destination later creates a distinct entry. Repeated taps on the current destination do not add duplicate entries.
- `AppNavigator` wraps Miuix's controller and persisted back stack. Popping keeps the root. Account changes replace the root identity and clear the detail stack so old account screens cannot be restored.
- `AppNavigationActions` retains the existing Coolapk link parsing and external-browser behavior.

## Main sections and tabs

Home / Following / Profile are peer sections, not entries in the back history. They use a Pager with Miuix's `springAnimateToPage`, following the upstream example's split between page navigation and main-section navigation. Main-section dragging remains disabled so nested topic/home pagers keep their existing gestures. The existing selected-section state, page saveable state, badge handling, and repeated-selection refresh are retained.

Tab click animations use the same Miuix pager helper. Top tab rows remain native Miuix components. Loading states, list expansion, dialogs, and image viewers retain their component-specific behavior; they are not forced into the page back stack.

The bottom bar and large-screen rail share a persistent display order. Selection, refresh triggers,
account-switch navigation and unread badges use stable page IDs rather than display positions. Sorting
does not change the Home launch/back destination. The main pager and its saved state use page identity.
Home tab positions remain in Room and are preserved when the editor or Home ViewModel is recreated.

## Detail pane

The adaptive feed pane has its own Miuix stack with an empty root. Its host stays composed at zero width when hidden, and is not placed, to retain state without intercepting touches. `NavigationPaneScope` caps a hidden pane's lifecycle at CREATED and disables its child back dispatcher. Closing the detail waits for the native exit before hiding the pane. Mid-screen pane boundaries use zero screen-corner radius and clip the transition to the pane.

## Back behavior

Settings → Appearance includes a persistent predictive-back switch, enabled by default for existing
preferences. Turning it off disables in-content dismissal and uses a completion-only navigation-event
handler at the start of each entry, before its page content. The page stays still until system back
commits; cancellation does not pop. Inner page handlers and window/IME dismissal keep their existing
priority. No hidden APIs or system settings are changed; system back-to-home and cross-activity
animations remain owned by Android.

Native in-content right swipes are enabled on ordinary routes when this switch is on, with the library's child-first gesture arbitration. The IME disables in-content dismissal while editing. WebView retains its internal-history BackHandler and disables in-content page dismissal; system back continues to reach the page handler before the navigator.

共享 `MiuixBottomSheet`（包括全部回复和产品参数）也遵循预测返回开关。关闭时，在弹层独立窗口的内容起始处注册仅处理完成事件的 `NavigationBackHandler`：手势过程中弹层不移动，取消不关闭，完成仅调用一次关闭回调。始终组合该处理器，只切换启用状态，让内容自身的返回处理器保留更高优先级；开启时继续使用 Miuix 原生预览。普通下拉关闭和遮罩点击不受影响，其他对话框和菜单仍沿用既有 Miuix 窗口行为。

## Build and verification

JDK 21 and app JVM target 21 are required by Miuix's public inline navigation APIs. Lifecycle dependencies align with the library's 2.11.0 requirement. The serialization compiler plugin uses the project's Kotlin version.

- `AppNavigatorTest`: route serialization, special/nullable arguments, duplicate taps, repeated visits, account reset and pane replacement.
- `MiuixNavigationTest`: native drag completion/cancellation, pager and list arbitration, inner system-back priority, inactive pane handling, saved-state restoration and entry ViewModel lifetime.
- Compile and unit tests: `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`.
- The navigation instrumentation class is `com.example.c001apk.compose.ui.MiuixNavigationTest`. Building its APK does not constitute a device test pass.

Verified on 2026-10-26: debug and instrumentation APK builds succeeded, all 87 JVM tests passed, and all 9 navigation instrumentation tests passed on an Android 16 / HyperOS phone. The test fixture brings the test-only activity into the foreground through the instrumentation shell before ActivityScenario starts, because HyperOS blocks its initial background launch; no device permission changes are required. Manual checks also loaded Home, switched to Profile, opened Settings, returned with an in-content swipe, and loaded a real Feed detail with its assisted Hilt ViewModel. These checks do not cover every screen or all tablet/foldable postures. The isolated verification packages were removed afterward; the production package was not replaced.
