/*
 * GoToInputRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.*

class GoToSuggestionAdapter(context: Context, textViewResourceId: Int, private val appCore: AppCore) : ArrayAdapter<String>(context, textViewResourceId), Filterable {
    private var resultList: List<String> = arrayListOf()

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(position: Int): String {
        return resultList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    val results = appCore.simulation.completionForText(constraint.toString(), 10)

                    // Assign the data to the FilterResults
                    filterResults.values = results
                    filterResults.count = results.size
                } else {
                    filterResults.values = listOf<String>()
                    filterResults.count = 0
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                resultList = results?.values as? List<String> ?: listOf()
                if (resultList.size > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
