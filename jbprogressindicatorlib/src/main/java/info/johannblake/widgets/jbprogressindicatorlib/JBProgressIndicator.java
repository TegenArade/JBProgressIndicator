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

package info.johannblake.widgets.jbprogressindicatorlib;

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
 * A progress indicator that conforms to Material Design. It currently supports indeterminate and determinate modes. Buffer mode is currently not supported.
 * <p/>
 * Source code can be downloaded at:<br/>
 * <a href='https://github.com/JohannBlake/JBProgressIndicator'>https://github.com/JohannBlake/JBProgressIndicator</a>
 * <p/>
 * For further information on Material Design progress indicators, see:<br/>
 * <a href='http://www.google.com/design/spec/components/progress-activity.html#progress-activity-types-of-indicators'>http://www.google.com/design/spec/components/progress-activity.html#progress-activity-types-of-indicators</a>
 */
public class JBProgressIndicator extends RelativeLayout {
    private final float ANIMATION_RATE_DETERMINATE_MODE = 1.5f; // milliseconds.
    private final int ANIMATION_RATE_INDETERMINATE_MODE = 800; // milliseconds.

    private final String LOG_TAG = "JBProgressIndicator";

    private Context mContext;
    private Integer mAnimatedBarColor;
    private int mIndicatorType;
    private float mAnimationRateDeterminateMode;
    private int mAnimationRateIndeterminateMode;
    private Thread mThreadAnimate;
    private int mBgColor;
    private int mAnimationRateMilliseconds;
    private int mAnimationRateNanoseconds;
    private LinearLayout mLLBar1;
    private LinearLayout mLLBar2;
    private LinearLayout mLLBar3;
    private boolean mCtlInitialized;
    private Random mRandom = new Random();
    private ObjectAnimator mObjAnimBar2;
    private ObjectAnimator mObjAnimBar3;
    private float mBar2StartThreshold = getStartingPercent();
    private float mBar3StartThreshold = getStartingPercent();

    private int mDeterminateBarWidth;
    private boolean mTerminateProgress;

    private double mDeterminateValue;
    private double mDeterminateModeMaxValue = 100;
    private double mDeterminateModeMinValue = 0;

    private boolean mIndeterminateModeRTL;


    public enum IndicatorTypes {
        DETERMINATE(0),
        INDETERMINATE(1);

        private final int mValue;

        private IndicatorTypes(int val) {
            mValue = val;
        }

        public int getValue() {
            return mValue;
        }
    }

    public JBProgressIndicator(Context context) {
        super(context);
        mContext = context;
    }

