package com.alokkumar.onlinevotingapp.viewmodel.admin.polls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PollResultViewModel : ViewModel() {

    private val _results = MutableStateFlow<List<Pair<String, Int>>?>(null)
    val results: StateFlow<List<Pair<String, Int>>?> = _results

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun fetchResults(pollId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("polls").document(pollId)
                    .collection("candidates")
                    .get().await()

                val resultList = snapshot.documents.map {
                    val name = it.getString("voterName") ?: "Unknown"
                    val votes = it.getLong("votes")?.toInt() ?: 0
                    name to votes
                }
                _results.value = resultList
            } catch (e: Exception) {
                e.printStackTrace()
                _results.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}