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
    SensorManager sensorManager;
    Sensor gyroscope;
    int soundID;
    private SoundPool sound;
    final Messenger serviceMessenger = new Messenger(new IncomingHandler());
    boolean activeTheremin = false;
    int sensorLog = 0;
    double x, y, z; // These are position variables, not badly-named variables (maybe)
    AudioManager am;
    public ThereminService() {}

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
                    unregisterGyro();
                    break;
                case MSG_RESET:
                    x = 0;
                    y = 0;
                    z = 0;
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundID = sound.load(this, R.raw.boop, 1);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
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
    int volume;
    public void onSensorChanged(SensorEvent event) {
        x += getPositionFromVelocity(event.values[0]);
        y += getPositionFromVelocity(event.values[1]);
        z += getPositionFromVelocity(event.values[2]);
        /*Log.i ("VALUES", "Run: " + sensorLog +
                "\nX: " + x +
                "\nY: " + y +
                "\nZ: " + z + '\n');
        sensorLog++;
*/
        if (x < 0) volume = 0;
        else if (x > 100) volume = 100;
        else volume = (int) x;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume / 6, 0);

    }
    public double getPositionFromVelocity(double vel) {
        return  vel / 4              // This is the derivative to convert velocity to position (rad/s => rad)
                * 180 / Math.PI;    // This converts radians to degrees                        (rad => °)
    }

    public void registerGyro() {
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterGyro() {
        sensorManager.unregisterListener(this);
    }
}
