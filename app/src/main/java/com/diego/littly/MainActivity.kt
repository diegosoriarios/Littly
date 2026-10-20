package com.diego.littly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.diego.littly.core.designsystem.theme.LittlyTheme
import com.diego.littly.core.domain.usecase.IdeaQueueFiller
import com.diego.littly.core.llm.ModelLocator
import com.diego.littly.core.navigation.DeepLink
import com.diego.littly.core.navigation.DeepLinkState
import com.diego.littly.core.notifications.ReminderPlanner
import com.diego.littly.navigation.LittlyAppRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var reminderPlanner: ReminderPlanner
    @Inject lateinit var deepLinkState: DeepLinkState
    @Inject lateinit var ideaQueueFiller: IdeaQueueFiller
    @Inject lateinit var modelLocator: ModelLocator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        processIntent(intent)
        setContent {
            LittlyTheme {
                LittlyAppRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        val habitId = intent?.getLongExtra(EXTRA_HABIT_ID, -1L) ?: -1L
        if (habitId > 0) {
            val generateIdea = intent?.getBooleanExtra(EXTRA_GENERATE_IDEA, false) ?: false
            deepLinkState.set(DeepLink.HabitDetail(habitId, generateIdea))
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            reminderPlanner.rescheduleAll()
        }
        lifecycleScope.launch {
            if (modelLocator.isModelPresent()) {
                ideaQueueFiller.fillAllQueues()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_GENERATE_IDEA = "extra_generate_idea"
    }
}
