package com.alpan.lostfound.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Item(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val deviceId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)
