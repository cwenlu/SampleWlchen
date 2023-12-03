package com.wlchen.sample.task

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.ResValue

/**
 * @Author cwl
 * @Date 2023/12/2 11:35 AM
 */

fun androidComponentsConfig(androidComponents: AndroidComponentsExtension<*, *, *>) {
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