package com.example.myfitzone.DataModels

import com.google.firebase.firestore.FieldValue
import java.sql.Timestamp

data class UserBodyMetrics(
    val timestamp: Long,
    val metricType: String,
    val metricName: String,
    val metricValue: Double,
    val dateLastModified: Long
)
