package hpittin1ccunnin5msausne2.combolock;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView current_combination_label;
    private int[] current_combination;
    private int correct_count = 0;
    private boolean going_right = false;
    private int actual_tick = 0;
    private Button generate_combo_button;
    private final String log_tag = "development";
    private ImageView inner_combo_img;

    private SensorManager mSensorManager;
    private Sensor accelerometer, gyroscope;

    private static final float NS2S = 1.0f / 1000000000.0f;

    private float timestamp = 0, gyroscope_data = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensors();

        current_combination_label = (TextView) findViewById(R.id.current_combination_label);
        generate_combo_button = (Button) findViewById(R.id.generate_combo_button);
        generate_combo_button.setOnClickListener(new GenerateComboBtnListener());
        inner_combo_img = (ImageView) findViewById(R.id.inner_combo_img);
        inner_combo_img.setRotation(4);
        current_combination = new int[3];

        generateRandomCombination();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            List<Sensor> gyroscopeSensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            gyroscope = gyroscopeSensors.get(0);
            mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Phone has no gyroscope", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Generates three random numbers that correspond to the combination needed to unlock the
     * combination lock.
     * Sets the global array (current_combination) to the randomly selected values for use
     * throughout the app.
     * Sets the current_combination_label to display the current combination
     */
    private void generateRandomCombination() {
        Random num_generator = new Random();
        for (int i = 0; i < 3; i++) {
            current_combination[i] = num_generator.nextInt(40);
        }
        //Converting to an array still contains [], so remove them with some substring magic.
        String result = Arrays.toString(current_combination);
        result = result.substring(1, result.length() - 1);
        Log.d(log_tag, "Combination is: " + result);
        current_combination_label.setText(result);
    }

    private void toggleBtnState(Button btn) {
        btn.setEnabled(!btn.isEnabled());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x_axis_reading = event.values[0];
        float radians;
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp);
            gyroscope_data = (x_axis_reading * dT) * NS2S;
            radians = gyroscope_data;
//            Log.d(log_tag, "Radians: " + Float.toString(radians));
            if (radians < -.4) {
                if (actual_tick == 39) {
                    actual_tick = 0;
                } else {
                    actual_tick += 1;
                }
                going_right = true;
                inner_combo_img.setRotation(inner_combo_img.getRotation() - 9);
            } else if (radians > .4) {
                if (actual_tick == 0) {
                    actual_tick = 39;
                } else {
                    actual_tick -= 1;
                }
                going_right = false;
                inner_combo_img.setRotation(inner_combo_img.getRotation() + 9);
            } else if (radians < 1 && radians > -1 && Math.abs(radians) != 0.0) {


                if (actual_tick == current_combination[correct_count]) {
//                    Log.d(log_tag, "Correct value: " + Integer.toString(actual_tick));
                    if (correct_count == 0 && going_right) {
                        Log.d(log_tag, "Correct val 0");
                        correct_count++;
                    } else if (correct_count == 1 && !going_right) {
                        Log.d(log_tag, "Correct val 1");
                        correct_count++;
                    } else if (correct_count == 2 && going_right) {
                        correct_count++;
                        Toast.makeText(getApplicationContext(), "Combo Lock successfully unlocked!", Toast.LENGTH_SHORT).show();
                        correct_count = 0;
                        toggleBtnState(generate_combo_button);
                    }
                }
                Log.d(log_tag, "Current: " + Integer.toString(actual_tick));
            }
        }
        timestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     *
     */
    private class GenerateComboBtnListener implements View.OnClickListener {
        /**
         * @param v
         */
        @Override
        public void onClick(View v) {
            generateRandomCombination();
            toggleBtnState(generate_combo_button);
        }
    }
}

