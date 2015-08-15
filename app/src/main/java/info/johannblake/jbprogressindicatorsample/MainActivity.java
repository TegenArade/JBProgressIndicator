/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Johann Blake
 *
 * https://www.linkedin.com/in/johannblake
 * https://plus.google.com/+JohannBlake
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.johannblake.jbprogressindicatorsample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import info.johannblake.widgets.jbprogressindicatorlib.JBProgressIndicator;


/**
 * This the test app test for testing the JBProgressIndicator control.
 */
public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = "MainActivity";

    private JBProgressIndicator mProgressIndicator;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressIndicator = (JBProgressIndicator) findViewById(R.id.jbProgressIndicator);

        // Set the state of the toggle button to indicate the direction of animation for indeterminate mode.
        ToggleButton tbIndeterminateModeAnimationDirection = (ToggleButton) findViewById(R.id.tbIndeterminateModeAnimationDirection);
        tbIndeterminateModeAnimationDirection.setChecked(mProgressIndicator.indeterminateModeIsRTL());

        // Handle user changing animation rate for indeterminate mode.
        SeekBar sbAnimationRateIndeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateIndeterminateMode);
        int animationRateIndeterminateMode = (int) ((mProgressIndicator.getAnimationRateIndeterminateMode() - 300) / 10);
        sbAnimationRateIndeterminateMode.setProgress(animationRateIndeterminateMode);

        sbAnimationRateIndeterminateMode.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int rate, boolean fromUser) {
                // Don't set the rate faster than 300 ms, otherwise the animation might crash.
                // The slider's range 0 to 100 maps to the animation rate of 300 ms to 1300 ms.

                int animRate = (10 * rate) + 300;

                mProgressIndicator.setAnimationRateIndeterminateMode(animRate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Handle user changing animation rate for determinate mode.
        SeekBar sbAnimationRateDeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateDeterminateMode);
        int animationRateDeterminateMode = (int) (mProgressIndicator.getAnimationRateDeterminateMode() * 1000000f);
        sbAnimationRateDeterminateMode.setProgress(animationRateDeterminateMode);

        sbAnimationRateDeterminateMode.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int rate, boolean fromUser) {
                float animRate = ((float) rate / 1000000f);
                mProgressIndicator.setAnimationRateDeterminateMode(animRate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void setPercent(View v) {
        int val = Integer.valueOf(v.getTag().toString());
        mProgressIndicator.setDeterminateValue(val);
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        LinearLayout llPercentButtons = (LinearLayout) findViewById(R.id.llPercentButtons);

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.rbDeterminate:
                if (checked) {
                    if (mProgressIndicator.getIndicatorType() != JBProgressIndicator.IndicatorTypes.DETERMINATE.getValue()) {
                        // Show the slider for the animation rate for determinate mode.
                        SeekBar sbAnimationRateDeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateDeterminateMode);
                        sbAnimationRateDeterminateMode.setVisibility(View.VISIBLE);

                        SeekBar sbAnimationRateIndeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateIndeterminateMode);
                        sbAnimationRateIndeterminateMode.setVisibility(View.GONE);

                        ToggleButton tbIndeterminateModeAnimationDirection = (ToggleButton) findViewById(R.id.tbIndeterminateModeAnimationDirection);
                        tbIndeterminateModeAnimationDirection.setVisibility(View.INVISIBLE);

                        llPercentButtons.setVisibility(View.VISIBLE);
                        mProgressIndicator.setDeterminateValue(0);
                        mProgressIndicator.setIndicatorType(JBProgressIndicator.IndicatorTypes.DETERMINATE.getValue());
                    }
                }
                break;

            case R.id.rbIndeterminate:
                if (checked) {
                    if (mProgressIndicator.getIndicatorType() != JBProgressIndicator.IndicatorTypes.INDETERMINATE.getValue()) {
                        SeekBar sbAnimationRateDeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateDeterminateMode);
                        sbAnimationRateDeterminateMode.setVisibility(View.GONE);

                        SeekBar sbAnimationRateIndeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateIndeterminateMode);
                        sbAnimationRateIndeterminateMode.setVisibility(View.VISIBLE);

                        ToggleButton tbIndeterminateModeAnimationDirection = (ToggleButton) findViewById(R.id.tbIndeterminateModeAnimationDirection);
                        tbIndeterminateModeAnimationDirection.setVisibility(View.VISIBLE);

                        llPercentButtons.setVisibility(View.GONE);
                        mProgressIndicator.setIndicatorType(JBProgressIndicator.IndicatorTypes.INDETERMINATE.getValue());
                    }
                }
                break;
        }
    }


    public void onShowHideProgressIndicatorChange(View v) {
        boolean on = ((ToggleButton) v).isChecked();

        if (on) {
            mProgressIndicator.setDeterminateValue(0);
            showHideProgressIndicator(true);
        } else
            showHideProgressIndicator(false);
    }


    private void showHideProgressIndicator(boolean show) {
        mProgressIndicator.showHide(show);
    }


    public void onChangeIndeterminateModeDirection(View v) {
        boolean rtl = ((ToggleButton) v).isChecked();
        mProgressIndicator.setIndeterminateModeDirection(rtl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
