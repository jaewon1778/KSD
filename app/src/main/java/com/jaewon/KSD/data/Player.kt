package com.jaewon.KSD.data

data class Player(
    var name: String,
    var active: Boolean = true,
    var stateOn : Boolean = true,
    var statePay : Boolean = false,
    var gameSurplus : Boolean = true,
)