package com.example.myfitzone.DataModels

import com.google.firebase.firestore.PropertyName

data class UserBodyMetrics(
    @PropertyName("timestamp")val timestamp: Long,
    @PropertyName("metricType")val metricType: String,
    @PropertyName("metricName")val metricName: String,
    @PropertyName("metricValue")val metricValue: Double,
    @PropertyName("dateLastModified")val dateLastModified: Long
)
