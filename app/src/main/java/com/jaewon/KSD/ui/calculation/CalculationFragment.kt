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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.MainActivity
import com.jaewon.KSD.R
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.databinding.FragmentCalculationBinding

class CalculationFragment : Fragment() {

    private var _binding: FragmentCalculationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var mainActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val calculationViewModel =
            ViewModelProvider(this).get(CalculationViewModel::class.java)

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
        binding.btnAddNBBReceipt.setOnClickListener {
            addReceipt(calculationViewModel.getActivePNList())
        }
        binding.btnAddGameReceipt.setOnClickListener {
            addGameResult(calculationViewModel.getActivePNList())
        }


        return root
    }

    @SuppressLint("InflateParams")
    private fun addReceipt(pnAllList:MutableList<Player>) {

        val newReceipt = LayoutInflater.from(mainActivity).inflate(R.layout.receipt_view,null,false)
        val rcyReceiptPlayer = newReceipt.findViewById<RecyclerView>(R.id.rcy_receipt_player)
        val playerNameAdapter = CalculationViewModel.PlayerNameAdapter()
        playerNameAdapter.pnList = pnAllList
        playerNameAdapter.type = 1
        rcyReceiptPlayer.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(13))
        rcyReceiptPlayer.apply {
            layoutManager = GridLayoutManager(mainActivity,7)
            adapter = playerNameAdapter
        }
        val txtReceiptKind = newReceipt.findViewById<TextView>(R.id.txt_receipt_kind)
        txtReceiptKind.setOnClickListener {
            val nbbOption = arrayOf<String>(
                resources.getString(R.string.receipt_nbb_1_launch),
                resources.getString(R.string.receipt_nbb_2_dinner),
                resources.getString(R.string.receipt_nbb_3_midnightSnack),
                resources.getString(R.string.receipt_nbb_4_NBB))
            AlertDialog.Builder(mainActivity)
                .setTitle("N빵 종류를 선택하세요").setItems(nbbOption) {
                    dialog, whitch ->
                    txtReceiptKind.text = nbbOption[whitch]
                }.show()
        }

        val edtReceiptAmount = newReceipt.findViewById<EditText>(R.id.edt_receipt_amount)
        var pointNumStr = ""
        edtReceiptAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(s.toString()) && !s.toString().equals(pointNumStr)) {
                    pointNumStr = DecimalFormat("###,###,###,###,###").format(Integer.parseInt(s.toString().replace(",", "")))
                    edtReceiptAmount.setText(pointNumStr)
                    edtReceiptAmount.setSelection(pointNumStr.length)  //커서를 오른쪽 끝으로 보냄
                }
            }

        })

        val imgBtnReceiptDelete = newReceipt.findViewById<ImageButton>(R.id.imgBtn_receipt_delete)
        imgBtnReceiptDelete.setOnClickListener {
            binding.llReceiptNote.removeView(newReceipt)
        }

        binding.llReceiptNote.addView(newReceipt,-1)
    }
    @SuppressLint("InflateParams")
    private fun addGameResult(pnAllList:MutableList<Player>) {

        val newGameResult = LayoutInflater.from(mainActivity).inflate(R.layout.game_result_view, null, false)

        val pnSurplusList = mutableListOf<Player>()
        val pnDeficitList = mutableListOf<Player>()
        val rcyGameSurplusPlayer = newGameResult.findViewById<RecyclerView>(R.id.rcy_game_surplus_player)
        val rcyGameDeficitPlayer = newGameResult.findViewById<RecyclerView>(R.id.rcy_game_deficit_player)
        val playerWMSurplusAdapter = CalculationViewModel.PlayerWithMoneyAdapter()
        val playerWMDeficitAdapter = CalculationViewModel.PlayerWithMoneyAdapter()
        playerWMSurplusAdapter.pnList = pnSurplusList
        playerWMDeficitAdapter.pnList = pnDeficitList
        rcyGameSurplusPlayer.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(5))
        rcyGameDeficitPlayer.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(5))

        val txtGameKind = newGameResult.findViewById<TextView>(R.id.txt_game_kind)
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
                    txtGameKind.text = gameOption[whitch]
                }.show()
        }

        val imgBtnGameAssignPlayer = newGameResult.findViewById<ImageButton>(R.id.imgBtn_game_assignPlayer)
        imgBtnGameAssignPlayer.setOnClickListener {
            val dialogAP = DialogAssignPlayer(mainActivity)
            dialogAP.dialogAP(pnAllList)
            dialogAP.setOnClickedListener(object : DialogAssignPlayer.ButtonClickListener{
                override fun onClicked(pnGameResultList: MutableList<Player>) {
                    pnSurplusList.clear()
                    pnDeficitList.clear()
                    for (player in pnAllList){
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

        val imgBtnGameDelete = newGameResult.findViewById<ImageButton>(R.id.imgBtn_game_delete)
        imgBtnGameDelete.setOnClickListener {
            binding.llReceiptNote.removeView(newGameResult)
        }

        binding.llReceiptNote.addView(newGameResult,-1)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}