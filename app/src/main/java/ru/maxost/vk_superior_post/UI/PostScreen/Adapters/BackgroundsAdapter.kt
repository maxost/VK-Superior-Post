package ru.maxost.vk_superior_post.UI.PostScreen.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.maxost.vk_superior_post.Model.Background
import ru.maxost.vk_superior_post.Model.BackgroundType
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UIUtils.GlideDrawableViewBackgroundTarget
import ru.maxost.vk_superior_post.UI.UIUtils.RoundedCornersTransformation
import ru.maxost.vk_superior_post.UI.UIUtils.dp2px

/**
 * Created by Maksim Ostrovidov on 08.09.17.
 * dustlooped@yandex.ru
 */

interface BackgroundsAdapter {
    fun setSelectedItem(background: Background?)
}

inline fun backgroundsAdapter(
        context: Context,
        crossinline onItemClick: (Background) -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>
        = object : RecyclerView.Adapter<RecyclerView.ViewHolder>(), BackgroundsAdapter {

    init { setHasStableIds(true) }

    private var data = listOf(
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_white_full),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_blue_full),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_green_full),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_orange_full),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_red_full),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_violet_full),
            Background(type = BackgroundType.BEACH),
            Background(type = BackgroundType.STARS),
            Background(type = BackgroundType.IMAGE)
    )
    private var selectedItem: Background? = null
    private var recyclerView: RecyclerView? = null

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int) = data[position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = object : RecyclerView.ViewHolder
    (LayoutInflater.from(parent.context).inflate(R.layout.background_list_item, parent, false)) {}

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image: ImageView = holder.itemView.findViewById(R.id.background_list_item_image)
        val item = data[position]
        when(item.type) {
            BackgroundType.COLORED -> {
                val resource = when(item.colorDrawableResId!!) {
                    R.drawable.background_white_full -> R.drawable.background_white
                    R.drawable.background_blue_full -> R.drawable.background_blue
                    R.drawable.background_green_full -> R.drawable.background_green
                    R.drawable.background_orange_full -> R.drawable.background_orange
                    R.drawable.background_red_full -> R.drawable.background_red
                    R.drawable.background_violet_full -> R.drawable.background_violet
                    else -> throw IllegalArgumentException("unknown resId")
                }
                image.setBackgroundResource(resource)
            }
            BackgroundType.BEACH -> {
                Glide.with(context)
                        .load(R.drawable.thumb_beach)
                        .bitmapTransform(RoundedCornersTransformation(context, 4.dp2px(context), 0))
                        .into(GlideDrawableViewBackgroundTarget(image))
            }
            BackgroundType.STARS -> {
                Glide.with(context)
                        .load(R.drawable.thumb_stars)
                        .bitmapTransform(RoundedCornersTransformation(context, 4.dp2px(context), 0))
                        .into(GlideDrawableViewBackgroundTarget(image))
            }
            BackgroundType.IMAGE -> {
                Glide.with(context)
                        .load(R.drawable.thumb_add)
                        .bitmapTransform(RoundedCornersTransformation(context, 4.dp2px(context), 0))
                        .into(GlideDrawableViewBackgroundTarget(image))
            }
        }

        if(item == selectedItem ||
                item.type == BackgroundType.IMAGE && selectedItem?.type == BackgroundType.IMAGE) {
            image.setImageResource(R.drawable.drawable_list_selection)
        } else {
            image.setImageResource(android.R.color.transparent)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun setSelectedItem(background: Background?) {
        selectedItem = background
        selectedItem?.let {
            if(it.type == BackgroundType.IMAGE) recyclerView?.smoothScrollToPosition(data.size - 1)
            else recyclerView?.smoothScrollToPosition(data.indexOf(it))
        }
        notifyDataSetChanged()
    }
}