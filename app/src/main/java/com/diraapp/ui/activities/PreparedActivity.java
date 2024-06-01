package com.diraapp.ui.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;

import com.diraapp.ui.activities.roominfo.RoomInfoActivity;

public class PreparedActivity {
    protected final Intent intent;
    protected final ActivityOptions options;
    protected final Activity activity;

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


    public static class MessagePreviewPreparedActivity extends PreparedActivity {

        public MessagePreviewPreparedActivity(Activity activity, Intent intent, ActivityOptions options) {
            super(activity, intent, options);
        }

        @Override
        public void start() {
            activity.startActivityForResult(intent,
                    RoomInfoActivity.RESULT_CODE_SCROLL_TO_MESSAGE, options.toBundle());
        }
    }
}
