package io.kotless.plugin.gradle.dsl

import io.kotless.plugin.gradle.utils.ext
import io.kotless.plugin.gradle.utils.myExt
import org.gradle.api.Project

internal var Project.kotless: KotlessDSL
    get() = this.ext("kotless")
    set(value) {
        this.myExt["kotless"] = value
    }

/** Configuration of Kotless application */
@KotlessDSLTag
fun Project.kotless(configure: KotlessDSL.() -> Unit) {
    kotless = KotlessDSL(this).apply(configure)
}


