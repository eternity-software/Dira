package com.diraapp.ui.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;

public class PreparedActivity {
    private final Intent intent;
    private final ActivityOptions options;
    private final Activity activity;

    public PreparedActivity(Activity activity, Intent intent, ActivityOptions options) {
        this.activity = activity;
        this.intent = intent;
        this.options = options;
    }

    public Intent getIntent() {
        return intent;
    }

    public void start() {
        activity.startActivity(intent, options.toBundle());
    }
}
