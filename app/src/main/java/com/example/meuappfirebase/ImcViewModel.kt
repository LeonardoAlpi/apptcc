package com.example.meuappfirebase

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.DecimalFormat

// Data class para empacotar o resultado do IMC para a UI
data class ImcResult(
    val value: String,
    val classification: String,
    val color: Int
)

class ImcViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _imcResult = MutableLiveData<ImcResult?>()
    val imcResult: LiveData<ImcResult?> = _imcResult

    /**
     * Carrega os dados do usuário logado a partir do Room e calcula o IMC.
     */
    fun loadUserData() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val profile = userDao.getUserById(user.uid)
            _userProfile.postValue(profile)
            profile?.let { calculateImc(it) }
        }
    }

    private fun calculateImc(user: User) {
        if (user.altura > 0 && user.peso > 0) {
            val imcValue = user.peso / (user.altura * user.altura)
            val df = DecimalFormat("#.#")
            val (classification, color) = getClassificacaoImc(imcValue)
            _imcResult.postValue(ImcResult(df.format(imcValue), classification, color))
        } else {
            _imcResult.postValue(null)
        }
    }

    private fun getClassificacaoImc(imc: Float): Pair<String, Int> {
        return when {
            imc < 18.5 -> "Abaixo do peso" to Color.YELLOW
            imc < 24.9 -> "Peso Normal" to Color.GREEN
            imc < 29.9 -> "Sobrepeso" to Color.rgb(255, 165, 0) // Laranja
            imc < 39.9 -> "Obesidade" to Color.RED
            else -> "Obesidade Grave" to Color.RED
        }
    }
}