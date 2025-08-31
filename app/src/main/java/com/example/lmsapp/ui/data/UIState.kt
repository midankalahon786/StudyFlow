package com.example.lmsapp.ui.data.DataClasses

sealed class UIState<out T> {
    data class Success<out T>(val data: T) : UIState<T>()
    data class Error(val message: String, val error: Throwable) : UIState<Nothing>()
    object Empty : UIState<Nothing>()
    object Loading : UIState<Nothing>()
}
