package com.example.myfitzone.Callbacks

interface FirestoreGetCompleteAny {
    fun onGetComplete(result: Any)
    fun onGetFailure(string: String)
}