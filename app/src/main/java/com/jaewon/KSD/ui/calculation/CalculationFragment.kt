package com.jaewon.KSD.ui.calculation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.MainActivity
import com.jaewon.KSD.R
import com.jaewon.KSD.data.Group
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.data.ReceiptInfo
import com.jaewon.KSD.databinding.FragmentCalculationBinding
import kotlin.math.abs

class CalculationFragment : Fragment() {

    private var _binding: FragmentCalculationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var mainActivity: MainActivity

    private lateinit var calculationViewModel : CalculationViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        calculationViewModel =
            ViewModelProvider(this)[CalculationViewModel::class.java]

        _binding = FragmentCalculationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val rcyPlayerName: RecyclerView = binding.rcyPlayerName
        val playerNameAdapter = CalculationViewModel.PlayerNameAdapter()
        playerNameAdapter.pnList = calculationViewModel.pnAllList
        rcyPlayerName.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(20))
        rcyPlayerName.apply {
            layoutManager = LinearLayoutManager(mainActivity,LinearLayoutManager.HORIZONTAL,false)
            adapter = playerNameAdapter
        }
        binding.imgBtnSettingPlayer.setOnClickListener {

        }

        binding.btnAddNBBReceipt.setOnClickListener {
            addNBBReceipt(calculationViewModel.getActivePNList())
        }
        binding.btnAddGameReceipt.setOnClickListener {
            addGameResult(calculationViewModel.getActivePNList())
        }

        binding.btnCalReceipt.setOnClickListener {
            val calculatedPlayerList = calculationViewModel.getCalculatedActivePNList()

//            Toast.makeText(mainActivity,calculatedPlayerList[0].name + calculatedPlayerList[0].amount.toString(),Toast.LENGTH_SHORT).show()

            // 무리 나누기 함수
            // 송금하기 함수
            // 표시하기 함수

            val surplusPList = mutableListOf<Player>()
            val deficitPList = mutableListOf<Player>()
            val zeroPList = mutableListOf<Player>()

            for (player in calculatedPlayerList){
                if (player.amount == 0) zeroPList.add(player)
                else if (player.amount > 0 ) surplusPList.add(player)
                else deficitPList.add(player)
            }

            val surplusGroupList: MutableList<Group> = list2Group(surplusPList, 1)
            val deficitGroupList: MutableList<Group> = list2Group(deficitPList, -1)
            val zeroGroupList: MutableList<Group> = list2Group(zeroPList, 0)

            val dividedGroupList = divGroup(surplusGroupList, deficitGroupList)
            for (divG in dividedGroupList){
                var txt = ""
                for (pairTI in divG.typeIndexList){
                    when(pairTI.first){
                        1 -> txt += surplusPList[pairTI.second].name
                        -1 -> txt += deficitPList[pairTI.second].name
                    }
                }
                Toast.makeText(mainActivity, txt, Toast.LENGTH_SHORT).show()
            }

        }

        /// 실험용
        binding.btnTest.setOnClickListener {
            val newReceipt : View = if (calculationViewModel.receiptInfoList[0].typeOfReceipt == 1){
                createNBBView(calculationViewModel.receiptInfoList[0])
            } else {
                createGameView(calculationViewModel.receiptInfoList[0])
            }
            binding.llReceiptNote.addView(newReceipt,-1)
        }
        /// 실험용
        return root
    }

    fun list2Group(playerList:MutableList<Player>, groupType: Int) : MutableList<Group>{
        val newGroupList = mutableListOf<Group>()
        for ((index,player) in playerList.withIndex()){
            val newG = Group()
            newG.typeIndexList.add(Pair(groupType,index))
            newG.totalAmount = player.amount
            newGroupList.add(newG)
        }
        return newGroupList
    }

    fun divGroup(surplusGroupList: MutableList<Group>, deficitGroupList: MutableList<Group>) : MutableList<Group>{
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
        var comIndex = 0

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
                for (pairOfTI in dividedGroupList.last().typeIndexList){
                    when(pairOfTI.first*comparisonValue){
                        1 -> comparisonGroupList.removeIf{it.typeIndexList.contains(pairOfTI)}
                        -1 -> basicGroupList.removeIf{it.typeIndexList.contains(pairOfTI)}

                    }
                    when(pairOfTI.first){
                        1 -> surplusGroupList.removeIf{it.typeIndexList.contains(pairOfTI)}
                        -1 -> deficitGroupList.removeIf{it.typeIndexList.contains(pairOfTI)}
                    }
                }
                basIndex = 0
                marker = false
            }


        }
