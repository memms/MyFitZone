package com.example.myfitzone.AdapterHelper

class HeaderItem: ListItem() {
    override fun getType(): Int {
        return TYPE_HEADER
    }

    override fun toString(): String {
        return "HeaderItem(header='$header')"
    }

    private lateinit var header: String

    fun setHeader(header: String) {
        this.header = header
    }

    fun getHeader(): String {
        return header
    }

}