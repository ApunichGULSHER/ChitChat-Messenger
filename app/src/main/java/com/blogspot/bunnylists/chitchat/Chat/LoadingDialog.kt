package com.blogspot.bunnylists.chitchat.Chat

import android.app.Activity
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import com.blogspot.bunnylists.chitchat.R

class LoadingDialog (val mActivity : Activity) {
    private lateinit var isDialog: AlertDialog
    fun startLoading(){
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_item, null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        isDialog.show()
    }
    fun isDismiss(){
        isDialog.dismiss()
    }
}