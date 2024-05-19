package com.example.dictofin

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

class Utils {
    companion object {
        fun getCurrentDate():String {
            val sdf = SimpleDateFormat("HH:mm:ss")
            return sdf.format(Date())
        }
        fun getID(): Int{
            return (Math.random()*1000000).roundToInt()
        }
    }
}