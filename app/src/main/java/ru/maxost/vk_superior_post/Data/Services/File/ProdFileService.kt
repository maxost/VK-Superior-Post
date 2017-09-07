package ru.maxost.vk_superior_post.Data.Services.File

import android.content.Context
import io.reactivex.Single
import java.net.URI

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class ProdFileService(private val context: Context): FileService {

    override fun getImagesFromGallery(count: Int): Single<List<URI>> {
        throw NotImplementedError()
    }
}