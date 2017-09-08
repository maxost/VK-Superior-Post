package ru.maxost.vk_superior_post.Data.Services.File

import io.reactivex.Single
import java.io.File
import java.net.URI

/**
 * Created by Maxim Ostrovidov on 06.09.17.
 * (c) White Soft
 */
interface FileService {
    fun getImagesFromGallery(count: Int): Single<List<File>>
}