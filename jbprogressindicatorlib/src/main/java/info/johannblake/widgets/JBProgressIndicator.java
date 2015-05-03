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

package info.johannblake.widgets;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.UUID;


/**
 * A progress indicator that conforms to Material Design. It currently supports indeterminate and determinate modes.
 * <p/>
 * For further information on Material Design progress indicators, see
 * http://www.google.com/design/spec/components/progress-activity.html#progress-activity-types-of-indicators
 */
public class JBProgressIndicator extends RelativeLayout
{
  private final int DEFAULT_ANIMATION_RATE = 500000; // nanoseconds.
  private final String TAG_BAR = "jbprogressindicator_long_bar";

  private final String LOG_TAG = "JBProgressIndicator";
  private Context context;
  private Integer animatedBarColor;
  private int indicatorType;
  private int animationRate;
  private Thread threadAnimate;
  private int bgColor;
  private int animationRateMilliseconds;
  private int animationRateNanoseconds = DEFAULT_ANIMATION_RATE;
  private LinearLayout llBar;

  private boolean resetBarToZero;

  private int determinateBarWidth;
  private boolean terminateProgress;

  private double determinateValue;
  private double determinateModeMaxValue = 100;
  private double determinateModeMinValue = 0;


  public enum IndicatorTypes
  {
    DETERMINATE(0),
    INDETERMINATE(1);

    private final int value;

    private IndicatorTypes(int value)
    {
      this.value = value;
    }

    public int getValue()
    {
      return value;
    }
  }

  public JBProgressIndicator(Context context)
  {
    super(context);
    this.context = context;
  }

  public JBProgressIndicator(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    try
    {
      this.context = context;
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JBProgressIndicator, 0, 0);

      // Get the type of progress indicator.
      this.indicatorType = a.getInt(R.styleable.JBProgressIndicator_indicatorType, IndicatorTypes.DETERMINATE.getValue());

      // Get the background color.
      ColorDrawable bgColorDrawable = (ColorDrawable) getBackground();

      if (bgColorDrawable == null)
      {
        // No background color was specified, so set it to the default color.
        this.bgColor = getResources().getColor(R.color.default_progress_indicator_background_color);
        this.setBackgroundColor(this.bgColor);
      }
      else
        this.bgColor = bgColorDrawable.getColor();

      // Get the color of the animated bar.
      String animBarColor = a.getString(R.styleable.JBProgressIndicator_animatedBarColor);

      if (animBarColor != null)
        this.animatedBarColor = Color.parseColor(animBarColor);
      else
        this.animatedBarColor = getResources().getColor(R.color.default_progress_indicator_bar_color);

      // Get the animation rate.
      this.animationRate = a.getInt(R.styleable.JBProgressIndicator_animationRate, DEFAULT_ANIMATION_RATE);

      a.recycle();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onFinishInflate: " + ex.toString());
    }
  }

  public JBProgressIndicator(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);

    this.context = context;
  }


  @Override
  protected void onFinishInflate()
  {
    super.onFinishInflate();

    try
    {
      this.llBar = new LinearLayout(context);
      LayoutParams loParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
      this.llBar.setLayoutParams(loParams);
      this.llBar.setBackgroundColor(animatedBarColor);
      this.llBar.setTag(TAG_BAR);

      addView(llBar);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onFinishInflate: " + ex.toString());
    }
  }


  private class AnimateIndicatorRunnable implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        if (indicatorType == IndicatorTypes.DETERMINATE.getValue())
          runDeterminateMode();
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "AnimateIndicator: " + ex.toString());
      }
    }
  }

  private void runDeterminateMode()
  {
    try
    {
      this.determinateBarWidth = 0;

      while (true)
      {
        while (true)
        {
          // Calculate the width of the bar. We do this each time as it is possible that the client changes the progress indicator's value
          // while the animation is already under way.
          int width = (int) (((this.determinateValue - this.determinateModeMinValue) / (this.determinateModeMaxValue - this.determinateModeMinValue)) * getWidth());

          post(updateBarRunnable);

          if (this.terminateProgress)
            return;

          if ((this.animationRateMilliseconds != 0) || (this.animationRateNanoseconds != 0))
            Thread.sleep(this.animationRateMilliseconds, this.animationRateNanoseconds);

          if (this.determinateBarWidth < width)
            this.determinateBarWidth++;
          else if (this.determinateBarWidth > width)
            this.determinateBarWidth--;
          else
            break;
        }

        double currentVal = this.determinateValue;

        while ((currentVal == this.determinateValue) && !this.terminateProgress)
        {
          Thread.sleep(1);

          if (this.terminateProgress)
            return;
        }
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "runDeterminateMode: " + ex.toString());
    }
  }


  private Runnable updateBarRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      try
      {
        RelativeLayout.LayoutParams loParams = (RelativeLayout.LayoutParams) llBar.getLayoutParams();
        loParams.width = getDeterminateBarWidth();
        llBar.setLayoutParams(loParams);
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "runDeterminateMode: " + ex.toString());
      }
    }
  };


  public void showHide(boolean show)
  {
    try
    {
      ObjectAnimator anim;

      if (show)
        anim = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
      else
        anim = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);

      anim.setDuration(300);
      anim.start();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "showHideProgressIndicator: " + ex.toString());
    }
  }


  private int getDeterminateBarWidth()
  {
    return this.determinateBarWidth;
  }


  public void setAnimationRate(int rate)
  {
    try
    {
      this.animationRate = rate;

      this.animationRateMilliseconds = rate / 1000000;
      this.animationRateNanoseconds = rate % 1000000;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setAnimationRate: " + ex.toString());
    }
  }


  public double getAnimationRate()
  {
    try
    {
      return this.animationRate;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "getAnimationRate: " + ex.toString());
      return this.DEFAULT_ANIMATION_RATE;
    }
  }


  public void setDeterminateModeMaxValue(double maxValue)
  {
    try
    {
      this.determinateModeMaxValue = maxValue;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setDeterminateModeMaxValue: " + ex.toString());
    }
  }


  public double getDeterminateModeMaxValue()
  {
    try
    {
      return this.determinateModeMaxValue;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "getDeterminateModeMaxValue: " + ex.toString());
      return this.determinateModeMaxValue;
    }
  }


  public void setDeterminateModeMinValue(double minValue)
  {
    try
    {
      this.determinateModeMinValue = minValue;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setDeterminateModeMinValue: " + ex.toString());
    }
  }


  public double getDeterminateModeMinValue()
  {
    try
    {
      return this.determinateModeMinValue;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "getDeterminateModeMinValue: " + ex.toString());
      return this.determinateModeMinValue;
    }
  }


  public void setDeterminateValue(double value)
  {
    try
    {
      int width = getWidth();

      if (width == 0)
        return;

      this.determinateValue = value;

      if ((this.threadAnimate == null) || ((this.threadAnimate != null) && (this.threadAnimate.getState() == Thread.State.TERMINATED)))
      {
        this.terminateProgress = false;
        this.determinateBarWidth = 0;

        this.threadAnimate = new Thread(null, new AnimateIndicatorRunnable(), "AnimateIndicator_" + UUID.randomUUID());
        this.threadAnimate.start();
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setValue: " + ex.toString());
    }
  }


  public void stopProgressIndicator()
  {
    try
    {
      this.terminateProgress = true;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "stopProgressIndicator: " + ex.toString());
    }
  }


  @Override
  public void onWindowFocusChanged(boolean hasFocus)
  {
    super.onWindowFocusChanged(hasFocus);

    try
    {
      //startControlThread();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onWindowFocusChanged: " + ex.toString());
    }
  }
}
