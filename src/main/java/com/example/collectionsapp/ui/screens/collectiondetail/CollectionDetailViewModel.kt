package com.example.collectionsapp.ui.screens.collectiondetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.collectionsapp.CollectionsApp
import com.example.collectionsapp.data.database.entities.CollectionItem
import com.example.collectionsapp.data.repository.CollectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CollectionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as CollectionsApp).database
    private val repository = CollectionRepository(
        database.collectionDao(),
        database.collectionItemDao()
    )

    private val _collectionId = MutableStateFlow(0L)

    val items = _collectionId.flatMapLatest { id ->
        repository.getItemsByCollectionId(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _itemName = MutableStateFlow("")
    val itemName = _itemName.asStateFlow()

    private val _itemModel = MutableStateFlow("")
    val itemModel = _itemModel.asStateFlow()

    private val _itemDescription = MutableStateFlow("")
    val itemDescription = _itemDescription.asStateFlow()

    private val _itemLocation = MutableStateFlow("")
    val itemLocation = _itemLocation.asStateFlow()

    private val _itemPhotoPath = MutableStateFlow("")
    val itemPhotoPath = _itemPhotoPath.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    private val _editingItem = MutableStateFlow<CollectionItem?>(null)

    fun loadCollection(collectionId: Long) {
        _collectionId.value = collectionId
    }

    fun showAddDialog() {
        _editingItem.value = null
        _itemName.value = ""
        _itemModel.value = ""
        _itemDescription.value = ""
        _itemLocation.value = ""
        _itemPhotoPath.value = ""
        _nameError.value = null
        _showAddDialog.value = true
    }

    fun showEditDialog(item: CollectionItem) {
        _editingItem.value = item
        _itemName.value = item.name
        _itemModel.value = item.model
        _itemDescription.value = item.description
        _itemLocation.value = item.location
        _itemPhotoPath.value = item.photoPath
        _nameError.value = null
        _showAddDialog.value = true
    }

    fun dismissDialog() {
        _showAddDialog.value = false
    }

    fun updateItemName(name: String) {
        _itemName.value = name
        if (name.isNotEmpty()) {
            _nameError.value = null
        }
    }

    fun updateItemModel(model: String) {
        _itemModel.value = model
    }

    fun updateItemDescription(description: String) {
        _itemDescription.value = description
    }

    fun updateItemLocation(location: String) {
        _itemLocation.value = location
    }

    fun updateItemPhotoPath(path: String) {
        _itemPhotoPath.value = path
    }

    fun saveItem() {
        val name = _itemName.value.trim()
        if (name.isEmpty()) {
            _nameError.value = "Название обязательно"
            return
        }

        viewModelScope.launch {
            val editingItem = _editingItem.value
            if (editingItem != null) {
                repository.updateItem(
                    editingItem.copy(
                        name = name,
                        model = _itemModel.value.trim(),
                        description = _itemDescription.value.trim(),
                        location = _itemLocation.value.trim(),
                        photoPath = _itemPhotoPath.value.trim()
                    )
                )
            } else {
                val item = CollectionItem(
                    collectionId = _collectionId.value,
                    name = name,
                    model = _itemModel.value.trim(),
                    description = _itemDescription.value.trim(),
                    location = _itemLocation.value.trim(),
                    photoPath = _itemPhotoPath.value.trim()
                )
                repository.insertItem(item)
            }
            _showAddDialog.value = false
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItemById(itemId)
        }
    }
}