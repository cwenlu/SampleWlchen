@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("java-gradle-plugin")
}

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(17))
//    }
//}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.ow2.asm)
    implementation(libs.asm.commons)
    implementation(libs.asm.util)
}

group = "com.wlchen"
version = "1.0.0"

gradlePlugin {
    plugins {
        //生成规则 id:id.gradle.plugin:version
        //如我们这: com.wlchen.sample1:com.wlchen.sample1.gradle.plugin:1.0.0
        create("sample1") {
            id = "com.wlchen.sample"
            implementationClass = "com.wlchen.sample.SamplePlugin1"
        }
    }
}

