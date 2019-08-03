package com.playgilround.schedule.client.addschedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.playgilround.schedule.client.R;
import com.playgilround.schedule.client.calendar.CalendarView;
import com.playgilround.schedule.client.locationschedule.LocationScheduleActivity;
import com.playgilround.schedule.client.model.Schedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.playgilround.schedule.client.addschedule.AddSchedulePresenter.SCHEDULE_SAVE_FAIL;
import static com.playgilround.schedule.client.detailschedule.DetailScheduleFragment.INTENT_MODIFY_ID;
import static com.playgilround.schedule.client.infoschedule.InfoScheduleActivity.ADD_SCHEDULE;

/**
 * 18-12-30
 * 스케줄 추가 관련 Activity
 */
public class AddScheduleActivity extends AppCompatActivity implements OnDateSetListener, AddScheduleContract.View {

    static final String TAG = AddScheduleActivity.class.getSimpleName();

    @BindView(R.id.tvdate)
    TextView tvDate;

    @BindView(R.id.tvScheduleTime)
    TextView tvTime;

    @BindView(R.id.tvScheduleLocation)
    TextView tvLocation;

    @BindView(R.id.btnConfirm)
    Button btnConfirm;

    @BindView(R.id.etScheduleTitle)
    EditText etTitle;

    @BindView(R.id.etScheduleDesc)
    EditText etDesc;

    String strMDay, strMTime, strMYearMonth, strInput, strRetTime;

    //Schedule DB  dateDay 컬럼에 들어갈 항목
    ArrayList<String> arrDateDay;

    //Schedule DB date 컬럼에 들어갈 항목
    ArrayList<String> arrDate;
    int chooseSize;

    //LocationScheduleActivity.class 에서 받은 위치정보.
    String resLocation;
    Double resLatitude = 0.0;
    Double resLongitude = 0.0;

    public static final int LOCATION_START = 0x1000;
    public static final int LOCATION_OK = 0x1001;
    public static final String HOUR_MINUTE = "hour_minute";

    TimePickerDialog timePickerDialog;

    //단일인지 다중인지 판단하는 플래그.
    public boolean isManyDay = false;

    //추가인지, 수정인지 판단하는 플래그
    public boolean isModify = false;

    Realm realm;

    int scheduleId;

    private AddScheduleContract.Presenter mPresenter;

