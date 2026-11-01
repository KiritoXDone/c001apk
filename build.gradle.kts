// 统一声明各模块使用的插件版本。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.dagger.hilt.android) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.google.protobuf) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

allprojects {
    // clean 只清理 Gradle 输出，保留便携工具链与本地验证记录。
    val outputName = if (project == rootProject) "root" else project.path.removePrefix(":").replace(":", "/")
    layout.buildDirectory.set(rootProject.layout.projectDirectory.dir("build/gradle/$outputName"))

    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("me.panpf:sketch-gif"))
                .using(module("io.github.panpf.sketch:sketch-gif:2.7.1"))
        }
    }
}
