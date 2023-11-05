# 直接定义插件
https://docs.gradle.org/current/userguide/custom_plugins.html#behind_the_scenes

### 插件使用

```agsl
//将发布的插件路径添加到插件管理中
//https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_version_management
pluginManagement {
    repositories {
        maven {
            url = uri("${rootDir}/maven-repo")
        }
    }
    //https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_resolution_rules
    resolutionStrategy {
        eachPlugin {
            //https://docs.gradle.org/current/userguide/custom_plugins.html#note_for_plugins_published_without_java_gradle_plugin
            // 没有使用java-gradle-plugin 插件方式发布缺少标记，需要配置发现策略
             if("com.wlchen.sample2" == requested.id.id){
                useModule("com.wlchen.sample2:com.wlchen.sample2.gradle.plugin:1.0.0")
            }
        }
    }
}
```

