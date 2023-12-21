package com.jaewon.KSD.data

data class Group(
    var surplusIndexMoneyList: MutableList<Pair<Int,Int>> = mutableListOf(),
    var deficitIndexMoneyList: MutableList<Pair<Int,Int>> = mutableListOf(),
    var totalAmount:Int = 0
) {

    operator fun plus(other: Group) : Group {
        val newGroup = Group()
        newGroup.surplusIndexMoneyList = surplusIndexMoneyList.plus(other.surplusIndexMoneyList) as MutableList<Pair<Int, Int>>
        newGroup.deficitIndexMoneyList = deficitIndexMoneyList.plus(other.deficitIndexMoneyList) as MutableList<Pair<Int, Int>>
        newGroup.totalAmount = totalAmount + other.totalAmount

        return newGroup
    }
    fun maxSize(): Int{
        return surplusIndexMoneyList.size.coerceAtLeast(deficitIndexMoneyList.size)
    }
    fun sortList(){
        surplusIndexMoneyList.sortBy { it.second }
        deficitIndexMoneyList.sortBy { it.second }
    }

}
