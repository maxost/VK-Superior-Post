package ru.maxost.vk_superior_post.Data.Services.File

import android.content.Context
import io.reactivex.Single
import android.provider.MediaStore.MediaColumns
import android.net.Uri
import java.io.File

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class ProdFileService(private val context: Context): FileService {

    override fun getImagesFromGallery(count: Int): Single<List<File>> = Single.fromCallable {
        val uri: Uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val list = mutableListOf<File>()
        val projection = arrayOf(MediaColumns.DATA)

        context.contentResolver.query(uri, projection, null, null, null).use {
            val column_index_data = it.getColumnIndexOrThrow(MediaColumns.DATA)
            while (it.moveToNext()) {
                val path = it.getString(column_index_data).replace(" ","%20")
                list.add(File(path))
            }
        }
        return@fromCallable list
    }
}