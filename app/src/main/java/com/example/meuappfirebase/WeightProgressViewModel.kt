package com.example.meuappfirebase

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apol.myapplication.AppDatabase
import com.apol.myapplication.data.model.User
import com.apol.myapplication.data.model.WeightEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.DecimalFormat



class WeightProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _weightHistory = MutableLiveData<List<WeightEntry>>()
    val weightHistory: LiveData<List<WeightEntry>> = _weightHistory

    private val _imcResult = MutableLiveData<ImcResult?>()
    val imcResult: LiveData<ImcResult?> = _imcResult

    private val _operationStatus = MutableLiveData<Event<String>>()
    val operationStatus: LiveData<Event<String>> = _operationStatus

    fun loadData() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val profile = userDao.getUserById(user.uid)
            _userProfile.postValue(profile)

            val history = userDao.getWeightHistory(user.uid)
            _weightHistory.postValue(history)

            profile?.let { calculateImc(it) }
        }
    }

    fun addWeightEntry(newWeight: Float) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val currentUser = userDao.getUserById(user.uid) ?: return@launch

            // 1. Insere o novo registro de peso
            val newEntry = WeightEntry(userOwnerId = user.uid, weight = newWeight)
            userDao.insertWeightEntry(newEntry)

            // 2. Atualiza o peso principal no perfil do usuário
            val updatedUser = currentUser.copy(peso = newWeight.toInt())
            userDao.updateUser(updatedUser)

            _operationStatus.postValue(Event("Peso salvo com sucesso!"))

            // 3. Recarrega todos os dados para atualizar a tela
            loadData()
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
            imc < 25 -> "Peso Normal" to Color.GREEN
            imc < 30 -> "Sobrepeso" to Color.rgb(255, 165, 0)
            else -> "Obesidade" to Color.RED
        }
    }
}