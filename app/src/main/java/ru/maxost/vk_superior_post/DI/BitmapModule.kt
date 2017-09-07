package ru.maxost.vk_superior_post.DI

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.maxost.vk_superior_post.Data.Services.Bitmap.BitmapService
import ru.maxost.vk_superior_post.Data.Services.Bitmap.ProdBitmapService
import javax.inject.Singleton

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
@Singleton
@Module
class BitmapModule(private val context: Context) {

    @Provides @Singleton
    fun bitmapService(): BitmapService = ProdBitmapService(context)
}