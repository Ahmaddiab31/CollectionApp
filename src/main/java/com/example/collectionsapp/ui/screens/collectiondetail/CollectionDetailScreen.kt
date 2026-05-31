package com.example.collectionsapp.ui.screens.collectiondetail

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.collectionsapp.R
import com.example.collectionsapp.data.database.entities.CollectionItem
import com.example.collectionsapp.util.ImagePickerUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    viewModel: CollectionDetailViewModel = viewModel()
) {
    val items by viewModel.items.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val itemName by viewModel.itemName.collectAsState()
    val itemModel by viewModel.itemModel.collectAsState()
    val itemDescription by viewModel.itemDescription.collectAsState()
    val itemLocation by viewModel.itemLocation.collectAsState()
    val itemPhotoPath by viewModel.itemPhotoPath.collectAsState()
    val nameError by viewModel.nameError.collectAsState()

    val context = LocalContext.current
    val imagePickerUtil = remember { ImagePickerUtil(context) }

    // متغيرات لاختيار الصورة
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // ✅ 1. تعريف cameraLauncher أولاً
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                val savedPath = imagePickerUtil.saveImageToAppStorage(uri)
                if (savedPath != null) {
                    viewModel.updateItemPhotoPath(savedPath)
                    Toast.makeText(context, "Фото сохранено", Toast.LENGTH_SHORT).show()
                }
            }
        }
        showImagePickerDialog = false
    }

    // ✅ 2. تعريف galleryLauncher ثانياً
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val savedPath = imagePickerUtil.saveImageToAppStorage(it)
            if (savedPath != null) {
                viewModel.updateItemPhotoPath(savedPath)
                Toast.makeText(context, "Фото выбрано", Toast.LENGTH_SHORT).show()
            }
        }
        showImagePickerDialog = false
    }

    // ✅ 3. تعريف cameraPermissionLauncher ثالثاً
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val tempFile = ImagePickerUtil.createTempImageFile(context)
            tempImageUri = imagePickerUtil.getUriForFile(tempFile)
            cameraLauncher.launch(tempImageUri!!)
        } else {
            Toast.makeText(context, "Разрешение на камеру отклонено", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(collectionId) {
        viewModel.loadCollection(collectionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Элементы коллекции") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить элемент")
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
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
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "В коллекции пока нет элементов",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Нажмите + чтобы добавить элемент",
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
                items(items, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onDeleteClick = { viewModel.deleteItem(item.id) },
                        onEditClick = { viewModel.showEditDialog(item) }
                    )
                }
            }
        }
    }

    // حوار إضافة/تعديل عنصر
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text("Добавить элемент") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // قسم الصورة
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // معاينة الصورة
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (itemPhotoPath.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = itemPhotoPath),
                                        contentDescription = "Фото",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.basic_photo),
                                        contentDescription = "Нет фото",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // أزرار اختيار الصورة
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // زر الكاميرا
                                OutlinedButton(
                                    onClick = {
                                        showImagePickerDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Камера")
                                }

                                // زر المعرض
                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch("image/*")
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Галерея")
                                }
                            }

                            // زر حذف الصورة
                            if (itemPhotoPath.isNotEmpty()) {
                                TextButton(
                                    onClick = { viewModel.updateItemPhotoPath("") },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Удалить фото")
                                }
                            }
                        }
                    }

                    // حقل الاسم
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { viewModel.updateItemName(it) },
                        label = { Text("Название *") },
                        isError = nameError != null,
                        supportingText = nameError?.let { error ->
                            { Text(error) }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // حقل الموديل
                    OutlinedTextField(
                        value = itemModel,
                        onValueChange = { viewModel.updateItemModel(it) },
                        label = { Text("Модель") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // حقل الوصف
                    OutlinedTextField(
                        value = itemDescription,
                        onValueChange = { viewModel.updateItemDescription(it) },
                        label = { Text("Описание") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // حقل الموقع
                    OutlinedTextField(
                        value = itemLocation,
                        onValueChange = { viewModel.updateItemLocation(it) },
                        label = { Text("Местоположение") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.saveItem() }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // حوار اختيار مصدر الصورة (كاميرا أو معرض)
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Выбрать фото") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // خيار الكاميرا
                    TextButton(
                        onClick = {
                            val permission = Manifest.permission.CAMERA
                            if (ContextCompat.checkSelfPermission(context, permission)
                                == PackageManager.PERMISSION_GRANTED) {
                                val tempFile = ImagePickerUtil.createTempImageFile(context)
                                tempImageUri = imagePickerUtil.getUriForFile(tempFile)
                                cameraLauncher.launch(tempImageUri!!)
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                            showImagePickerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Сделать фото")
                    }

                    // خيار المعرض
                    TextButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showImagePickerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Выбрать из галереи")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImagePickerDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun ItemCard(
    item: CollectionItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.photoPath.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = item.photoPath,
                            error = painterResource(R.drawable.basic_photo)
                        ),
                        contentDescription = "Фото элемента",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.basic_photo),
                        contentDescription = "Нет фото",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.model.isNotEmpty()) {
                    Text(
                        text = item.model,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.location.isNotEmpty()) {
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}