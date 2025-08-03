package com.alokkumar.onlinevotingapp.viewmodel.user

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UsersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _users = mutableStateOf<List<UserModel>>(emptyList())
    val users: State<List<UserModel>> = _users

    val loadingIds = mutableStateListOf<String>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private var listener: ListenerRegistration? = null

    init {
        listenToUsers()
    }

    private fun listenToUsers() {
        isLoading.value = true
        listener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage.value = error.localizedMessage
                    isLoading.value = false
                    return@addSnapshotListener
                }

                val userList = snapshot?.documents?.mapNotNull { doc ->
                    val user = doc.toObject(UserModel::class.java)
                    user?.copy(uid = doc.id)
                } ?: emptyList()

                _users.value = userList
                isLoading.value = false
            }
    }

    fun toggleVerification(user: UserModel) {
        loadingIds.add(user.uid)
        db.collection("users").document(user.uid)
            .update("verified", !user.verified)
            .addOnCompleteListener { loadingIds.remove(user.uid) }
    }

    fun deleteUser(user: UserModel) {
        loadingIds.add(user.uid)
        db.collection("users").document(user.uid)
            .update("deleted", true)
            .addOnCompleteListener { loadingIds.remove(user.uid) }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}