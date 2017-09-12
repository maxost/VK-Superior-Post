package ru.maxost.vk_superior_post.Model

import java.io.Serializable

/**
 * Created by Maxim Ostrovidov on 08.09.17.
 * (c) White Soft
 */
data class Sticker(val id: String,
                   val resId: Int,
                   var xFactor: Float = 0.2f,
                   var yFactor: Float = 0.2f,
                   var scaleFactor: Float = 0.3f,
                   var angle: Float = 0f) : Serializable