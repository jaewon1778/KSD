package com.jaewon.KSD.data

data class Player(
    var name: String,
    var active: Boolean = true,
    var stateOn : Boolean = true,
    var statePay : Boolean = false,
    var gameSurplus : Boolean = true,
    var amount: Int = 0
) {
    fun dupPlayer(oldPlayer: Player) {
        name = oldPlayer.name
        active = oldPlayer.active
        stateOn = oldPlayer.stateOn
        statePay = oldPlayer.statePay
        gameSurplus = oldPlayer.gameSurplus
        amount = oldPlayer.amount
    }

}