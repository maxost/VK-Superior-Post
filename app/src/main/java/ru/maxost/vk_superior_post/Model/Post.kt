package ru.maxost.vk_superior_post.Model

import java.io.Serializable
import java.util.*

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
data class Post(var text: String = "",
                var textStyle: TextStyle = TextStyle.WHITE,
                var stickers: Stack<Sticker>): Serializable

enum class TextStyle {
    BLACK, WHITE, WHITE_WITH_BACKGROUND, BLACK_WITH_BACKGROUND
}