package com.example.dictofin

interface IFilter {
    fun filter(query: String)
    abstract fun nofilyItemRemoved(position: Int)
}




