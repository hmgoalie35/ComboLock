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
    private Button generate_combo_button;
    private final String log_tag = "development";
    private ImageView inner_combo_img;

    private SensorManager mSensorManager;
    private Sensor accelerometer, gyroscope;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp = 0;
    private float angle = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensors();

        current_combination_label = (TextView) findViewById(R.id.current_combination_label);
        generate_combo_button = (Button) findViewById(R.id.generate_combo_button);
        generate_combo_button.setOnClickListener(new GenerateComboBtnListener());
        inner_combo_img = (ImageView) findViewById(R.id.inner_combo_img);
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

    private void initSensors(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            List<Sensor> gyroscopeSensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            gyroscope = gyroscopeSensors.get(0);
            mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), "Phone has no gyroscope", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     *
     * Generates three random numbers that correspond to the combination needed to unlock the
     * combination lock.
     * Sets the global array (current_combination) to the randomly selected values for use
     * throughout the app.
     * Sets the current_combination_label to display the current combination
     */
    private void generateRandomCombination(){
        Random num_generator = new Random();
        for(int i = 0; i < 3; i++){
            current_combination[i] = num_generator.nextInt(41);
        }
        //Converting to an array still contains [], so remove them with some substring magic.
        String result = Arrays.toString(current_combination);
        result = result.substring(1, result.length() - 1);
        Log.d(log_tag, "Combination is: " + result);
        current_combination_label.setText(result);
    }

    private void toggleBtnState(Button btn){
        btn.setEnabled(!btn.isEnabled());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] valuesClone = event.values.clone();
        int v;
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            angle = valuesClone[0] * dT;
            if (Math.abs(angle) >= .02) {
                if(Math.abs(angle) > .5){
                    angle /= 4;
                }
                v = (int) (inner_combo_img.getRotation() + (int) Math.toDegrees(angle));
                v = convertTo360(v);
                //accumulate values
                inner_combo_img.setRotation(v);
            }
            //check if accumulated degrees matches auto generated combo
            //check if user went over the degrees
            //if degrees matches auto generated combo, relatively reset to current number


            Log.d(log_tag, "Current Val: " + Float.toString(inner_combo_img.getRotation()));
            Log.d(log_tag, "ASDF: " + Integer.toString(convertComboToDegrees(current_combination[correct_count])));
            if(inner_combo_img.getRotation()-5 <= current_combination[correct_count] && inner_combo_img.getRotation()+5 >= current_combination[correct_count]){
                Log.d(log_tag, "Correct combo: " + Integer.toString(current_combination[correct_count]));
                Toast toast = Toast.makeText(getApplicationContext(), "Correct combo", Toast.LENGTH_SHORT);
                toast.show();
                correct_count = (correct_count+1) % 3;
            }
        }
//        Log.d(log_tag, Float.toString(v));
        timestamp = event.timestamp;
    }


    private int convertComboToDegrees(int combo){
        return combo * 9;
    }




    private int convertTo360(int v){
        return (v + 360) % 360;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     *
     */
    private class GenerateComboBtnListener implements View.OnClickListener{
        /**
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            generateRandomCombination();
            toggleBtnState(generate_combo_button);
        }
    }
}

