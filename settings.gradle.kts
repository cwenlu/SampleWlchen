//https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_version_management
pluginManagement {
    //https://docs.gradle.org/current/userguide/composite_builds.html
    includeBuild("plugin-project")
    repositories {
        //将自己定义的插件仓库放前面优先从这里找
        maven {
            url = uri("${rootDir}/maven-repo")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        //https://docs.gradle.org/current/javadoc/org/gradle/plugin/use/PluginDependencySpec.html#apply-boolean-
        //apply(false)指定先不应用只是包含进来,
        id("com.wlchen.sample1") version ("1.0.0") apply (false)
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Sample-wlchen"
include(":app")
include(":asm-sample")
include(":plugin-sample:sample1")
include(":plugin-sample:sample2")
include(":use-plugin")
