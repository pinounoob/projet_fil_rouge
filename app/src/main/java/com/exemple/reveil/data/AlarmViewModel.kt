package com.exemple.reveil.data

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlarmViewModel : ViewModel() {
    private val db = Firebase.firestore
    private var listenerRegistration: ListenerRegistration? = null

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms

    // Écoute en temps réel les alarmes dans Firestore
    fun fetchAlarms() {
        listenerRegistration?.remove()
        listenerRegistration = db.collection("alarms")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val alarmList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Alarm::class.java)?.copy(id = doc.id)
                }
                _alarms.value = alarmList
            }
    }

    // Ajouter une alarme
    fun addAlarm(name: String, time: String, ownerId: String) {
        val newAlarm = Alarm(
            name = name,
            time = time,
            ownerId = ownerId,
            enabled = true
        )
        db.collection("alarms").add(newAlarm)
    }

    // Activer / Désactiver une alarme
    fun toggleAlarm(alarmId: String, currentStatus: Boolean) {
        db.collection("alarms").document(alarmId)
            .update("enabled", !currentStatus)
    }

    // Supprimer une alarme
    fun deleteAlarm(alarmId: String) {
        db.collection("alarms").document(alarmId).delete()
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}