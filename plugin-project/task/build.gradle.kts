plugins {
    `kotlin-dsl`
}
apply(from = "simple.gradle.kts")

//******************************************
//task添加依赖
//https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:adding_dependencies_to_tasks
project(":sample") {
    tasks.register("taskA") {
        group = "sample"
        doLast {
            println("taskA")
        }
    }
}

project(":task") {
    task("taskB") {
        group = "sample"
        dependsOn(":sample:taskA")
        doLast {
            println("taskB")
        }
    }
}


//******************************************
//对task指定排序规则 gradle -q taskY taskX
//https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:ordering_tasks
//mustRunAfter，shouldRunAfter只能改变现有Task的顺序，不能触发它们的创建
//taskX和taskY同时执行的时候必须保证taskY在taskX后
//可以在gradle任务中选中多个task一起执行，按选的先后顺序执行(可以看出排序对任务的作用)
val taskX by tasks.registering {
    group = "sample"
    doLast {
        println("taskX")
    }
}
val taskY by tasks.registering {
    group = "sample"
    doLast {
        println("taskY")
    }
}
taskY {
    mustRunAfter(taskX)
}


//******************************************
//跳过task ./gradlew :task:skipTask -Pskip
//https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:skipping_tasks
task("taskSkipDirect") {
    //当前版本还不支持？
    //timeout = Duration.ofMillis(500)
    doFirst {
        if (true) {
            throw StopExecutionException()
        }
    }
    //禁用task
    enabled = false
}

val skipTask by tasks.registering {
    group = "sample"
    dependsOn("taskSkipDirect")
    doLast {
        println("skipTask executed")
    }
}

skipTask {
    val skip = providers.gradleProperty("skip")
    //没有skip参数才执行 第一个参数表示执行的原因
    onlyIf("there is no property skip") {
        !skip.isPresent
    }
}


//******************************************
//https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:task_rules
//规则描述  ./gradlew :task:pingServer1
//可以执行pingXXX 类的任务，内部做了处理
tasks.addRule("Pattern: ping<ID>") {
    val taskName = this
    if (startsWith("ping")) {
        task(taskName) {
            doLast {
                println("Pinging: " + (taskName.replace("ping", "")))
            }
        }
    }
}

//配置了一组ping task
tasks.register("groupPing") {
    dependsOn("pingServer1", "pingServer2")
}


//******************************************
//https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:finalizer_tasks
//dependsOn，finalizedBy 这将强制执行引用的Task
val taskA by tasks.registering {
    doLast {
        println("taskA")
        //即使这个失败了，finalizedBy指定的还是会执行
        //throw RuntimeException()
    }
}
val taskB by tasks.registering {
    doLast {
        println("taskB")
    }
}

taskB { finalizedBy(taskA) }