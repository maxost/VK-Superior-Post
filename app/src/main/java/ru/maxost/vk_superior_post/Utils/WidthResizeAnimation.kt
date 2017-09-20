package ru.maxost.vk_superior_post.Utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import ru.maxost.switchlog.SwitchLog

/**
 * Created by Maxim Ostrovidov on 20.09.17.
 * (c) White Soft
 */
class WidthResizeAnimation(private var view: View,
                            private var startWidth: Int,
                            private val targetWidth: Int) : Animation() {

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

        val newWidth = if(startWidth > targetWidth) {
            startWidth - (startWidth - targetWidth) * interpolatedTime
        } else {
            startWidth + (targetWidth - startWidth) * interpolatedTime
        }

        SwitchLog.scream("startWidth: $startWidth targetWidth: $targetWidth newWidth: $newWidth")

        view.layoutParams.width = newWidth.toInt()
        view.requestLayout()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}