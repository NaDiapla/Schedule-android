package com.playgilround.schedule.client.calendarschedule

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.playgilround.schedule.client.R
import com.playgilround.schedule.client.calendar.adapter.ScheduleAdapter
import com.playgilround.schedule.client.calendar.util.EventDay
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.schedule_view.*

class CalendarScheduleFragment: Fragment(), CalendarScheduleContract.View {

    private lateinit var mPresenter: CalendarScheduleContract.Presenter
    private lateinit var mScheduleAdapter: ScheduleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        CalendarSchedulePresenter(context, this)
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onResume() {
        super.onResume()
        initScheduleList()

    }


    override fun getOneIcon(context: Context): Drawable {
        val drawable = ContextCompat.getDrawable(context, R.drawable.sample_one_icons)
        return InsetDrawable(drawable, 100, 0, 100, 0)
    }

    override fun getTwoIcon(context: Context): Drawable {
        val drawable = ContextCompat.getDrawable(context, R.drawable.sample_two_icons)
        return InsetDrawable(drawable, 100, 0, 100, 0)
    }

    override fun getThreeIcon(context: Context): Drawable {
        val drawable = ContextCompat.getDrawable(context, R.drawable.sample_three_icons)
        return InsetDrawable(drawable, 100, 0, 100, 0)
    }

    override fun getFourIcon(context: Context): Drawable {
        val drawable = ContextCompat.getDrawable(context, R.drawable.sample_four_icons)
        return InsetDrawable(drawable, 100, 0, 100, 0)
    }

    override fun addEvents(events: MutableList<EventDay>?) {

    }

    override fun eventFloating(open: Animation?, close: Animation?) {
        floatingBtn.setOnClickListener {

        }
    }

    @SuppressLint("WrongConstant")
    fun initScheduleList() {
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.VERTICAL

        Log.d("TEST", "rvSchedule List ->$rvScheduleList")
        rvScheduleList!!.layoutManager = manager

        val itemAnimation = DefaultItemAnimator()
        itemAnimation.supportsChangeAnimations = false
        rvScheduleList!!.itemAnimator = itemAnimation

        mScheduleAdapter = ScheduleAdapter(context)
        rvScheduleList!!.adapter = mScheduleAdapter
    }

    override fun setPresenter(presenter: CalendarScheduleContract.Presenter) {
        mPresenter = presenter
    }

}