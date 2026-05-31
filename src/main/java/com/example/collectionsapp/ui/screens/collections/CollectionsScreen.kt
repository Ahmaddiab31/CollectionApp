package com.example.collectionsapp.ui.screens.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.collectionsapp.data.database.entities.Collection
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onCollectionClick: (Long) -> Unit,
    viewModel: CollectionsViewModel = viewModel()
) {
    val collections by viewModel.collections.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val showCreateManuallyDialog by viewModel.showCreateManuallyDialog.collectAsState()
    val collectionName by viewModel.collectionName.collectAsState()
    val collectionDescription by viewModel.collectionDescription.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showSaveChoiceDialog by viewModel.showSaveChoiceDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои коллекции") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать коллекцию")
            }
        }
    ) { paddingValues ->
        if (collections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "У вас пока нет коллекций",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Нажмите + чтобы создать новую коллекцию",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(collections, key = { it.id }) { collection ->
                    CollectionCard(
                        collection = collection,
                        onClick = { onCollectionClick(collection.id) },
                        onDeleteClick = { viewModel.showDeleteDialog(collection.id) },
                        onExportClick = { viewModel.showExportDialog(collection.id) }
                    )
                }
            }
        }
    }

    // حوار إنشاء مجموعة جديدة
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateDialog() },
            title = { Text("Создать новую коллекцию") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { viewModel.showCreateManuallyDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать вручную")
                    }

                    TextButton(
                        onClick = { viewModel.generateAutoCollection() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Автоматическая генерация")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // حوار إدخال اسم المجموعة
    if (showCreateManuallyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateManuallyDialog() },
            title = { Text("Новая коллекция") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = collectionName,
                        onValueChange = { viewModel.updateCollectionName(it) },
                        label = { Text("Название коллекции") },
                        isError = nameError != null,
                        supportingText = nameError?.let { error ->
                            { Text(error) }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = collectionDescription,
                        onValueChange = { viewModel.updateCollectionDescription(it) },
                        label = { Text("Описание (необязательно)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.createCollection() }) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateManuallyDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // حوار تأكيد الحذف
    showDeleteDialog?.let { collectionId ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Удалить коллекцию") },
            text = {
                Text("Вы уверены, что хотите удалить эту коллекцию? Это действие нельзя отменить.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteCollection(collectionId) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // حوار خيارات التصدير
    showSaveChoiceDialog?.let { collectionId ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportDialog() },
            title = {
                Text(
                    "Экспорт коллекции",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // خيار 1: تصدير TXT ومشاركة
                    TextButton(
                        onClick = { viewModel.exportAndShareAsTxt(collectionId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "Экспорт TXT",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Сохранить и поделиться",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // خيار 2: تصدير HTML ومشاركة
                    TextButton(
                        onClick = { viewModel.exportAndShareAsHtml(collectionId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "Экспорт HTML",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Сохранить и поделиться",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // خيار 3: اختيار مكان حفظ TXT
                    TextButton(
                        onClick = { viewModel.exportTxtToCustomLocation(collectionId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "Сохранить TXT в...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Выбрать папку для сохранения",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // خيار 4: اختيار مكان حفظ HTML
                    TextButton(
                        onClick = { viewModel.exportHtmlToCustomLocation(collectionId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "Сохранить HTML в...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Выбрать папку для сохранения",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExportDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row {
                    // زر التصدير
                    IconButton(onClick = onExportClick) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Экспорт",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // زر الحذف
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (collection.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (collection.isAutoGenerated) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Авто") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = dateFormat.format(Date(collection.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}