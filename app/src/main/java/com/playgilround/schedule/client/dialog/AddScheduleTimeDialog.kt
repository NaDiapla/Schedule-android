package com.playgilround.schedule.client.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.playgilround.schedule.client.R
import kotlinx.android.synthetic.main.dialog_add_schedule_time.*

class AddScheduleTimeDialog(context: Context?): Dialog(context) {

    private var hour = 0
    private var min = 0

    //Todo: Header 변경 필요
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_schedule_time)

        dialogTimePicker.setIs24HourView(true)
        dialogTimeCancel.setOnClickListener {
            dismiss()
        }
        dialogTimeConfirm.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = dialogTimePicker.hour
                min = dialogTimePicker.minute
            } else {
                hour = dialogTimePicker.currentHour
                min = dialogTimePicker.currentMinute
            }
            dismiss()
        }
    }

    fun getTime(): Array<Int> {
        return arrayOf(hour, min)
    }
}