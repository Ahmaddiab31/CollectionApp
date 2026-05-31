package com.example.collectionsapp.ui.screens.itemdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.collectionsapp.CollectionsApp
import com.example.collectionsapp.data.database.entities.CollectionItem
import com.example.collectionsapp.data.repository.CollectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ItemDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as CollectionsApp).database
    private val repository = CollectionRepository(
        database.collectionDao(),
        database.collectionItemDao()
    )

    private val _item = MutableStateFlow<CollectionItem?>(null)
    val item = _item.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            _item.value = repository.getItemById(itemId)
        }
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun deleteItem() {
        viewModelScope.launch {
            _item.value?.let { item ->
                repository.deleteItemById(item.id)
            }
        }
    }
}