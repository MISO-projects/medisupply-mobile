package com.medisupply.monkeyTesting

import android.app.Instrumentation
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.medisupply.ui.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class MonkeyTest {

    private lateinit var device: UiDevice
    private var pathScreenshots = "/sdcard/Pictures"

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun runRandomTapTest() {
        prepareFolderForScreenshots()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        val screenWidth = instrumentation.targetContext.resources.displayMetrics.widthPixels
        val screenHeight = instrumentation.targetContext.resources.displayMetrics.heightPixels

        val numberOfActions = 500
        val random = Random(System.currentTimeMillis())

        var x = 0
        var y = 0

        for (i in 0..numberOfActions) {
            val chance = Random.nextDouble()
            try {
                if (chance <= 0.05 && !isOnHomeScreen()) {
                    device.pressBack()
                } else {
                    x = random.nextInt(screenWidth)
                    y = random.nextInt(screenHeight)
                    executeTap(instrumentation, x, y)
                }

                Thread.sleep(300)
            } catch (e: Exception) {
                Log.e("MonkeyTest", "($i:) Error tapping ($x, $y) on screen: $e")
                captureScreenshot(context, "error_tap_$i")
                e.printStackTrace()
            }
        }
    }

    private fun prepareFolderForScreenshots() {
        val screenshotsDir = File(pathScreenshots, "screenshots")
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs()
        } else {
            screenshotsDir.listFiles()?.forEach { file -> file.delete() }
        }
    }

    private fun isOnHomeScreen(): Boolean {
        val greetingItem = device.findObject(UiSelector().resourceId("com.medisupply:id/greeting"))
        return greetingItem.exists()
    }

    private fun executeTap(instrumentation: Instrumentation, x: Int, y: Int) {
        if (!device.findObject(UiSelector().packageName("com.medisupply")).exists()) {
            Log.e("MonkeyTest", "Activity is not in the foreground, skipping tap at ($x, $y)")
            return
        }

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        val eventDown = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            x.toFloat(),
            y.toFloat(),
            0
        )

        val eventUp = MotionEvent.obtain(
            downTime,
            eventTime + 100,
            MotionEvent.ACTION_UP,
            x.toFloat(),
            y.toFloat(),
            0
        )

        instrumentation.sendPointerSync(eventDown)
        instrumentation.sendPointerSync(eventUp)

        eventDown.recycle()
        eventUp.recycle()
    }

    private fun captureScreenshot(context: Context, fileName: String) {
        val screenshotsDir = File(pathScreenshots, "screenshots")
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs()
        }

        val screenshotFile = File(screenshotsDir, "$fileName.png")

        if (device.takeScreenshot(screenshotFile)) {
            Log.d("MonkeyTest", "Screenshot captured: ${screenshotFile.absolutePath}")
        } else {
            Log.e("MonkeyTest", "Failed to capture screenshot")
        }
    }
}
