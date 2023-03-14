package ru.dira.transition;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class Transitions {
    public static Bundle makeOneViewTransition(View fromView, Activity context, Intent intent, String transitionid)
    {
        Bundle bundle = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            View v = fromView;
            if (v != null) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, v, transitionid);



                bundle = options.toBundle();
            }
        }

       return bundle;
    }
}
