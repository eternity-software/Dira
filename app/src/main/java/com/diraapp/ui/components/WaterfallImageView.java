package com.diraapp.ui.components;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.diraapp.storage.DiraMediaInfo;

public interface WaterfallImageView  {

     DiraMediaInfo getFileInfo();
     ImageView getImageView();
     default void onImageBind(Bitmap bitmap){};
}
