/*
 * ExampleInstrumentedTest.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.screenshot.Screenshot
import com.microsoft.appcenter.utils.InstrumentationRegistryHelper
import org.hamcrest.CoreMatchers.endsWith
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.io.File
import java.lang.Exception


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    val urlsToTest: List<String>
        get() = listOf(
            "cel://PhaseLock/Sol:Cassini/Sol:Saturn/2004-06-30T21:36:21.34826Z?x=ccpEjvb//////////////w&y=9+3vjvv//////////////w&z=6m32U////////////////w&ow=-0.5156361&ox=0.5204204&oy=-0.49717823&oz=-0.46486115&select=Sol:Cassini&fov=45.483105&ts=10&ltd=0&p=0&rf=203855107&nrf=255&lm=0&tsrc=0&ver=3",
            "cel://Follow/Sol:Jupiter/1997-11-11T02:57:38.68119Z?x=ADxHP+Q+dND//////////w&y=6OYY9EgpmQ&z=APip8LBZYt7//////////w&ow=0.4596731&ox=0.027018305&oy=0.8876392&oz=-0.008204532&select=Sol:Jupiter&fov=4.70441&ts=2&ltd=1&p=0&rf=134258071&nrf=255&lm=32768&tsrc=0&ver=3",
            "cel://Follow/NGC2237/2021-06-26T08:03:39.23761Z?x=AAAAAGDEJ4krmiAD&y=AAAAAAArivObqvYG&z=AAAAAAAxDtGUAQIV&ow=0.98484325&ox=0.1578347&oy=-0.07176972&oz=-0.0045886277&select=NGC2237&fov=29.498112&ts=1&ltd=0&p=0&rf=136615319&nrf=255&lm=16384&tsrc=0&ver=3",
        )

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun testAll() {
        testInfoView()
        testURLs()
    }

    fun testURLs() {
        for (i in 0 until urlsToTest.size) {
            screenshotURL(urlsToTest[i], "URL$i")
        }
    }

    fun testInfoView() {
        startActivity(null)

        onView(withContentDescription("Info")).perform(click())

        Thread.sleep(5000)

        val capture = Screenshot.capture()
        capture.setFormat(Bitmap.CompressFormat.PNG)
        capture.setName("Info")
        capture.process()

        Thread.sleep(1000)

        onView(withContentDescription("Close")).perform(click())

        Thread.sleep(1000)
    }

    fun startActivity(intent: Intent?) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activity = activityRule.activity
        if (activity != null) {
            context.startActivity(intent)
            Thread.sleep(1000)
        } else {
            activityRule.launchActivity(intent)
            Thread.sleep(50000)
        }
    }

    fun screenshotURL(url: String, name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse( url)).setPackage(context.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)

        onView(withText("Open URL?")).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withContentDescription("Hide")).perform(click())

        Thread.sleep(5000)

        val capture = Screenshot.capture()
        capture.setFormat(Bitmap.CompressFormat.PNG)
        capture.setName(name)
        capture.process()

        Thread.sleep(1000)
    }
}
