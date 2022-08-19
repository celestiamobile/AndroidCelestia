/*
 * LPLinkView.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.ui.linkpreview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.card.MaterialCardView

class LPLinkView(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {
    private var titleLabelSize = context.resources.getDimension(R.dimen.LPLinkView_titleTextDefaultSize)
    private var footerLabelSize = context.resources.getDimension(R.dimen.LPLinkView_footnoteTextDefaultSize)
    private var titleLabelColor: ColorStateList?
    private var footerLabelColor: ColorStateList?
    private var textContentSpacing = context.resources.getDimensionPixelSize(R.dimen.LPLinkView_textContentDefaultSpacing)
    private var textContentVerticalPadding = context.resources.getDimensionPixelSize(R.dimen.LPLinkView_textContentDefaultVerticalPadding)
    private var textContentHorizontalPadding = context.resources.getDimensionPixelSize(R.dimen.LPLinkView_textContentDefaultHorizontalPadding)
    private var favIconPadding = context.resources.getDimensionPixelSize(R.dimen.LPLinkView_favIconDefaultPadding)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LPLinkView, 0, 0)
        titleLabelSize = typedArray.getDimension(R.styleable.LPLinkView_titleTextSize, titleLabelSize)
        footerLabelSize = typedArray.getDimension(R.styleable.LPLinkView_footnoteTextSize, footerLabelSize)
        titleLabelColor = typedArray.getColorStateList(R.styleable.LPLinkView_titleTextColor) ?: ResourcesCompat.getColorStateList(resources, R.color.colorLabel, null)
        footerLabelColor = typedArray.getColorStateList(R.styleable.LPLinkView_footnoteTextColor) ?: ResourcesCompat.getColorStateList(resources, R.color.colorSecondaryLabel, null)
        textContentSpacing = typedArray.getDimensionPixelSize(R.styleable.LPLinkView_textContentSpacing, textContentSpacing)
        textContentVerticalPadding = typedArray.getDimensionPixelSize(R.styleable.LPLinkView_textContentPaddingVertical, textContentVerticalPadding)
        textContentHorizontalPadding = typedArray.getDimensionPixelSize(R.styleable.LPLinkView_textContentPaddingHorizontal, textContentHorizontalPadding)
        favIconPadding = typedArray.getDimensionPixelOffset(R.styleable.LPLinkView_favIconPadding, favIconPadding)
        typedArray.recycle()
    }

    var linkData: LPLinkViewData? = null
        set(value) {
            field = value
            reload()
        }

    private fun reload() {
        removeAllViews()

        val metaData = linkData ?: return

        val container = LinearLayout(context)
        addView(container)

        val containerParams = container.layoutParams
        containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        val textContentView = LinearLayout(context)
        textContentView.orientation = LinearLayout.VERTICAL
        val titleView = TextView(context)
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleLabelSize)
        titleView.setTextColor(titleLabelColor)
        titleView.text = metaData.title
        titleView.textAlignment = TEXT_ALIGNMENT_VIEW_START
        titleView.setBackgroundColor(Color.TRANSPARENT)
        val urlView = TextView(context)
        urlView.text = metaData.url.host
        urlView.setTextSize(TypedValue.COMPLEX_UNIT_PX, footerLabelSize)
        urlView.setTextColor(footerLabelColor)
        urlView.textAlignment = TEXT_ALIGNMENT_VIEW_START
        urlView.setBackgroundColor(Color.TRANSPARENT)

        (urlView.layoutParams as? MarginLayoutParams)?.topMargin = textContentSpacing

        textContentView.addView(titleView)
        textContentView.addView(urlView)
        textContentView.setPadding(textContentHorizontalPadding, textContentVerticalPadding, textContentHorizontalPadding, textContentVerticalPadding)

        if (metaData.usesIcon) {
            container.orientation = LinearLayout.HORIZONTAL

            val iconView = ImageView(context)
            val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            iconView.setPadding(favIconPadding, if (isRTL) 0 else favIconPadding, if (isRTL) favIconPadding else 0, favIconPadding)
            container.addView(iconView)

            val params = iconView.layoutParams
            params.height = LayoutParams.MATCH_PARENT

            container.addView(textContentView)
            val textViewParams = textContentView.layoutParams as LinearLayout.LayoutParams
            textViewParams.width = 0
            textViewParams.height = LayoutParams.MATCH_PARENT
            textViewParams.weight = 1f

            iconView.setImageBitmap(metaData.image)
        } else {
            container.orientation = LinearLayout.VERTICAL

            val imageView = ImageView(context)
            imageView.adjustViewBounds = true
            container.addView(imageView)

            val params = imageView.layoutParams
            params.width = LayoutParams.MATCH_PARENT

            container.addView(textContentView)
            val textViewParams = textContentView.layoutParams as LinearLayout.LayoutParams
            textViewParams.width = LayoutParams.MATCH_PARENT

            imageView.setImageBitmap(metaData.image)
        }
    }
}