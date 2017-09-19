package ru.maxost.vk_superior_post.Utils.LayoutManagers;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;

import ru.maxost.switchlog.SwitchLog;

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */

public class CenterScroller extends LinearSmoothScroller {

    public CenterScroller(Context context) {
        super(context);
    }

    @Override
    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
        SwitchLog.scream("viewStart: " + viewStart + " viewEnd: " + viewEnd + " boxStart: " + boxStart + " boxEnd: " + boxEnd);
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
    }

    @Override
    protected int calculateTimeForScrolling(int dx) {
        return 300;
    }
}
