package com.simplevideo.whiteiptv.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsPreferencesTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var settingsPreferences: SettingsPreferences

    @BeforeTest
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "test_settings_${kotlin.random.Random.nextInt()}.preferences_pb".toPath() },
        )
        settingsPreferences = SettingsPreferences(dataStore)
    }

    // --- Accent Color ---

    @Test
    fun `default accent color is Teal`() = runTest {
        assertEquals(AccentColor.Teal, settingsPreferences.getAccentColor())
    }

    @Test
    fun `setAccentColor Blue persists and reads back`() = runTest {
        settingsPreferences.setAccentColor(AccentColor.Blue)
        assertEquals(AccentColor.Blue, settingsPreferences.getAccentColor())
    }

    @Test
    fun `setAccentColor Red persists and reads back`() = runTest {
        settingsPreferences.setAccentColor(AccentColor.Red)
        assertEquals(AccentColor.Red, settingsPreferences.getAccentColor())
    }

    // --- Channel View Mode ---

    @Test
    fun `default channel view mode is List`() = runTest {
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
    }

    @Test
    fun `setChannelViewMode Grid persists and reads back`() = runTest {
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        assertEquals(ChannelViewMode.Grid, settingsPreferences.getChannelViewMode())
    }

    @Test
    fun `setChannelViewMode List persists and reads back`() = runTest {
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setChannelViewMode(ChannelViewMode.List)
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
    }

    // --- Auto Update ---

    @Test
    fun `default auto update is false`() = runTest {
        assertFalse(settingsPreferences.getAutoUpdateEnabled())
    }

    @Test
    fun `setAutoUpdateEnabled true persists and reads back`() = runTest {
        settingsPreferences.setAutoUpdateEnabled(true)
        assertTrue(settingsPreferences.getAutoUpdateEnabled())
    }

    @Test
    fun `setAutoUpdateEnabled false after true persists and reads back`() = runTest {
        settingsPreferences.setAutoUpdateEnabled(true)
        settingsPreferences.setAutoUpdateEnabled(false)
        assertFalse(settingsPreferences.getAutoUpdateEnabled())
    }

    // --- Reset All ---

    @Test
    fun `resetAll clears all settings to defaults`() = runTest {
        settingsPreferences.setAccentColor(AccentColor.Red)
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setAutoUpdateEnabled(true)

        settingsPreferences.resetAll()

        assertEquals(AccentColor.Teal, settingsPreferences.getAccentColor())
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
        assertFalse(settingsPreferences.getAutoUpdateEnabled())
    }

    // --- Auto Update Flow ---

    @Test
    fun `autoUpdateEnabledFlow initial value is false by default`() = runTest {
        assertFalse(settingsPreferences.autoUpdateEnabledFlow.first())
    }

    @Test
    fun `autoUpdateEnabledFlow emits true when setAutoUpdateEnabled true`() = runTest {
        settingsPreferences.setAutoUpdateEnabled(true)
        assertTrue(settingsPreferences.autoUpdateEnabledFlow.first())
    }

    @Test
    fun `autoUpdateEnabledFlow emits false when toggled off`() = runTest {
        settingsPreferences.setAutoUpdateEnabled(true)
        settingsPreferences.setAutoUpdateEnabled(false)
        assertFalse(settingsPreferences.autoUpdateEnabledFlow.first())
    }

    @Test
    fun `autoUpdateEnabledFlow resets to false on resetAll`() = runTest {
        settingsPreferences.setAutoUpdateEnabled(true)
        assertTrue(settingsPreferences.autoUpdateEnabledFlow.first())

        settingsPreferences.resetAll()
        assertFalse(settingsPreferences.autoUpdateEnabledFlow.first())
    }

    // --- Accent Color Flow ---

    @Test
    fun `accentColorFlow initial value is Teal by default`() = runTest {
        assertEquals(AccentColor.Teal, settingsPreferences.accentColorFlow.first())
    }

    @Test
    fun `accentColorFlow emits Blue when setAccentColor Blue`() = runTest {
        settingsPreferences.setAccentColor(AccentColor.Blue)
        assertEquals(AccentColor.Blue, settingsPreferences.accentColorFlow.first())
    }

    @Test
    fun `accentColorFlow emits Red when setAccentColor Red`() = runTest {
        settingsPreferences.setAccentColor(AccentColor.Red)
        assertEquals(AccentColor.Red, settingsPreferences.accentColorFlow.first())
    }

    @Test
    fun `accentColorFlow resets to Teal on resetAll`() = runTest {
        settingsPreferences.setAccentColor(AccentColor.Blue)
        assertEquals(AccentColor.Blue, settingsPreferences.accentColorFlow.first())

        settingsPreferences.resetAll()
        assertEquals(AccentColor.Teal, settingsPreferences.accentColorFlow.first())
    }

    // --- Channel View Mode Flow ---

    @Test
    fun `channelViewModeFlow initial value is List by default`() = runTest {
        assertEquals(ChannelViewMode.List, settingsPreferences.channelViewModeFlow.first())
    }

    @Test
    fun `channelViewModeFlow emits Grid when setChannelViewMode Grid`() = runTest {
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        assertEquals(ChannelViewMode.Grid, settingsPreferences.channelViewModeFlow.first())
    }

    @Test
    fun `channelViewModeFlow emits List when toggled back to List`() = runTest {
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setChannelViewMode(ChannelViewMode.List)
        assertEquals(ChannelViewMode.List, settingsPreferences.channelViewModeFlow.first())
    }

    @Test
    fun `channelViewModeFlow resets to List on resetAll`() = runTest {
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        assertEquals(ChannelViewMode.Grid, settingsPreferences.channelViewModeFlow.first())

        settingsPreferences.resetAll()
        assertEquals(ChannelViewMode.List, settingsPreferences.channelViewModeFlow.first())
    }

    // --- Persistence across instances ---

    @Test
    fun `new SettingsPreferences instance reads previously persisted values`() = runTest {
        settingsPreferences.setAccentColor(AccentColor.Blue)
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setAutoUpdateEnabled(true)

        val newPreferences = SettingsPreferences(dataStore)
        assertEquals(AccentColor.Blue, newPreferences.getAccentColor())
        assertEquals(ChannelViewMode.Grid, newPreferences.getChannelViewMode())
        assertTrue(newPreferences.getAutoUpdateEnabled())
    }
}
