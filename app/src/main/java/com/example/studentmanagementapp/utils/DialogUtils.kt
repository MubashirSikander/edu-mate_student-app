package com.example.studentmanagementapp.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {
    fun showNoInternetDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("No Internet Connection")
            .setMessage("Please connect to the internet to sync your latest changes.")
            .setPositiveButton("OK", null)
            .show()
    }
}
