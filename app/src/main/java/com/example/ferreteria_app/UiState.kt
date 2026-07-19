package com.example.ferreteria_app

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val mensaje: String) : UiState<Nothing>()
}
