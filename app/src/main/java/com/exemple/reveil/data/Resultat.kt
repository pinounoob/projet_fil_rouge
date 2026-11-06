package com.exemple.reveil.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Resultat(
    val uid: String = "",
    val pseudo: String = "",
    val jour: String = "",
    @ServerTimestamp val heureArret: Timestamp? = null
)