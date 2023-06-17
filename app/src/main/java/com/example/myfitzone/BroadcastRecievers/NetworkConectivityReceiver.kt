package com.example.myfitzone.BroadcastRecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.example.myfitzone.MainActivity

class NetworkConectivityReceiver: BroadcastReceiver() {
    companion object{
        private val networkConnectivityListener = ArrayList<NetworkConnectivityListener>()
        fun registerNetworkConnectivityListener(listener: NetworkConnectivityListener){
            networkConnectivityListener.add(listener)
        }
        fun unregisterNetworkConnectivityListener(listener: NetworkConnectivityListener){
            networkConnectivityListener.remove(listener)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        networkConnectivityListener.forEach {
            it.onNetworkConnectivityChanged(connectivity(context!!))
        }
    }

    /*
    * This function is used to check the network connectivity
    * 0 -> No internet
    * 1 -> Wifi
    * 2 -> Mobile Data
    * 3 -> Ethernet
    */
    private fun connectivity(context: Context): Int {
        var result = 0
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return 0
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return 0
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 1
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 2
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 3
            else -> 0
        }
        return result
    }

    interface NetworkConnectivityListener{
        fun onNetworkConnectivityChanged(connection: Int)
    }
}