package com.jaewon.KSD.ui.calculation

import androidx.lifecycle.ViewModel
import com.jaewon.KSD.data.Group
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.data.ReceiptInfo
import kotlin.math.abs

class CalculationViewModel : ViewModel() {

    private var nameList = mutableListOf<String>("고", "오", "성", "석", "찬", "영", "현", "도", "장", "민")
    val pnAllList = string2PlayerList(nameList)
    val receiptInfoList = mutableListOf<ReceiptInfo>()
    val calResultInfoList = mutableListOf<Triple<String,String,Int>>()
    val detailDetailsMap = mutableMapOf<String,MutableList<Pair<String,Int>>>()
    val detailCalculationMap = mutableMapOf<String,MutableList<Pair<String,Int>>>()
    val detailDetailsAmountMap = mutableMapOf<String,Int>()
    val detailCalculationAmountMap = mutableMapOf<String,Int>()

    fun getActivePNList(): MutableList<Player>{
        val activePNList = mutableListOf<Player>()

        for (player in pnAllList){
            if (player.active) activePNList.add(player.copy())
        }

        return activePNList
    }

    private fun getCalculatedActivePNList(): MutableList<Player>{
        val newActivePNList = getActivePNList()
        detailDetailsMap.clear()
        detailCalculationMap.clear()

        for (player in newActivePNList){
            detailDetailsMap[player.name] = mutableListOf()
            detailCalculationMap[player.name] = mutableListOf()
        }

        for (receipt in receiptInfoList){
            receipt.calculateReceipt(newActivePNList, detailDetailsMap)
        }

        return newActivePNList
    }

    private fun string2PlayerList(nameList: MutableList<String>):MutableList<Player>{
        val pnList : MutableList<Player> = mutableListOf()
        for(name in nameList){
            pnList.add(Player(name))
        }
        return pnList
    }

    fun calculationAll(){
        val calculatedPlayerList = getCalculatedActivePNList()
        val surplusPList = mutableListOf<Player>()
        val deficitPList = mutableListOf<Player>()
        val zeroPList = mutableListOf<Player>()
        val allPList = mutableListOf<MutableList<Player>>(deficitPList,zeroPList,surplusPList)

        calResultInfoList.clear()
        for (player in calculatedPlayerList){
            if (player.amount == 0) zeroPList.add(player)
            else if (player.amount > 0 ) surplusPList.add(player)
            else deficitPList.add(player)
        }

        val surplusGroupList: MutableList<Group> = list2Group(surplusPList, 1)
        val deficitGroupList: MutableList<Group> = list2Group(deficitPList, -1)
//        val zeroGroupList: MutableList<Group> = list2Group(zeroPList, 0)

        val dividedGroupList = divGroup(surplusGroupList, deficitGroupList)
        for (divG in dividedGroupList) {
            var one : String
            var other : String
            var amount : Int
            for (remitInfo in remitWho(divG)){

                if (remitInfo.first.first == 0) {
                    for (defP in divG.deficitIndexMoneyList){
                        one = allPList[0][defP.first].name
                        other = allPList[remitInfo.second.first+1][remitInfo.second.second].name
                        amount = -defP.second
                        calResultInfoList.add(Triple(one,other,amount))
                        detailCalculationMap[one]?.add(Pair(other,-amount))
                        detailCalculationMap[other]?.add(Pair(one,amount))
                    }
                    break
                }
                if (remitInfo.second.first == 0){
                    for (surP in divG.surplusIndexMoneyList){
                        one = allPList[remitInfo.first.first+1][remitInfo.first.second].name
                        other = allPList[2][surP.first].name
                        amount = surP.second
                        calResultInfoList.add(Triple(one,other,amount))
                        detailCalculationMap[one]?.add(Pair(other,-amount))
                        detailCalculationMap[other]?.add(Pair(one,amount))
                    }
                    break
                }

                one = allPList[remitInfo.first.first+1][remitInfo.first.second].name
                other = allPList[remitInfo.second.first+1][remitInfo.second.second].name
                amount = remitInfo.third
                calResultInfoList.add(Triple(one,other,amount))
                detailCalculationMap[one]?.add(Pair(other,-amount))
                detailCalculationMap[other]?.add(Pair(one,amount))
            }
        }

        for (detailsLine in detailDetailsMap){
            var detailsAmount = 0
            for (lineAmount in detailsLine.value) {
                detailsAmount += lineAmount.second
            }
            detailDetailsAmountMap[detailsLine.key] = detailsAmount
        }
        for (calculationLine in detailCalculationMap){
            var calculationAmount = 0
            for (lineAmount in calculationLine.value) {
                calculationAmount += lineAmount.second
            }
            detailCalculationAmountMap[calculationLine.key] = calculationAmount
        }

    }

