package com.example.myfitzone.AdapterHelper

abstract class ListItem {
    companion object {
        final val TYPE_HEADER = 0
        final val TYPE_ITEM = 1
    }
    abstract fun getType(): Int

    abstract override fun toString(): String

}