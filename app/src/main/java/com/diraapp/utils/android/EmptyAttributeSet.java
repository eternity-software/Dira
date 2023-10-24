package com.diraapp.utils.android;

import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Xml;

import com.diraapp.R;

import org.xmlpull.v1.XmlPullParser;

public class EmptyAttributeSet {
    public static AttributeSet get(Resources resources) {
        XmlPullParser parser = resources.getXml(R.xml.backup_rules);
        return Xml.asAttributeSet(parser);

    }
};