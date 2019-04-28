package com.guidedmeditationtreks.gmt2;

import android.media.MediaPlayer;

import com.guidedmeditationtreks.gmt2.R;

/**
 * Created by mrrussell on 2/17/16.
 */
public class MeditationPlayer {

    public static final int MED_DRAMA_CYCLE = 1;
    public static final int MED_ASK_HIGHERSELF = 2;
    public static final int MED_CRYSTAL = 3;
    public static final int MED_HELPANOTHER = 4;

    private int activeMeditation;
    private boolean isIsochronic;
    private boolean isPlaying;
    private int medDuration;

    private MediaPlayer voicePlayer;
    private MediaPlayer binauralPlayer;
    private MediaPlayer isoPlayer;
    private MediaPlayer naturePlayer;
    private MediaPlayer musicPlayer;
    private MainActivity activity;

    private int voiceCurrentPosition;

    private float currentToneVolume;
    private float currentVoiceVolume;
    private float currentNatureVolume;
    private float currentMusicVolume;

    public MeditationPlayer (MainActivity activity, float currentToneVolume, float currentVoiceVolume, float currentNatureVolume, float currentMusicVolume) {
        this.activity = activity;
        this.currentToneVolume = currentToneVolume;
        this.currentNatureVolume = currentNatureVolume;
        this.currentVoiceVolume = currentVoiceVolume;
        this.currentMusicVolume = currentMusicVolume;

    }

    public void beginMeditation(int activeMeditation) {

        if (this.activeMeditation != 0) {

            if (voicePlayer.isPlaying()) {
                voicePlayer.stop();
            }
            if (binauralPlayer.isPlaying()) {
                binauralPlayer.stop();
            }
            if (isoPlayer.isPlaying()) {
                isoPlayer.stop();
            }
            if (musicPlayer.isPlaying()) {
                musicPlayer.stop();
            }
            if (naturePlayer.isPlaying()) {
                naturePlayer.stop();
            }
        }

        this.activeMeditation = activeMeditation;

        switch (this.activeMeditation) {
            case MeditationPlayer.MED_DRAMA_CYCLE:
                voicePlayer = MediaPlayer.create(activity, R.raw.m01voice);
                isoPlayer = MediaPlayer.create(activity, R.raw.m01iso);
                binauralPlayer = MediaPlayer.create(activity, R.raw.m01binaural);
                musicPlayer = MediaPlayer.create(activity, R.raw.m01music);
                naturePlayer = MediaPlayer.create(activity, R.raw.m02nature);
                break;
            case MeditationPlayer.MED_ASK_HIGHERSELF:
                voicePlayer = MediaPlayer.create(activity, R.raw.m02voice);
                isoPlayer = MediaPlayer.create(activity, R.raw.m02iso);
                binauralPlayer = MediaPlayer.create(activity, R.raw.m02bin);
                musicPlayer = MediaPlayer.create(activity, R.raw.m02music);
                naturePlayer = MediaPlayer.create(activity, R.raw.m02nature);
                break;
            case MeditationPlayer.MED_CRYSTAL:
                voicePlayer = MediaPlayer.create(activity, R.raw.m03voice);
                isoPlayer = MediaPlayer.create(activity, R.raw.m03iso);
                binauralPlayer = MediaPlayer.create(activity, R.raw.m03bin);
                musicPlayer = MediaPlayer.create(activity, R.raw.m03music);
                naturePlayer = MediaPlayer.create(activity, R.raw.m03nature);
                break;
            case MeditationPlayer.MED_HELPANOTHER:
                voicePlayer = MediaPlayer.create(activity, R.raw.m04voice);
                isoPlayer = MediaPlayer.create(activity, R.raw.m04iso);
                binauralPlayer = MediaPlayer.create(activity, R.raw.m04bin);
                musicPlayer = MediaPlayer.create(activity, R.raw.m04music);
                naturePlayer = MediaPlayer.create(activity, R.raw.m04nature);
                break;
            default:
                return;
        }

        if (isIsochronic) {
            binauralPlayer.setVolume(0, 0);
            isoPlayer.setVolume(currentToneVolume, currentToneVolume);
        } else {
            isoPlayer.setVolume(0, 0);
            binauralPlayer.setVolume(currentToneVolume, currentToneVolume);
        }

        voicePlayer.setVolume(currentVoiceVolume, currentVoiceVolume);
        voiceCurrentPosition = 0;

        voicePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                activity.hidePlayButton();
                naturePlayer.stop();
                musicPlayer.stop();
                setActiveMeditation(0);
                binauralPlayer.stop();
                isoPlayer.stop();
            }
        });

        medDuration = voicePlayer.getDuration();
        play();
    }

    public void play(){
        voicePlayer.seekTo(voiceCurrentPosition);
        binauralPlayer.seekTo(voiceCurrentPosition);
        isoPlayer.seekTo(voiceCurrentPosition);
        musicPlayer.seekTo(voiceCurrentPosition);
        naturePlayer.seekTo(voiceCurrentPosition);

        setVoiceVolume(currentVoiceVolume);
        setTonesVolume(currentToneVolume);
        setNatureVolume(currentNatureVolume);
        setMusicVolume(currentMusicVolume);

        voicePlayer.start();
        isoPlayer.start();
        binauralPlayer.start();
        musicPlayer.start();
        naturePlayer.start();
        isPlaying = true;
    }

    public void pause() {
        voiceCurrentPosition = voicePlayer.getCurrentPosition();
        voicePlayer.pause();
        naturePlayer.pause();
        musicPlayer.pause();
        isoPlayer.pause();
        binauralPlayer.pause();
        isPlaying = false;
    }

    public void setIsochronic(boolean isIsochronic) {
        this.isIsochronic = isIsochronic;
        setTonesVolume(currentToneVolume);
    }

    public void setVoiceVolume(float volume) {
        currentVoiceVolume = volume;
        if (isPlaying)
            voicePlayer.setVolume(volume, volume);
    }

    public void setTonesVolume(float volume) {
        currentToneVolume = volume;
        if (isPlaying)
        {
            if (isIsochronic) {
                binauralPlayer.setVolume(0, 0);
                isoPlayer.setVolume(currentToneVolume, currentToneVolume);
            } else {
                isoPlayer.setVolume(0, 0);
                binauralPlayer.setVolume(currentToneVolume, currentToneVolume);
            }
        }
    }

    public void setMusicVolume(float volume) {
        currentMusicVolume = volume;
        if (isPlaying)
            musicPlayer.setVolume(volume, volume);
    }

    public void setNatureVolume(float volume) {
        currentNatureVolume = volume;
        if (isPlaying)
            naturePlayer.setVolume(volume, volume);
    }

    private void setActiveMeditation(int a) {
        this.activeMeditation = a;
    }

    public MediaPlayer getVoicePlayer() {
        return voicePlayer;
    }

    public void setVoiceCurrentPosition(int position) {
        voiceCurrentPosition = position;
    }

    public int getActiveMeditation() {
        return this.activeMeditation;
    }


}
