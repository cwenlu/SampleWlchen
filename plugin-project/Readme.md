# 插件项目
本身就是一个项目只是放在了大的文件夹下,可以配合`includeBuild`使用
https://docs.gradle.org/current/userguide/composite_builds.html

作为included builds 使用时内部如果需要需要单独配置gradle.properties
Gradle properties are not passed to included builds https://github.com/gradle/gradle/issues/2534

```agsl
pluginManagement {
    //https://docs.gradle.org/current/userguide/composite_builds.html
    includeBuild("plugin-project")
}
```
