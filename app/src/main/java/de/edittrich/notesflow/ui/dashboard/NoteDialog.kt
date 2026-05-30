package de.edittrich.notesflow.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import de.edittrich.notesflow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    
    // Hoist localized string resources to Composable scope to resolve LocalContextGetResourceValueCall lint errors
    val titleRequiredStr = stringResource(R.string.form_validation_title_required)
    val titleMaxStr = stringResource(R.string.form_validation_title_max)
    val descRequiredStr = stringResource(R.string.form_validation_description_required)
    val descMaxStr = stringResource(R.string.form_validation_description_max)

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true

        if (title.trim().isEmpty()) {
            titleError = titleRequiredStr
            isValid = false
        } else if (title.length > 120) {
            titleError = titleMaxStr
            isValid = false
        } else {
            titleError = null
        }

        if (description.trim().isEmpty()) {
            descriptionError = descRequiredStr
            isValid = false
        } else if (description.length > 5000) {
            descriptionError = descMaxStr
            isValid = false
        } else {
            descriptionError = null
        }

        return isValid
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = if (isEditing) stringResource(R.string.form_edit_title) else stringResource(R.string.form_create_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEditing) stringResource(R.string.form_edit_desc) else stringResource(R.string.form_create_desc),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Title Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.form_field_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${title.length}/120",
                            fontSize = 11.sp,
                            color = if (title.length > 120) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (titleError != null) titleError = null
                        },
                        placeholder = { Text(stringResource(R.string.form_field_title_placeholder)) },
                        singleLine = true,
                        isError = titleError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (titleError != null) {
                        Text(
                            text = titleError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }

                // Description Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.form_field_description),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${description.length}/5000",
                            fontSize = 11.sp,
                            color = if (description.length > 5000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            if (descriptionError != null) descriptionError = null
                        },
                        placeholder = { Text(stringResource(R.string.form_field_description_placeholder)) },
                        minLines = 4,
                        maxLines = 8,
                        isError = descriptionError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (descriptionError != null) {
                        Text(
                            text = descriptionError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text(stringResource(R.string.form_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (validate()) {
                                onSubmit(title.trim(), description.trim())
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isEditing) stringResource(R.string.form_submit_edit) else stringResource(R.string.form_submit_create)
                            )
                        }
                    }
                }
            }
        }
    }
}
