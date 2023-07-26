package com.example.myfitzone.DataModels

import java.sql.Timestamp

data class UserBodyMetrics(
    val timestamp: Int,
    val metricType: String,
    val metricName: String,
    val metricValue: String
)
