package net.glxn.android;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.timerView = (TextView) this.findViewById(R.id.timer);

        final Button startCookingBtn = (Button) findViewById(R.id.startCookingBtn);
        startCookingBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "startCookingBtn clicked");

                final Dialog dialog = new Dialog(Main.this);
                dialog.setContentView(R.layout.custom_dialog);
                final TextView textView = (TextView) dialog.findViewById(R.id.text);
                textView.setText("Place 'em on the grill");
                iteration = 0;
                ImageView imageView = (ImageView) dialog.findViewById(R.id.image);

                dialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, "okBtn clicked");
                        startCookingBtn.setVisibility(View.GONE);
                        iteration++;

                        new CountDownTimer(iterationTimeMillis, ONE_SEC_IN_MILLIS) {

                            public void onTick(long millisUntilFinished) {
                                timerView.setText("" + millisUntilFinished / 1000);
                            }

                            public void onFinish() {
                                try {
                                    beep();
                                } catch (IOException ignored) {}

                                timerView.setText("0");
                                if(iteration < 4) {
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
                        }.start();

                        dialog.dismiss();
                    }
                });


                dialog.show();
            }
        });
    }

    private void beep() throws IOException {
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

    private void cleanUp() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    protected void onStop() {
        cleanUp();
    }

    @Override
    protected void onPause() {
        cleanUp();
    }

    @Override
    protected void onDestroy() {
        cleanUp();
    }
}
