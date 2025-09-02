package com.example.meuappfirebase

/**
 * Classe wrapper para eventos. Usada para garantir que uma mensagem (como um Toast)
 * seja exibida apenas uma vez, mesmo após rotações de tela.
 * O ViewModel emite o evento, e a UI consome o conteúdo. Uma vez consumido,
 * ele não pode ser consumido novamente.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Permite leitura externa, mas escrita apenas interna

    /**
     * Retorna o conteúdo e marca o evento como "consumido",
     * prevenindo que seja usado novamente.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Retorna o conteúdo mesmo que já tenha sido consumido.
     * Útil para "espiar" o valor sem marcá-lo como tratado.
     */
    fun peekContent(): T = content
}