package com.camcraft.metadata

data class PhotoMetadata(
    val dateTime: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val deviceMake: String? = null,
    val deviceModel: String? = null,
    val iso: String? = null,
    val aperture: String? = null,
    val exposureTime: String? = null,
    val focalLength: String? = null,
    val hasGps: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null
)
