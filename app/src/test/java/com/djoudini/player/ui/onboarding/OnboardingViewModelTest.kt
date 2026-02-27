package com.djoudini.player.ui.onboarding

import android.content.Context
import androidx.work.WorkManager
import app.cash.turbine.test
import com.djoudini.player.data.local.PreferencesManager
import com.djoudini.player.data.remote.UserInfo
import com.djoudini.player.data.remote.XtreamApi
import com.djoudini.player.data.remote.XtreamAuthResponse
import com.djoudini.player.data.remote.XtreamCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Response

@ExperimentalCoroutinesApi
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnboardingViewModel
    private val xtreamApi: XtreamApi = mockk()
    private val preferencesManager: PreferencesManager = mockk(relaxUnitFun = true)
    private val context: Context = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        viewModel = OnboardingViewModel(preferencesManager, xtreamApi, context)
    }

    @Test
    fun `authenticate success - fetches categories and moves to next step`() = runTest {
        val server = "http://server.com"
        val user = "user"
        val pass = "pass"
        val authResponse = XtreamAuthResponse(userInfo = UserInfo("user", "1672527600", "1", "2"), serverInfo = null)
        val categories = listOf(XtreamCategory("1", "News", 0))

        coEvery { xtreamApi.authenticate(any()) } returns Response.success(authResponse)
        coEvery { xtreamApi.getLiveCategories(any()) } returns Response.success(categories)
        coEvery { xtreamApi.getVodCategories(any()) } returns Response.success(emptyList())
        coEvery { xtreamApi.getSeriesCategories(any()) } returns Response.success(emptyList())

        viewModel.authenticate(server, user, pass)

        viewModel.uiState.test {
            val successState = expectMostRecentItem()
            assertEquals(OnboardingStep.LIVE_CATEGORIES, successState.currentStep)
            assertFalse(successState.isLoading)
            assertNull(successState.error)
            assertEquals(1, successState.liveCategories.size)
            coVerify { preferencesManager.saveAccountInfo(any(), user, pass, any()) }
        }
    }

    @Test
    fun `authenticate failed - invalid credentials updates state with error`() = runTest {
        coEvery { xtreamApi.authenticate(any()) } returns Response.error(401, mockk(relaxed = true))

        viewModel.authenticate("server", "user", "pass")

        viewModel.uiState.test {
            val errorState = expectMostRecentItem()
            assertEquals(OnboardingStep.LOGIN, errorState.currentStep)
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.error)
            assertTrue(errorState.error!!.contains("Login failed"))
        }
    }

    @Test
    fun `authenticate success but invalid data - updates state with error`() = runTest {
        val authResponse = XtreamAuthResponse(userInfo = null, serverInfo = null)
        coEvery { xtreamApi.authenticate(any()) } returns Response.success(authResponse)

        viewModel.authenticate("server", "user", "pass")

        viewModel.uiState.test {
            val errorState = expectMostRecentItem()
            assertEquals(OnboardingStep.LOGIN, errorState.currentStep)
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.error)
            assertTrue(errorState.error!!.contains("invalid user data"))
        }
    }

    @Test
    fun `completeOnboarding - saves categories and enqueues worker`() = runTest {
        val live = listOf("1", "2")
        val vod = listOf("3")
        val series = emptyList<String>()

        viewModel.completeOnboarding(live, vod, series)

        coVerify { preferencesManager.saveCategorySelections(live, vod, series) }
        verify { workManager.enqueue(any<androidx.work.OneTimeWorkRequest>()) }
        coVerify { preferencesManager.setLoggedIn(true) }
    }
}

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
