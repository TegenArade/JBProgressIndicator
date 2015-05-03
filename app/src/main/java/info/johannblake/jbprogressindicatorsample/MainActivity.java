package info.johannblake.jbprogressindicatorsample;

import android.animation.ObjectAnimator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.util.UUID;

import info.johannblake.widgets.JBProgressIndicator;


public class MainActivity extends ActionBarActivity
{
  private final String LOG_TAG = "MainActivity";

  private JBProgressIndicator.IndicatorTypes indicatorType;
  private JBProgressIndicator progressIndicator;

  protected void onCreate(Bundle savedInstanceState)
  {
    try
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      this.progressIndicator = (JBProgressIndicator) findViewById(R.id.jbProgressIndicator);

      SeekBar sbAnimationRate = (SeekBar) findViewById(R.id.sbAnimationRate);

      sbAnimationRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
      {
        @Override
        public void onProgressChanged(SeekBar seekBar, int rate, boolean fromUser)
        {
          try
          {
            progressIndicator.setAnimationRate(rate);
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
    // Is the button now checked?
    boolean checked = ((RadioButton) view).isChecked();

    RelativeLayout llPercentButtons = (RelativeLayout) findViewById(R.id.llPercentButtons);

    // Check which radio button was clicked
    switch (view.getId())
    {
      case R.id.rbDeterminate:
        if (checked)
          this.indicatorType = JBProgressIndicator.IndicatorTypes.DETERMINATE;
        break;

      case R.id.rbIndeterminate:
        if (checked)
          this.indicatorType = JBProgressIndicator.IndicatorTypes.INDETERMINATE;
        break;
    }
  }


  public void onShowHideProgressIndicatorChange(View v)
  {
    try
    {
      boolean on = ((ToggleButton) v).isChecked();

      if (on)
        showHideProgressIndicator(true);
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
