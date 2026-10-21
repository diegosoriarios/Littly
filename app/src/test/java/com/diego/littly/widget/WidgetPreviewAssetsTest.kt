package com.diego.littly.widget

import android.content.Context
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import com.diego.littly.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetPreviewAssetsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun previewLayoutsInflate() {
        val inflater = LayoutInflater.from(context)
        assertNotNull(inflater.inflate(R.layout.widget_preview, null))
        assertNotNull(inflater.inflate(R.layout.widget_streak_preview, null))
        assertNotNull(inflater.inflate(R.layout.widget_idea_preview, null))
    }

    @Test
    fun previewImagesAreAvailable() {
        listOf(
            R.drawable.widget_preview_overview,
            R.drawable.widget_preview_streak,
            R.drawable.widget_preview_idea
        ).forEach { drawableRes ->
            val drawable = context.getDrawable(drawableRes)
            assertNotNull(drawable)
            assertTrue(drawable!!.intrinsicWidth > 0)
            assertTrue(drawable.intrinsicHeight > 0)
        }
    }
}
