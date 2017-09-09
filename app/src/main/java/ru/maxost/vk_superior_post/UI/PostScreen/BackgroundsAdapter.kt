package ru.maxost.vk_superior_post.UI.PostScreen

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.maxost.vk_superior_post.Model.Background
import ru.maxost.vk_superior_post.Model.BackgroundType
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.Utils.*

/**
 * Created by Maksim Ostrovidov on 08.09.17.
 * dustlooped@yandex.ru
 */

inline fun backgroundsAdapter(
        context: Context,
        crossinline onItemClick: (Background) -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>
        = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init { setHasStableIds(true) }

    private var data = listOf(
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_white),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_blue),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_green),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_orange),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_red),
            Background(type = BackgroundType.COLORED, colorDrawableResId = R.drawable.background_violet),
            Background(type = BackgroundType.BEACH),
            Background(type = BackgroundType.STARS),
            Background(type = BackgroundType.IMAGE)
    )
    private var selectedItem: Background = Background()

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int) = data[position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = object : RecyclerView.ViewHolder
    (LayoutInflater.from(parent.context).inflate(R.layout.background_list_item, parent, false)) {}

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image: ImageView = holder.itemView.findViewById(R.id.background_list_item_image)
        val item = data[position]
        when(item.type) {
            BackgroundType.COLORED -> {
                image.setPadding(0, 0, 0, 0)
                image.setBackgroundResource(0)
                image.setImageResource(item.colorDrawableResId!!)
            }
            BackgroundType.BEACH -> {
                image.setPadding(0, 0, 0, 0)
                image.setBackgroundResource(0)
                Glide.with(context)
                        .load(R.drawable.thumb_beach)
                        .bitmapTransform(RoundedCornersTransformation(context, 4.dp2px(context), 0))
                        .into(image)
            }
            BackgroundType.STARS -> {
                image.setPadding(0, 0, 0, 0)
                image.setBackgroundResource(0)
                Glide.with(context)
                        .load(R.drawable.thumb_stars)
                        .bitmapTransform(RoundedCornersTransformation(context, 4.dp2px(context), 0))
                        .into(image)
            }
            BackgroundType.IMAGE -> {
                image.setPadding(4.dp2px(context), 4.dp2px(context), 4.dp2px(context), 4.dp2px(context))
                image.setBackgroundResource(R.color.cornFlowerBlueTwoTransparent)
                Glide.with(context)
                        .load(R.drawable.ic_toolbar_new)
                        .bitmapTransform(RoundedCornersTransformation(context, 4.dp2px(context), 0))
                        .into(image)
            }
        }

        if(item == selectedItem) image.foreground = ContextCompat.getDrawable(context, R.drawable.drawable_list_selection)
        else image.foreground = ColorDrawable(Color.TRANSPARENT)

        holder.itemView.setOnClickListener {
            onItemClick(item)
            setSelectedItem(item)
        }
    }

    fun setSelectedItem(background: Background) {
        selectedItem = background
        notifyDataSetChanged() //TODO optimize
    }
}