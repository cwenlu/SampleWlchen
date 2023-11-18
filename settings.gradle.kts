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
            if ("com.wlchen.sample2" == requested.id.id) {
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
include(":kotlin-java-sample")
include(":plugin-sample:sample1")
include(":plugin-sample:sample2")
include(":use-plugin")



initHook(gradle)
/**
 * [构建的生命周期](https://docs.gradle.org/current/userguide/build_lifecycle.html)
 */
fun initHook(gradle: Gradle) {
    //需要多个都监听时，不然可以使用下面单一的
    gradle.addBuildListener(object : BuildListener {
        override fun settingsEvaluated(settings: Settings) {}

        override fun projectsLoaded(gradle: Gradle) {}

        override fun projectsEvaluated(gradle: Gradle) {}

        override fun buildFinished(result: BuildResult) {}

    })

    //初始化阶段 start
    gradle.settingsEvaluated {
        //此时我们可以针对所有项目做些通用处理
        println("settings.gradle执行完成,Settings评估完毕")
    }

    gradle.projectsLoaded {
        rootProject.allprojects {
            println("projectsLoaded: " + name)
        }
        println("project初始化完成，但各project自己的配置还没执行")
    }

    //配置阶段的 start
    gradle.beforeProject {
        println("beforeProject:${this}")
    }
    gradle.afterProject {
        println("afterProject:${this}")
    }
    gradle.projectsEvaluated {
        println("所有project评估完成")
    }

    //配置阶段结束后所有task会形成一个有向无环图(dag)
    gradle.taskGraph.addTaskExecutionGraphListener {
        println("task执行前")
    }

}