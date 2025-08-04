package com.alokkumar.onlinevotingapp.viewmodel.user

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alokkumar.onlinevotingapp.model.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _users = mutableStateOf<List<UserModel>>(emptyList())
    val users: State<List<UserModel>> = _users

    val loadingButtons = mutableStateListOf<String>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private var listener: ListenerRegistration? = null

    private val _userProfile = MutableStateFlow(UserModel())
    val userProfile: StateFlow<UserModel> = _userProfile
    private var profileListener: ListenerRegistration? = null

    init {
        listenToUsers()
    }

    fun listenToUser(userId: String) {
        isLoading.value = true
        profileListener?.remove() // remove previous listener if any

        profileListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage.value = error.message
                    isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(UserModel::class.java)?.copy(uid = snapshot.id)
                    if (user != null) {
                        _userProfile.value = user
                    }
                }
                isLoading.value = false
            }
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

    fun updateNameAndPhone(
        userId: String, name: String, phone: String,
        onSuccess: () -> Unit, onError: (String) -> Unit,
    ) {
        db.collection("users").document(userId)
            .update(mapOf("name" to name, "phone" to phone))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Update failed") }
    }

    fun toggleVerification(user: UserModel) {
        loadingButtons.add(user.uid)
        db.collection("users").document(user.uid)
            .update("verified", !user.verified)
            .addOnCompleteListener { loadingButtons.remove(user.uid) }
            .addOnFailureListener { e-> e.printStackTrace() }
    }

    fun toggleDelete(user: UserModel) {
        loadingButtons.add(user.uid)
        db.collection("users").document(user.uid)
            .update("deleted", !user.deleted)
            .addOnCompleteListener { loadingButtons.remove(user.uid) }
            .addOnFailureListener { e-> e.printStackTrace() }
    }

    override fun onCleared() {
        listener?.remove()
        profileListener?.remove()
        super.onCleared()
    }
}