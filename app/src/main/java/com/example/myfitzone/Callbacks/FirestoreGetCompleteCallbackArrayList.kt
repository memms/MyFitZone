package com.example.myfitzone.Callbacks

interface FirestoreGetCompleteCallbackArrayList {
    fun onGetComplete(result: ArrayList<String>)

    fun onGetFailure(string: String)
}