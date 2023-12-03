// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.jvm) apply false
}
true // Needed to make the Suppress annotation work for the plugins block


//gradle build --scan
//需要下面配置gradle<4.3,反之不需要
////https://docs.gradle.com/enterprise/gradle-plugin/
//plugins {
//    id 'com.gradle.enterprise' version '3.8.1'
//}
//
//gradleEnterprise {
//    buildScan {
//        termsOfServiceUrl = 'https://gradle.com/terms-of-service'
//        termsOfServiceAgree = 'yes'
//    }
//}


//https://juejin.cn/post/6997396071055900680  Version Catalog