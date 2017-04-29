package com.hervieu.antoine.lumos;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("minutesL", 10);
        editor.putInt("minutesS", 5);

        editor.commit();





        TimePicker.OnTimeChangedListener timy =  new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt("heure", hourOfDay);
                editor.putInt("minute", minute);

                editor.commit();

                int[][] kList={{R.id.monday, Calendar.MONDAY},{R.id.tuesday,Calendar.TUESDAY}, {R.id.wednesday,Calendar.WEDNESDAY}, {R.id.thursday,Calendar.THURSDAY}, {R.id.friday,Calendar.FRIDAY}, {R.id.satursday, Calendar.SATURDAY}, {R.id.sunday, Calendar.SUNDAY}};
                for(int[] k : kList){
                    CheckBox checkou = (CheckBox) findViewById(k[0]);
                    if (checkou.isChecked()){

                        deactivateAlarm(k[1]);
                        addAlarm(k[1], k[1]);
                    }

                }

            }
        };

        final TimePicker mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setMinute(preferences.getInt("minute",0));
        mTimePicker.setHour(preferences.getInt("heure",12));
        mTimePicker.setOnTimeChangedListener(timy);

        int[][] kList={{R.id.monday, Calendar.MONDAY},{R.id.tuesday,Calendar.TUESDAY}, {R.id.wednesday,Calendar.WEDNESDAY}, {R.id.thursday,Calendar.THURSDAY}, {R.id.friday, Calendar.FRIDAY}, {R.id.satursday, Calendar.SATURDAY}, {R.id.sunday, Calendar.SUNDAY}};

        for(int[] day : kList){
            CheckBox checkou = (CheckBox)findViewById(day[0]);
            checkou.setChecked(preferences.getBoolean("checkbox"+day[1], false));

        }
        final Button button = (Button) findViewById(R.id.music);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri ringtone=RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_ALARM);;
                Intent intent=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtone);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, ringtone);
                startActivityForResult(intent , 1);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri alert =data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                    String ringtone = alert.toString();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("music", ringtone);
                    editor.commit();

                    break;

                default:
                    break;
            }
        }
    }

    public void addAlarm(int dayThen, int idAlarm){
        Calendar calNow = Calendar.getInstance();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int dayNow = calNow.get(Calendar.DAY_OF_WEEK);
        Date dateNow = calNow.getTime();
        int calNowHour = calNow.get(Calendar.HOUR_OF_DAY);
        int differenceInDays = dayThen-dayNow;
        if(differenceInDays<0){
            differenceInDays = 7+differenceInDays;
        }
        if(dayNow==dayThen){
            if ((calNowHour<preferences.getInt("heure",0))||((calNowHour==preferences.getInt("heure",0))&&((calNow.get(Calendar.MINUTE)<preferences.getInt("minute",0))))){
                differenceInDays = 0;
            }
            else  {
                differenceInDays = 7;
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateNow);
        calendar.add(Calendar.DAY_OF_YEAR, differenceInDays);
        calendar.set(Calendar.HOUR_OF_DAY, preferences.getInt("heure",0));
        calendar.set(Calendar.MINUTE, preferences.getInt("minute",0));
        calendar.set(Calendar.SECOND,0);
        Date newDate = calendar.getTime();

        long tempsenMSec = newDate.getTime()-dateNow.getTime();
        long tempsenSec = TimeUnit.MILLISECONDS.toSeconds(tempsenMSec);
        createAlarm((int)tempsenSec, idAlarm);

    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = preferences.edit();

        int[][] kList={{R.id.monday, Calendar.MONDAY},{R.id.tuesday,Calendar.TUESDAY}, {R.id.wednesday,Calendar.WEDNESDAY}, {R.id.thursday,Calendar.THURSDAY}, {R.id.friday, Calendar.FRIDAY}, {R.id.satursday, Calendar.SATURDAY}, {R.id.sunday, Calendar.SUNDAY}};

        for(int[] k : kList){
            if(k[0]==((CheckBox) view).getId()){
                if (checked){

                    addAlarm(k[1], k[1]);

                }
                else{
                    deactivateAlarm(k[1]);
                }

                editor.putBoolean("checkbox"+k[1], checked);




            }
            editor.commit();
        }



    }
    private void createAlarm(int timeoutInSeconds, int idAlarm) {


        Intent intent = new Intent(this, AlarmReceiverActivity.class);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        if(timeoutInSeconds<PreferenceManager.getDefaultSharedPreferences(this).getInt("minutesL",10)*60) {

            Toast.makeText(this, getString(R.string.noLight), Toast.LENGTH_SHORT).show();
            intent.putExtra("bSound",1);
            time.add(Calendar.SECOND, timeoutInSeconds);

        }
        else {
            intent.putExtra("bSound",0);
            time.add(Calendar.SECOND, timeoutInSeconds - PreferenceManager.getDefaultSharedPreferences(this).getInt("minutesL",10) * 60);

        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                idAlarm, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am =
                (AlarmManager) getSystemService(Activity.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                pendingIntent);




    }
    private void deactivateAlarm(int idAlarm) {
        Intent intent1 = new Intent(this, AlarmReceiverActivity.class);
        PendingIntent senderr = PendingIntent.getActivity(this,
                idAlarm, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManage = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManage.cancel(senderr);

    }
    public void test5sec(){
        long tempsenSec = 5+PreferenceManager.getDefaultSharedPreferences(this).getInt("minutesL",10)*60;
        createAlarm((int)tempsenSec, Calendar.SUNDAY);
        Toast.makeText(this,"This alarm will ring on Sunday in 5s", Toast.LENGTH_SHORT).show();

    }
}
