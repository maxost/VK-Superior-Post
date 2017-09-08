package ru.maxost.vk_superior_post.Model

import java.io.Serializable
import java.util.*

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
data class Post(var text: String = "",
                var stickers: Stack<Sticker>): Serializable