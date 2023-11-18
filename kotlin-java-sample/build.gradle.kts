@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}

//默认不指定则使用gradle使用的的版本,如果指定则必须同时指定java和kotlin的(如果两种文件混合的话)
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(libs.ow2.asm)
    implementation(libs.asm.commons)
    implementation(libs.asm.util)
}
