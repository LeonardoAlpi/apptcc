package com.example.meuappfirebase

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> = _updateStatus

    /**
     * Carrega o perfil do usuário logado a partir do banco de dados local (Room).
     */
    fun loadUserProfile() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val roomUser = userDao.getUserById(user.uid)
            _userProfile.postValue(roomUser)
        }
    }

    /**
     * Atualiza o perfil do usuário no Firestore e no Room.
     */
    fun updateUserProfile(updatedRoomUser: User) {
        val user = auth.currentUser ?: return

        // Cria o objeto para o Firestore a partir dos dados atualizados do Room
        val firestoreProfileUpdate = mapOf(
            "nome" to updatedRoomUser.nome,
            "idade" to updatedRoomUser.idade,
            "peso" to updatedRoomUser.peso.toDouble(),
            "altura" to updatedRoomUser.altura.toDouble(),
            "genero" to updatedRoomUser.genero,
            "profilePicUri" to updatedRoomUser.profilePicUri
        )

        // Passo 1: Atualizar no Firestore
        firestore.collection("usuarios").document(user.uid)
            .update(firestoreProfileUpdate)
            .addOnSuccessListener {
                Log.d("Sync", "Perfil atualizado com sucesso no Firestore.")
                // Passo 2: Atualizar no Room
                viewModelScope.launch {
                    userDao.updateUser(updatedRoomUser)
                    _userProfile.postValue(updatedRoomUser) // Atualiza o LiveData
                    _updateStatus.postValue("Dados atualizados com sucesso!")
                    Log.d("Sync", "Perfil atualizado com sucesso no Room.")
                }
            }
            .addOnFailureListener { e ->
                _updateStatus.postValue("Erro ao atualizar perfil: ${e.message}")
            }
    }

    /**
     * Faz upload de uma nova imagem de perfil para o Firebase Storage e atualiza o link no perfil.
     */
    fun uploadProfilePicture(imageUri: Uri) {
        val user = auth.currentUser ?: return
        val storageRef = storage.reference.child("profile_pictures/${user.uid}")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Pega a URL de download da imagem
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Agora atualiza o perfil com a nova URL
                    viewModelScope.launch {
                        val roomUser = userDao.getUserById(user.uid)
                        roomUser?.let {
                            val updatedUser = it.copy(profilePicUri = downloadUrl.toString())
                            updateUserProfile(updatedUser)
                        }
                    }
                }
            }
            .addOnFailureListener {
                _updateStatus.postValue("Erro no upload da imagem.")
            }
    }
}