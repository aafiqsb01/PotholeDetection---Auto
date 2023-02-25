package com.example.potholedetectionv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener, LocationListener {
    static SensorManager SensorManager1;
    static Sensor AccelerometerSensor;
    LocationManager locationManager;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    public ArrayList<ArrayList<String>> values  = new ArrayList<ArrayList<String>>();

    File storagePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS);
    String DataFile = "/data.csv";
    File path_DataFile = new File(storagePath + DataFile);

    public static FileWriter writer;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    float x;
    float y;
    float z;
    double currentSpeed;

    public Button button_NoPothole;
    public Button button_SmallPothole;
    public Button button_MediumPothole;
    public Button button_LargePothole;
    public Button button_DrainCover;
    public Button button_SpeedBump;
    public Button STOP_Button;

    double latitude, longitude;
    CheckBox SwitchMetric;
    TextView DeviceSpeed;
    TextView GForceValue;
    private Handler mHandler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Linking buttons
        GForceValue = findViewById(R.id.showGForce);
        DeviceSpeed = findViewById(R.id.DeviceSpeed);
//        SwitchMetric = findViewById(R.id.SwitchMetric);

        button_NoPothole = findViewById(R.id.NoPothole_Button);
        button_NoPothole.setOnClickListener(this);

        button_SmallPothole = findViewById(R.id.SmallPothole_Button);
        button_SmallPothole.setOnClickListener(this);

        button_MediumPothole = findViewById(R.id.MediumPothole_Button);
        button_MediumPothole.setOnClickListener(this);

        button_LargePothole = findViewById(R.id.LargePothole_Button);
        button_LargePothole.setOnClickListener(this);

        button_DrainCover = findViewById(R.id.DrainCover_Button);
        button_DrainCover.setOnClickListener(this);

        button_SpeedBump = findViewById(R.id.SpeedBump_Button);
        button_SpeedBump.setOnClickListener(this);

        STOP_Button = findViewById(R.id.Stop_Button);
        STOP_Button.setOnClickListener(this);

