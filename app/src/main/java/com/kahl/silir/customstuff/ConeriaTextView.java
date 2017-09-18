package com.kahl.silir.customstuff;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Paskahlis Anjas Prabowo on 29/07/2017.
 */

public class ConeriaTextView extends AppCompatTextView {
    public ConeriaTextView(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public ConeriaTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public ConeriaTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), "fonts/coneria_script.ttf");
        setTypeface(customFont);
    }
}
