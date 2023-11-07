//这里写没提示，但是可以使用
class SimplePlugin : Plugin<Project>{
    override fun apply(project: Project) {
        project.task("hello") {
            doLast {
                println("Hello from the GreetingPlugin")
            }
        }
    }
}

apply<SimplePlugin>()