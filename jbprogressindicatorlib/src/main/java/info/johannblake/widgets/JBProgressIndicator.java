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
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Random;
import java.util.UUID;


/**
 * A progress indicator that conforms to Material Design. It currently supports indeterminate and determinate modes.
 *  <p/>
 * Source code can be downloaded at:<br/>
 * <a href='https://github.com/JohannBlake/JBProgressIndicator'>https://github.com/JohannBlake/JBProgressIndicator</a>
 * <p/>
 * For further information on Material Design progress indicators, see:<br/>
 * <a href='http://www.google.com/design/spec/components/progress-activity.html#progress-activity-types-of-indicators'>http://www.google.com/design/spec/components/progress-activity.html#progress-activity-types-of-indicators</a>
 */
public class JBProgressIndicator extends RelativeLayout
{
  private final float ANIMATION_RATE_DETERMINATE_MODE = 1.5f; // milliseconds.
  private final int ANIMATION_RATE_INDETERMINATE_MODE = 800; // milliseconds.

  private final String LOG_TAG = "JBProgressIndicator";
  private Context context;
  private Integer animatedBarColor;
  private int indicatorType;
  private float animationRateDeterminateMode;
  private int animationRateIndeterminateMode;
  private Thread threadAnimate;
  private int bgColor;
  private int animationRateMilliseconds;
  private int animationRateNanoseconds;
  private LinearLayout llBar1;
  private LinearLayout llBar2;
  private LinearLayout llBar3;
  private boolean ctlInitialized;
  private Random random = new Random();
  private ObjectAnimator objAnimBar2;
  private ObjectAnimator objAnimBar3;
  private float bar2StartThreshold = getStartingPercent();
  private float bar3StartThreshold = getStartingPercent();

  private int determinateBarWidth;
  private boolean terminateProgress;

  private double determinateValue;
  private double determinateModeMaxValue = 100;
  private double determinateModeMinValue = 0;

  private boolean indeterminateModeRTL;


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

      // Get the direction of animation for indeterminate mode.
      this.indeterminateModeRTL = a.getBoolean(R.styleable.JBProgressIndicator_indeterminateModeRTL, false);

      // Get the animation rate for determinate mode.
      this.animationRateDeterminateMode = a.getFloat(R.styleable.JBProgressIndicator_animationRateDeterminateMode, ANIMATION_RATE_DETERMINATE_MODE);

      setAnimationRateDeterminateMode(this.animationRateDeterminateMode);

      // Get the animation rate for indeterminate mode.
      this.animationRateIndeterminateMode = a.getInt(R.styleable.JBProgressIndicator_animationRateIndeterminateMode, ANIMATION_RATE_INDETERMINATE_MODE);

      a.recycle();

      getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
      {
        @Override
        public void onGlobalLayout()
        {
          try
          {
            // Initialize the control in the onGlobalLayout because this is the only reliable place where the
            // properties of the controls like width become active for the first time.
            if (!ctlInitialized)
            {
              if (getWidth() == 0)
                return;

              addControls();

              ctlInitialized = true;
            }

            // If the control is visible, then start the animation thread.
            if (ctlInitialized && (getVisibility() == View.VISIBLE) && (llBar2.getWidth() > 0))
            {
              startAnimationThread();
              getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
          }
          catch (Exception ex)
          {
            Log.e(LOG_TAG, "onGlobalLayout: " + ex.toString());
          }
        }
      });

    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "constructor: " + ex.toString());
    }
  }

  /**
   * Adds layouts to handle the bars that get animated.
   */
  private void addControls()
  {
    try
    {
      this.llBar1 = createDeterminateBar();
      this.llBar2 = createIndeterminateBar(.5f);
      this.llBar3 = createIndeterminateBar(.6f);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "addControls: " + ex.toString());
    }
  }


  /**
   * Creates bars that will get animated in indeterminate mode.
   * @param widthScaleFactor The scale factor to use when creating the bar's initial width. The
   *                         width of the bar is the width of the progress indicator control
   *                         multiplied by this scale factor.
   * @return The view representing the bar is returned.
   */
  private LinearLayout createIndeterminateBar(float widthScaleFactor)
  {
    try
    {
      LinearLayout llBar = new LinearLayout(context);
      int barWidth = (int) (getWidth() * widthScaleFactor);
      LayoutParams loParams = new LayoutParams(barWidth, LayoutParams.MATCH_PARENT);
      llBar.setLayoutParams(loParams);
      llBar.setBackgroundColor(animatedBarColor);
      llBar.setX(this.indeterminateModeRTL ? getWidth() : -barWidth);
      llBar.setVisibility(View.VISIBLE);

      addView(llBar);

      return  llBar;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "createIndeterminateBar: " + ex.toString());
      return null;
    }
  }

  public JBProgressIndicator(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);

    this.context = context;
  }


  /**
   * Creates the bar that is used in determinate mode.
   * @return The view representing the bar is returned.
   */
  private LinearLayout createDeterminateBar()
  {
    try
    {
      LinearLayout llBar = new LinearLayout(context);
      LayoutParams loParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
      llBar.setLayoutParams(loParams);
      llBar.setBackgroundColor(animatedBarColor);

      addView(llBar);

      return llBar;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "createDeterminateBar: " + ex.toString());
      return new LinearLayout(context);
    }
  }


  /**
   * The runnable used to start an animation. The animation will show either a determinate mode
   * progress indicator or an indetermine indicator.
   */
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


  /**
   * Runs the indeterminate mode showing the indicator.
   */
  private void runIndeterminateMode()
  {
    try
    {
      // Animate the first bar and once the bar reaches a random point along the x axis, start the
      // second bar animation.
      this.objAnimBar2 = createIndeterminateModeAnimation(this.llBar2, 1.4f);

      this.objAnimBar2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        @Override
        public void onAnimationUpdate(ValueAnimator animation)
        {
          try
          {
            if (terminateProgress)
              return;

            long elapsedTime = animation.getCurrentPlayTime();
            long totalDuration = animation.getDuration();

            if ((float) elapsedTime / (float) totalDuration > bar2StartThreshold)
            {
              if (!objAnimBar3.isStarted())
              {
                bar3StartThreshold = getStartingPercent();
                post(bar3AnimationRunnable);
              }
            }
          }
          catch (Exception ex)
          {
            Log.e(LOG_TAG, "runIndeterminateMode.onAnimationUpdate(Bar2): " + ex.toString());
          }
        }
      });

      this.objAnimBar3 = createIndeterminateModeAnimation(this.llBar3, .2f);

      this.objAnimBar3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        @Override
        public void onAnimationUpdate(ValueAnimator animation)
        {
          try
          {
            if (terminateProgress)
              return;

            long elapsedTime = animation.getCurrentPlayTime();
            long totalDuration = animation.getDuration();

            if ((float) elapsedTime / (float) totalDuration > bar3StartThreshold)
            {
              if (!objAnimBar2.isStarted())
              {
                bar2StartThreshold = getStartingPercent();
                post(bar2AnimationRunnable);
              }
            }
          }
          catch (Exception ex)
          {
            Log.e(LOG_TAG, "runIndeterminateMode.onAnimationUpdate(Bar3): " + ex.toString());
          }
        }
      });

      // Start the animation by animating the first bar.
      post(bar2AnimationRunnable);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "runIndeterminateMode: " + ex.toString());
    }
  }


  /**
   * Creates an animation for a bar in indeterminate mode.
   * @param llBar The bar that will be animated.
   * @param widthChange The scale factor that the bar will scale to during the second half of its animation.
   * @return The animation object for the bar is returned.
   */
  private ObjectAnimator createIndeterminateModeAnimation(LinearLayout llBar, final float widthChange)
  {
    try
    {
      int w = llBar.getWidth();
      PropertyValuesHolder pvhXBar;

      if (this.indeterminateModeIsRTL())
        if (widthChange < 1)
          pvhXBar = PropertyValuesHolder.ofFloat("x", getWidth(), -w);
        else
          pvhXBar = PropertyValuesHolder.ofFloat("x", getWidth(), -(w * widthChange));
      else
      {
        if (widthChange < 1)
          pvhXBar = PropertyValuesHolder.ofFloat("x", -w, getWidth());
        else
          pvhXBar = PropertyValuesHolder.ofFloat("x", -w, getWidth() * widthChange);
      }

      Keyframe kf0Bar = Keyframe.ofFloat(0f, 1f);
      Keyframe kf1Bar = Keyframe.ofFloat(.5f, widthChange);
      PropertyValuesHolder pvhScaleXBar2 = PropertyValuesHolder.ofKeyframe("scaleX", kf0Bar, kf1Bar);
      ObjectAnimator objAnim1 = ObjectAnimator.ofPropertyValuesHolder(llBar, pvhScaleXBar2, pvhXBar);
      objAnim1.setInterpolator(new LinearInterpolator());
      objAnim1.setDuration(this.animationRateIndeterminateMode);

      return objAnim1;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "createIndeterminateModeAnimation: " + ex.toString());
      return null;
    }
  }

  /**
   * Returns a random number between 0.5 and 0.9. This represents the percentage of time that must be reached during an
   * animation before the next bar is animated. 0.5 is 50% of the total animation duration and 0.9 is 90%.
   * @return Returns a number between 0.5 and 0.9.
   */
  private float getStartingPercent()
  {
    final int START_LOWER_THRESHOLD = 50; // 50%
    final int START_UPPER_THRESHOLD = 90; // 90%

    return ((float) this.random.nextInt(START_UPPER_THRESHOLD - START_LOWER_THRESHOLD) + START_LOWER_THRESHOLD) / 100f;
  }


  /**
   * Runs the determinate mode displaying a progress indicator that increases or decreases based upon the desired value and
   * its current location.
   */
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

          post(updateDeterminateBarRunnable);

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


  /**
   * A runnable that updates the bar in determinate mode. Runs on the UI thread.
   */
  private Runnable updateDeterminateBarRunnable = new Runnable()
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
        Log.e(LOG_TAG, "updateDeterminateBarRunnable: " + ex.toString());
      }
    }
  };


  /**
   * Starts the animation for bar2 in indeterminate mode.
   */
  private Runnable bar2AnimationRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      try
      {
        objAnimBar2.cancel();
        objAnimBar2.start();
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "bar2AnimationRunnable: " + ex.toString());
      }
    }
  };


  /**
   * Starts the animation for bar3 in indeterminate mode.
   */
  private Runnable bar3AnimationRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      try
      {
        objAnimBar3.cancel();
        objAnimBar3.start();
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "bar3AnimationRunnable: " + ex.toString());
      }
    }
  };


  /**
   * Shows or hides the progress indicator. The displaying or hiding is done using animation.
   * @param show Set to true to show the indicator. If set to false, the indicator will be hidden and any currently running animation is terminated.
   */
  public void showHide(boolean show)
  {
    try
    {
      ObjectAnimator anim;

      if (show)
      {
        anim = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
        startAnimationThread();
      }
      else
      {
        anim = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
        stopProgressIndicator();
      }

      anim.setDuration(300);
      anim.start();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "showHide: " + ex.toString());
    }
  }


  /**
   * A getter to access the bar width in determinate mode.
   */
  private int getDeterminateBarWidth()
  {
    return this.determinateBarWidth;
  }


  /**
   * Set the rate of animation in indeterminate mode. Avoid setting this rate too low as it could prevent the
   * animation of showing. If indeterminate mode is currently running, the new rate value will take affect
   * when the next bar get animated.
   * @param rate The rate in milliseconds.
   */
  public void setAnimationRateIndeterminateMode(int rate)
  {
    try
    {
      this.animationRateIndeterminateMode = rate;
      objAnimBar2.setDuration(this.animationRateIndeterminateMode);
      objAnimBar3.setDuration(this.animationRateIndeterminateMode);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setAnimationRateIndeterminateMode: " + ex.toString());
    }
  }


  /**
   * A getter to access the animation rate in indeterminate mode.
   */
  public double getAnimationRateIndeterminateMode()
  {
    try
    {
      return this.animationRateIndeterminateMode;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "getAnimationRateIndeterminateMode: " + ex.toString());
      return this.ANIMATION_RATE_INDETERMINATE_MODE;
    }
  }


  /**
   * Sets the rate of animation in determinate mode. A value of zero causes the progress indicator
   * to display the determinate value without animating towards it.
   * @param rate The rate in milliseconds. Fractions can be used.
   */
  public void setAnimationRateDeterminateMode(float rate)
  {
    try
    {
      this.animationRateDeterminateMode = rate;

      this.animationRateMilliseconds = (int) (rate);
      this.animationRateNanoseconds = (int) ((rate % 1) * 1000000f);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setAnimationRateDeterminateMode: " + ex.toString());
    }
  }


  /**
   * A getter to return the value of the animation rate for determinate mode.
   */
  public float getAnimationRateDeterminateMode()
  {
    try
    {
      return this.animationRateDeterminateMode;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "getAnimationRateDeterminateMode: " + ex.toString());
      return this.ANIMATION_RATE_DETERMINATE_MODE;
    }
  }

  /**
   * Sets the maximum value used in determinate mode that can be displayed. This can be any value.
   */
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


  /**
   * Returns the maximum value that can be displayed in determinate mode.
   * @return
   */
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


  /**
   * Sets the minimum value in determinate mode that can be displayed. This can be any value but needs to be less than
   * the value set with setDeterminateModeMaxValue.
   */
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


  /**
   * Returns the minimum value that can be displayed in determinate mode.
   */
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


  /**
   * Sets the value to display in determinate mode.
   */
  public void setDeterminateValue(double value)
  {
    try
    {
      this.determinateValue = value;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setDeterminateValue: " + ex.toString());
    }
  }


  /**
   * Returns the value displayed in determinate mode.
   */
  public double getDeterminateValue()
  {
    try
    {
      return this.determinateValue;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "getDeterminateValue: " + ex.toString());
      return this.determinateValue;
    }
  }


  /**
   * Sets the direction of animation for indeterminate mode.
   * @param rtl Set to true to have the animation go from right to left (rtl).
   */
  public void setIndeterminateModeDirection(boolean rtl)
  {
    try
    {
      if (rtl != this.indeterminateModeRTL)
      {
        stopProgressIndicator();
        this.indeterminateModeRTL = rtl;
        startAnimationThread();
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setDeterminateModeDirection: " + ex.toString());
    }
  }


  /**
   * Returns true if the direction of indeterminate mode is right to left (rtl)
   */
  public boolean indeterminateModeIsRTL()
  {
    try
    {
      return this.indeterminateModeRTL;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "indeterminateModeIsRTL: " + ex.toString());
      return this.indeterminateModeRTL;
    }
  }

  /**
   * Starts the animation thread if the control is visible.
   */
  private void startAnimationThread()
  {
    try
    {
      int width = getWidth();

      if (width == 0)
        return;

      if (getVisibility() != View.VISIBLE)
        return;

      new Thread(null, new StartAnimationThreadRunnable(), "StartAnimationThreadRunnable_" + UUID.randomUUID()).start();

    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "startAnimationThread: " + ex.toString());
    }
  }


  /**
   * The runnable used to start the animation thread. If the thread is currently running, it will block
   * until the current thread terminates.
   */
  private class StartAnimationThreadRunnable implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        while ((threadAnimate != null) && (threadAnimate.getState() != Thread.State.TERMINATED))
        {
        }

        terminateProgress = false;
        determinateBarWidth = 0;

        threadAnimate = new Thread(null, new AnimateIndicatorRunnable(), "AnimateIndicatorRunnable_" + UUID.randomUUID());
        threadAnimate.start();
      }
      catch (Exception ex)
      {
        Log.e(LOG_TAG, "StartAnimationThreadRunnable: " + ex.toString());
      }
    }
  }


  /**
   * Set the type of progress indicator to display. This can be either determinate or indeterminate.
   * @param type Can be either IndicatorTypes.DETERMINATE or IndicatorTypes.INDETERMINATE.
   */
  public void setIndicatorType(int type)
  {
    try
    {
      if (type != this.indicatorType)
      {
        stopProgressIndicator();
        this.indicatorType = type;
        startAnimationThread();
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "setIndicatorType: " + ex.toString());
    }
  }


  /**
   * Returns the current progress indicator type.
   */
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


  /**
   * Stops the progress indicator and resets it. If the indicator is in indeterminate mode, animation stops and the animated bars are not shown.
   * If the indicator is in determinate mode, the bar is also not shown. The animation thread is terminated.
   */
  private void stopProgressIndicator()
  {
    try
    {
      this.terminateProgress = true;

      if (this.objAnimBar2 != null)
      {
        this.objAnimBar2.cancel();
        this.objAnimBar2.removeAllUpdateListeners();
      }

      if (this.objAnimBar3 != null)
      {
        this.objAnimBar3.cancel();
        this.objAnimBar3.removeAllUpdateListeners();
      }

      resetBars();
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "stopProgressIndicator: " + ex.toString());
    }
  }

  /**
   * Resets the bars so that they are not shown and are ready for animating from a starting position.
   */
  private void resetBars()
  {
    try
    {
      RelativeLayout.LayoutParams loParams = (RelativeLayout.LayoutParams) llBar1.getLayoutParams();
      this.determinateBarWidth = 0;
      loParams.width = getDeterminateBarWidth();
      llBar1.setLayoutParams(loParams);

      int bar2Width = (int) (getWidth() * .5f);
      LayoutParams loParams2 = new LayoutParams(bar2Width, LayoutParams.MATCH_PARENT);
      this.llBar2.setLayoutParams(loParams2);
      this.llBar2.setX(-bar2Width);
      this.llBar2.setScaleX(1f);

      int bar3Width = (int) (getWidth() * .6f);
      LayoutParams loParams3 = new LayoutParams(bar3Width, LayoutParams.MATCH_PARENT);
      this.llBar3.setLayoutParams(loParams3);
      this.llBar3.setX(-bar3Width);
      this.llBar3.setScaleX(1f);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "resetBars: " + ex.toString());
    }
  }
}
