package ru.maxost.vk_superior_post.UI.UIUtils

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
fun TextView.onTextChanged(block: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            block(s.toString())
        }
    })
}