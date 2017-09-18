package ru.maxost.vk_superior_post.Utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import ru.maxost.switchlog.SwitchLog

/**
 * Created by Maksim Ostrovidov on 18.09.17.
 * dustlooped@yandex.ru
 */
class ResizeAnimation(private var view: View,
                      private var startHeight: Int,
                      private val targetHeight: Int) : Animation() {

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

        val newHeight = if(startHeight > targetHeight) {
            startHeight - (startHeight - targetHeight) * interpolatedTime
        } else {
            startHeight + (targetHeight - startHeight) * interpolatedTime
        }

        SwitchLog.scream("$newHeight")

        view.layoutParams.height = newHeight.toInt()
        view.requestLayout()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}