//https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_version_management
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "plugin-project"
include(":sample")
include(":agp-sample")
include(":playground-sample")
include(":task")

//PREFER_PROJECT：默认值，优先使用build.gradle中的repositories { }，忽略settings.gradle中的repositories { } ；
//PREFER_SETTINGS：优先settings.gradle中的repositories { } ，忽略build.gradle中的repositories { }；
//FAIL_ON_PROJECT_REPOS：这个厉害了，表示在build.gradle中声明的repositories { }会导致编译错误；
//
//如果只有app模块，可以把仓库地址都写在dependencyResolutionManagement>repositories里面，如果有多个模块，且依赖差别很大，还是建议分开写，毕竟从仓库找依赖也是耗时的，虽然并不是编译痛点...