    public JBProgressIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JBProgressIndicator, 0, 0);

        // Get the type of progress indicator.
        mIndicatorType = a.getInt(R.styleable.JBProgressIndicator_indicatorType, IndicatorTypes.DETERMINATE.getValue());

        // Get the background color.
        ColorDrawable bgColorDrawable = (ColorDrawable) getBackground();

        if (bgColorDrawable == null) {
            // No background color was specified, so set it to the default color.
            mBgColor = getResources().getColor(R.color.default_progress_indicator_background_color);
            setBackgroundColor(mBgColor);
        } else
            mBgColor = bgColorDrawable.getColor();

        // Get the color of the animated bar.
        String animBarColor = a.getString(R.styleable.JBProgressIndicator_animatedBarColor);

        if (animBarColor != null)
            mAnimatedBarColor = Color.parseColor(animBarColor);
        else
            mAnimatedBarColor = getResources().getColor(R.color.default_progress_indicator_bar_color);

        // Get the direction of animation for indeterminate mode.
        mIndeterminateModeRTL = a.getBoolean(R.styleable.JBProgressIndicator_indeterminateModeRTL, false);

        // Get the animation rate for determinate mode.
        mAnimationRateDeterminateMode = a.getFloat(R.styleable.JBProgressIndicator_animationRateDeterminateMode, ANIMATION_RATE_DETERMINATE_MODE);

        setAnimationRateDeterminateMode(mAnimationRateDeterminateMode);

        // Get the animation rate for indeterminate mode.
        mAnimationRateIndeterminateMode = a.getInt(R.styleable.JBProgressIndicator_animationRateIndeterminateMode, ANIMATION_RATE_INDETERMINATE_MODE);

        a.recycle();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Initialize the control in the onGlobalLayout because this is the only reliable place where the
                // properties of the controls like width become active for the first time.
                if (!mCtlInitialized) {
                    if (getWidth() == 0)
                        return;

                    addControls();

                    mCtlInitialized = true;
                }

                // If the control is visible, then start the animation thread.
                if (mCtlInitialized && (getVisibility() == View.VISIBLE) && (mLLBar2.getWidth() > 0)) {
                    startAnimationThread();
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

    }

    /**
     * Adds layouts to handle the bars that get animated.
     */
    private void addControls() {
        mLLBar1 = createDeterminateBar();
        mLLBar2 = createIndeterminateBar(.5f);
        mLLBar3 = createIndeterminateBar(.6f);
    }


    /**
     * Creates bars that will get animated in indeterminate mode.
     *
     * @param widthScaleFactor The scale factor to use when creating the bar's initial width. The
     *                         width of the bar is the width of the progress indicator control
     *                         multiplied by this scale factor.
     * @return The view representing the bar is returned.
     */
    private LinearLayout createIndeterminateBar(float widthScaleFactor) {
        LinearLayout llBar = new LinearLayout(mContext);
        int barWidth = (int) (getWidth() * widthScaleFactor);
        LayoutParams loParams = new LayoutParams(barWidth, LayoutParams.MATCH_PARENT);
        llBar.setLayoutParams(loParams);
        llBar.setBackgroundColor(mAnimatedBarColor);
        llBar.setX(mIndeterminateModeRTL ? getWidth() : -barWidth);
        llBar.setVisibility(View.VISIBLE);

        addView(llBar);

        return llBar;
    }

    public JBProgressIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    /**
     * Creates the bar that is used in determinate mode.
     *
     * @return The view representing the bar is returned.
     */
    private LinearLayout createDeterminateBar() {
        LinearLayout llBar = new LinearLayout(mContext);
        LayoutParams loParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        llBar.setLayoutParams(loParams);
        llBar.setBackgroundColor(mAnimatedBarColor);

        addView(llBar);

        return llBar;
    }


    /**
     * The runnable used to start an animation. The animation will show either a determinate mode
     * progress indicator or an indetermine indicator.
     */
    private class AnimateIndicatorRunnable implements Runnable {
        @Override
        public void run() {
            if (mIndicatorType == IndicatorTypes.DETERMINATE.getValue())
                runDeterminateMode();
            else if (mIndicatorType == IndicatorTypes.INDETERMINATE.getValue())
                runIndeterminateMode();
        }
    }


    /**
     * Runs the indeterminate mode showing the indicator.
     */
    private void runIndeterminateMode() {
        // Animate the first bar and once the bar reaches a random point along the x axis, start the
        // second bar animation.
        mObjAnimBar2 = createIndeterminateModeAnimation(mLLBar2, 1.4f);

        mObjAnimBar2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mTerminateProgress)
                    return;

                long elapsedTime = animation.getCurrentPlayTime();
                long totalDuration = animation.getDuration();

                if ((float) elapsedTime / (float) totalDuration > mBar2StartThreshold) {
                    if (!mObjAnimBar3.isStarted()) {
                        mBar3StartThreshold = getStartingPercent();
                        post(bar3AnimationRunnable);
                    }
                }
            }
        });

        mObjAnimBar3 = createIndeterminateModeAnimation(mLLBar3, .2f);

        mObjAnimBar3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mTerminateProgress)
                    return;

                long elapsedTime = animation.getCurrentPlayTime();
                long totalDuration = animation.getDuration();

                if ((float) elapsedTime / (float) totalDuration > mBar3StartThreshold) {
                    if (!mObjAnimBar2.isStarted()) {
                        mBar2StartThreshold = getStartingPercent();
                        post(bar2AnimationRunnable);
                    }
                }
            }
        });

        // Start the animation by animating the first bar.
        post(bar2AnimationRunnable);
    }


    /**
     * Creates an animation for a bar in indeterminate mode.
     *
     * @param llBar       The bar that will be animated.
     * @param widthChange The scale factor that the bar will scale to during the second half of its animation.
     * @return The animation object for the bar is returned.
     */
    private ObjectAnimator createIndeterminateModeAnimation(LinearLayout llBar, final float widthChange) {
        int w = llBar.getWidth();
        PropertyValuesHolder pvhXBar;

        if (indeterminateModeIsRTL())
            if (widthChange < 1)
                pvhXBar = PropertyValuesHolder.ofFloat("x", getWidth(), -w);
            else
                pvhXBar = PropertyValuesHolder.ofFloat("x", getWidth(), -(w * widthChange));
        else {
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
        objAnim1.setDuration(mAnimationRateIndeterminateMode);

        return objAnim1;
    }

    /**
     * Returns a random number between 0.5 and 0.9. This represents the percentage of time that must be reached during an
     * animation before the next bar is animated. 0.5 is 50% of the total animation duration and 0.9 is 90%.
     *
     * @return Returns a number between 0.5 and 0.9.
     */
    private float getStartingPercent() {
        final int START_LOWER_THRESHOLD = 50; // 50%
        final int START_UPPER_THRESHOLD = 90; // 90%

        return ((float) mRandom.nextInt(START_UPPER_THRESHOLD - START_LOWER_THRESHOLD) + START_LOWER_THRESHOLD) / 100f;
    }


    /**
     * Runs the determinate mode displaying a progress indicator that increases or decreases based upon the desired value and
     * its current location.
     */
    private void runDeterminateMode() {
        try {
            mDeterminateBarWidth = 0;

            while (true) {
                while (true) {
                    // Calculate the width of the bar. We do this each time as it is possible that the client changes the progress indicator's value
                    // while the animation is already under way.
                    int width = (int) (((mDeterminateValue - mDeterminateModeMinValue) / (mDeterminateModeMaxValue - mDeterminateModeMinValue)) * getWidth());

                    post(updateDeterminateBarRunnable);

                    if (mTerminateProgress)
                        return;

                    if ((mAnimationRateMilliseconds != 0) || (mAnimationRateNanoseconds != 0))
                        Thread.sleep(mAnimationRateMilliseconds, mAnimationRateNanoseconds);

                    if (mDeterminateBarWidth < width)
                        mDeterminateBarWidth++;
                    else if (mDeterminateBarWidth > width)
                        mDeterminateBarWidth--;
                    else
                        break;
                }

                double currentVal = mDeterminateValue;

                while ((currentVal == mDeterminateValue) && !mTerminateProgress) {
                    Thread.sleep(1);

                    if (mTerminateProgress)
                        return;
                }
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "runDeterminateMode: " + ex.toString());
        }
    }


    /**
     * A runnable that updates the bar in determinate mode. Runs on the UI thread.
     */
    private Runnable updateDeterminateBarRunnable = new Runnable() {
        @Override
        public void run() {
            RelativeLayout.LayoutParams loParams = (RelativeLayout.LayoutParams) mLLBar1.getLayoutParams();
            loParams.width = getDeterminateBarWidth();
            mLLBar1.setLayoutParams(loParams);
        }
    };


    /**
     * Starts the animation for bar2 in indeterminate mode.
     */
    private Runnable bar2AnimationRunnable = new Runnable() {
        @Override
        public void run() {
            mObjAnimBar2.cancel();
            mObjAnimBar2.start();
        }
    };


    /**
     * Starts the animation for bar3 in indeterminate mode.
     */
    private Runnable bar3AnimationRunnable = new Runnable() {
        @Override
        public void run() {
            mObjAnimBar3.cancel();
            mObjAnimBar3.start();
        }
    };


    /**
     * Shows or hides the progress indicator. The displaying or hiding is done using animation.
     *
     * @param show Set to true to show the indicator. If set to false, the indicator will be hidden and any currently running animation is terminated.
     */
    public void showHide(boolean show) {
        if (show) {
            setVisibility(View.VISIBLE);

            // Don't animate the indicator into view if it is already showing.
            if ((mThreadAnimate == null) || (mThreadAnimate.getState() == Thread.State.TERMINATED)) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
                anim.setDuration(300);
                anim.start();
            }

            startAnimationThread();
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
            stopProgressIndicator();

            anim.setDuration(300);
            anim.start();
        }
    }


    /**
     * A getter to access the bar width in determinate mode.
     */
    private int getDeterminateBarWidth() {
        return mDeterminateBarWidth;
    }


    /**
     * Set the rate of animation in indeterminate mode. Avoid setting this rate too low as it could prevent the
     * animation of showing. If indeterminate mode is currently running, the new rate value will take affect
     * when the next bar get animated.
     *
     * @param rate The rate in milliseconds.
     */
    public void setAnimationRateIndeterminateMode(int rate) {
        mAnimationRateIndeterminateMode = rate;
        mObjAnimBar2.setDuration(mAnimationRateIndeterminateMode);
        mObjAnimBar3.setDuration(mAnimationRateIndeterminateMode);
    }


    /**
     * A getter to access the animation rate in indeterminate mode.
     */
    public double getAnimationRateIndeterminateMode() {
        return mAnimationRateIndeterminateMode;
    }


    /**
     * Sets the rate of animation in determinate mode. A value of zero causes the progress indicator
     * to display the determinate value without animating towards it.
     *
     * @param rate The rate in milliseconds. Fractions can be used.
     */
    public void setAnimationRateDeterminateMode(float rate) {
        mAnimationRateDeterminateMode = rate;
        mAnimationRateMilliseconds = (int) (rate);
        mAnimationRateNanoseconds = (int) ((rate % 1) * 1000000f);
    }


    /**
     * A getter to return the value of the animation rate for determinate mode.
     */
    public float getAnimationRateDeterminateMode() {
        return mAnimationRateDeterminateMode;
    }

    /**
     * Sets the maximum value used in determinate mode that can be displayed. This can be any value.
     */
    public void setDeterminateModeMaxValue(double maxValue) {
        mDeterminateModeMaxValue = maxValue;
    }


    /**
     * Returns the maximum value that can be displayed in determinate mode.
     *
     * @return
     */
    public double getDeterminateModeMaxValue() {
        return mDeterminateModeMaxValue;
    }


    /**
     * Sets the minimum value in determinate mode that can be displayed. This can be any value but needs to be less than
     * the value set with setDeterminateModeMaxValue.
     */
    public void setDeterminateModeMinValue(double minValue) {
        mDeterminateModeMinValue = minValue;
    }


    /**
     * Returns the minimum value that can be displayed in determinate mode.
     */
    public double getDeterminateModeMinValue() {
        return mDeterminateModeMinValue;
    }


    /**
     * Sets the value to display in determinate mode.
     */
    public void setDeterminateValue(double value) {
        mDeterminateValue = value;
    }


    /**
     * Returns the value displayed in determinate mode.
     */
    public double getDeterminateValue() {
        return mDeterminateValue;
    }


    /**
     * Sets the direction of animation for indeterminate mode.
     *
     * @param rtl Set to true to have the animation go from right to left (rtl).
     */
    public void setIndeterminateModeDirection(boolean rtl) {
        if (rtl != mIndeterminateModeRTL) {
            stopProgressIndicator();
            mIndeterminateModeRTL = rtl;
            startAnimationThread();
        }
    }


    /**
     * Returns true if the direction of indeterminate mode is right to left (rtl)
     */
    public boolean indeterminateModeIsRTL() {
        return mIndeterminateModeRTL;
    }

    /**
     * Starts the animation thread if the control is visible.
     */
    private void startAnimationThread() {
        int width = getWidth();

        if (width == 0)
            return;

        if (getVisibility() != View.VISIBLE)
            return;

        new Thread(null, new StartAnimationThreadRunnable(), "StartAnimationThreadRunnable_" + UUID.randomUUID()).start();

    }


    /**
     * The runnable used to start the animation thread. If the thread is currently running, it will block
     * until the current thread terminates.
     */
    private class StartAnimationThreadRunnable implements Runnable {
        @Override
        public void run() {
            while ((mThreadAnimate != null) && (mThreadAnimate.getState() != Thread.State.TERMINATED)) {
            }

            mTerminateProgress = false;
            mDeterminateBarWidth = 0;

            mThreadAnimate = new Thread(null, new AnimateIndicatorRunnable(), "AnimateIndicatorRunnable_" + UUID.randomUUID());
            mThreadAnimate.start();
        }
    }


    /**
     * Set the type of progress indicator to display. This can be either determinate or indeterminate.
     *
     * @param type Can be either IndicatorTypes.DETERMINATE or IndicatorTypes.INDETERMINATE.
     */
    public void setIndicatorType(int type) {
        if (type != mIndicatorType) {
            stopProgressIndicator();
            mIndicatorType = type;
            startAnimationThread();
        }
    }


    /**
     * Returns the current progress indicator type.
     */
    public double getIndicatorType() {
        return mIndicatorType;
    }


    /**
     * Stops the progress indicator and resets it. If the indicator is in indeterminate mode, animation stops and the animated bars are not shown.
     * If the indicator is in determinate mode, the bar is also not shown. The animation thread is terminated.
     */
    private void stopProgressIndicator() {
        mTerminateProgress = true;

        if (mObjAnimBar2 != null) {
            mObjAnimBar2.cancel();
            mObjAnimBar2.removeAllUpdateListeners();
        }

        if (mObjAnimBar3 != null) {
            mObjAnimBar3.cancel();
            mObjAnimBar3.removeAllUpdateListeners();
        }

        resetBars();
    }

    /**
     * Resets the bars so that they are not shown and are ready for animating from a starting position.
     */
    private void resetBars() {
        if (mLLBar1 == null)
            return;

        RelativeLayout.LayoutParams loParams = (RelativeLayout.LayoutParams) mLLBar1.getLayoutParams();
        mDeterminateBarWidth = 0;
        loParams.width = getDeterminateBarWidth();
        mLLBar1.setLayoutParams(loParams);

        int bar2Width = (int) (getWidth() * .5f);
        LayoutParams loParams2 = new LayoutParams(bar2Width, LayoutParams.MATCH_PARENT);
        mLLBar2.setLayoutParams(loParams2);
        mLLBar2.setX(-bar2Width);
        mLLBar2.setScaleX(1f);

        int bar3Width = (int) (getWidth() * .6f);
        LayoutParams loParams3 = new LayoutParams(bar3Width, LayoutParams.MATCH_PARENT);
        mLLBar3.setLayoutParams(loParams3);
        mLLBar3.setX(-bar3Width);
        mLLBar3.setScaleX(1f);
    }
}
