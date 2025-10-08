// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // 為了清晰起見，我們暫時不使用 alias，直接寫明版本號
    id("com.android.application") version "8.12.3" apply false

    // **關鍵修正：將所有 Kotlin 相關工具的版本全部統一為 1.9.22**
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false

    // **關鍵修正：將 KSP 的版本降級，使其與 Kotlin 1.9.22 匹配**
    id("com.google.devtools.ksp") version "1.9.22-1.0.18" apply false
}