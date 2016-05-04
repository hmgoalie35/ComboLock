package hpittin1ccunnin5msausne2.combolock;

import android.content.Context;
import android.graphics.Color;
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

import java.util.List;
import java.util.Random;

/**
 * TODO
 * make filters better
 * implement time at correct combo
 * readme - include no plagiarism statement
 * - document how to use app
 * unlock sounds?
 * new page
 * fix layouts
 */


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView combo_1, combo_2, combo_3;
    private Button generate_combo_button;
    private ImageView inner_combo_img, arrow;

    private SensorManager mSensorManager;
    private Sensor accelerometer, gyroscope;

    private final String log_tag = "development";
    private int[] current_combination;
    private int correct_count = 0;
    private float timestamp = 0;
    private int deg_leeway = 2;
    private boolean not_done = true;
    private int on_correct_combo = 0;

    //Each tick on the lock is separated by 9 degrees
    int deg_per_tick = 9;

    //Convert nanoseconds to seconds
    private static final float NS2S = 1.0f / 1000000000.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensors();

        combo_1 = (TextView) findViewById(R.id.combo_1);
        combo_2 = (TextView) findViewById(R.id.combo_2);
        combo_3 = (TextView) findViewById(R.id.combo_3);

        generate_combo_button = (Button) findViewById(R.id.generate_combo_button);
        generate_combo_button.setOnClickListener(new GenerateComboBtnListener());
        inner_combo_img = (ImageView) findViewById(R.id.inner_combo_img);

        arrow = (ImageView) findViewById(R.id.arrow);

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

        //Warning shows up on phones with gyroscopes - commented out
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            List<Sensor> gyroscopeSensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            gyroscope = gyroscopeSensors.get(0);
            mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            //Toast.makeText(getApplicationContext(), "Phone has no gyroscope", Toast.LENGTH_LONG).show();
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

        //First combo number should not be in range -5 to 5.
        int temp_combo_one;
        do {
            temp_combo_one = num_generator.nextInt(40);
        } while (temp_combo_one >= -5 && temp_combo_one <= 5);
        current_combination[0] = temp_combo_one;

        //Combo numbers should not repeat
        //Consecutive combo numbers should be separated by ~5 ticks in either direction
        int temp_combo_two;
        do {
            temp_combo_two = num_generator.nextInt(40);
        }
        while (temp_combo_two == temp_combo_one || (temp_combo_two >= temp_combo_one - 5 && temp_combo_two <= temp_combo_one + 5));
        current_combination[1] = temp_combo_two;

        //Combo numbers should not repeat
        //Consecutive combo numbers should be separated by ~5 ticks in either direction
        int temp_combo_three;
        do {
            temp_combo_three = num_generator.nextInt(40);
        }
        while (temp_combo_three == temp_combo_one || temp_combo_three == temp_combo_two || (temp_combo_three >= temp_combo_two - 5 && temp_combo_three <= temp_combo_two + 5));
        current_combination[2] = temp_combo_three;

        //Display combination
        combo_1.setText(Integer.toString(current_combination[0]));
        combo_2.setText(Integer.toString(current_combination[1]));
        combo_3.setText(Integer.toString(current_combination[2]));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (not_done) {

            float x_axis_reading = event.values[0];
            int current_combo;

            //Configure first and third combination number
            if (correct_count != 1) {
                current_combo = -(current_combination[correct_count] * deg_per_tick);
            } else {
                //Configuring middle combination number
                if (current_combination[correct_count] != 0) {
                    if (current_combination[0] > current_combination[1]) {
                        current_combo = -(current_combination[1] * deg_per_tick);
                    } else {
                        current_combo = 360 - (current_combination[correct_count] * deg_per_tick);
                    }
                } else {
                    current_combo = current_combination[correct_count];
                }
            }

            int lower_limit = current_combo - deg_leeway;
            int upper_limit = current_combo + deg_leeway;

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

                    //change lower limit
                    if (rotation_from_pivot >= lower_limit && rotation_from_pivot <= upper_limit) {
                        if (on_correct_combo >= 3) {
                            on_correct_combo = 0;
                            switch (correct_count) {
                                case 0:
                                    combo_1.setTextColor(Color.GREEN);
                                    break;
                                case 1:
                                    combo_2.setTextColor(Color.GREEN);
                                    break;
                                case 2:
                                    combo_3.setTextColor(Color.GREEN);
                                    break;
                            }
                            correct_count++;
                            Log.d(log_tag, "Combo correct!");
                        }
                        //filter
                        on_correct_combo++;
                    }

                    //Combination is correct
                    if (correct_count >= 3) {
                        correct_count = 0;
                        on_correct_combo = 0;
                        Log.d(log_tag, "Stop.");
                        Toast.makeText(getApplicationContext(), "You have successfully unlocked the lock", Toast.LENGTH_LONG).show();
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
            //Generate new combo
            generateRandomCombination();

            combo_1.setTextColor(Color.BLACK);
            combo_2.setTextColor(Color.BLACK);
            combo_3.setTextColor(Color.BLACK);

            on_correct_combo = 0;
            correct_count = 0;
            not_done = true;

            //Set inner lock image to original offset
            inner_combo_img.setRotation(4);

        }
    }
}

