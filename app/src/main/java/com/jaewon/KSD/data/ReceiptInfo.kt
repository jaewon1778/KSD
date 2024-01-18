package com.jaewon.KSD.data

import kotlin.math.roundToInt

data class ReceiptInfo(
    var players: MutableList<Player>,
    var typeOfReceipt : Int = 0,
    var subtypeOfReceipt : String = "",
    var totalAmount : Int = 0
) {
    fun isReadyForCal(): Int{
        if (typeOfReceipt == 1 ){
            if (totalAmount == 0) return 1
            for (player in players){
                if (player.statePay) return 0
            }
            return 2
        }
        if (typeOfReceipt == 2 && totalAmount != 0) return 3
        return 0
    }

    fun updatePlayers(newPlayers : MutableList<Player>, isAdd : Boolean){
        // 기존 players와 비교해서 새로운 players 만들기
        var indexOfNew = 0
        for (oldP in players){
            if (oldP.name == newPlayers[indexOfNew].name){
                newPlayers[indexOfNew].dupPlayer(oldP)
                indexOfNew++
            }
            else if (isAdd) {
                indexOfNew++
                newPlayers[indexOfNew].dupPlayer(oldP)
                indexOfNew++
            }
            if (indexOfNew == newPlayers.size) break
        }
        players = newPlayers
        onUpdatePListener.onUpdateP()
    }

    interface UpdatePListener {
        fun onUpdateP()
    }
    private lateinit var onUpdatePListener: UpdatePListener
    fun setOnUpdatePListener (listener: UpdatePListener){
        onUpdatePListener = listener
    }

    fun getNumOfStateOnPlayers():Int {
        var num = 0
        for (player in players){
            if (player.stateOn) num++
        }
        return num
    }
    fun getPlayerToPay():Player{
        var player2pay : Player = Player("__fail")
        for (player in players){
            if (player.statePay) player2pay = player
        }
        return player2pay
    }
    fun getAmountForOne(): Int {
        if (getNumOfStateOnPlayers() == 0) return 0
        return (totalAmount / (getNumOfStateOnPlayers() + 0.0)).roundToInt()
    }
    fun allocateTotalMoney(pnList : MutableList<Player>){
        val amountForOne = getAmountForOne()
        for ((index,player) in players.withIndex()){
            if (!player.stateOn) continue
            if (player.statePay) {
                pnList[index].amount += amountForOne*(getNumOfStateOnPlayers()-1)
                continue
            }
            pnList[index].amount -= amountForOne
        }
//        return players
    }
    fun allocateGameMoney(pnList : MutableList<Player>){
        for ((index,player) in players.withIndex()){
            if (player.gameSurplus) pnList[index].amount += player.amount
            else pnList[index].amount -= player.amount
        }
//        return players
    }
    fun calculateReceipt(pnList : MutableList<Player>){
        when(typeOfReceipt){
            1 -> allocateTotalMoney(pnList)
            2 -> allocateGameMoney(pnList)
        }
//        return mutableListOf()
    }
}
