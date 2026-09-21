package com.elachi.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log

//This is the class that plays the alarm when the timer is up
//It works by playing a ringtone or a beep sound

class AlarmPlayer(context: Context) {
    private val appContext = context.applicationContext
    private var ringtone: Ringtone? = null

    fun play() {
        stop()
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val tone = RingtoneManager.getRingtone(appContext, uri)
            if (tone != null) {
                tone.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) tone.isLooping = true
                tone.play()
                ringtone = tone
                return
            }
        } catch (e: Exception) {
            Log.e("AlarmPlayer", "Could not play alarm ringtone", e)
        }
        try {
            ToneGenerator(AudioManager.STREAM_ALARM, 100)
                .startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000)
        } catch (e: Exception) {
            Log.e("AlarmPlayer", "Could not play fallback beep", e)
        }
    }

    fun stop() {
        try {
            ringtone?.stop()
        } catch (_: Exception) {
        }
        ringtone = null
    }
}