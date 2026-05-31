package com.example.collectionsapp.ui.screens.collections

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.collectionsapp.CollectionsApp
import com.example.collectionsapp.data.database.entities.Collection
import com.example.collectionsapp.data.repository.CollectionRepository
import com.example.collectionsapp.util.ExportUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class CollectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as CollectionsApp).database
    private val repository = CollectionRepository(
        database.collectionDao(),
        database.collectionItemDao()
    )
    private val exportUtil = ExportUtil(application)

    val collections = repository.getAllCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog = _showCreateDialog.asStateFlow()

    private val _showCreateManuallyDialog = MutableStateFlow(false)
    val showCreateManuallyDialog = _showCreateManuallyDialog.asStateFlow()

    private val _collectionName = MutableStateFlow("")
    val collectionName = _collectionName.asStateFlow()

    private val _collectionDescription = MutableStateFlow("")
    val collectionDescription = _collectionDescription.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow<Long?>(null)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    private val _showSaveChoiceDialog = MutableStateFlow<Long?>(null)
    val showSaveChoiceDialog = _showSaveChoiceDialog.asStateFlow()

    // ==================== دوال الإنشاء ====================

    fun showCreateDialog() {
        _showCreateDialog.value = true
    }

    fun dismissCreateDialog() {
        _showCreateDialog.value = false
    }

    fun showCreateManuallyDialog() {
        _showCreateDialog.value = false
        _showCreateManuallyDialog.value = true
        _collectionName.value = ""
        _collectionDescription.value = ""
        _nameError.value = null
    }

    fun dismissCreateManuallyDialog() {
        _showCreateManuallyDialog.value = false
    }

    fun updateCollectionName(name: String) {
        _collectionName.value = name
        if (name.length >= 3) {
            _nameError.value = null
        }
    }

    fun updateCollectionDescription(description: String) {
        _collectionDescription.value = description
    }

    fun createCollection() {
        val name = _collectionName.value.trim()
        if (name.length < 3) {
            _nameError.value = "Название должно содержать минимум 3 символа"
            return
        }

        viewModelScope.launch {
            val collection = Collection(
                name = name,
                description = _collectionDescription.value.trim()
            )
            repository.insertCollection(collection)
            _showCreateManuallyDialog.value = false
        }
    }

    fun generateAutoCollection() {
        viewModelScope.launch {
            repository.generateAutoCollection()
            _showCreateDialog.value = false
        }
    }

    // ==================== دوال الحذف ====================

    fun showDeleteDialog(collectionId: Long) {
        _showDeleteDialog.value = collectionId
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch {
            repository.deleteCollectionById(collectionId)
            _showDeleteDialog.value = null
        }
    }

    // ==================== دوال التصدير ====================

    fun showExportDialog(collectionId: Long) {
        _showSaveChoiceDialog.value = collectionId
    }

    fun dismissExportDialog() {
        _showSaveChoiceDialog.value = null
    }

    // ✅ تصدير TXT ومشاركة
    fun exportAndShareAsTxt(collectionId: Long) {
        viewModelScope.launch {
            try {
                val collection = repository.getCollectionById(collectionId)
                if (collection == null) {
                    Toast.makeText(getApplication(), "Коллекция не найдена", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val items = repository.getItemsByCollectionIdOnce(collectionId)
                val file = exportUtil.exportAsTxt(collection, items)

                // مشاركة الملف
                shareFile(file, "text/plain")

                _showSaveChoiceDialog.value = null
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ تصدير HTML ومشاركة
    fun exportAndShareAsHtml(collectionId: Long) {
        viewModelScope.launch {
            try {
                val collection = repository.getCollectionById(collectionId)
                if (collection == null) {
                    Toast.makeText(getApplication(), "Коллекция не найдена", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val items = repository.getItemsByCollectionIdOnce(collectionId)
                val file = exportUtil.exportAsHtml(collection, items)

                // مشاركة الملف
                shareFile(file, "text/html")

                _showSaveChoiceDialog.value = null
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ اختيار مكان حفظ TXT
    fun exportTxtToCustomLocation(collectionId: Long) {
        viewModelScope.launch {
            try {
                val collection = repository.getCollectionById(collectionId)
                if (collection == null) {
                    Toast.makeText(getApplication(), "Коллекция не найдена", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val items = repository.getItemsByCollectionIdOnce(collectionId)
                val file = exportUtil.exportAsTxt(collection, items)

                // فتح مدير الملفات لاختيار مكان الحفظ
                shareFile(file, "text/plain")

                _showSaveChoiceDialog.value = null
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ اختيار مكان حفظ HTML
    fun exportHtmlToCustomLocation(collectionId: Long) {
        viewModelScope.launch {
            try {
                val collection = repository.getCollectionById(collectionId)
                if (collection == null) {
                    Toast.makeText(getApplication(), "Коллекция не найдена", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val items = repository.getItemsByCollectionIdOnce(collectionId)
                val file = exportUtil.exportAsHtml(collection, items)

                // فتح مدير الملفات لاختيار مكان الحفظ
                shareFile(file, "text/html")

                _showSaveChoiceDialog.value = null
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ دالة مشاركة الملف
    private fun shareFile(file: File, mimeType: String) {
        try {
            val context = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Поделиться коллекцией")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooser)

            Toast.makeText(context, "Файл готов: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Ошибка отправки: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}