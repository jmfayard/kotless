package io.kotless.dsl.model

import kotlinx.serialization.Serializable

/** AWS CloudWatch event representation */
@Serializable
internal data class CloudWatch(val source: String, val `detail-type`: String, val resources: Set<String>)
