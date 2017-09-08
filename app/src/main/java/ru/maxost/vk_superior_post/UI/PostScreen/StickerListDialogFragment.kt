package ru.maxost.vk_superior_post.UI.PostScreen

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide

import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.Utils.dp2px
import ru.maxost.vk_superior_post.Utils.px2dp

class StickerListDialogFragment : BottomSheetDialogFragment() {

    interface Listener {
        fun onStickerClicked(stickerId: Int)
    }

    private var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_sticker_list_dialog, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val recyclerView = view!!.findViewById<RecyclerView>(R.id.fragment_sticker_list_dialog_recyclerview)
        recyclerView!!.layoutManager = GridLayoutManager(context, calculateGridColumns())
        recyclerView.adapter = StickerAdapter(arguments.getInt(ARG_ITEM_COUNT))
        recyclerView.addItemDecoration(object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                super.getItemOffsets(outRect, view, parent, state)

                val position = parent!!.getChildLayoutPosition(view)
                if(position > 4 || position < 19) {
                    outRect!!.bottom = 8.dp2px(context)
                }

                //TODO shift left & right edge stickers to edges
            }
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val parent = parentFragment
        if (parent != null) {
            listener = parent as Listener?
        } else {
            listener = context as Listener?
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun calculateGridColumns(): Int {
        val scalingFactor = 80 //width of sticker with margins in dp
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels.px2dp(context) - 24 // - paddings
        return dpWidth / scalingFactor
    }

    private inner class ViewHolder internal constructor(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_sticker_list_dialog_item, parent, false)) {

        internal val image: ImageView =
                itemView.findViewById(R.id.fragment_sticker_list_dialog_item_image)

        init {
            image.setOnClickListener {
                if (listener != null) {
                    listener!!.onStickerClicked(itemView.tag as Int)
                    dismiss()
                }
            }
        }
    }

    private inner class StickerAdapter internal constructor(private val mItemCount: Int) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageResId = resources.getIdentifier(
                    "sticker_fish_${position + 1}",
                    "drawable",
                    context.packageName)

            Glide.with(context)
                    .load(imageResId)
                    .into(holder.image)

            holder.itemView.tag = imageResId
        }

        override fun getItemCount() = mItemCount
    }

    companion object {
        private val ARG_ITEM_COUNT = "ARG_ITEM_COUNT"

        fun newInstance(itemCount: Int): StickerListDialogFragment {
            val fragment = StickerListDialogFragment()
            val args = Bundle()
            args.putInt(ARG_ITEM_COUNT, itemCount)
            fragment.arguments = args
            return fragment
        }
    }
}
