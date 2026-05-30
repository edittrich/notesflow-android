package de.edittrich.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.edittrich.R
import de.edittrich.data.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit,
    sessionManager: SessionManager
) {
    var selectedTheme by remember { mutableStateOf(sessionManager.themePreference) }
    var selectedLang by remember { mutableStateOf(sessionManager.languagePreference) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_theme_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Light Theme Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTheme = "LIGHT"
                                sessionManager.themePreference = "LIGHT"
                                onThemeChanged("LIGHT")
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedTheme == "LIGHT"),
                            onClick = {
                                selectedTheme = "LIGHT"
                                sessionManager.themePreference = "LIGHT"
                                onThemeChanged("LIGHT")
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_theme_light), color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Dark Theme Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTheme = "DARK"
                                sessionManager.themePreference = "DARK"
                                onThemeChanged("DARK")
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedTheme == "DARK"),
                            onClick = {
                                selectedTheme = "DARK"
                                sessionManager.themePreference = "DARK"
                                onThemeChanged("DARK")
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_theme_dark), color = MaterialTheme.colorScheme.onSurface)
                    }

                    // System Theme Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTheme = "SYSTEM"
                                sessionManager.themePreference = "SYSTEM"
                                onThemeChanged("SYSTEM")
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedTheme == "SYSTEM"),
                            onClick = {
                                selectedTheme = "SYSTEM"
                                sessionManager.themePreference = "SYSTEM"
                                onThemeChanged("SYSTEM")
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_theme_system), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Language Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_language_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // English
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLang = "en"
                                sessionManager.languagePreference = "en"
                                onLanguageChanged("en")
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedLang == "en"),
                            onClick = {
                                selectedLang = "en"
                                sessionManager.languagePreference = "en"
                                onLanguageChanged("en")
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_language_en), color = MaterialTheme.colorScheme.onSurface)
                    }

                    // German
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLang = "de"
                                sessionManager.languagePreference = "de"
                                onLanguageChanged("de")
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedLang == "de"),
                            onClick = {
                                selectedLang = "de"
                                sessionManager.languagePreference = "de"
                                onLanguageChanged("de")
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_language_de), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
