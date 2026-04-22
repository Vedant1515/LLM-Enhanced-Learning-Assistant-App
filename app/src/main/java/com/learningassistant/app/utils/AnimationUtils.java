package com.learningassistant.app.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import com.learningassistant.app.R;
import java.util.List;

public class AnimationUtils {

    public static void slideInRight(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void slideInLeft(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public static void fadeIn(View view, int durationMs) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(durationMs)
                .setListener(null)
                .start();
    }

    public static void bounceIn(View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.0f);
        scaleX.setDuration(400);
        scaleY.setDuration(400);
        scaleX.setInterpolator(new OvershootInterpolator(2f));
        scaleY.setInterpolator(new OvershootInterpolator(2f));
        scaleX.start();
        scaleY.start();
    }

    public static void staggerFadeIn(List<View> views, int delayMs) {
        for (int i = 0; i < views.size(); i++) {
            final View view = views.get(i);
            view.setAlpha(0f);
            view.setTranslationY(30f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay((long) i * delayMs)
                    .setListener(null)
                    .start();
        }
    }

    public static void shimmerLoading(final View view) {
        final ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.3f);
        animator.setDuration(700);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(anim -> view.setAlpha((Float) anim.getAnimatedValue()));
        animator.start();
        view.setTag(animator);
    }

    public static void stopShimmer(View view) {
        Object tag = view.getTag();
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
        }
        view.setAlpha(1.0f);
    }
}