    /**
     * 총합이 0인 그룹에서 누가 누구에게 얼마를 보내야하는지 정산하는 함수
     *
     * @param dividedGroup 총합이 0인 그룹
     * @return MutableList of Triple{(돈을 보낼 PlayerType, 돈을 보낼 PlayerIndex),(돈을 받을 PlayerType, 돈을 받을 PlayerIndex),Amount}
     */
    private fun remitWho(dividedGroup: Group):MutableList<Triple<Pair<Int,Int>,Pair<Int,Int>,Int>>{
        val remitList: MutableList<Triple<Pair<Int,Int>,Pair<Int,Int>,Int>> = mutableListOf() //0이 1에게 2만큼 보내세요

        if (dividedGroup.surplusIndexMoneyList.size == 1){
            remitList.add(Triple(Pair(0,0), Pair(1,dividedGroup.surplusIndexMoneyList[0].first),0))
            return remitList
        }
        if (dividedGroup.deficitIndexMoneyList.size == 1){
            remitList.add(Triple(Pair(-1,dividedGroup.deficitIndexMoneyList[0].first), Pair(0,0),0))
            return remitList
        }


        dividedGroup.sortList()
        val surplusArray: Array<Pair<Int,Int>> = dividedGroup.surplusIndexMoneyList.toTypedArray()
        val surplusLastIndex: Int = surplusArray.size - 1
        for ((index,deficitP) in dividedGroup.deficitIndexMoneyList.withIndex()){
            if (index == dividedGroup.deficitIndexMoneyList.size - 1){
                surplusArray[surplusLastIndex] = Pair(surplusArray[surplusLastIndex].first, surplusArray[surplusLastIndex].second + deficitP.second)
                remitList.add(Triple(Pair(-1,deficitP.first), Pair(1,surplusArray[surplusLastIndex].first),-deficitP.second))
                break
            }
            val sIndex = if (index < surplusLastIndex) index else if (surplusLastIndex == 1) 0 else index - surplusLastIndex + 1
            surplusArray[sIndex] = Pair(surplusArray[sIndex].first, surplusArray[sIndex].second + deficitP.second)
            remitList.add(Triple(Pair(-1,deficitP.first), Pair(1,surplusArray[sIndex].first),-deficitP.second))


        }
        var firstPlus: Int? = surplusArray.indices.find { surplusArray[it].second > 0 }
        for (overP in surplusArray){
            if (overP.second == 0) break
            surplusArray[firstPlus!!] = Pair(surplusArray[firstPlus].first, surplusArray[firstPlus].second + overP.second)
            remitList.add(Triple(Pair(1,overP.first), Pair(1,surplusArray[firstPlus].first), -overP.second))

            if (surplusArray[firstPlus].second <= 0) firstPlus++

        }
        return remitList

    }

    /**
     * Player 리스트를 Player Group으로 변환
     * @param playerList MutableList of Player
     * @param groupType 그룹의 Type을 지정
     * @return MutableList of Group
     */
    private fun list2Group(playerList:MutableList<Player>, groupType: Int) : MutableList<Group>{
        val newGroupList = mutableListOf<Group>()
        if (groupType == 1){
            for ((index,player) in playerList.withIndex()){
                val newG = Group()
                newG.surplusIndexMoneyList.add(Pair(index,player.amount))
                newG.totalAmount = player.amount
                newGroupList.add(newG)
            }
        }
        else {
            for ((index,player) in playerList.withIndex()){
                val newG = Group()
                newG.deficitIndexMoneyList.add(Pair(index,player.amount))
                newG.totalAmount = player.amount
                newGroupList.add(newG)
            }
        }
        return newGroupList
    }

