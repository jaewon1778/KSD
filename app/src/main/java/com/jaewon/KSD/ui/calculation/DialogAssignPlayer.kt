package com.jaewon.KSD.ui.calculation

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.R
import com.jaewon.KSD.data.Player

class DialogAssignPlayer(context: Context) {
    private val dialog = Dialog(context)

    fun dialogAP(pnAllList:MutableList<Player>) {
        dialog.setContentView(R.layout.dialog_assign_player)
//        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val rcyDapPlayer = dialog.findViewById<RecyclerView>(R.id.rcy_dap_player)
        val playerNameAdapter = CalculationViewModel.PlayerNameAdapter()
        playerNameAdapter.pnList = pnAllList
        playerNameAdapter.type = 2
        rcyDapPlayer.addItemDecoration(CalculationViewModel.PNRecyclerViewDecoration(10))
        rcyDapPlayer.apply {
            layoutManager = GridLayoutManager(context, 5)
            adapter =playerNameAdapter
        }

        dialog.findViewById<Button>(R.id.btn_dap_confirm).setOnClickListener {
            onClickedListener.onClicked(playerNameAdapter.pnList)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.btn_dap_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    interface ButtonClickListener {
        fun onClicked(pnGameResultList:MutableList<Player>)
    }
    private lateinit var onClickedListener : ButtonClickListener

    fun setOnClickedListener (listener: ButtonClickListener){
        onClickedListener = listener
    }

}