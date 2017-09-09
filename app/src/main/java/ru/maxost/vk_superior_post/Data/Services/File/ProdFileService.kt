package ru.maxost.vk_superior_post.Data.Services.File

import android.content.Context
import io.reactivex.Single
import android.provider.MediaStore.MediaColumns
import android.net.Uri
import android.provider.MediaStore
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

        context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                MediaColumns.DATE_MODIFIED + " DESC").use {
            val column_index_data = it.getColumnIndexOrThrow(MediaColumns.DATA)
            var counter = 0
            while (it.moveToNext() && counter < 100) {
                val path = it.getString(column_index_data)
                list.add(File(path))
                counter++
            }
        }
        return@fromCallable list
    }
}