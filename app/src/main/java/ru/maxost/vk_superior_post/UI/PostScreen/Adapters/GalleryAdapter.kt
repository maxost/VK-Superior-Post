package ru.maxost.vk_superior_post.UI.PostScreen.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UIUtils.GlideDrawableViewBackgroundTarget
import ru.maxost.vk_superior_post.UI.UIUtils.RoundedCornersTransformation
import ru.maxost.vk_superior_post.UI.UIUtils.dp2px
import java.io.File

/**
 * Created by Maksim Ostrovidov on 09.09.17.
 * dustlooped@yandex.ru
 */
interface GalleryAdapter {
    fun setNewData(data: List<File>)
    fun setSelectedFile(file: File?)
    fun getItemsCount(): Int
}

const val OPTION_ITEM = 345
const val IMAGE_ITEM = 234

inline fun galleryAdapter(context: Context,
                          crossinline onItemClick: (File) -> Unit,
                          crossinline onTakePhotoClick: () -> Unit,
                          crossinline onOpenGalleryClick: () -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>
        = object : RecyclerView.Adapter<RecyclerView.ViewHolder>(), GalleryAdapter {

    init { setHasStableIds(true) }

    private var data = listOf<File>()
    private var selectedFile: File? = null
    private var recyclerView: RecyclerView? = null

    override fun getItemCount(): Int = data.size + 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OPTION_ITEM -> {
                object : RecyclerView.ViewHolder
                (LayoutInflater.from(parent.context).inflate(R.layout.gallery_option_item, parent, false)) {}
            }
            IMAGE_ITEM -> {
                object : RecyclerView.ViewHolder
                (LayoutInflater.from(parent.context).inflate(R.layout.gallery_list_item, parent, false)) {}
            }
            else -> {
                throw IllegalArgumentException("unknown viewType")
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemId(position: Int): Long {
        return if (position > 1) data[position - 2].hashCode().toLong()
        else position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 1) IMAGE_ITEM else OPTION_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            OPTION_ITEM -> {
                val image = holder.itemView.findViewById<ImageView>(R.id.gallery_option_item_icon)
                when (position) {
                    0 -> {
                        Glide.with(context)
                                .load(R.drawable.ic_photopicker_camera)
                                .into(image)
                        holder.itemView.setOnClickListener { onTakePhotoClick() }
                    }
                    1 -> {
                        Glide.with(context)
                                .load(R.drawable.ic_photopicker_albums)
                                .into(image)
                        holder.itemView.setOnClickListener { onOpenGalleryClick() }
                    }
                }
            }
            IMAGE_ITEM -> {
                val image = holder.itemView as ImageView
                val file = data[position - 2]
                Glide.with(context)
                        .load(file)
                        .placeholder(R.drawable.drawable_placeholder)
                        .bitmapTransform(CenterCrop(context), RoundedCornersTransformation(context, 4.dp2px(context), 0))
                        .into(GlideDrawableViewBackgroundTarget(image))

                if (file == selectedFile) image.setImageResource(R.drawable.drawable_list_selection)
                else image.setImageResource(android.R.color.transparent)

                holder.itemView.setOnClickListener {
                    onItemClick(file)
                }
            }
        }
    }

    override fun setSelectedFile(file: File?) {
        selectedFile = file
        notifyDataSetChanged()
        file?.let {
            recyclerView?.smoothScrollToPosition(data.indexOf(it) + 2)
        }
    }

    override fun setNewData(data: List<File>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getItemsCount() = data.size + 2
}
