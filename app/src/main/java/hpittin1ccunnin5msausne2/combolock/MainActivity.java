package hpittin1ccunnin5msausne2.combolock;

import android.content.Context;
import android.content.Intent;
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

/**
 *
 * TODO
 * DONE combo generator should not have repeats.
 * DONE combos shouldn't start with 0
 * DONE each combo number should be more than 5 degrees apart
 * DONE account for change in direction
 * reduce number of times we have to do a full rotation
 * make filters better
 * discuss leeway
 * implement reset
 * maybe time how long user spends at correct combo
 * readme - include no plagiarism statement
 *        - document how to use app
 * change combo color to green when user gets correct combo - separate textfields for each number
 * fix toast
 * unlock sounds?
 * unlock animation?
 *
 */


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView current_combination_label;
    private Button generate_combo_button;
    private ImageView inner_combo_img;

    private SensorManager mSensorManager;
    private Sensor accelerometer, gyroscope;

    private final String log_tag = "development";
    private int[] current_combination;
    private int correct_count  = 0;
    private float timestamp  = 0;
    private int deg_leeway   = 2;
    private boolean not_done = true;

    //Each tick on the lock is separated by 9 degrees
    int deg_per_tick = 9;

    //Convert nanoseconds to seconds
    private static final float NS2S = 1.0f / 1000000000.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensors();

        current_combination_label = (TextView) findViewById(R.id.current_combination_label);
        generate_combo_button = (Button) findViewById(R.id.generate_combo_button);
        generate_combo_button.setOnClickListener(new GenerateComboBtnListener());
        inner_combo_img = (ImageView) findViewById(R.id.inner_combo_img);

        //Correct image distortion
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

        //This showed up on my phone but I have a gyroscope
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

        //First combo number should not be 0
        int temp_combo_one;
        do {
            temp_combo_one = num_generator.nextInt(40);
        } while (temp_combo_one == 0);
        current_combination[0] = temp_combo_one;

        //Combo numbers should not repeat
        //Consecutive combo numbers should be separated by ~5 ticks in either direction
        int temp_combo_two;
        do {
            temp_combo_two = num_generator.nextInt(40);
        } while (temp_combo_two == temp_combo_one || (temp_combo_two >= temp_combo_one - 5 && temp_combo_two <= temp_combo_one + 5 ));
        current_combination[1] = temp_combo_two;

        //Combo numbers should not repeat
        //Consecutive combo numbers should be separated by ~5 ticks in either direction
        int temp_combo_three;
        do {
            temp_combo_three = num_generator.nextInt(40);
        } while (temp_combo_three == temp_combo_one || temp_combo_three == temp_combo_two || (temp_combo_three >= temp_combo_two - 5 && temp_combo_three <= temp_combo_two + 5 ));
        current_combination[2] = temp_combo_three;

        //Converting to an array still contains [], so remove them with some substring magic.
        String result = Arrays.toString(current_combination);
        result = result.substring(1, result.length() - 1);
        //Log.d(log_tag, "Combination is: " + result);
        current_combination_label.setText(result);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (not_done) {

            float x_axis_reading = event.values[0];
            int current_combo;
            if (correct_count == 0 || correct_count == 2) {
                current_combo = -(current_combination[correct_count] * deg_per_tick);
            } else {
                current_combo = 360 - (current_combination[correct_count] * deg_per_tick);
            }

            int lower_limit   = current_combo - deg_leeway;
            int upper_limit   = current_combo + deg_leeway;

            if (timestamp != 0) {

                final float time_interval = (event.timestamp - timestamp) * NS2S;
                float radians = x_axis_reading * time_interval;

                //Low pass filter
                if (Math.abs(radians) >= .02) {

                    //Low pass filter
                    if (Math.abs(radians) > .5) {
                        radians /= 4;
                    }

                    int rotation_from_pivot = (int) (inner_combo_img.getRotation() + (int) Math.toDegrees(radians));

                    //Full rotation
                    if (Math.abs(rotation_from_pivot) > 360) {
                        rotation_from_pivot = (rotation_from_pivot % 360);
                    }

                    inner_combo_img.setRotation(rotation_from_pivot);

                    if (rotation_from_pivot >= lower_limit && rotation_from_pivot <= upper_limit) {
                        correct_count++;
                        Log.d(log_tag, "Combo correct!");
                    }

                    //Combination is correct
                    if (correct_count >= 3) {
                        correct_count = 0;
                        Log.d(log_tag, "Stop.");
                        not_done = false;
                    }

                    //---Debug---//
                    //Log.d(log_tag, "Correct count: " + Integer.toString(correct_count));
                    Log.d(log_tag, "Current combo: " + Integer.toString(current_combo));
                    Log.d(log_tag, "Rotation from pivot: " + Integer.toString((int) rotation_from_pivot));

                }
            }
            timestamp = event.timestamp;
        }
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
        }
    }
}

