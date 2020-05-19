package com.david.drawer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RewardsDrawer extends FrameLayout implements GestureDetector.OnGestureListener {


    private float mCurrentScaleFactor;
    private float mCurrentTransX;
    private float mLastX;
    private FrameLayout mMobileContainer;
    private float mMaxTransX = 650f;
    private float mMaxScaleFactor;
    private float mMinTransX = 0f;
    private float mMinScaleFactor = 1f;
    private ViewStatev1 mState;

    private static long OPEN_CLOSE_DURATION = 300;
    private static long ANIMATION_DURATION = 100;


    private enum ViewStatev1 {
        CLOSE, OPEN_FROM_LEFT, OPEN_FROM_RIGHT, DRAG_FROM_LEFT, DRAG_FROM_RIGHT;
    }


    private GestureDetector mGestureDetector;
    private ViewConfiguration mViewConfiguration;


    public RewardsDrawer(@NonNull Context context) {
        super(context);
        init();
    }

    public RewardsDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RewardsDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mGestureDetector = new GestureDetector(getContext(), this);
        mViewConfiguration = ViewConfiguration.get(getContext());
        setState(ViewStatev1.CLOSE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMobileContainer = findViewById(R.id.mobile_container);
        calculateScaleFactor();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mState == ViewStatev1.CLOSE) {
            return false;
        } else if (mState == ViewStatev1.DRAG_FROM_LEFT) {
            return true;
        } else if (mState == ViewStatev1.OPEN_FROM_LEFT) {
            if (ev.getX() < mMaxTransX) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean gotTheFling = mGestureDetector.onTouchEvent(ev);
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getX();
                return true;
            case MotionEvent.ACTION_UP:
                if (!gotTheFling) {
                    if (mCurrentTransX > mMaxTransX / 2) {
                        animateToLeftDrawerEnd(ANIMATION_DURATION);
                    } else {
                        animateToLeftDrawerStart(ANIMATION_DURATION);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mState == ViewStatev1.DRAG_FROM_LEFT) {
                    for (int i = 0; i < ev.getHistorySize(); i++) {
                        float dx = ev.getHistoricalX(i) - mLastX;
                        mLastX = ev.getHistoricalX(i);
                        float newTransX = mCurrentTransX + dx;
                        if (newTransX >= 0 && newTransX <= mMaxTransX) {
                            mCurrentTransX = newTransX;
                            mMobileContainer.setTranslationX(mCurrentTransX);
                            mCurrentScaleFactor = getScaleFactorForTrans(mCurrentTransX);
                            mMobileContainer.setScaleX(mCurrentScaleFactor);
                            mMobileContainer.setScaleY(mCurrentScaleFactor);
                        }
                    }
                } else {
                    float dx = ev.getX() - mLastX;
                    if (dx > mViewConfiguration.getScaledTouchSlop() || dx < -1 * mViewConfiguration.getScaledTouchSlop()) {
                        mLastX = ev.getX();
                        setState(ViewStatev1.DRAG_FROM_LEFT);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mCurrentTransX > mMaxTransX / 2) {
                    animateToLeftDrawerEnd(ANIMATION_DURATION);
                } else {
                    animateToLeftDrawerStart(ANIMATION_DURATION);
                }
                break;

        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }


    public void openLeftDrawer() {
        setState(ViewStatev1.DRAG_FROM_LEFT);
        animateToLeftDrawerEnd(OPEN_CLOSE_DURATION);
    }


    public void closeLeftDrawer() {
        setState(ViewStatev1.DRAG_FROM_LEFT);
        animateToLeftDrawerStart(OPEN_CLOSE_DURATION);
    }


    public void openRightDrawer() {
        setState(ViewStatev1.DRAG_FROM_RIGHT);
        animateToRightDrawerEnd(OPEN_CLOSE_DURATION);
    }


    public void closeRightDrawer() {
        setState(ViewStatev1.DRAG_FROM_RIGHT);
        animateToRightDrawerStart(OPEN_CLOSE_DURATION);
    }

    private float getScaleFactorForTrans(float trans) {
        float slope = (mMaxScaleFactor - mMinScaleFactor) / (mMaxTransX - 0);
        float constant = mMaxScaleFactor - slope * mMaxTransX;
        return slope * trans + constant;
    }


    private void calculateScaleFactor() {
        float rootHeight = getHeight();
        float containerScaledHeight = rootHeight - 2 * 100;
        float scaleFactor = containerScaledHeight / rootHeight;
        mMaxScaleFactor = scaleFactor;
    }

    private void animateToRightDrawerEnd(long duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, -1 * mMaxTransX);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentAnimatedValue = ((Float) animation.getAnimatedValue());
                mCurrentTransX = currentAnimatedValue;
                mMobileContainer.setTranslationX(mCurrentTransX);
                mCurrentScaleFactor = getScaleFactorForTrans(-1 * mCurrentTransX);
                mMobileContainer.setScaleX(mCurrentScaleFactor);
                mMobileContainer.setScaleY(mCurrentScaleFactor);
                if (currentAnimatedValue == mMaxTransX) {
                    setState(ViewStatev1.OPEN_FROM_RIGHT);
                }
            }
        });
        valueAnimator.start();
    }

    private void animateToRightDrawerStart(long duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(-1 * mMaxTransX, 0);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentAnimatedValue = ((Float) animation.getAnimatedValue());
                mCurrentTransX = currentAnimatedValue;
                mMobileContainer.setTranslationX(mCurrentTransX);
                mCurrentScaleFactor = getScaleFactorForTrans(-1 * mCurrentTransX);
                mMobileContainer.setScaleX(mCurrentScaleFactor);
                mMobileContainer.setScaleY(mCurrentScaleFactor);
                if (currentAnimatedValue == mMinTransX) {
                    setState(ViewStatev1.CLOSE);
                }
            }
        });
        valueAnimator.start();
    }


    private void animateToLeftDrawerEnd(long duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCurrentTransX, mMaxTransX);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentAnimatedValue = ((Float) animation.getAnimatedValue());
                mCurrentTransX = currentAnimatedValue;
                mMobileContainer.setTranslationX(mCurrentTransX);
                mCurrentScaleFactor = getScaleFactorForTrans(mCurrentTransX);
                mMobileContainer.setScaleX(mCurrentScaleFactor);
                mMobileContainer.setScaleY(mCurrentScaleFactor);
                if (currentAnimatedValue == mMaxTransX) {
                    setState(ViewStatev1.OPEN_FROM_LEFT);
                }
            }
        });
        valueAnimator.start();

    }


    private void animateToLeftDrawerStart(long duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCurrentTransX, mMinTransX);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentAnimatedValue = ((Float) animation.getAnimatedValue());
                mCurrentTransX = currentAnimatedValue;
                mMobileContainer.setTranslationX(mCurrentTransX);
                mCurrentScaleFactor = getScaleFactorForTrans(mCurrentTransX);
                mMobileContainer.setScaleX(mCurrentScaleFactor);
                mMobileContainer.setScaleY(mCurrentScaleFactor);
                if (currentAnimatedValue == mMinTransX) {
                    setState(ViewStatev1.CLOSE);
                }
            }
        });
        valueAnimator.start();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mState == ViewStatev1.OPEN_FROM_LEFT) {
            if (e.getX() > mMaxTransX) {
                closeLeftDrawer();
            }
        }
        return true;
    }


    private void setState(ViewStatev1 state) {
        mState = state;
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX > 0) {
            animateToLeftDrawerEnd(ANIMATION_DURATION);
        } else {
            animateToLeftDrawerStart(ANIMATION_DURATION);
        }
        return true;
    }
}
