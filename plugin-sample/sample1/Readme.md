# 利用java-gradle-plugin定义插件

### 插件使用

- 方式一
```agsl
//将发布的插件路径添加到插件管理中
pluginManagement {
    repositories {
        maven {
            url = uri("${rootDir}/maven-repo")
        }
    }
}
// 应用插件 注意这种必须指定版本
plugins {
    id("com.wlchen.sample1") version("1.0.0")
}
```

- 方式二
```agsl
//将发布的插件路径添加到插件管理中
////https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_version_management
pluginManagement {
    repositories {
        maven {
            url = uri("${rootDir}/maven-repo")
        }
    }
    plugins {
        //https://docs.gradle.org/current/javadoc/org/gradle/plugin/use/PluginDependencySpec.html#apply-boolean-
        //apply(false)指定先不应用只是包含进来,
        id("com.wlchen.sample1") version ("1.0.0") apply (false)
    }
}
```