    /**
     * 개인 그룹 리스트를 받아서 총합이 0인 그룹들의 리스트를 반환
     *
     * @param surplusGroupList 돈을 받아야하는 Group List
     * @param deficitGroupList 돈을 내야하는 Group List
     * @return 총합이 0인 Group List
     */
    private fun divGroup(surplusGroupList: MutableList<Group>, deficitGroupList: MutableList<Group>) : MutableList<Group>{
        val dividedGroupList = mutableListOf<Group>()

        val comparisonGroupList : MutableList<Group>
        val basicGroupList : MutableList<Group>
        val comparisonValue : Int
        if (surplusGroupList.size > deficitGroupList.size) {
            comparisonValue = -1
            comparisonGroupList = makeAllCombinationGroup(deficitGroupList, deficitGroupList.size-1)
            basicGroupList = makeAllCombinationGroup(surplusGroupList, surplusGroupList.size/2)
        } else {
            comparisonValue = 1
            comparisonGroupList = makeAllCombinationGroup(surplusGroupList, surplusGroupList.size-1)
            basicGroupList = makeAllCombinationGroup(deficitGroupList, deficitGroupList.size/2)
        }
//        comparisonGroupList.sortBy { it.totalAmount }
//        basicGroupList.sortBy { it.totalAmount }

        var marker = false
        var basIndex = 0
        var comIndex : Int

        while (basIndex < basicGroupList.size){
            comIndex = 0
            while (comIndex < comparisonGroupList.size){
                val isGroup = comparisonGroupList[comIndex]+basicGroupList[basIndex]
                if (isGroup.totalAmount == 0){
                    dividedGroupList.add(isGroup)
                    marker = true
                    break
                }
                if (isGroup.totalAmount*comparisonValue > 0){
                    break
                }
                comIndex++
            }

            basIndex++

            if (marker){
                val sGL : MutableList<Group>
                val dGL : MutableList<Group>
                if (comparisonValue == 1){
                    sGL = comparisonGroupList
                    dGL = basicGroupList
                }
                else {
                    dGL = comparisonGroupList
                    sGL = basicGroupList
                }
                for (surplusPairOfIM in dividedGroupList.last().surplusIndexMoneyList){
                    sGL.removeIf{it.surplusIndexMoneyList.contains(surplusPairOfIM)}
                    surplusGroupList.removeIf{it.surplusIndexMoneyList.contains(surplusPairOfIM)}
                }
                for (deficitPairOfIM in dividedGroupList.last().deficitIndexMoneyList){
                    dGL.removeIf{it.deficitIndexMoneyList.contains(deficitPairOfIM)}
                    deficitGroupList.removeIf{it.deficitIndexMoneyList.contains(deficitPairOfIM)}
                }
                basIndex = 0
                marker = false
            }


        }
        if (basicGroupList.size != 0){
            var remainedGroup = Group()
            for (remainedG in surplusGroupList + deficitGroupList){
                remainedGroup += remainedG
            }
            dividedGroupList.add(remainedGroup)
        }
        return dividedGroupList
    }

    /**
     * Group List의 전체 집합을 제외한 모든 부분 집합 생성
     *
     * @param groupList 참조 Group List
     * @param maxSize 부분 집합의 최대 크기 제한
     * @return 최대 size가 maxSize인 groupList의 모든 부분 집합 List
     */
    private fun makeAllCombinationGroup(groupList: MutableList<Group>, maxSize: Int):MutableList<Group>{

        val newGroupList : MutableList<Group> = mutableListOf()
        if (groupList.size == 1) return groupList

        newGroupList.add(Group())
        for ( group in groupList){
            val size = newGroupList.size
            for (indexOfSubset in 0 until size){
                if (newGroupList[indexOfSubset].maxSize() == maxSize) continue
                newGroupList.add(newGroupList[indexOfSubset]+group)
            }
        }

        newGroupList.removeAt(0)
        newGroupList.sortBy { abs(it.totalAmount) }
        return newGroupList

    }

}