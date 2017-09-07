package ru.maxost.vk_superior_post.DI

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.maxost.vk_superior_post.Data.Services.File.FileService
import ru.maxost.vk_superior_post.Data.Services.File.ProdFileService
import javax.inject.Singleton

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
@Singleton
@Module
class FileModule(private val context: Context) {

    @Provides @Singleton
    fun fileService(): FileService = ProdFileService(context)
}