package ru.maxost.vk_superior_post.Utils;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by Maksim Ostrovidov on 08.09.17.
 * dustlooped@yandex.ru
 */

public class DrawableViewBackgroundTarget extends ViewBackgroundTarget<Drawable> {
    public DrawableViewBackgroundTarget(View view) { super(view); }
    @Override protected void setResource(Drawable resource) { setBackground(resource); }
}