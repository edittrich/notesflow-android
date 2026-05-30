package de.edittrich

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.edittrich.data.ApiClient
import de.edittrich.data.SessionManager
import de.edittrich.ui.auth.LoginScreen
import de.edittrich.ui.auth.SignupScreen
import de.edittrich.ui.dashboard.DashboardScreen
import de.edittrich.ui.settings.SettingsScreen
import de.edittrich.ui.theme.NotesFlowTheme
import java.util.Locale

enum class Screen {
    LOGIN, SIGNUP, DASHBOARD, SETTINGS
}

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient

    override fun attachBaseContext(newBase: Context) {
        sessionManager = SessionManager(newBase)
        val lang = sessionManager.languagePreference
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        apiClient = ApiClient(this)

        setContent {
            // Manage theme state dynamically
            var themeState by remember { mutableStateOf(sessionManager.themePreference) }
            val darkTheme = when (themeState) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            // Manage navigation screen state
            var currentScreen by remember {
                mutableStateOf(
                    if (sessionManager.isLoggedIn) Screen.DASHBOARD else Screen.LOGIN
                )
            }

            NotesFlowTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { currentScreen = Screen.DASHBOARD },
                                onNavigateToSignup = { currentScreen = Screen.SIGNUP },
                                onLanguageChanged = { recreate() },
                                onThemeChanged = { themeState = it },
                                apiClient = apiClient,
                                sessionManager = sessionManager
                            )
                        }
                        Screen.SIGNUP -> {
                            SignupScreen(
                                onSignupSuccess = { autoLoggedIn ->
                                    if (autoLoggedIn) {
                                        currentScreen = Screen.DASHBOARD
                                    } else {
                                        currentScreen = Screen.LOGIN
                                    }
                                },
                                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                                onLanguageChanged = { recreate() },
                                onThemeChanged = { themeState = it },
                                apiClient = apiClient,
                                sessionManager = sessionManager
                            )
                        }
                        Screen.DASHBOARD -> {
                            DashboardScreen(
                                onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                                onLogout = { currentScreen = Screen.LOGIN },
                                apiClient = apiClient,
                                sessionManager = sessionManager
                            )
                        }
                        Screen.SETTINGS -> {
                            SettingsScreen(
                                onNavigateBack = { currentScreen = Screen.DASHBOARD },
                                onLanguageChanged = { recreate() },
                                onThemeChanged = { themeState = it },
                                sessionManager = sessionManager
                            )
                        }
                    }
                }
            }
        }
    }
}
