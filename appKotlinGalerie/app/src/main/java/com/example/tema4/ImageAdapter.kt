package com.example.tema4

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class ImageAdapter(private val context: Context, private val imageResIds: List<Int>) : BaseAdapter() {

    override fun getCount(): Int = imageResIds.size

    override fun getItem(position: Int): Int = imageResIds[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = convertView as? ImageView ?: ImageView(context)
        imageView.layoutParams = ViewGroup.LayoutParams(250, 250)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageResource(imageResIds[position])
        return imageView
    }
}
