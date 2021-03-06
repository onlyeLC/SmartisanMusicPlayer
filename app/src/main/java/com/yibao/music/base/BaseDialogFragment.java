package com.yibao.music.base;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Des：${TODO}
 * Time:2017/5/31 18:50
 *
 * @author Stran
 */
public abstract class BaseDialogFragment
        extends DialogFragment {

    public static int MAX_DOWN_PREGRESS = 100;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow()
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return getViews();
    }

    /**
     * 经过数据绑定的View
     *
     * @return
     */
    public abstract View getViews();

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
