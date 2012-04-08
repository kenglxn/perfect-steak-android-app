package net.glxn.android;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends Activity {

    private static final int ONE_SEC_IN_MILLIS = 1000;
    private static final int COOK_TIME = 2 * 60 * ONE_SEC_IN_MILLIS;
    private static final int REST_TIME = 5 * 60 * ONE_SEC_IN_MILLIS;

    private int iterationTimeMillis = COOK_TIME;

    private final String TAG = this.getClass().getSimpleName();

    private TextView timerView;
    private int iteration;
    private MediaPlayer mediaPlayer;
    private CountDownTimer countDownTimer;
    private Dialog dialog;
    private TextView textView;
    private Button startCookingBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.timerView = (TextView) this.findViewById(R.id.timer);

        startCookingBtn = (Button) findViewById(R.id.startCookingBtn);
        startCookingBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "startCookingBtn.onClick()");

                dialog = new Dialog(Main.this);
                dialog.setContentView(R.layout.custom_dialog);
                textView = (TextView) dialog.findViewById(R.id.text);
                textView.setText("Place 'em on the grill");
                iteration = 0;
                ImageView imageView = (ImageView) dialog.findViewById(R.id.image);

                dialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, "okBtn.onClick()");

                        startCookingBtn.setVisibility(View.GONE);
                        iteration++;

                        countDownTimer = new CountDownTimer((long) iterationTimeMillis, ONE_SEC_IN_MILLIS) {

                            public void onTick(long millisUntilFinished) {
                                Log.d(TAG, "onTick() " + millisUntilFinished);

                                timerView.setText("" + millisUntilFinished / 1000);
                            }

                            public void onFinish() {
                                Log.d(TAG, "onFinish()");

                                try {
                                    beep();
                                } catch (IOException ignored) {}

                                timerView.setText("0");
                                if (iteration < 4) {
                                    textView.setText("Flip 'em " + (iteration == 1 ? "" : "again"));
                                    dialog.show();
                                } else if (iteration == 4) {
                                    textView.setText("Let 'em rest");
                                    iterationTimeMillis = REST_TIME;
                                    dialog.show();
                                } else {
                                    timerView.setText("You're done. Enjoy!");
                                }
                            }
                        };
                        countDownTimer.start();

                        dialog.dismiss();
                    }
                });


                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");

        new AlertDialog.Builder(this)
            .setMessage(R.string.quit_alert_message)
            .setTitle(R.string.rating_exit_title)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Main.super.onBackPressed();
                    }
                })
            .setNeutralButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
            .show();
    }

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "Disable Home Button by setting window type to TYPE_KEYGUARD");

        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        super.onAttachedToWindow();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");

        super.onPause();
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private void beep() throws IOException {
        Log.d(TAG, "beep()");

        Vibrator vibrator;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mediaPlayer = new MediaPlayer();
        Context applicationContext = getApplicationContext();
        mediaPlayer.setDataSource(applicationContext, soundUri);
        final AudioManager audioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
    }

}
