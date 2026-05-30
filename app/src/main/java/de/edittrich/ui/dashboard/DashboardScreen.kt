package de.edittrich.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.edittrich.R
import de.edittrich.data.ApiClient
import de.edittrich.data.SessionManager
import de.edittrich.data.model.Note
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    apiClient: ApiClient,
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    var sortBy by remember { mutableStateOf("NEWEST_FIRST") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog States
    var showCreateDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteIdToDelete by remember { mutableStateOf<String?>(null) }
    
    var isDialogLoading by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    fun loadNotes() {
        isLoading = true
        isError = false
        coroutineScope.launch {
            try {
                notes = apiClient.getNotes(sortBy)
            } catch (e: Exception) {
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun handleCreateNote(title: String, description: String) {
        isDialogLoading = true
        coroutineScope.launch {
            try {
                apiClient.createNote(title, description)
                showCreateDialog = false
                loadNotes()
            } catch (e: Exception) {
                // handle error
            } finally {
                isDialogLoading = false
            }
        }
    }

    fun handleUpdateNote(title: String, description: String) {
        val note = noteToEdit ?: return
        isDialogLoading = true
        coroutineScope.launch {
            try {
                apiClient.updateNote(note.id, title, description)
                noteToEdit = null
                loadNotes()
            } catch (e: Exception) {
                // handle error
            } finally {
                isDialogLoading = false
            }
        }
    }

    fun handleDeleteNote() {
        val id = noteIdToDelete ?: return
        coroutineScope.launch {
            try {
                apiClient.deleteNote(id)
                noteIdToDelete = null
                loadNotes()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // Load initial notes
    LaunchedEffect(sortBy) {
        loadNotes()
    }

    // Filter notes locally
    val filteredNotes = remember(notes, searchQuery) {
        notes.filter { note ->
            val query = searchQuery.trim().lowercase()
            query.isEmpty() || 
            note.title.lowercase().contains(query) || 
            note.description.lowercase().contains(query)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings))
                    }
                    IconButton(
                        onClick = {
                            sessionManager.clearSession()
                            onLogout()
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = stringResource(R.string.nav_sign_out))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.dash_new_note_button))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toolbar Search and Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.dash_search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Sort Dropdown button
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.sort_by),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_newest_first)) },
                            onClick = {
                                sortBy = "NEWEST_FIRST"
                                showSortMenu = false
                            },
                            trailingIcon = { if (sortBy == "NEWEST_FIRST") Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_oldest_first)) },
                            onClick = {
                                sortBy = "OLDEST_FIRST"
                                showSortMenu = false
                            },
                            trailingIcon = { if (sortBy == "OLDEST_FIRST") Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_title_az)) },
                            onClick = {
                                sortBy = "TITLE_AZ"
                                showSortMenu = false
                            },
                            trailingIcon = { if (sortBy == "TITLE_AZ") Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_title_za)) },
                            onClick = {
                                sortBy = "TITLE_ZA"
                                showSortMenu = false
                            },
                            trailingIcon = { if (sortBy == "TITLE_ZA") Icon(Icons.Default.Check, null) }
                        )
                    }
                }
            }

            // Notes Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.dash_loading), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
                isError -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                stringResource(R.string.dash_error_loading),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(onClick = { loadNotes() }) {
                                Text(stringResource(R.string.dash_try_again))
                            }
                        }
                    }
                }
                filteredNotes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.dash_no_notes_title),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.dash_no_notes_desc),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { showCreateDialog = true }) {
                                Text(stringResource(R.string.dash_new_note_button))
                            }
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(filteredNotes, key = { it.id }) { note ->
                            NoteCardItem(
                                note = note,
                                onEditClick = { noteToEdit = note },
                                onDeleteClick = { noteIdToDelete = note.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Note Dialog
    if (showCreateDialog) {
        NoteDialog(
            isEditing = false,
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, desc -> handleCreateNote(title, desc) },
            isLoading = isDialogLoading
        )
    }

    // Edit Note Dialog
    if (noteToEdit != null) {
        NoteDialog(
            initialTitle = noteToEdit!!.title,
            initialDescription = noteToEdit!!.description,
            isEditing = true,
            onDismiss = { noteToEdit = null },
            onSubmit = { title, desc -> handleUpdateNote(title, desc) },
            isLoading = isDialogLoading
        )
    }

    // Delete Confirmation Dialog
    if (noteIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteIdToDelete = null },
            title = { Text(stringResource(R.string.delete_title), color = MaterialTheme.colorScheme.error) },
            text = { Text(stringResource(R.string.delete_desc)) },
            dismissButton = {
                TextButton(onClick = { noteIdToDelete = null }) {
                    Text(stringResource(R.string.delete_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = { handleDeleteNote() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_confirm))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCardItem(
    note: Note,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEditClick,
                onLongClick = onDeleteClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.card_edit),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.card_delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = note.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.card_created) + ": " + formatTimestamp(note.createdAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                if (note.createdAt != note.updatedAt) {
                    Text(
                        text = stringResource(R.string.card_updated) + ": " + formatTimestamp(note.updatedAt),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// Simple bullet-proof parser for showing date/time
private fun formatTimestamp(isoString: String): String {
    return try {
        // e.g. "2026-05-30T15:24:14.123Z" -> "2026-05-30 15:24"
        val datePart = isoString.substringBefore("T")
        val timePart = isoString.substringAfter("T").substring(0, 5)
        "$datePart $timePart"
    } catch (e: Exception) {
        isoString
    }
}
