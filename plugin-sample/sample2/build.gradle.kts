@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("maven-publish")
}


dependencies {
    //https://docs.gradle.org/current/userguide/declaring_dependencies.html#sub:api_dependencies
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    implementation(libs.ow2.asm)
    implementation(libs.asm.commons)
    implementation(libs.asm.util)
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.wlchen.sample2"
            artifactId = "com.wlchen.sample2.gradle.plugin"
            version = "1.0.0"
        }
    }

    repositories {
        maven {
            //指定发布到的目录
            //这里我们在当前module的build目录下新建了maven-repo，也可以指定其他路径
            //url = uri(layout.buildDirectory.dir("maven-repo"))
            url = uri("${rootDir}/maven-repo")
        }
    }

}
