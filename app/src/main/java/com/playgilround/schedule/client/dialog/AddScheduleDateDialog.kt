package com.playgilround.schedule.client.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.playgilround.schedule.client.R
import java.util.*

class AddScheduleDateDialog(context: Context?): Dialog(context) {

    private var calendar = Calendar.getInstance()

    //todo: custom calendarView 터치 시 dismiss 되도록 수정, header DateFormat 수정
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_schedule_date)

    }

    fun getDate(): Calendar {
        calendar.set(2019, 7, 27)
        return calendar
    }
}