package edu.fsu.cs.p1theremin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;



public class MainActivity extends AppCompatActivity  {

    public Messenger messenger;
    private ToggleButton playButton;
    private Button resetButton;

    private ServiceConnection thereminConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messenger = new Messenger(iBinder);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messenger = null;
        }
    };

    protected void onDestroy (Bundle savedInstanceState) {
        super.onDestroy();
        unbindService(thereminConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playButton = (ToggleButton) findViewById(R.id.thereminButton);
        resetButton = (Button) findViewById(R.id.button);
        Intent soundIntent = new Intent(this, ThereminService.class);
        bindService(soundIntent, thereminConnection, Context.BIND_AUTO_CREATE);

        final SeekBar octaveSelector = findViewById(R.id.octaveBar);
        playButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Message msg;
                if (isChecked) msg = Message.obtain(null, ThereminService.MSG_PLAY, 0, 0);
                else msg = Message.obtain(null, ThereminService.MSG_STOP, 0, 0);
                try {messenger.send(msg);}
                catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, ThereminService.MSG_RESET, 0, 0);
                try {messenger.send(msg);}
                catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
        octaveSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 12) {
                    octaveSelector.setProgress(0);
                } else if (progress < 38) {
                    octaveSelector.setProgress(25);
                } else if (progress < 63) {
                    octaveSelector.setProgress(50);
                } else if (progress < 88) {
                    octaveSelector.setProgress(75);
                } else {
                    octaveSelector.setProgress(100);
                }
                if (octaveSelector.getProgress() != progress)
                    switch (octaveSelector.getProgress()) {
                        case 0:
                        case 25:
                        case 50:
                        case 75:
                        case 100:
                    }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }
}