    Double latitude;
    Double longitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_add_schedule);

        ButterKnife.bind(this);

        arrDateDay = new ArrayList<>();
        arrDate = new ArrayList<>();


        new AddSchedulePresenter(this, this);
        Intent intent = getIntent();
        strInput = intent.getStringExtra("inputText");
        etTitle.setText(strInput);

        strMTime = "00:00";

        int scheduleId = intent.getIntExtra(INTENT_MODIFY_ID, -1);
        if (scheduleId != -1) {
            //스케줄 수정 모드
            mPresenter.getScheduleInfo(scheduleId);
        } else if (intent.getStringArrayListExtra("dateArr") != null) {
            //다중 날짜 선택일 경우
            arrDateDay = intent.getStringArrayListExtra("dateArr");

            //yyyy-MM 포맷으로 arrDate 저장
            for (String date : arrDateDay) {
                String strYear = date.substring(0, 4);
                String strMonth = date.substring(5, 7);
                strMYearMonth = strYear + "-" + strMonth;
                arrDate.add(strMYearMonth);
            }

            String strTime = arrDateDay.get(0);
            int checkDate = arrDateDay.size() -1;

            if (checkDate == 0) {
                strRetTime = strTime;
            } else {
                strRetTime = strTime + " 외 " + (arrDateDay.size() - 1) + "일";
            }

            chooseSize = arrDateDay.size();
            isManyDay = true;
            tvDate.setText(arrDateDay.get(0) + " ~ " + arrDateDay.get(arrDateDay.size() - 1));
            tvTime.setText(strRetTime);
        }

        btnConfirm.setOnClickListener(l -> {
            //arrDateDay, arrDate 부분 추후 수정 예정.
            Log.d(TAG, "arrDateDay ->" + strMDay);
            Log.d(TAG, "arrDateDay2 ->" + tvTime.getText().toString());
            Log.d(TAG, "arrDate->" + strMYearMonth);
            Log.d(TAG, "arrDate ->" + tvTime.getText().toString().substring(0, 7));
            if (arrDateDay.size() == 0) {
                if (strMDay == null) {
                    arrDateDay.add(tvTime.getText().toString());
                } else {
                    arrDateDay.add(strMDay);
                }
            }

            if (arrDate.size() == 0) {
                if (strMYearMonth == null) {
                    arrDate.add(tvTime.getText().toString().substring(0, 7));
                } else {
                    arrDate.add(strMYearMonth);
                }
            }
            mPresenter.confirm(scheduleId, arrDate, arrDateDay, etTitle.getText().toString(), etDesc.getText().toString(), strMTime, resLatitude, resLongitude, resLocation);
        });

        //TimePicker
        timePickerDialog = new TimePickerDialog.Builder()
                .setType(Type.HOURS_MINS)
                .setCallBack(this)
                .setSureStringId(getString(R.string.button_confirm))
                .setCancelStringId(getString(R.string.button_cancel))
                .setTitleStringId(getString(R.string.text_time_set))
                .setThemeColor(getResources().getColor(R.color.colorGreen))
                .setHourText(getString(R.string.text_hour))
                .setMinuteText(getString(R.string.text_minute))
                .setWheelItemTextSize(20)
                .build();
    }

    @OnClick(R.id.llScheduleTime)
    void onShowTimeDialog() {
       /* DatePickerBuilder dateBuilder = new DatePickerBuilder(this, this)
                .headerColor(R.color.colorGreen)
                .headerLabelColor(android.R.color.white)
                .selectionColor(R.color.colorGreen)
                .todayLabelColor(R.color.colorAccent)
                .dialogButtonsColor(android.R.color.holo_green_dark)
                .previousButtonSrc(R.drawable.ic_chevron_left_black_24dp)
                .forwardButtonSrc(R.drawable.ic_chevron_right_black_24dp);
        if (isManyDay) {
            dateBuilder.pickerType(CalendarView.MANY_DAYS_PICKER);
        } else {
            dateBuilder.pickerType(CalendarView.ONE_DAY_PICKER);
        }

        DatePicker datePicker = dateBuilder.build();
        datePicker.show();*/
    }


    //최초 null
    @OnClick(R.id.llScheduleLocation)
    void onShowLocationActivity() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(this, LocationScheduleActivity.class);
            Log.d(TAG, "latitude ->" + latitude + "--" + longitude);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivityForResult(intent, LOCATION_START);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_gps_enable), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);
    }

    @Override
    public void setScheduleInfo(Schedule schedule) {
        scheduleId = schedule.getId();
        tvDate.setText(schedule.getDateDay());
        etTitle.setText(schedule.getTitle());

        long time = schedule.getTime();
        DateTime dateTime = new DateTime(time, DateTimeZone.getDefault());
        String strTime = dateTime.toString(getString(R.string.text_date_year_month_day));

        tvTime.setText(strTime);
        tvLocation.setText(schedule.getLocation());
        etDesc.setText(schedule.getDesc());

        latitude = schedule.getLatitude();
        longitude = schedule.getLongitude();

        isModify = true;
    }

    @Override
    public void onScheduleSave(String state, Schedule schedule) {
        Log.d(TAG, "onScheduleSave ->" + state);

        if (state.equals(SCHEDULE_SAVE_FAIL)) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_input_schedule), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_save_schedule), Toast.LENGTH_LONG).show();
            Intent intent = new Intent();

            if (isModify) {
                setResult(ADD_SCHEDULE, intent);
            } else if (!isManyDay) {
                intent.putExtra("date", tvDate.getText());
                intent.putExtra("dateDay", arrDateDay.get(0));
                setResult(ADD_SCHEDULE, intent);
            } else {
                intent.putExtra("date", arrDate.get(0));
                setResult(ADD_SCHEDULE, intent);
            }
            mPresenter.onNewSchedule(schedule);
            finish();
        }
    }

    //Dialog Day Click Event
//    @Override
//    public void onSelect(List<Calendar> calendars) {
//        mPresenter.onSelectDay(calendars);
//    }

    //날짜 재 지정
    @SuppressLint("SetTextI18n")
    @Override
    public void setDaySchedule(ArrayList<String> arrDateDay, String title, int size) {
        strMDay = arrDateDay.get(0);
        if (size > 1) {
            chooseSize = size - 1;
            tvDate.setText(strMDay + " ~ " + arrDateDay.get(size - 1));
            tvTime.setText(strMDay + " 외 " + chooseSize + "일");
            isManyDay = true;
        } else {
            tvDate.setText(title);
            tvTime.setText(strMDay);
            isManyDay = false;
        }
        //시, 분을 설정하는 다이얼로그 표시
        timePickerDialog.show(getSupportFragmentManager(), HOUR_MINUTE);
    }

    //Dialog Time Click Event
    @Override
    public void onDateSet(TimePickerDialog timePickerDialog, long milliseconds) {
        mPresenter.onSelectTime(strMDay, milliseconds);
    }

    //시간 재 지정
    @SuppressLint("SetTextI18n")
    @Override
    public void setTimeSchedule(String dayTime, String time) {
        if (isManyDay) {
            tvTime.setText(dayTime + " 외 " + chooseSize + "일");
            strMTime = time;
        } else {
            tvTime.setText(dayTime);
            strMTime = time;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == LOCATION_OK) {
            switch (requestCode) {
                case LOCATION_START:

                    resLocation = data.getStringExtra(LocationScheduleActivity.INTENT_EXTRA_LOCATION);
                    resLatitude = data.getDoubleExtra(LocationScheduleActivity.INTENT_EXTRA_LATITUDE, 0);
                    resLongitude = data.getDoubleExtra(LocationScheduleActivity.INTENT_EXTRA_LONGITUDE, 0);

                    if (TextUtils.isEmpty(resLocation)) {
                        tvLocation.setText(R.string.text_add_location);
                    } else {
                        tvLocation.setText(resLocation);
                    }
                    break;
            }
        }
    }

    @Override
    public void setPresenter(AddScheduleContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.realmClose();
    }
}
