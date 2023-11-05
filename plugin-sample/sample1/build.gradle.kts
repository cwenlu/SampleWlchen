@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("java-gradle-plugin")
    id("maven-publish")
}

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
            id = "com.wlchen.sample1"
            implementationClass = "com.wlchen.sample1.SamplePlugin1"
        }
    }
}

publishing {
    repositories {
        maven {
            //指定发布到的目录
            //这里我们在当前module的build目录下新建了maven-repo，也可以指定其他路径
            //url = uri(layout.buildDirectory.dir("maven-repo"))
            url = uri("${rootDir}/maven-repo")
        }
    }
}
