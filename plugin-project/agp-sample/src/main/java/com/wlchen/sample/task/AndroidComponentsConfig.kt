package com.wlchen.sample.task

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.ResValue

/**
 * @Author cwl
 * @Date 2023/12/2 11:35 AM
 */

fun androidComponentsConfig(androidComponents: AndroidComponentsExtension<*, *, *>) {
    //注册source文件夹, eg:main下和java平级的toml文件夹
    //This sample shows how to add a new custom source folders to all source sets.
    // The source folder will not be used by any AGP tasks (since we do no know about it),
    // however, it can be used by plugins and tasks participating into the Variant API callbacks
    //./gradlew :app:sourceSets
    androidComponents.registerSourceType("toml")
    //高版本apg 默认不生成BuildConfig,需要配置启用
    //android.defaults.buildfeatures.buildconfig=true
    androidComponents.onVariants {
        //生成BuildConfig数据
        it.buildConfigFields.put("FloatValue", BuildConfigField("Float", "1f", "Float Value"))
        it.buildConfigFields.put("LongValue", BuildConfigField("Long", "1L", "Long Value"))
        it.buildConfigFields.put("booleanValue", BuildConfigField("boolean", "false", "boolean Value"))
        it.buildConfigFields.put("BooleanValue", BuildConfigField("Boolean", "false", "Boolean Value"))
        it.buildConfigFields.put(
            "VariantName",
            BuildConfigField("String", "\", $, {name}\"", "Variant Name")
        )

        //生成res values 数据
        it.resValues.put(it.makeResValueKey("string","VariantName"), ResValue(it.name,"Variant Name"))
        it.resValues.put(it.makeResValueKey("color","black"), ResValue("#000000","black"))

        //设置ManifestPlaceholder
        it.manifestPlaceholders.put("MyName","wlchen")

    }
    androidComponents.finalizeDsl { extension ->
        extension.defaultConfig {
            minSdk = 20
            buildConfigField("String","finalizeDslAdd","\"yes\"")
        }
        extension.buildTypes.create("extra").let {
            it.isJniDebuggable = true
        }
    }
}