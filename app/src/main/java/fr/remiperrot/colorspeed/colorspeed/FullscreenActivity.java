package fr.remiperrot.colorspeed.colorspeed;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class FullscreenActivity extends AppCompatActivity {

    private final String TAG = "ColorSpeedActivity";
    // Handler for inner Timer
    Handler mHandler = new Handler();

    // Runnable executed by handler
    Runnable mGpsLost = new Runnable() {
        @Override
        public void run() {
            mSpeedLines.setBackground(getResources().getDrawable(R.drawable.speedlost));
        }
    };

    // Code executed when a speed is received
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            mHandler.removeCallbacks(mGpsLost);

            float speedKmh = ((float) location.getSpeed()) *3600/1000;

            int[] speeds = {50, 70, 90, 110, 130};

            int color = getResources().getColor(R.color.neutral);

            // For lines
            int drawable = -1;

            for (int speed: speeds) {
                if        (speedKmh >= speed - 5 && speedKmh < speed - 3) {
                    color = getResources().getColor(R.color.verySlow);
                    break;
                } else if (speedKmh >= speed - 3 && speedKmh < speed - 1) {
                    color = getResources().getColor(R.color.slow);
                    break;
                } else if (speedKmh >= speed - 1 && speedKmh < speed + 1) {
                    color = getResources().getColor(R.color.normal);

                    // In normal mode, we add lines for precision.
                    int orientation = Math.round(speedKmh * 10) - (speed * 10);
                    switch (orientation) {
                        case -9: drawable = R.drawable.speedminus9; break;
                        case -8: drawable = R.drawable.speedminus8; break;
                        case -7: drawable = R.drawable.speedminus7; break;
                        case -6: drawable = R.drawable.speedminus6; break;
                        case -5: drawable = R.drawable.speedminus5; break;
                        case -4: drawable = R.drawable.speedminus4; break;
                        case -3: drawable = R.drawable.speedminus3; break;
                        case -2: drawable = R.drawable.speedminus2; break;
                        case -1: drawable = R.drawable.speedminus1; break;
                        case 1:  drawable = R.drawable.speedplus1;  break;
                        case 2:  drawable = R.drawable.speedplus2;  break;
                        case 3:  drawable = R.drawable.speedplus3;  break;
                        case 4:  drawable = R.drawable.speedplus4;  break;
                        case 5:  drawable = R.drawable.speedplus5;  break;
                        case 6:  drawable = R.drawable.speedplus6;  break;
                        case 7:  drawable = R.drawable.speedplus7;  break;
                        case 8:  drawable = R.drawable.speedplus8;  break;
                        case 9:  drawable = R.drawable.speedplus9;  break;
                        default: drawable = R.drawable.speedok;
                    }
                    break;
                } else if (speedKmh >= speed + 1 && speedKmh < speed + 3) {
                    color = getResources().getColor(R.color.fast);
                    break;
                } else if (speedKmh >= speed + 3 && speedKmh < speed + 5) {
                    color = getResources().getColor(R.color.veryFast);
                    break;
                }
            }

            frameLayout.setBackgroundColor(color);

            if (drawable == -1) {
                mSpeedLines.setBackgroundResource(0);
            } else {
                mSpeedLines.setBackground(getResources().getDrawable(drawable));
            }

            // http://www.journaldunet.com/developpeur/pratique/developpement/12312/comment-arrondir-un-nombre-a-n-decimales-en-java.html
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            mContentView.setText(df.format(speedKmh));

            Log.d("location", location.toString());
            Log.d("speed_9", Float.toString(speedKmh));

            mHandler.postDelayed(mGpsLost, 1000*5);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            return;
        }

        @Override
        public void onProviderEnabled(String provider) {
            return;
        }

        @Override
        public void onProviderDisabled(String provider) {
            return;
        }
    };

    FrameLayout frameLayout;

    private TextView mContentView;

    private FrameLayout mSpeedLines;

    private Button mButtonNext;
    private Button mButtonPrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mContentView = (TextView) findViewById(R.id.fullscreen_content);

        mButtonNext = (Button) findViewById(R.id.button_next);
        mButtonPrev = (Button) findViewById(R.id.button_prev);

        frameLayout = (FrameLayout) findViewById(R.id.backgroundlayout);
        frameLayout.setBackgroundColor(getResources().getColor(R.color.normal));

        mSpeedLines = (FrameLayout) findViewById(R.id.speedLines);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            frameLayout.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSpeedLines.setBackground(getResources().getDrawable(R.drawable.speedlost));

        // Location
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "Requesting location updates");
        mContentView.setText("Searching GPS...");

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch(SecurityException se) {
            Log.d(TAG, "We don't have access to permission");
        }

        // Controlling Spotify Player
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.spotify.mobile.android.ui.widget.NEXT");
                sendOrderedBroadcast(intent, null);
            }
        });
        mButtonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.spotify.mobile.android.ui.widget.PREVIOUS");
                sendOrderedBroadcast(intent, null);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        mButtonNext.setOnClickListener(null);
        mButtonPrev.setOnClickListener(null);
        mHandler.removeCallbacks(mGpsLost);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.removeUpdates(locationListener);
        } catch(SecurityException se) {
            Log.d("fscreen", "We don't have access to permission");
        }
    }
}
