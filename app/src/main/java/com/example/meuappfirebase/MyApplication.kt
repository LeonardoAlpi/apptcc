package com.example.meuappfirebase // Pacote corrigido

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * A classe Application é o primeiro componente a ser instanciado quando seu app é iniciado.
 * Nós a usamos para inicializar serviços que precisam estar disponíveis em todo o aplicativo,
 * como o Firebase.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializa o Firebase explicitamente. Embora muitas vezes seja automático,
        // é uma boa prática garantir que ele seja o primeiro a iniciar.
        FirebaseApp.initializeApp(this)

        // Mantemos a inicialização do App Check para desenvolvimento.
        // Isso ajuda a proteger seus recursos do Firebase (como o Firestore) contra abuso,
        // garantindo que as requisições venham do seu app autêntico.
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // Todo o código de inicialização do AWS Amplify foi removido,
        // pois não é mais necessário em nosso novo sistema com Firebase.
    }
}