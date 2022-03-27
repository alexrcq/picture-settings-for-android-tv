package com.alexrcq.tvpicturesettings.util;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({DialogButton.POSITIVE_BUTTON, DialogButton.NEGATIVE_BUTTON, DialogButton.NEUTRAL_BUTTON})
public @interface DialogButton {
    int POSITIVE_BUTTON = -1;
    int NEGATIVE_BUTTON = -2;
    int NEUTRAL_BUTTON = -3;
}