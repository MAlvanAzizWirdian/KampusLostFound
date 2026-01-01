package com.alpan.lostfound.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {
    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}
