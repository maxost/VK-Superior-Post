package ru.maxost.vk_superior_post.Model

import ru.maxost.vk_superior_post.R
import java.io.File
import java.io.Serializable

/**
 * Created by Maksim Ostrovidov on 08.09.17.
 * dustlooped@yandex.ru
 */
data class Background(val type: BackgroundType = BackgroundType.COLORED,
                      val colorDrawableResId: Int? = R.drawable.background_white_full,
                      val imageFile: File? = null): Serializable

enum class BackgroundType {
    COLORED,
    BEACH,
    STARS,
    IMAGE
}