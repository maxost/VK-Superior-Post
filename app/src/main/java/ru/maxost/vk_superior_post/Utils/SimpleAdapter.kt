package ru.maxost.vk_superior_post.Utils

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by Maksim Ostrovidov on 08.09.17.
 * dustlooped@yandex.ru
 */
interface MySimpleAdapter<in T> {
    fun setNewData(data: List<T>)
}

inline fun <T> simpleAdapter(
        itemLayout: Int,
        crossinline getId: (T) -> Long,
        crossinline bind: (holder: RecyclerView.ViewHolder, item: T) -> Unit,
        crossinline onItemClick: (T) -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>
        = object : RecyclerView.Adapter<RecyclerView.ViewHolder>(), MySimpleAdapter<T> {

    init { setHasStableIds(true) }

    private var data = listOf<T>()

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = object : RecyclerView.ViewHolder
    (LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)) {}

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        bind(holder!!, data[position])
        holder.itemView.tag = data[position]
        holder.itemView.setOnClickListener { onItemClick(it.tag as T) }
    }

    override fun getItemId(position: Int): Long = getId(data[position])

    override fun setNewData(data: List<T>) {
        this.data = data
        notifyDataSetChanged()
    }
}
