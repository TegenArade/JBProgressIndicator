JBProgressIndicator
================
A customizable progress indicator for Android with Material Design.

![JBProgressIndicator](https://github.com/JohannBlake/JBProgressIndicator/blob/master/Graphics/progress-indicators.gif)

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

### Demo & Integration

To try out the widget, just install the demo app *JBProgressIndicator-Demo-1.0.apk* located in the root directory. The source code for the widget is located under the folder *jbprogressindicatorlib*. If you don't want to bother downloading and compiling the library, you can download the compiled version, *jbprogressindicator-1.0.aar*, located in the root directory.

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
        jb:indicatorType="indeterminate"
        jb:animationRateDeterminateMode="1.5"
        jb:animationRateIndeterminateMode="800"
        jb:indeterminateModeRTL="false"
        jb:animatedBarColor="#3687ed" />

</RelativeLayout>

```

**android:layout_height**

Sets the height of the progress indicator.

**android:background**

Sets the background color of the progress indicator. If not specified, a light blue is used (#acceeb).

**jb:indicatorType**

  Set this to either "determinate" or "indeterminate". If not specified, determinate mode will be used.

**jb:animationRateDeterminateMode**

  The rate at which the progress indicator will move from one value to the next in determinate mode. The rate is specified in milliseconds or even fractional milliseconds (1.5, 0.6, etc). If not specified, the rate will be set to 1.5 ms. If the value is set to zero, the progress indicator will change to new values almost instantly. For example, if the progress indicator's scale is from zero to 100 and the current value is zero and then set to 100, the progress indicator will go straight from zero to 100. But if you specify a rate greater than zero, the progress indicator will animate gradually from zero to 100. The smaller the rate, the faster the animation.
  
**jb:animationRateIndeterminateMode**

  The rate at which the progress indicator is animated in indeterminate mode. The rate is specified in milliseconds. Don't set the rate to a small number to achieve a fast animation as this may result in control not functioning. A fast rate is around 300 ms while an average rate is around 800ms. If not specified, the rate will be set to 800 ms.

**jb:indeterminateModeRTL**

Sets the direction of animation in indeterminate mode. If set to true, the animation will go from right-to-left, otherwise from left-to-right. If not specified, left-to-right is used.

**jb:animatedBarColor**

Sets the color of the animated bars used in determinate or indeterminate mode. If not specified, the color will be a medium blue (#3687ed).

In addition to attributes that can be set in xml, there are a number of properties and methods available that can be set programmatically:

**setIndicatorType(int type)**

Sets the type of indicator to display. Set *type* to 0 for determinate mode and 1 for indeterminatemode.

**showHide(boolean show)**

If *show* is set to true, the progress indicator will be shown. It will be animated from its invisible state to its visible state. If you don't want to have animation when showing or hiding the control, just use the control's normal visibility property (setVisibility). There is one important difference between hiding the contrl with *showHide* and hiding it with setVisibility(View.INVISIBLE) or setVisibility(View.GONE). showHide will also terminate the progress indicator's internal thread that handles indeterminate and determinate modes when the control is hidden. Using the normal setVisibility method only hides the control but the thread remains running. Keeping the thread running even when the control is hidden may be useful under certain conditions depending on how your app is using the progress indicator.

**setDeterminateModeMinValue**

This is the lower end of the range that applies to determinate mode. It does not have to be zero but can be any number including negative or fractional numbers. This is useful if you're showing a progress indicator for things like temperature, stock prices, speed, etc.

**setDeterminateModeMaxValue**

This is the upper end of the range that applies to determinate mode. Like setDeterminateModeMinValue, it too can be any number but needs to be greater than the value specified by setDeterminateModeMinValue.

**setDeterminateValue**

Sets the value of the progress indicator in determinate mode. It should be a number that falls between setDeterminateModeMinValue and setDeterminateModeMaxValue.

**setIndeterminateModeDirection(boolean rtl)**

Call this with the *rtl* parameter set to true to have animation go from right-to-left.

**indeterminateModeIsRTL**

Returns true if the direction of animation in determinate mode is right-to-left.


The remaining methods are fairly self explanatory:

-setAnimationRateDeterminateMode

-setAnimationRateIndeterminateMode

There are also getter methods available for all the setter methods.

### MIT License

```
    The MIT License (MIT)

    Copyright (c) 2015 Johann Blake

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
```
