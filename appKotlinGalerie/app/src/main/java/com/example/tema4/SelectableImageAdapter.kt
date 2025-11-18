package com.example.tema4

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat

class SelectableImageAdapter(
    private val context: Context,
    private val imageIds: List<Int>,
    private val onSelectChanged: (Int, Boolean) -> Unit
) : BaseAdapter() {

    private val selectedPositions = mutableSetOf<Int>()

    override fun getCount(): Int = imageIds.size
    override fun getItem(position: Int): Any = imageIds[position]
    override fun getItemId(position: Int): Long = position.toLong()

    fun clearSelections() {
        selectedPositions.clear()
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = (convertView as? ImageView) ?: ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(300, 300)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(8, 8, 8, 8)
        }

        val imgId = imageIds[position]
        imageView.setImageDrawable(ContextCompat.getDrawable(context, imgId))

        val isSelected = selectedPositions.contains(position)
        imageView.alpha = if (isSelected) 0.5f else 1.0f

        imageView.setOnClickListener {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position)
                imageView.alpha = 1.0f
                onSelectChanged(position, false)
            } else {
                selectedPositions.add(position)
                imageView.alpha = 0.5f
                onSelectChanged(position, true)
            }
        }

        return imageView
    }
}