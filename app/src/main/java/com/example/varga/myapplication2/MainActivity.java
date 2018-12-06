package com.example.varga.myapplication2;

import android.app.Activity;
        import android.os.Bundle;
        import android.view.MenuItem;

        import android.hardware.Sensor;
        import android.hardware.SensorManager;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.widget.TextView;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.util.ArrayList;
        import java.util.List;

public class MainActivity extends Activity implements SensorEventListener{

    private TextView Xvalue, Yvalue, Zvalue,Stepvalue;
    private Sensor mySensor;
    private SensorManager SM;
    private boolean mInitialized;
    private String tag;
    private List<Point> pointList = new ArrayList<Point>();
    private final float NOISE = (float) 2.0;
    private double stepsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mInitialized = false;
        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Assign TextView
        Xvalue= (TextView)findViewById(R.id.textView);
        Yvalue = (TextView)findViewById(R.id.textView2);
        Zvalue = (TextView)findViewById(R.id.textView3);
        Stepvalue = (TextView)findViewById(R.id.textView4);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double mLastX=0;
        double mLastY=0;
        double mLastZ=0;

        Xvalue.setText("X: " + x);
        Yvalue.setText("Y: " + y);
        Zvalue.setText("Z: " + z);

        Point point = new Point(x,y,z);

        try {
            FileOutputStream logFile = openFileOutput("lepes.txt", MODE_PRIVATE);
            logFile.write(point.toString().getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        pointList.add(point);
        final double alpha = 0.8; // constant for our filter below

        double[] gravity = {0,0,0};

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
        gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
        gravity[2] = alpha * gravity[2] + (1 - alpha) * z;

        // Remove the gravity contribution with the high-pass filter.
        x = x - gravity[0];
        y = y - gravity[1];
        z = z - gravity[2];

        if (!mInitialized) {
            // sensor is used for the first time, initialize the last read values
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        } else {
            // sensor is already initialized, and we have previously read values.
            // take difference of past and current values and decide which
            // axis acceleration was detected by comparing values

            double deltaX = Math.abs(mLastX - x);
            double deltaY = Math.abs(mLastY - y);
            double deltaZ = Math.abs(mLastZ - z);
            if (deltaX < NOISE)
                deltaX = (float) 0.0;
            if (deltaY < NOISE)
                deltaY = (float) 0.0;
            if (deltaZ < NOISE)
                deltaZ = (float) 0.0;
            mLastX = x;
            mLastY = y;
            mLastZ = z;

            if (deltaX > deltaY) {
                // Horizontal shake
                // do something here if you like

            } else if (deltaY > deltaX) {
                // Vertical shake
                // do something here if you like

            } else if ((deltaZ > deltaX) && (deltaZ > deltaY)) {
                // Z shake
                stepsCount = stepsCount + 1;
                if (stepsCount > 0) {
                    System.out.println(stepsCount);
                    Stepvalue.setText("Step: " + stepsCount);
                }


            }
        }

        // System.out.println("X="+ event.values[0] + "Y=" + event.values[1] + "Z=" + event.values[2]);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
}
