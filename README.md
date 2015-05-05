JBProgressIndicator
================
A customizable progress indicator for Android with Material Design.

![JBProgressIndicator](https://github.com/JohannBlake/JBProgressIndicator/blob/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.png)
Features:

* Determinate and indeterminate modes
* Adjustable animation rate
* Adjustable rate of change between values in determinate mode
* Left-to-right or right-to-left animation in indeterminate mode
* Arbitrary lower and upper values. Not limited from zero to some upper value. Can be fractional.
* Animated hide/show
* Adjustable colors

Watch a video demonstrating the control (Select resolution 720 and watch in Theater mode):
[https://www.youtube.com/watch?v=w5Z4blnt6Xg](https://www.youtube.com/watch?v=w5Z4blnt6Xg)

The JBProgressIndicator follows the design pattern specified at:
[http://www.google.com/design/spec/components/progress-activity.html#progress-activity-types-of-indicators](http://www.google.com/design/spec/components/progress-activity.html#progress-activity-types-of-indicators)

### Usage

The following is an example of how to integrate the control in your layout:

``` xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:jb="http://schemas.android.com/apk/res-auto"
                xmlns:tools="http://schemas.android.com/tools"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="10dp"
                tools:context=".MainActivity">

    <info.johannblake.widgets.JBProgressIndicator
        android:id="@+id/jbProgressIndicator"
        android:layout_width="fill_parent"
        android:layout_height="2dp"
        android:layout_alignParentBottom="true"
        android:visibility="visible"
        jb:animationRateDeterminateMode="1.5"
        jb:animationRateIndeterminateMode="800"
        jb:indeterminateModeRTL="false"
        jb:indicatorType="indeterminate"/>

</RelativeLayout>

```

**jb:indicatorType**

  Set this to either "determinate" or "indeterminate". If not specified, determinate mode will be used.


**jb:animationRateDeterminateMode**

  The rate at which the progress indicator will move from one value to the next. The rate is specified in milliseconds. If not specified, the rate will be set to 800 ms. If the value is set to zero, the progress indicator will change to new values almost instantly. For example, if the progress indicator's scale is from zero to 100 and the current value is zero and then set to 100, the progress indicator will go straight from zero to 100. But if you specify a rate greater than zero, the progress indicator will animate gradually from zero to 100. The smaller the rate, the faster the animation.
  
