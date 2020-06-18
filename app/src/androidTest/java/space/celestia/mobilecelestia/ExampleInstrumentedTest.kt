// ExampleInstrumentedTest.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.takeScreenshot
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Semaphore

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    class TestItem(val celURL: String, val addonID: String?, val showInfo: Boolean)

    private var firstTest = true

    @get:Rule
    val rule = activityScenarioRule<MainActivity>()

    val itemsToTest: List<TestItem>
        get() = listOf(
            TestItem("cel://Follow/Sol:Earth/2023-07-02T13:52:59.06554Z?x=gHqIoxcYrv7//////////w&y=JAcLdlkIUQ&z=ySumWO0O/v///////////w&ow=-0.73719114&ox=-0.26776052&oy=-0.6141897&oz=0.087318935&select=Sol:Earth&fov=15.534162&ts=1&ltd=0&p=1&rf=71227287&nrf=255&lm=2048&tsrc=0&ver=3", null, true),
            TestItem("cel://Follow/Sol/2023-06-10T04:01:09.36594Z?x=AADgxNkenTx7Ag&y=AAAAeu5N7SvJAw&z=AABAbpqtfATN+v///////w&ow=-0.1762914&ox=0.039147615&oy=0.9339327&oz=0.30847773&fov=15.497456&ts=1&ltd=0&p=0&rf=71227315&nrf=255&lm=6147&tsrc=0&ver=3", null, false),
            TestItem("cel://SyncOrbit/Sol:Earth/2024-07-30T06:00:31.79943Z?x=5H8ym9O86f///////////w&y=Jx39zwAGIw&z=6bKOqSApDw&ow=0.88020444&ox=-0.3483816&oy=0.18829681&oz=-0.26156196&select=TYC%204123-1214-1&fov=51.175&ts=1&ltd=0&p=1&rf=71235487&nrf=255&lm=15&tsrc=0&ver=3", null, false),
            TestItem("cel://Follow/Westerhout%2051/2023-06-10T03:31:39.13768Z?x=AAAAAABxRUqY3KUS&y=AAAAAAA1xr8Nhgv2/////w&z=AAAAAAC8ueP73lXs/////w&ow=-0.4382953&ox=0.5837729&oy=0.678968&oz=0.07815942&fov=15.497456&ts=1&ltd=0&p=0&rf=71227287&nrf=255&lm=2048&tsrc=0&ver=3", null, false),
            TestItem("cel://Follow/Sol:Jupiter:Callisto/2023-07-02T11:03:12.01362Z?x=YJG63ya8Sf///////////w&y=k1RN8wszCw&z=ZY4WNWkjGQ&ow=0.7285262&ox=-0.043709736&oy=0.682417&oz=0.0405734&fov=5.30302&ts=10&ltd=0&p=1&rf=71227287&nrf=255&lm=2048&tsrc=0&ver=3", null, false),
            TestItem("cel://Follow/Cygnus%20X-1/2023-07-02T13:52:59.06554Z?x=ACwEriWpVvv//////////w&y=cKpIoLiUl////////////w&z=AOyX+4hMTQg&ow=0.10665737&ox=-0.3053027&oy=-0.0070079123&oz=0.94623744&select=HD%20226868%20A&fov=15.534161&ts=10&ltd=0&p=1&rf=71227287&nrf=255&lm=2048&tsrc=0&ver=3", "87D5FBAB-5722-70A9-6D4C-F4FD22EA87BC", false),
        )

    @Test
    fun testAll() {
        testItems()
    }

    private fun testItems() {
        for (i in 0 until itemsToTest.size) {
            screenshotItem(itemsToTest[i], "URL$i")
        }
    }

    private fun <T: Activity> tryAcquireScenarioActivity(activityScenario: ActivityScenario<T>): T {
        val activityResource = Semaphore(1)
        var acquiredActivity: T? = null
        activityScenario.onActivity { activity ->
            acquiredActivity = activity
            activityResource.release()
        }
        activityResource.acquire()
        return acquiredActivity!!
    }

    private fun screenshotItem(item: TestItem, name: String) {
        val activity = tryAcquireScenarioActivity(rule.scenario)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.celURL)).setPackage(context.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        activity.onNewIntent(intent)

        Thread.sleep(if (firstTest) 30000 else 5000)
        firstTest = false

        onView(withText("Open URL?")).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        Thread.sleep(5000)

        val addonID = item.addonID
        if (addonID != null) {
            val addonIntent = Intent(Intent.ACTION_VIEW, Uri.parse("celaddon://item?item=$addonID")).setPackage(context.packageName)
            addonIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.onNewIntent(addonIntent)
            Thread.sleep(5000)
        }

        if (item.showInfo) {
            onView(withContentDescription("Get Info")).perform(click())
            Thread.sleep(5000)
        }

        takeScreenshot().writeToTestStorage(name)

        if (item.showInfo || addonID != null) {
            onView(withContentDescription("Close")).perform(click())
        }

        Thread.sleep(1000)
    }
}
