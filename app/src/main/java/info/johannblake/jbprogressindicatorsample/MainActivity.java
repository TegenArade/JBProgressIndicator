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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import info.johannblake.widgets.JBProgressIndicator;


/**
 * This the test app test for testing the JBProgressIndicator control.
 */
public class MainActivity extends ActionBarActivity
{
  private final String LOG_TAG = "MainActivity";

  private JBProgressIndicator progressIndicator;

  protected void onCreate(Bundle savedInstanceState)
  {
    try
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      this.progressIndicator = (JBProgressIndicator) findViewById(R.id.jbProgressIndicator);

      // Set the state of the toggle button to indicate the direction of animation for indeterminate mode.
      ToggleButton tbIndeterminateModeAnimationDirection = (ToggleButton) findViewById(R.id.tbIndeterminateModeAnimationDirection);
      tbIndeterminateModeAnimationDirection.setChecked(this.progressIndicator.indeterminateModeIsRTL());

      // Handle user changing animation rate for indeterminate mode.
      SeekBar sbAnimationRateIndeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateIndeterminateMode);
      int animationRateIndeterminateMode = (int) ((this.progressIndicator.getAnimationRateIndeterminateMode() - 300) / 10);
      sbAnimationRateIndeterminateMode.setProgress(animationRateIndeterminateMode);

      sbAnimationRateIndeterminateMode.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
      {
        @Override
        public void onProgressChanged(SeekBar seekBar, int rate, boolean fromUser)
        {
          try
          {
            // Don't set the rate faster than 300 ms, otherwise the animation might crash.
            // The slider's range 0 to 100 maps to the animation rate of 300 ms to 1300 ms.

            int animRate = (10 * rate) + 300;

            progressIndicator.setAnimationRateIndeterminateMode(animRate);
          }
          catch (Exception ex)
          {
            Log.e(LOG_TAG, "onProgressChanged: " + ex.toString());
          }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
        }
      });

      // Handle user changing animation rate for determinate mode.
      SeekBar sbAnimationRateDeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateDeterminateMode);
      int animationRateDeterminateMode = (int) (this.progressIndicator.getAnimationRateDeterminateMode() * 1000000f);
      sbAnimationRateDeterminateMode.setProgress(animationRateDeterminateMode);

      sbAnimationRateDeterminateMode.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
      {
        @Override
        public void onProgressChanged(SeekBar seekBar, int rate, boolean fromUser)
        {
          try
          {
            float animRate = ((float)rate / 1000000f);
            progressIndicator.setAnimationRateDeterminateMode(animRate);
          }
          catch (Exception ex)
          {
            Log.e(LOG_TAG, "onProgressChanged: " + ex.toString());
          }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
        }
      });
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onCreate: " + ex.toString());
    }
  }

  public void setPercent(View v)
  {
    try
    {
      int val = Integer.valueOf(v.getTag().toString());
      this.progressIndicator.setDeterminateValue(val);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setPercent: " + ex.toString());
    }
  }


  public void onRadioButtonClicked(View view)
  {
    try
    {
      // Is the button now checked?
      boolean checked = ((RadioButton) view).isChecked();

      LinearLayout llPercentButtons = (LinearLayout) findViewById(R.id.llPercentButtons);

      // Check which radio button was clicked
      switch (view.getId())
      {
        case R.id.rbDeterminate:
          if (checked)
          {
            if (this.progressIndicator.getIndicatorType() != JBProgressIndicator.IndicatorTypes.DETERMINATE.getValue())
            {
              // Show the slider for the animation rate for determinate mode.
              SeekBar sbAnimationRateDeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateDeterminateMode);
              sbAnimationRateDeterminateMode.setVisibility(View.VISIBLE);

              SeekBar sbAnimationRateIndeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateIndeterminateMode);
              sbAnimationRateIndeterminateMode.setVisibility(View.GONE);

              ToggleButton tbIndeterminateModeAnimationDirection = (ToggleButton) findViewById(R.id.tbIndeterminateModeAnimationDirection);
              tbIndeterminateModeAnimationDirection.setVisibility(View.INVISIBLE);

              llPercentButtons.setVisibility(View.VISIBLE);
              this.progressIndicator.setDeterminateValue(0);
              this.progressIndicator.setIndicatorType(JBProgressIndicator.IndicatorTypes.DETERMINATE.getValue());
            }
          }
          break;

        case R.id.rbIndeterminate:
          if (checked)
          {
            if (this.progressIndicator.getIndicatorType() != JBProgressIndicator.IndicatorTypes.INDETERMINATE.getValue())
            {
              SeekBar sbAnimationRateDeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateDeterminateMode);
              sbAnimationRateDeterminateMode.setVisibility(View.GONE);

              SeekBar sbAnimationRateIndeterminateMode = (SeekBar) findViewById(R.id.sbAnimationRateIndeterminateMode);
              sbAnimationRateIndeterminateMode.setVisibility(View.VISIBLE);

              ToggleButton tbIndeterminateModeAnimationDirection = (ToggleButton) findViewById(R.id.tbIndeterminateModeAnimationDirection);
              tbIndeterminateModeAnimationDirection.setVisibility(View.VISIBLE);

              llPercentButtons.setVisibility(View.GONE);
              this.progressIndicator.setIndicatorType(JBProgressIndicator.IndicatorTypes.INDETERMINATE.getValue());
            }
          }
          break;
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onRadioButtonClicked: " + ex.toString());
    }
  }


  public void onShowHideProgressIndicatorChange(View v)
  {
    try
    {
      boolean on = ((ToggleButton) v).isChecked();

      if (on)
      {
        this.progressIndicator.setDeterminateValue(0);
        showHideProgressIndicator(true);
      }
      else
        showHideProgressIndicator(false);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onShowHideProgressIndicatorChange: " + ex.toString());
    }
  }


  private void showHideProgressIndicator(boolean show)
  {
    try
    {
      this.progressIndicator.showHide(show);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "showHideProgressIndicator: " + ex.toString());
    }
  }


  public void onChangeIndeterminateModeDirection(View v)
  {
    try
    {
      boolean rtl = ((ToggleButton) v).isChecked();

      this.progressIndicator.setIndeterminateModeDirection(rtl);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onChangeIndeterminateModeDirection: " + ex.toString());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings)
    {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
