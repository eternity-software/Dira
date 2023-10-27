package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;

public interface WaterfallImageView  {

     SelectorFileInfo getFileInfo();
     ImageView getImageView();
     default void onImageBind(Bitmap bitmap){};
}
