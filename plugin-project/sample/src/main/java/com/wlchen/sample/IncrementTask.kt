package com.wlchen.sample

/**
 *
 * [增量构建](https://docs.gradle.org/current/userguide/incremental_build.html#incremental_build)
 *
 * Gradle测试自上次构建以来任何任务输入或输出是否发生了变化。
 * 如果他们还没有，Gradle可以考虑最新的任务，因此跳过执行其操作。
 * 另请注意，除非任务至少有一个任务输出，否则增量构建将不起作用
 * 您需要告诉Gradle哪些任务属性是输入，哪些是输出。
 * 如果任务属性影响输出，请务必将其注册为输入，否则任务将被视为最新的。
 * 相反，如果属性不影响输出，请不要将属性注册为输入，否则任务可能会在不需要时执行。
 * 还要小心可能为完全相同的输入产生不同输出的非确定性任务：这些任务不应配置为增量构建，因为最新的检查不起作用
 *
 * @Author cwl
 * @Date 2023/11/10 1:39 PM
 */