//        Toast.makeText(mainActivity,"그룹 수 : "+dividedGroupList.size, Toast.LENGTH_SHORT).show()
        if (basicGroupList.size != 0){
            var remainedGroup = Group()
            for (remainedG in surplusGroupList + deficitGroupList){
               remainedGroup += remainedG
            }
            dividedGroupList.add(remainedGroup)
        }
        return dividedGroupList
    }

    fun makeAllCombinationGroup(groupList: MutableList<Group>, maxSize: Int):MutableList<Group>{

        val newGroupList : MutableList<Group> = mutableListOf()
        if (groupList.size == 1) return groupList

        newGroupList.add(Group())
        for ( group in groupList){
            val size = newGroupList.size
            for (indexOfSubset in 0 until size){
                if (newGroupList[indexOfSubset].typeIndexList.size == maxSize) continue
                newGroupList.add(newGroupList[indexOfSubset]+group)
            }
        }
        newGroupList.removeAt(0)
        newGroupList.sortBy { abs(it.totalAmount) }
        return newGroupList

    }


    @SuppressLint("InflateParams")
    private fun addNBBReceipt(pnAllList:MutableList<Player>) {

        val newReceiptInfo = ReceiptInfo(pnAllList,1)
        calculationViewModel.receiptInfoList.add(newReceiptInfo)
        val newNBBReceipt = createNBBView(newReceiptInfo)

        binding.llReceiptNote.addView(newNBBReceipt,-1)
    }

    private fun createNBBView(receiptInfo:ReceiptInfo):View{
        val newNBBReceipt = LayoutInflater.from(mainActivity).inflate(R.layout.receipt_view,null,false)
        val rcyReceiptPlayer = newNBBReceipt.findViewById<RecyclerView>(R.id.rcy_receipt_player)
        val playerNameAdapter = CalculationViewModel.PlayerNameAdapter()
        playerNameAdapter.pnList = receiptInfo.players
        playerNameAdapter.type = 1
        rcyReceiptPlayer.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(13))
        rcyReceiptPlayer.apply {
            layoutManager = GridLayoutManager(mainActivity,7)
            adapter = playerNameAdapter
        }
        val txtReceiptKind = newNBBReceipt.findViewById<TextView>(R.id.txt_receipt_kind)
        if (receiptInfo.subtypeOfReceipt == ""){
            receiptInfo.subtypeOfReceipt = resources.getString(R.string.receipt_nbb_1_launch)
        }
        txtReceiptKind.text = receiptInfo.subtypeOfReceipt

        txtReceiptKind.setOnClickListener {
            val nbbOption = arrayOf<String>(
                resources.getString(R.string.receipt_nbb_1_launch),
                resources.getString(R.string.receipt_nbb_2_dinner),
                resources.getString(R.string.receipt_nbb_3_midnightSnack),
                resources.getString(R.string.receipt_nbb_4_NBB))
            AlertDialog.Builder(mainActivity)
                .setTitle("N빵 종류를 선택하세요").setItems(nbbOption) {
                        dialog, whitch ->
                    receiptInfo.subtypeOfReceipt = nbbOption[whitch]
                    txtReceiptKind.text = receiptInfo.subtypeOfReceipt
                }.show()
        }
        val txtReceiptForOne = newNBBReceipt.findViewById<TextView>(R.id.txt_receipt_for_one)
        val edtReceiptAmount = newNBBReceipt.findViewById<EditText>(R.id.edt_receipt_amount)

        var pointNumInt = receiptInfo.totalAmount
        var pointNumStr = DecimalFormat("###,###,###").format(pointNumInt)
        if (pointNumInt != 0){
            edtReceiptAmount.setText(pointNumStr)
        }

        edtReceiptAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s.toString())){
                    pointNumInt = 0
                    pointNumStr = ""
                }
                if (!s.toString().equals(pointNumStr)) {
                    pointNumInt = Integer.parseInt(s.toString().replace(",", ""))
                    pointNumStr = DecimalFormat("###,###,###").format(pointNumInt)
                    edtReceiptAmount.setText(pointNumStr)
                    edtReceiptAmount.setSelection(pointNumStr.length)  //커서를 오른쪽 끝으로 보냄
                }
                receiptInfo.totalAmount = pointNumInt
                txtReceiptForOne.text = DecimalFormat("인당 ###,###,###원").format(receiptInfo.getAmountForOne())

            }
        })

        playerNameAdapter.setOnPNBBChangeListener(object : CalculationViewModel.PlayerNameAdapter.PNBBChangeListener{
            override fun onPNBBChanged() {
                txtReceiptForOne.text = DecimalFormat("인당 ###,###,###원").format(receiptInfo.getAmountForOne())
            }
        })

        val imgBtnReceiptDelete = newNBBReceipt.findViewById<ImageButton>(R.id.imgBtn_receipt_delete)
        imgBtnReceiptDelete.setOnClickListener {
            binding.llReceiptNote.removeView(newNBBReceipt)
            calculationViewModel.receiptInfoList.remove(receiptInfo)
        }
        return newNBBReceipt
    }

    @SuppressLint("InflateParams")
    private fun addGameResult(pnAllList:MutableList<Player>) {

        val newReceiptInfo = ReceiptInfo(pnAllList,2)
        calculationViewModel.receiptInfoList.add(newReceiptInfo)
        val newGameReceipt = createGameView(newReceiptInfo)

        binding.llReceiptNote.addView(newGameReceipt,-1)
    }

    @SuppressLint("MissingInflatedId")
    private fun createGameView(receiptInfo: ReceiptInfo): View {
        val newGameReceipt = LayoutInflater.from(mainActivity).inflate(R.layout.game_result_view, null, false)

        val pnSurplusList = mutableListOf<Player>()
        val pnDeficitList = mutableListOf<Player>()
        val rcyGameSurplusPlayer = newGameReceipt.findViewById<RecyclerView>(R.id.rcy_game_surplus_player)
        val rcyGameDeficitPlayer = newGameReceipt.findViewById<RecyclerView>(R.id.rcy_game_deficit_player)
        val playerWMSurplusAdapter = CalculationViewModel.PlayerWithMoneyAdapter()
        val playerWMDeficitAdapter = CalculationViewModel.PlayerWithMoneyAdapter()
        playerWMSurplusAdapter.pnList = pnSurplusList
        playerWMDeficitAdapter.pnList = pnDeficitList
        rcyGameSurplusPlayer.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(5))
        rcyGameDeficitPlayer.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(5))


        val txtTotalAmount = newGameReceipt.findViewById<TextView>(R.id.txt_game_total_amount)
        val txtSurplusAmount = newGameReceipt.findViewById<TextView>(R.id.txt_game_surplus_amount)
        val txtDeficitAmount = newGameReceipt.findViewById<TextView>(R.id.txt_game_deficit_amount)
        var surplusAmount = 0
        var deficitAmount = 0

        playerWMSurplusAdapter.setOnPMChangeListener(object : CalculationViewModel.PlayerWithMoneyAdapter.PMChangeListener{
            override fun onPMChanged() {
                surplusAmount = 0
                for (player in pnSurplusList){
                    surplusAmount += player.amount
                }
                txtSurplusAmount.text = DecimalFormat("###,###,###").format(surplusAmount)
                txtTotalAmount.text = DecimalFormat("###,###,###").format(surplusAmount - deficitAmount)
            }
        })

        playerWMDeficitAdapter.setOnPMChangeListener(object : CalculationViewModel.PlayerWithMoneyAdapter.PMChangeListener{
            override fun onPMChanged() {
                deficitAmount = 0
                for (player in pnDeficitList){
                    deficitAmount += player.amount
                }
                txtDeficitAmount.text = DecimalFormat("###,###,###").format(deficitAmount)
                txtTotalAmount.text = DecimalFormat("###,###,###").format(surplusAmount - deficitAmount)
            }
        })


        val imgBtnGameAssignPlayer = newGameReceipt.findViewById<ImageButton>(R.id.imgBtn_game_assignPlayer)
        imgBtnGameAssignPlayer.setOnClickListener {
            val dialogAP = DialogAssignPlayer(mainActivity)
            dialogAP.dialogAP(receiptInfo.players)
            dialogAP.setOnClickedListener(object : DialogAssignPlayer.ButtonClickListener{
                override fun onClicked() {
                    pnSurplusList.clear()
                    pnDeficitList.clear()
                    for (player in receiptInfo.players){
                        if (player.gameSurplus) pnSurplusList.add(player)
                        else pnDeficitList.add(player)
                        rcyGameSurplusPlayer.apply {
                            layoutManager = GridLayoutManager(mainActivity, 3)
                            adapter = playerWMSurplusAdapter
                        }
                        rcyGameDeficitPlayer.apply {
                            layoutManager = GridLayoutManager(mainActivity, 3)
                            adapter = playerWMDeficitAdapter
                        }

                    }
                }
            })
        }

        val txtGameKind = newGameReceipt.findViewById<TextView>(R.id.txt_game_kind)
        if (receiptInfo.subtypeOfReceipt == "") {
            receiptInfo.subtypeOfReceipt = resources.getString(R.string.receipt_game_1_standUp)
        }
        else {
            pnSurplusList.clear()
            pnDeficitList.clear()
            for (player in receiptInfo.players){
                if (player.gameSurplus) pnSurplusList.add(player)
                else pnDeficitList.add(player)
                rcyGameSurplusPlayer.apply {
                    layoutManager = GridLayoutManager(mainActivity, 3)
                    adapter = playerWMSurplusAdapter
                }
                rcyGameDeficitPlayer.apply {
                    layoutManager = GridLayoutManager(mainActivity, 3)
                    adapter = playerWMDeficitAdapter
                }

            }

        }
        txtGameKind.text = receiptInfo.subtypeOfReceipt

        txtGameKind.setOnClickListener {
            val gameOption = arrayOf<String>(
                resources.getString(R.string.receipt_game_1_standUp),
                resources.getString(R.string.receipt_game_2_goStop),
                resources.getString(R.string.receipt_game_3_holdem),
                resources.getString(R.string.receipt_game_4_highLow),
                resources.getString(R.string.receipt_game_5_geneLowest),
                resources.getString(R.string.receipt_game_6_game))

            AlertDialog.Builder(mainActivity)
                .setTitle("게임 종류를 선택하세요").setItems(gameOption) {
                        dialog, whitch ->
                    receiptInfo.subtypeOfReceipt = gameOption[whitch]
                    txtGameKind.text = receiptInfo.subtypeOfReceipt
                }.show()
        }



        val imgBtnGameDelete = newGameReceipt.findViewById<ImageButton>(R.id.imgBtn_game_delete)
        imgBtnGameDelete.setOnClickListener {
            binding.llReceiptNote.removeView(newGameReceipt)
            calculationViewModel.receiptInfoList.remove(receiptInfo)
        }

        return newGameReceipt
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}