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
            if (totalAmount == 0) return PROBLEM_OF_NBB_TOTAL
            for (player in players){
                if (player.statePay) return OK
            }
            return PROBLEM_OF_NBB_PAY
        }
        if (typeOfReceipt == 2 && totalAmount != 0) return PROBLEM_OF_GAME_ZERO
        return OK
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
//    fun getPlayerToPay():Player{
//        var player2pay : Player = Player("__fail")
//        for (player in players){
//            if (player.statePay) player2pay = player
//        }
//        return player2pay
//    }
    fun getAmountForOne(): Int {
        if (getNumOfStateOnPlayers() == 0) return 0
        return (totalAmount / (getNumOfStateOnPlayers() + 0.0)).roundToInt()
    }
    private fun allocateTotalMoney(pnList : MutableList<Player>, detailDetailsMap: MutableMap<String,MutableList<Pair<String,Int>>>){
        val amountForOne = getAmountForOne()
        for ((index,player) in players.withIndex()){
            if (!player.stateOn) continue
            if (player.statePay) {
                detailDetailsMap[player.name]?.add(Pair(subtypeOfReceipt,amountForOne*(getNumOfStateOnPlayers()-1)))
                pnList[index].amount += amountForOne*(getNumOfStateOnPlayers()-1)
                continue
            }
            detailDetailsMap[player.name]?.add(Pair(subtypeOfReceipt,-amountForOne))
            pnList[index].amount -= amountForOne
        }
//        return players
    }
    private fun allocateGameMoney(pnList : MutableList<Player>, detailDetailsMap: MutableMap<String,MutableList<Pair<String,Int>>>){
        for ((index,player) in players.withIndex()){
            if (player.gameSurplus) {
                detailDetailsMap[player.name]?.add(Pair(subtypeOfReceipt,player.amount))
                pnList[index].amount += player.amount
            }
            else {
                detailDetailsMap[player.name]?.add(Pair(subtypeOfReceipt,-player.amount))
                pnList[index].amount -= player.amount
            }
        }
//        return players
    }
    fun calculateReceipt(pnList : MutableList<Player>, detailDetailsMap: MutableMap<String,MutableList<Pair<String,Int>>>){
        when(typeOfReceipt){
            1 -> allocateTotalMoney(pnList, detailDetailsMap)
            2 -> allocateGameMoney(pnList, detailDetailsMap)
        }
//        return mutableListOf()
    }

    companion object {
        const val NBB_TYPE: Int = 1
        const val GAME_TYPE: Int = 2
        const val OK: Int = 0
        const val PROBLEM_OF_NBB_TOTAL: Int = 1
        const val PROBLEM_OF_NBB_PAY: Int = 2
        const val PROBLEM_OF_GAME_ZERO: Int = 3
    }
}
