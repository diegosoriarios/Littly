package com.diego.littly.core.data.settings

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.diego.littly.testing.FixedTimeProvider
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataStoreSettingsRepositoryTest {

    private lateinit var scope: CoroutineScope
    private lateinit var repository: DataStoreSettingsRepository

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "test-settings-${UUID.randomUUID()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
        repository = DataStoreSettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun defaults_areSensible() = runBlocking {
        assertEquals(false, repository.onboardingComplete.first())
        assertEquals(true, repository.ideaEngineEnabled.first())
        assertEquals(true, repository.wifiOnlyDownload.first())
        assertEquals(false, repository.dynamicColorEnabled.first())
        assertEquals(true, repository.remindersMasterEnabled.first())
    }

    @Test
    fun setOnboardingComplete_persists() = runBlocking {
        repository.setOnboardingComplete()
        assertEquals(true, repository.onboardingComplete.first())
    }

    @Test
    fun setters_updateFlows() = runBlocking {
        repository.setIdeaEngineEnabled(false)
        repository.setWifiOnlyDownload(false)
        repository.setDynamicColorEnabled(true)
        repository.setRemindersMasterEnabled(false)

        assertEquals(false, repository.ideaEngineEnabled.first())
        assertEquals(false, repository.wifiOnlyDownload.first())
        assertEquals(true, repository.dynamicColorEnabled.first())
        assertEquals(false, repository.remindersMasterEnabled.first())
    }
}
