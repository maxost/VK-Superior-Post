package ru.maxost.vk_superior_post.Data.Services.File

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore.MediaColumns
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class ProdFileService(private val context: Context) : FileService {

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

    override fun storePost(bitmap: Bitmap): Completable = Completable.fromAction {
        val cw = ContextWrapper(context)
        val directory = cw.getDir("temp", Context.MODE_PRIVATE)
        val mypath = File(directory, "tempImage.jpg")

        val fos = FileOutputStream(mypath)
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun getPost(): Single<Bitmap> = Single.fromCallable {
        val cw = ContextWrapper(context)
        val directory = cw.getDir("temp", Context.MODE_PRIVATE)
        val f = File(directory, "tempImage.jpg")

        return@fromCallable BitmapFactory.decodeStream(FileInputStream(f))
    }
}