//        Defining Accelerometer Sensor variables
        SensorManager1 = (SensorManager) getSystemService(SENSOR_SERVICE);
        AccelerometerSensor = SensorManager1.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorManager1.registerListener(this, AccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

//        Get Speed
        runLocationManager();

//        Get Storage Access
        verifyStoragePermissions();

//        Write to CSV File
        checkIfFileExists();
        writeColumnsToFile(path_DataFile);
        runArrayStoring.run();
    }

    private final Runnable runArrayStoring = new Runnable() {
        @Override
        public void run() {
            double value_GForce = calculateGForce(x, y, z);
            ArrayList<String> subValues = new ArrayList<>();
//                writer.append(String.valueOf(value_GForce));
//                writer.append(",");
//
//                writer.append(String.valueOf(x));
//                writer.append(",");
//
//                writer.append(String.valueOf(y));
//                writer.append(",");
//
//                writer.append(String.valueOf(z));
//                writer.append(",");
//
//                writer.append(String.valueOf(currentSpeed));
//                writer.append(",");

            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
//                writer.append(currentTime);
//                writer.append("\n");

//                writer.flush();

            subValues.add(String.valueOf(value_GForce));
            subValues.add(String.valueOf(x));
            subValues.add(String.valueOf(y));
            subValues.add(String.valueOf(z));
            subValues.add(String.valueOf(currentSpeed));
            subValues.add(currentTime);
            values.add(subValues);

            subValues = new ArrayList<>();

//            values.add(String.valueOf(value_GForce));
//            values.add(String.valueOf(x));
//            values.add(String.valueOf(y));
//            values.add(String.valueOf(z));
//            values.add(String.valueOf(currentSpeed));

            mHandler.postDelayed(this, 100);
        }
    };

    public void checkIfFileExists () {
        if (!path_DataFile.exists()) {
            try {
                path_DataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void runLocationManager () {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//                Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_SHORT).show();
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentSpeed = location.getSpeed() ;
        currentSpeed = currentSpeed * 2.237f;

        DeviceSpeed.setText(String.valueOf("Device speed: " + currentSpeed + " mph"));
    }

    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.Stop_Button){
            Toast.makeText(this, "Stopping Code.", Toast.LENGTH_SHORT).show();
            SensorManager1.unregisterListener(this, AccelerometerSensor);
            locationManager.removeUpdates(this);
            mHandler.removeCallbacks(runArrayStoring);

            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(getApplicationContext(), "Data written to file.", Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.NoPothole_Button){

            try {
                writer.append("Start of Event");
                writer.append("\n");
                List<ArrayList<String>> lastFive = values.subList(Math.max(0, values.size() - 50), values.size());
                System.out.println(lastFive);

                for (ArrayList<String> element : lastFive) {
                    List<String> nthSecond = element.subList(Math.max(0, element.size() - 6), element.size());
                    // Output the last 5 elements of the inner ArrayList
                    for (String innerElement : nthSecond) {
                        writer.append(innerElement);
                        writer.append(",");
                    }

                    writer.append("No Pothole");
                    writer.append("\n");
                }

                writer.append("End of Event");
                writer.append("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Values added.", Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.SmallPothole_Button){

            try {
                writer.append("Start of Event");
                writer.append("\n");
                List<ArrayList<String>> lastFive = values.subList(Math.max(0, values.size() - 50), values.size());
                System.out.println(lastFive);

                for (ArrayList<String> element : lastFive) {
                    List<String> nthSecond = element.subList(Math.max(0, element.size() - 6), element.size());
                    // Output the last 5 elements of the inner ArrayList
                    for (String innerElement : nthSecond) {
                        writer.append(innerElement);
                        writer.append(",");
                    }

                    writer.append("Small Pothole");
                    writer.append("\n");
                }

                writer.append("End of Event");
                writer.append("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Values added.", Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.MediumPothole_Button){

            try {
                writer.append("Start of Event");
                writer.append("\n");
                List<ArrayList<String>> lastFive = values.subList(Math.max(0, values.size() - 50), values.size());
                System.out.println(lastFive);

                for (ArrayList<String> element : lastFive) {
                    List<String> nthSecond = element.subList(Math.max(0, element.size() - 6), element.size());
                    // Output the last 5 elements of the inner ArrayList
                    for (String innerElement : nthSecond) {
                        writer.append(innerElement);
                        writer.append(",");
                    }

                    writer.append("Medium Pothole");
                    writer.append("\n");
                }

                writer.append("End of Event");
                writer.append("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Values added.", Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.LargePothole_Button){

            try {
                writer.append("Start of Event");
                writer.append("\n");
                List<ArrayList<String>> lastFive = values.subList(Math.max(0, values.size() - 50), values.size());
                System.out.println(lastFive);

                for (ArrayList<String> element : lastFive) {
                    List<String> nthSecond = element.subList(Math.max(0, element.size() - 6), element.size());
                    // Output the last 5 elements of the inner ArrayList
                    for (String innerElement : nthSecond) {
                        writer.append(innerElement);
                        writer.append(",");
                    }

                    writer.append("Large Pothole");
                    writer.append("\n");
                }

                writer.append("End of Event");
                writer.append("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Values added.", Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.DrainCover_Button){

            try {
                writer.append("Start of Event");
                writer.append("\n");
                List<ArrayList<String>> lastFive = values.subList(Math.max(0, values.size() - 50), values.size());
                System.out.println(lastFive);

                for (ArrayList<String> element : lastFive) {
                    List<String> nthSecond = element.subList(Math.max(0, element.size() - 6), element.size());
                    // Output the last 5 elements of the inner ArrayList
                    for (String innerElement : nthSecond) {
                        writer.append(innerElement);
                        writer.append(",");
                    }

                    writer.append("Drain Cover");
                    writer.append("\n");
                }

                writer.append("End of Event");
                writer.append("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Values added.", Toast.LENGTH_SHORT).show();
        }

        else if (id == R.id.SpeedBump_Button){

            try {
                writer.append("Start of Event");
                writer.append("\n");
                List<ArrayList<String>> lastFive = values.subList(Math.max(0, values.size() - 50), values.size());
                System.out.println(lastFive);

                for (ArrayList<String> element : lastFive) {
                    List<String> nthSecond = element.subList(Math.max(0, element.size() - 6), element.size());
                    // Output the last 5 elements of the inner ArrayList
                    for (String innerElement : nthSecond) {
                        writer.append(innerElement);
                        writer.append(",");
                    }

                    writer.append("Speed Bump");
                    writer.append("\n");
                }

                writer.append("End of Event");
                writer.append("\n");
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Values added.", Toast.LENGTH_SHORT).show();

        }
    }

    public static void writeColumnsToFile(File file) {
        String[] Columns  = {"G Force Value", "X Value", "Y Value", "Z Value", "Speed Value", "Time"};

        try {
            writer = new FileWriter(file, true);

            for (int index = 0; index < Columns.length; index++) {
                writer.append(Columns[index]);
                writer.append(",");
            }

            writer.append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeArrayToFile(ArrayList<String> values, File file) {
        try {
            writer = new FileWriter(file, true);
            writer.append("\n");
//            for (String element : values) {
//                writer.append(element);
//                writer.append(",");
//            }

            int count = 0 ;
            for (int x = 1; x < values.size() - 6 ; x++){
                if (values.get(x).contains("Type")) {
                    writer.append(values.get(x));
                    writer.append(", ");
                    writer.append(values.get(x + 1));
                    writer.append("\n");
                    x += 2;
                }

                else {
                    writer.append(values.get(x));
                    writer.append(",");
                    count += 1;

                    if (count == 5) {
                        writer.append("\n");
                        count = 0;
                    }
                }
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x = event.values[ 0 ];
        y = event.values[ 1 ];
        z = event.values[ 2 ];

        double gforce = Math.sqrt((x * x) + (y * y) + (z * z)) / 9.8;
        GForceValue.setText(String.valueOf(gforce));
    }

    public double calculateGForce (float X_Value, float Y_Value, float Z_Value){
        double GForce_Value = Math.sqrt((X_Value * X_Value) + (Y_Value * Y_Value) + (Z_Value * Z_Value)) / 9.8;
//        System.out.println("G-Force value is:" + GForce_Value);

        return GForce_Value;
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}