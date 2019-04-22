package com.uncc.mileagetracker.Models

data class Ride(
    var id: String? = null,
    var startTime: String? = null,
    var endtTime: String? = null,
    var startReading: Int? = null,
    var endReading: Int? = null,
    var total: Int? = null
)

