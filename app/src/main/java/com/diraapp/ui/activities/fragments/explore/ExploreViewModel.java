package com.diraapp.ui.activities.fragments.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.diraapp.BuildConfig;

public class ExploreViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExploreViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Not implemented in " + BuildConfig.VERSION_NAME);
    }

    public LiveData<String> getText() {
        return mText;
    }
}