package com.jaewon.KSD.ui.calculation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Rect
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
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.MainActivity
import com.jaewon.KSD.R
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.data.ReceiptInfo
import com.jaewon.KSD.databinding.FragmentCalculationBinding

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
        val playerNameAdapter = PlayerNameAdapter()
        playerNameAdapter.pnList = calculationViewModel.pnAllList
        playerNameAdapter.setOnActivePChangeListener(object : PlayerNameAdapter.ActivePChangeListener{
            override fun onActivePChanged(player: Player) {
                for (receiptInfo in calculationViewModel.receiptInfoList){
                    receiptInfo.updatePlayers(calculationViewModel.getActivePNList(), player.active)

                }

            }
        })

        rcyPlayerName.addItemDecoration(PNRecyclerViewDecoration(20))
        rcyPlayerName.apply {
            layoutManager = LinearLayoutManager(mainActivity,LinearLayoutManager.HORIZONTAL,false)
            adapter = playerNameAdapter
        }

        binding.btnAddNBBReceipt.setOnClickListener {
            addNBBReceipt(calculationViewModel.getActivePNList())
        }
        binding.btnAddGameReceipt.setOnClickListener {
            addGameResult(calculationViewModel.getActivePNList())
        }

        binding.btnCalReceipt.setOnClickListener {
            binding.llResultNote.removeAllViews()
            // 영수증 확인 함수
            if (calculationViewModel.receiptInfoList.size == 0) {
                Toast.makeText(mainActivity,"계산할 내역이 없습니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var receiptName = ""
            var solution = ""
            for ((index,receipt) in calculationViewModel.receiptInfoList.withIndex()) {
                when(receipt.isReadyForCal()){
                    ReceiptInfo.OK -> continue
                    ReceiptInfo.PROBLEM_OF_NBB_TOTAL -> {
                        receiptName = binding.llReceiptNote[index].findViewById<TextView>(R.id.txt_receipt_kind).text.trim() as String
                        solution = "의 총액을 입력해주세요."
                        break
                    }
                    ReceiptInfo.PROBLEM_OF_NBB_PAY -> {
                        receiptName = binding.llReceiptNote[index].findViewById<TextView>(R.id.txt_receipt_kind).text.trim() as String
                        solution = "을 계산한 사람을 선택해주세요."
                        break
                    }
                    ReceiptInfo.PROBLEM_OF_GAME_ZERO -> {
                        receiptName = binding.llReceiptNote[index].findViewById<TextView>(R.id.txt_game_kind).text.trim() as String
                        solution = "의 총합을 0원으로 맞춰주세요."
                        break
                    }
                }
            }

            if (receiptName != "") {
                Toast.makeText(mainActivity,receiptName + solution,Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            calculationViewModel.calculationAll()
            updateCalResult(calculationViewModel.calResultInfoList)
            //calResultInfoList를 통해서 상세 결과도 만들어야할듯?

            Toast.makeText(mainActivity,"qq",Toast.LENGTH_SHORT).show()
        }

        /// 실험용
        binding.btnTest.setOnClickListener {
        }
        /// 실험용

        keepViews()
        return root
    }

    private fun keepViews(){
        for(receiptInfo in calculationViewModel.receiptInfoList){
            when(receiptInfo.typeOfReceipt){
                ReceiptInfo.NBB_TYPE -> binding.llReceiptNote.addView(createNBBView(receiptInfo),-1)
                ReceiptInfo.GAME_TYPE -> binding.llReceiptNote.addView(createGameView(receiptInfo),-1)
            }
        }
        if (calculationViewModel.calResultInfoList.size != 0){
            updateCalResult(calculationViewModel.calResultInfoList)
        }
    }

    private fun updateCalResult(calResultInfoList:MutableList<Triple<String,String,Int>>){

        val newCalResView = createCalResView(calResultInfoList)

        binding.llResultNote.addView(newCalResView)
    }

    @SuppressLint("MissingInflatedId", "InflateParams")
    //결과 View 생성
    private fun createCalResView(calResultInfoList:MutableList<Triple<String,String,Int>>):View{
        val newCalResView = LayoutInflater.from(mainActivity).inflate(R.layout.calculation_result_view,null,false)
        val rcyCalRes = newCalResView.findViewById<RecyclerView>(R.id.rcy_calculation_result)
        val calResultAdapter = CalResultAdapter()
        calResultAdapter.calResultInfoList = calResultInfoList
        rcyCalRes.apply {
            layoutManager = LinearLayoutManager(mainActivity)
            adapter = calResultAdapter
        }

        return newCalResView
    }

    @SuppressLint("InflateParams")
    private fun addNBBReceipt(pnAllList:MutableList<Player>) {

        val newReceiptInfo = ReceiptInfo(pnAllList,ReceiptInfo.NBB_TYPE)
        calculationViewModel.receiptInfoList.add(newReceiptInfo)
        val newNBBReceipt = createNBBView(newReceiptInfo)

        binding.llReceiptNote.addView(newNBBReceipt,-1)
    }

    private fun createNBBView(receiptInfo:ReceiptInfo):View{
        val newNBBReceipt = LayoutInflater.from(mainActivity).inflate(R.layout.receipt_view,null,false)
        val rcyReceiptPlayer = newNBBReceipt.findViewById<RecyclerView>(R.id.rcy_receipt_player)
        val playerNameAdapter = PlayerNameAdapter()
        playerNameAdapter.pnList = receiptInfo.players
        playerNameAdapter.type = 1
        rcyReceiptPlayer.addItemDecoration(PNRecyclerViewDecoration(13))
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

        playerNameAdapter.setOnPNBBChangeListener(object : PlayerNameAdapter.PNBBChangeListener{
            override fun onPNBBChanged() {
                txtReceiptForOne.text = DecimalFormat("인당 ###,###,###원").format(receiptInfo.getAmountForOne())
            }
        })

        receiptInfo.setOnUpdatePListener(object : ReceiptInfo.UpdatePListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onUpdateP() {
                playerNameAdapter.pnList = receiptInfo.players
                playerNameAdapter.notifyDataSetChanged()
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

        val newReceiptInfo = ReceiptInfo(pnAllList,ReceiptInfo.GAME_TYPE)
        calculationViewModel.receiptInfoList.add(newReceiptInfo)
        val newGameReceipt = createGameView(newReceiptInfo)

        binding.llReceiptNote.addView(newGameReceipt,-1)
    }

    @SuppressLint("MissingInflatedId", "InflateParams")
    private fun createGameView(receiptInfo: ReceiptInfo): View {
        val newGameReceipt = LayoutInflater.from(mainActivity).inflate(R.layout.game_result_view, null, false)

        val pnSurplusList = mutableListOf<Player>()
        val pnDeficitList = mutableListOf<Player>()
        val rcyGameSurplusPlayer = newGameReceipt.findViewById<RecyclerView>(R.id.rcy_game_surplus_player)
        val rcyGameDeficitPlayer = newGameReceipt.findViewById<RecyclerView>(R.id.rcy_game_deficit_player)
        val playerWMSurplusAdapter = PlayerWithMoneyAdapter()
        val playerWMDeficitAdapter = PlayerWithMoneyAdapter()
        playerWMSurplusAdapter.pnList = pnSurplusList
        playerWMDeficitAdapter.pnList = pnDeficitList
        rcyGameSurplusPlayer.addItemDecoration(PNRecyclerViewDecoration(5))
        rcyGameDeficitPlayer.addItemDecoration(PNRecyclerViewDecoration(5))


        val txtTotalAmount = newGameReceipt.findViewById<TextView>(R.id.txt_game_total_amount)
        val txtSurplusAmount = newGameReceipt.findViewById<TextView>(R.id.txt_game_surplus_amount)
        val txtDeficitAmount = newGameReceipt.findViewById<TextView>(R.id.txt_game_deficit_amount)
        var surplusAmount = 0
        var deficitAmount = 0

        playerWMSurplusAdapter.setOnPMChangeListener(object : PlayerWithMoneyAdapter.PMChangeListener{
            override fun onPMChanged() {
                surplusAmount = 0
                for (player in pnSurplusList){
                    surplusAmount += player.amount
                }
                txtSurplusAmount.text = DecimalFormat("###,###,###").format(surplusAmount)
                txtTotalAmount.text = DecimalFormat("###,###,###").format(surplusAmount - deficitAmount)
                receiptInfo.totalAmount = surplusAmount - deficitAmount
            }
        })

        playerWMDeficitAdapter.setOnPMChangeListener(object : PlayerWithMoneyAdapter.PMChangeListener{
            override fun onPMChanged() {
                deficitAmount = 0
                for (player in pnDeficitList){
                    deficitAmount += player.amount
                }
                txtDeficitAmount.text = DecimalFormat("###,###,###").format(deficitAmount)
                txtTotalAmount.text = DecimalFormat("###,###,###").format(surplusAmount - deficitAmount)
                receiptInfo.totalAmount = surplusAmount - deficitAmount
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

            }
            rcyGameSurplusPlayer.apply {
                layoutManager = GridLayoutManager(mainActivity, 3)
                adapter = playerWMSurplusAdapter
            }
            rcyGameDeficitPlayer.apply {
                layoutManager = GridLayoutManager(mainActivity, 3)
                adapter = playerWMDeficitAdapter
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

        receiptInfo.setOnUpdatePListener(object : ReceiptInfo.UpdatePListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onUpdateP() {
                pnSurplusList.clear()
                pnDeficitList.clear()
                for (player in receiptInfo.players){
                    if (player.gameSurplus) pnSurplusList.add(player)
                    else pnDeficitList.add(player)

                }
                playerWMSurplusAdapter.notifyDataSetChanged()
                playerWMDeficitAdapter.notifyDataSetChanged()
            }
        })

        val imgBtnGameDelete = newGameReceipt.findViewById<ImageButton>(R.id.imgBtn_game_delete)
        imgBtnGameDelete.setOnClickListener {
            binding.llReceiptNote.removeView(newGameReceipt)
            calculationViewModel.receiptInfoList.remove(receiptInfo)
        }

        return newGameReceipt
    }

    class PNRecyclerViewDecoration(val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.left = space
            outRect.bottom = space
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}