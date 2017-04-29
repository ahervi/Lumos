package com.hervieu.antoine.lumos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Antoine on 18/03/2017.
 */
public class AlarmReceiverActivity extends Activity {
    private static int progress;
    private int minutesS;
    private int minutesL;
    private float paramLumUser;
    private WindowManager.LayoutParams layout;
    private Window wiwi;
    private MediaPlayer mMediaPlayer;
    private final Timer timer = new Timer();
    private final Timer timerS = new Timer();
    private final Timer timerE = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        WakeLocker.acquire(getApplicationContext());
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.alarm);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.paramLumUser = this.getWindow().getAttributes().screenBrightness;
        this.minutesL=preferences.getInt("minutesL",10);
        this.minutesS=preferences.getInt("minutesS",5);

        Intent myIntent = getIntent(); // gets the previously created intent
        int bSound = myIntent.getIntExtra("bSound",0); // will return "FirstKeyValue"

        if(bSound==1){
            this.minutesL=0;
        }

        Button stopAlarm = (Button) findViewById(R.id.stopAlarm);
        stopAlarm.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(mMediaPlayer!=null) mMediaPlayer.stop();
                timer.cancel();
                timer.purge();
                timerS.cancel();
                timerS.purge();
                timerE.cancel();
                timerE.purge();
                finish();
                WakeLocker.release();
                return false;
            }
        });


        this.wiwi= getWindow();
        this.layout= getWindow().getAttributes();
        modifylumos();
        prepareSound();
        prepareEnd();


    }

    private void prepareEnd() {
        int MINUTES= minutesL+minutesS;

        timerE.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mMediaPlayer!=null) mMediaPlayer.stop();

                timerS.cancel();
                timerS.purge();
                timerE.cancel();
                timerE.purge();
                runOnUiThread(new Runnable() {
                    public void run()
                    {   layout.screenBrightness=paramLumUser;
                        wiwi.setAttributes(layout);
                    }
                });
                finish();
                WakeLocker.release();

            }

        }, 1000*60*MINUTES);
    }

    private void prepareSound() {
        int MINUTES= minutesL;
        timerS.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                timer.purge();
                runOnUiThread(new Runnable() {
                    public void run()
                    {   layout.screenBrightness=paramLumUser;
                        wiwi.setAttributes(layout);
                    }
                });
                playSound(AlarmReceiverActivity.this, getAlarmUri());

            }

        }, 1000*60*MINUTES);
    }


    private void modifylumos() {


        int SECONDS = 1;

        AlarmReceiverActivity.progress=0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                layout.screenBrightness = Math.abs((float)Math.sin(((AlarmReceiverActivity.progress%30)*Math.PI/30)));

                updateLumosUI(layout.screenBrightness);
                AlarmReceiverActivity.progress+=1;
            }
        }, 0, 100 * SECONDS);

    }

    private void updateLumosUI(final float bright) {
        runOnUiThread(new Runnable() {
            public void run()
            {
                wiwi.setAttributes(layout);
            }
        });
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(AlarmReceiverActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
        }
    }

    private Uri getAlarmUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Uri alert = Uri.parse(preferences.getString("music", "noMusic"));

        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }
}