package hpittin1ccunnin5msausne2.combolock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView current_combination_label;
    private int[] current_combination;
    private Button generate_combo_button;
    private final String log_tag = "development";
    private ImageView inner_combo_img;
    private SeekBar sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        current_combination_label = (TextView) findViewById(R.id.current_combination_label);
        generate_combo_button = (Button) findViewById(R.id.generate_combo_button);
        generate_combo_button.setOnClickListener(new GenerateComboBtnListener());

        inner_combo_img = (ImageView) findViewById(R.id.inner_combo_img);

        sb = (SeekBar) findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                inner_combo_img.setRotation(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        current_combination = new int[3];

        generateRandomCombination();
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

