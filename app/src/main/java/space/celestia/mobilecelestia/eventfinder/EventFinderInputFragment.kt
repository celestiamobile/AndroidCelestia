/*
 * EventFinderInputFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.*
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.util.*

class EventFinderInputFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    private var startTime = Date(Date().time - DEFAULT_SEARCHING_INTERVAL)
    private var endTime = Date()
    private var objectName = AppCore.getLocalizedString("Earth", "celestia-data")

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())

    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var objectNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_finder_input, container, false)

        val startTimeCell = view.findViewById<View>(R.id.start_time_cell)
        startTimeCell.findViewById<TextView>(R.id.title).text = CelestiaString("Start Time", "")
        startTimeTextView = startTimeCell.findViewById(R.id.detail)
        startTimeTextView.visibility = View.VISIBLE

        val endTimeCell = view.findViewById<View>(R.id.end_time_cell)
        endTimeCell.findViewById<TextView>(R.id.title).text = CelestiaString("End Time", "")
        endTimeTextView = endTimeCell.findViewById(R.id.detail)
        endTimeTextView.visibility = View.VISIBLE

        val objectNameCell = view.findViewById<View>(R.id.object_name_cell)
        objectNameCell.findViewById<TextView>(R.id.title).text = CelestiaString("Object", "")
        objectNameTextView = objectNameCell.findViewById(R.id.detail)
        objectNameTextView.visibility = View.VISIBLE

        val findButton = view.findViewById<Button>(R.id.find_button)
        findButton.text = CelestiaString("Find", "")
        objectNameCell.isClickable = true
        startTimeCell.isClickable = true
        endTimeCell.isClickable = true

        val weakSelf = WeakReference(this)
        startTimeCell.setOnClickListener {
            val self = weakSelf.get() ?: return@setOnClickListener
            val ac = self.activity ?: return@setOnClickListener
            val format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss")
            ac.showDateInput(
                CelestiaString(
                    "Please enter the time in \"%s\" format.",
                    ""
                ).format(format), format
            ) { date ->
                if (date == null) {
                    ac.showAlert(CelestiaString("Unrecognized time string.", ""))
                    return@showDateInput
                }
                self.startTime = date
                self.reload()
            }
        }

        endTimeCell.setOnClickListener {
            val self = weakSelf.get() ?: return@setOnClickListener
            val ac = self.activity ?: return@setOnClickListener
            val format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss")
            ac.showDateInput(
                CelestiaString(
                    "Please enter the time in \"%s\" format.",
                    ""
                ).format(format), format
            ) { date ->
                if (date == null) {
                    ac.showAlert(CelestiaString("Unrecognized time string.", ""))
                    return@showDateInput
                }
                self.endTime = date
                self.reload()
            }
        }

        objectNameCell.setOnClickListener {
            val self = weakSelf.get() ?: return@setOnClickListener
            val ac = self.activity ?: return@setOnClickListener
            val objects = listOf(
                AppCore.getLocalizedString("Earth", "celestia-data"),
                AppCore.getLocalizedString("Jupiter", "celestia-data")
            )
            val other = CelestiaString("Other", "")
            ac.showOptions(
                CelestiaString("Please choose an object.", ""),
                (objects + other).toTypedArray()
            ) { index ->
                if (index >= objects.size) {
                    // User choose other, show text input for the object name
                    ac.showTextInput(
                        CelestiaString("Please enter an object name.", ""),
                        self.objectName
                    ) { objectName ->
                        self.objectName = objectName
                        self.reload()
                    }
                    return@showOptions
                }
                self.objectName = objects[index]
                self.reload()
            }
        }

        findButton.setOnClickListener {
            val self = weakSelf.get() ?: return@setOnClickListener

            self.listener?.onSearchForEvent(objectName, startTime, endTime)
        }

        reload()
        return view
    }

    private fun reload() {
        startTimeTextView.text = formatter.format(startTime)
        endTimeTextView.text = formatter.format(endTime)
        objectNameTextView.text = objectName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            startTime = savedInstanceState.getSerializableValue(START_TIME_TAG, Date::class.java) ?: startTime
            endTime = savedInstanceState.getSerializableValue(END_TIME_TAG, Date::class.java) ?: endTime
            objectName = savedInstanceState.getString(OBJECT_TAG, objectName)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Eclipse Finder", "")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(START_TIME_TAG, startTime)
        outState.putSerializable(END_TIME_TAG, endTime)
        outState.putString(OBJECT_TAG, objectName)

        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement EventFinderInputFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onSearchForEvent(objectName: String, startDate: Date, endDate: Date)
    }

    companion object {
        private const val START_TIME_TAG = "start_time"
        private const val END_TIME_TAG = "end_time"
        private const val OBJECT_TAG = "object"

        private const val DEFAULT_SEARCHING_INTERVAL: Long = 365L * 24 * 60 * 60 * 1000

        @JvmStatic
        fun newInstance() =
            EventFinderInputFragment()
    }
}
