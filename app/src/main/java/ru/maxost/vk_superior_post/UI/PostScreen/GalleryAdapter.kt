package ru.maxost.vk_superior_post.UI.PostScreen

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Model.Background
import ru.maxost.vk_superior_post.Model.BackgroundType
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.Utils.GlideDrawableViewBackgroundTarget
import ru.maxost.vk_superior_post.Utils.MySimpleAdapter
import ru.maxost.vk_superior_post.Utils.RoundedCornersTransformation
import ru.maxost.vk_superior_post.Utils.dp2px
import java.io.File

/**
 * Created by Maksim Ostrovidov on 09.09.17.
 * dustlooped@yandex.ru
 */
interface GalleryAdapter {
    fun setNewData(data: List<File>)
}

const val OPTION_ITEM = "OPTION_ITEM"
const val IMAGE_ITEM = "IMAGE_ITEM"

inline fun galleryAdapter(context: Context,
                          crossinline onItemClick: (File) -> Unit,
                          crossinline onTakePhotoClick: () -> Unit,
                          crossinline onOpenGalleryClick: () -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>
        = object : RecyclerView.Adapter<RecyclerView.ViewHolder>(), GalleryAdapter {

    init { setHasStableIds(true) }

    private var data = listOf<File>()
    private var selectedFile: File? = null
    private val placeholder = ContextCompat.getDrawable(context, R.drawable.drawable_placeholder)
    private val transparentDrawable = ColorDrawable(Color.TRANSPARENT)

    override fun getItemCount(): Int = data.size + 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = object : RecyclerView.ViewHolder
    (LayoutInflater.from(parent.context).inflate(R.layout.gallery_list_item, parent, false)) {}

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image = holder.itemView as ImageView
        when (position) {
            0 -> {
                Glide.with(context)
                        .load(R.drawable.ic_photopicker_camera)
                        .bitmapTransform(RoundedCornersTransformation(context, 6.dp2px(context), 0))
                        .into(image)
                holder.itemView.setOnClickListener { onTakePhotoClick() }
            }
            1 -> {
                Glide.with(context)
                        .load(R.drawable.ic_photopicker_albums)
                        .bitmapTransform(RoundedCornersTransformation(context, 6.dp2px(context), 0))
                        .into(image)
                holder.itemView.setOnClickListener { onOpenGalleryClick() }
            }
            else -> {
                val file = data[position - 2]
                Glide.with(context)
                        .load(file)
                        .placeholder(placeholder)
                        .bitmapTransform(CenterCrop(context), RoundedCornersTransformation(context, 6.dp2px(context), 0))
                        .into(image)

                if(file == selectedFile) image.foreground = ContextCompat.getDrawable(context, R.drawable.drawable_list_selection)
                else image.foreground = transparentDrawable

                holder.itemView.setOnClickListener {
                    onItemClick(file)
                    setSelectedFile(file)
                }
            }
        }
    }

    fun setSelectedFile(file: File) {
        selectedFile = file
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return if(position > 1) data[position-2].hashCode().toLong()
        else position.toLong()
    }

    override fun setNewData(data: List<File>) {
        SwitchLog.scream("${data.size}")
        this.data = data
        notifyDataSetChanged()
    }
}
