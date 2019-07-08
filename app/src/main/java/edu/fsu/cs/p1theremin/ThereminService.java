package edu.fsu.cs.p1theremin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;


public class ThereminService extends Service implements SensorEventListener {

    public static final int MSG_PLAY = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_RESET = 3;
    public static final int MSG_FLIP_X = 4;
    public static final int MSG_FLIP_Y = 5;
    public static final int MSG_FLIP_Z = 6;
    SensorManager sensorManager;
    Sensor gyroscope;
    int soundID;
    private SoundPool sound;
    final Messenger serviceMessenger = new Messenger(new IncomingHandler());
    boolean activeTheremin = false;
    int sensorLog = 0;
    int volume;
    int volumeRangeUnits = 0;
    double x, y, z; // These are position variables, not badly-named variables (maybe)
    AudioManager am;
    public ThereminService() {}
    int[] switchPrefs;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLAY:
                    if (! activeTheremin) soundID = sound.play(soundID, 1.0f, 1.0f, 0, -1, 1.0f);
                    else sound.resume(soundID);
                    activeTheremin = true;
                    registerGyro();
                    break;
                case MSG_STOP:
                    sound.pause(soundID);
                    unregisterGyro();   // Intentionally falls through, resetting coordinates.
                case MSG_RESET:
                    sensorLog = 0;
                    x = 0;
                    y = 0;
                    z = 0;
                    break;
                case MSG_FLIP_X:
                    switchPrefs[0] *= -1;
                    break;
                case MSG_FLIP_Y:
                    switchPrefs[1] *= -1;
                    break;
                case MSG_FLIP_Z:
                    switchPrefs[2] *= -1;
                    break;

            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundID = sound.load(this, R.raw.thereminsamplenew, 1);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        volumeRangeUnits = 100 / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        switchPrefs = new int[] {1, 1, 1};
        return serviceMessenger.getBinder();

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }
    @Override
    public void onCreate() { super.onCreate(); }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {

        x = getDegreesFromRadians(event.values[0]) * switchPrefs[0];
        y = getDegreesFromRadians(event.values[1]) * switchPrefs[1];
        z = getDegreesFromRadians(event.values[2]) * switchPrefs[2];
        Log.i ("VALUES", "Run: " + sensorLog +
                "\nX: " + x +
                "\nY: " + y +
                "\nZ: " + z + '\n');
        sensorLog++;

        if (x < 0) volume = 0;
        else volume = (int) x;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume / volumeRangeUnits, 0);
        sound.setRate(soundID,  (float) (1.0 + y / 1000));
    }
    public double getDegreesFromRadians(double vel) {
        return (vel * 180 / Math.PI);
    }

    public void registerGyro() { sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL); }

    public void unregisterGyro() {
        sensorManager.unregisterListener(this);
    }
}
