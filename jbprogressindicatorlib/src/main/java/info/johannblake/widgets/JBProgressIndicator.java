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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Random;
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
  private final int DEFAULT_ANIMATION_RATE_INDETERMINATE = 1000;
  private final String TAG_BAR1 = "jbprogressindicator_bar1";
  private final String TAG_BAR2 = "jbprogressindicator_bar2";
  private final String TAG_BAR3 = "jbprogressindicator_bar3";

  private final String LOG_TAG = "JBProgressIndicator";
  private Context context;
  private Integer animatedBarColor;
  private int indicatorType;
  private int animationRate;
  private Thread threadAnimate;
  private int bgColor;
  private int animationRateMilliseconds;
  private int animationRateNanoseconds = DEFAULT_ANIMATION_RATE;
  private LinearLayout llBar1;
  private LinearLayout llBar2;
  private LinearLayout llBar3;
  private Animator animatorSetBar2;
  private Animator animatorSetBar3;
  private int ctlWidth;
  private boolean ctlInitialized;
  private Random random = new Random();
  private float bar2StartPercent;
  private float bar3StartPercent;
  private ObjectAnimator objAnimBar2;
  private ObjectAnimator objAnimBar3;

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
/*      this.llBar1 = createBar();

      this.llBar2 = new LinearLayout(context);
      LayoutParams loParams = new LayoutParams(getWidth(), LayoutParams.MATCH_PARENT);
      this.llBar2.setLayoutParams(loParams);
      this.llBar2.setBackgroundColor(animatedBarColor);
      this.llBar2.setTag(TAG_BAR2);
      this.llBar2.setVisibility(View.VISIBLE);

      addView(this.llBar2);*/
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onFinishInflate: " + ex.toString());
    }
  }


  private LinearLayout createBar()
  {
    try
    {

      LinearLayout llBar = new LinearLayout(context);
      LayoutParams loParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
      llBar.setLayoutParams(loParams);
      llBar.setBackgroundColor(animatedBarColor);
      llBar.setTag(TAG_BAR1);

      addView(llBar);

      return llBar;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "createBar: " + ex.toString());
      return new LinearLayout(context);
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
        else if (indicatorType == IndicatorTypes.INDETERMINATE.getValue())
          runIndeterminateMode();
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "AnimateIndicatorRunnable: " + ex.toString());
      }
    }
  }


  private void runIndeterminateMode()
  {
    try
    {
      final float PERCENT_INCREASE = 1.4f;

      PropertyValuesHolder pvhXBar2 = PropertyValuesHolder.ofFloat("x", -this.llBar2.getWidth(), this.ctlWidth * PERCENT_INCREASE);

      Keyframe kf0Bar2 = Keyframe.ofFloat(0f, 1f);
      Keyframe kf1Bar2 = Keyframe.ofFloat(.5f, PERCENT_INCREASE);
      PropertyValuesHolder pvhScaleXBar2 = PropertyValuesHolder.ofKeyframe("scaleX", kf0Bar2, kf1Bar2);
      this.objAnimBar2 = ObjectAnimator.ofPropertyValuesHolder(this.llBar2, pvhScaleXBar2, pvhXBar2);
      this.objAnimBar2.setInterpolator(new LinearInterpolator());

      this.bar2StartPercent = getStartingPercent();

      this.objAnimBar2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        @Override
        public void onAnimationUpdate(ValueAnimator animation)
        {
          try
          {
            long elapsedTime = animation.getCurrentPlayTime();
            long totalDuration = animation.getDuration();

            if ((float) elapsedTime / (float) totalDuration > bar2StartPercent)
            {
              if (!animatorSetBar3.isStarted())
              {
                bar3StartPercent = getStartingPercent();
                post(bar3Runnable);
              }
            }
          }
          catch (Exception ex)
          {
            Log.e(LOG_TAG, "runIndeterminateMode.objAnimBar2.onAnimationUpdate: " + ex.toString());
          }
        }
      });

      this.animatorSetBar2 = new AnimatorSet();
      this.animatorSetBar2.setDuration(DEFAULT_ANIMATION_RATE_INDETERMINATE);
      ((AnimatorSet) this.animatorSetBar2).play(this.objAnimBar2);


      final float PERCENT_DECREASE = .2f;

      PropertyValuesHolder pvhXBar3 = PropertyValuesHolder.ofFloat("x", -this.llBar3.getWidth(), this.ctlWidth * .77f);

      Keyframe kf0Bar3 = Keyframe.ofFloat(0f, 1f);
      Keyframe kf1Bar3 = Keyframe.ofFloat(.5f, PERCENT_DECREASE);
      PropertyValuesHolder pvhScaleXBar3 = PropertyValuesHolder.ofKeyframe("scaleX", kf0Bar3, kf1Bar3);
      this.objAnimBar3 = ObjectAnimator.ofPropertyValuesHolder(this.llBar3, pvhScaleXBar3, pvhXBar3); //.setDuration(2000);
      this.objAnimBar3.setInterpolator(new LinearInterpolator());

      this.objAnimBar3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        @Override
        public void onAnimationUpdate(ValueAnimator animation)
        {
          try
          {
            long elapsedTime = animation.getCurrentPlayTime();
            long totalDuration = animation.getDuration();

            if ((float) elapsedTime / (float) totalDuration > bar3StartPercent)
            {
              if (!animatorSetBar2.isStarted())
              {
                bar2StartPercent = getStartingPercent();
                post(bar2Runnable);
              }
            }
          }
          catch (Exception ex)
          {
            Log.e(LOG_TAG, "runIndeterminateMode.objAnimBar3.onAnimationUpdate: " + ex.toString());
          }
        }
      });

      this.animatorSetBar3 = new AnimatorSet();
      this.animatorSetBar3.setDuration(DEFAULT_ANIMATION_RATE_INDETERMINATE);
      ((AnimatorSet) this.animatorSetBar3).play(this.objAnimBar3);

      post(bar2Runnable);

    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "runIndeterminateMode: " + ex.toString());
    }
  }


  private float getStartingPercent()
  {
    final int START_LOWER_THRESHOLD = 60; // 60%
    final int START_UPPER_THRESHOLD = 90; // 90%

    return ((float) this.random.nextInt(START_UPPER_THRESHOLD - START_LOWER_THRESHOLD) + START_LOWER_THRESHOLD) / 100f;
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
        RelativeLayout.LayoutParams loParams = (RelativeLayout.LayoutParams) llBar1.getLayoutParams();
        loParams.width = getDeterminateBarWidth();
        llBar1.setLayoutParams(loParams);
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "runDeterminateMode: " + ex.toString());
      }
    }
  };


  private Runnable bar2Runnable = new Runnable()
  {
    @Override
    public void run()
    {
      try
      {
        animatorSetBar2.start();
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "bar2Runnable: " + ex.toString());
      }
    }
  };


  private Runnable bar3Runnable = new Runnable()
  {
    @Override
    public void run()
    {
      try
      {
        animatorSetBar3.start();
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "bar3Runnable: " + ex.toString());
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
      this.determinateValue = value;
      startAnimatorThread();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setValue: " + ex.toString());
    }
  }


  private void startAnimatorThread()
  {
    try
    {
      int width = getWidth();

      if (width == 0)
        return;

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
      Log.e(LOG_TAG, "startDeterminateModeThread: " + ex.toString());
    }
  }


  public void setIndicatorType(int type)
  {
    try
    {
      if (type != this.indicatorType)
        this.terminateProgress = true;

      this.indicatorType = type;
      startAnimatorThread();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setIndicatorType: " + ex.toString());
    }
  }


  public double getIndicatorType()
  {
    try
    {
      return this.indicatorType;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "getIndicatorType: " + ex.toString());
      return this.indicatorType;
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
      if (getWidth() == 0)
        return;

      if (!this.ctlInitialized)
      {
        this.ctlWidth = getWidth();
        this.llBar1 = createBar();

        this.llBar2 = new LinearLayout(context);
        int bar2Width = (int) (getWidth() * .5f);
        LayoutParams loParams = new LayoutParams(bar2Width, LayoutParams.MATCH_PARENT);
        loParams.setMargins(-bar2Width, 0, 0, 0);
        this.llBar2.setLayoutParams(loParams);
        this.llBar2.setBackgroundColor(animatedBarColor);
        this.llBar2.setTag(TAG_BAR2);
        this.llBar2.setVisibility(View.VISIBLE);

        addView(this.llBar2);

        this.llBar3 = new LinearLayout(context);
        int bar3Width = (int) (getWidth() * .6f);
        loParams = new LayoutParams(bar3Width, LayoutParams.MATCH_PARENT);
        loParams.setMargins(-bar3Width, 0, 0, 0);
        this.llBar3.setLayoutParams(loParams);
        this.llBar3.setBackgroundColor(animatedBarColor);
        this.llBar3.setTag(TAG_BAR3);
        this.llBar3.setVisibility(View.VISIBLE);

        addView(this.llBar3);

        //this.llBar2.setX(-this.llBar2.getWidth());

        this.ctlInitialized = true;
      }
      //startControlThread();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onWindowFocusChanged: " + ex.toString());
    }
  }
}
