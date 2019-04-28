package com.guidedmeditationtreks.gmt2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //Class variables
    private static final String KEY_PREFS_VOICE_VOL = "VoiceVolume";
    private static final String KEY_PREFS_TONES_VOL = "TonesVolume";
    private static final String KEY_PREFS_MUSIC_VOL = "MusicVolume";
    private static final String KEY_PREFS_NATURE_VOL = "NatureVolume";
    private static final int CRYSTAL_INTRO_END = 438000;


    private ToggleButton startStop;
    private ToggleButton tgTones;
    private TextView timerView;
    private MeditationPlayer meditationPlayer;
    private SeekBar skMusic;
    private SeekBar skNature;
    private SeekBar skVoice;
    private SeekBar skTones;
    private SharedPreferences sharedPreferences;
    private Timer timer;
    private Button btSkipIntro;

    private AlertDialog.Builder alertDialogExistingMed;
    private AlertDialog alertExistingMed;
    private int queuedMeditation;


    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (seekBar.getId()) {
                case (R.id.skNature):
                    editor.putInt(KEY_PREFS_NATURE_VOL, progress).commit();
                    meditationPlayer.setNatureVolume((float)progress/(float)100);
                    break;
                case (R.id.skMusic):
                    editor.putInt(KEY_PREFS_MUSIC_VOL, progress).commit();
                    meditationPlayer.setMusicVolume((float)progress/(float)100);
                    break;
                case (R.id.skTones):
                    editor.putInt(KEY_PREFS_TONES_VOL, progress).commit();
                    meditationPlayer.setTonesVolume((float)progress/(float)100);
                    break;
                case (R.id.skVoice):
                    editor.putInt(KEY_PREFS_VOICE_VOL, progress).commit();
                    meditationPlayer.setVoiceVolume((float)progress/(float)100);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alertDialogExistingMed = new AlertDialog.Builder(MainActivity.this);
        prepareMedPlayingAlertDialog();
        initializeView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeView() {
        startStop = (ToggleButton) findViewById(R.id.btnPlay);
        tgTones = (ToggleButton)findViewById(R.id.tgTones);
        skNature = (SeekBar) findViewById(R.id.skNature);
        skMusic = (SeekBar) findViewById(R.id.skMusic);
        skVoice = (SeekBar) findViewById(R.id.skVoice);
        skTones = (SeekBar) findViewById(R.id.skTones);
        timerView = (TextView) findViewById(R.id.tvTimer);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        btSkipIntro = (Button) findViewById(R.id.btSkipIntro);


        skNature.setProgress(sharedPreferences.getInt(KEY_PREFS_NATURE_VOL, 50));
        skTones.setProgress(sharedPreferences.getInt(KEY_PREFS_TONES_VOL, 50));
        skMusic.setProgress(sharedPreferences.getInt(KEY_PREFS_MUSIC_VOL, 50));
        skVoice.setProgress(sharedPreferences.getInt(KEY_PREFS_VOICE_VOL, 50));

        meditationPlayer = new MeditationPlayer(this,
                (float)skTones.getProgress() / 100,
                (float)skVoice.getProgress() / 100,
                (float)skNature.getProgress() / 100,
                (float)skMusic.getProgress() / 100
        );

        skNature.setOnSeekBarChangeListener(seekBarChangeListener);
        skMusic.setOnSeekBarChangeListener(seekBarChangeListener);
        skVoice.setOnSeekBarChangeListener(seekBarChangeListener);
        skTones.setOnSeekBarChangeListener(seekBarChangeListener);

        //Linkify the title using our internal version of linkify
        TextView gmt = (TextView) findViewById(R.id.gmtTitle);
        String scheme = getString(R.string.url);
        Linkify.addLinks(gmt, getString(R.string.gmt), scheme);
    }

    public void queueMeditation(View v) {
        switch (v.getId()) {
            case (R.id.btClearDrama):
                queuedMeditation = MeditationPlayer.MED_DRAMA_CYCLE;
                break;
            case (R.id.btAskSelf):
                queuedMeditation = MeditationPlayer.MED_ASK_HIGHERSELF;
                break;
            case (R.id.btCrystal):
                queuedMeditation = MeditationPlayer.MED_CRYSTAL;
                break;
            case (R.id.btHelpAnother):
                queuedMeditation = MeditationPlayer.MED_HELPANOTHER;
                break;
            default:
                break;
        }
        if (startStop.getVisibility() ==  View.INVISIBLE) {
            runQueuedMeditation();
        } else {
            alertDialogExistingMed.show();
        }
    }

    public void runQueuedMeditation() {
        boolean runMed = false;
        if (timer != null)
        {
            stopTimer();
            timerView.setText(R.string.countdown);
        }

        tgTones.setVisibility(View.VISIBLE);
        btSkipIntro.setVisibility(View.INVISIBLE);

        switch (this.queuedMeditation) {
            case MeditationPlayer.MED_DRAMA_CYCLE:
                meditationPlayer.beginMeditation(MeditationPlayer.MED_DRAMA_CYCLE);
                runMed = true;
                break;
            case MeditationPlayer.MED_ASK_HIGHERSELF:
                tgTones.setVisibility(View.INVISIBLE);
                meditationPlayer.beginMeditation(MeditationPlayer.MED_ASK_HIGHERSELF);
                tgTones.setChecked(false);
                tgTones.setActivated(false);
                runMed = true;
                break;
            case MeditationPlayer.MED_CRYSTAL:
                btSkipIntro.setVisibility(View.VISIBLE);
                meditationPlayer.beginMeditation(MeditationPlayer.MED_CRYSTAL);
                runMed = true;
                break;
            case MeditationPlayer.MED_HELPANOTHER:
                meditationPlayer.beginMeditation(MeditationPlayer.MED_HELPANOTHER);
                runMed = true;
                break;
            default:
                break;
        }
        if (runMed)
        {
            startStop.setVisibility(View.VISIBLE);
            startStop.setActivated(true);
            startStop.setChecked(true);
            startTimer();
        }
    }

    private void startTimer() {
        napThread();
        timer = new Timer();
        final MediaPlayer voicePlayer = meditationPlayer.getVoicePlayer();
        //Setup timer task to update the clock when meditation is playing
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (voicePlayer.isPlaying()) {
                            timerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    int curPos = voicePlayer.getCurrentPosition();
                                    int timeRemaining = voicePlayer.getDuration() - curPos;
                                    timerView.setText("" + String.format("%d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(timeRemaining),
                                            TimeUnit.MILLISECONDS.toSeconds(timeRemaining) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeRemaining))));
                                }
                            });
                            if (meditationPlayer.getActiveMeditation() == MeditationPlayer.MED_CRYSTAL && meditationPlayer.getVoicePlayer().getCurrentPosition() > CRYSTAL_INTRO_END) //Intro Ends at?
                            {
                                btSkipIntro.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void stopTimer() {
        napThread();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = null;
    }

    private static void napThread() {
        try {
            Thread.currentThread();
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    //Play button clicked
    public void clickPlay(View v) {
        togglePlay();
    }

    public void hidePlayButton() {
        startStop.setVisibility(View.INVISIBLE);
    }

    private void togglePlay() {
        startStop.setActivated(!startStop.isActivated());
        if (startStop.isActivated()) {
            meditationPlayer.play();
            startTimer();
        } else {
            meditationPlayer.pause();
            stopTimer();
        }
    }

    public void clickTones(View v) {
        tgTones.setActivated(!tgTones.isActivated());
        meditationPlayer.setIsochronic(tgTones.isActivated());
    }

    private void prepareMedPlayingAlertDialog() {
        // set dialog message
        alertDialogExistingMed
                .setTitle("End Current Meditation?")
                .setCancelable(false)
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Begin Queued Meditation
                        runQueuedMeditation();
                    }
                });
        alertExistingMed = alertDialogExistingMed.create();
    }

    public void rateApp(View v) {
        final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
        {
            startActivity(rateAppIntent);
        }
        else
        {
            String url = getString(R.string.url);
            Uri uriUrl = Uri.parse(url);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        }
    }

    public void skipIntro(View v) {
        skipIntro();
    }

    public void skipIntro() {
        meditationPlayer.setVoiceCurrentPosition(CRYSTAL_INTRO_END);
        meditationPlayer.play();
    }

}
