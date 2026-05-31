package com.example.collectionsapp.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.collectionsapp.data.database.entities.Collection
import com.example.collectionsapp.data.database.entities.CollectionItem
import java.io.File

class ExportUtil(private val context: Context) {

    fun exportAsTxt(collection: Collection, items: List<CollectionItem>): File {
        val fileName = "${collection.name.replace(" ", "_")}.txt"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        file.bufferedWriter().use { writer ->
            writer.write("Коллекция: ${collection.name}")
            writer.newLine()
            if (collection.description.isNotEmpty()) {
                writer.write("Описание: ${collection.description}")
                writer.newLine()
            }
            writer.write("=".repeat(50))
            writer.newLine()
            writer.newLine()

            items.forEachIndexed { index, item ->
                writer.write("${index + 1}. ${item.name}")
                writer.newLine()
                if (item.model.isNotEmpty()) {
                    writer.write("   Модель: ${item.model}")
                    writer.newLine()
                }
                if (item.description.isNotEmpty()) {
                    writer.write("   Описание: ${item.description}")
                    writer.newLine()
                }
                if (item.location.isNotEmpty()) {
                    writer.write("   Местоположение: ${item.location}")
                    writer.newLine()
                }
                if (item.photoPath.isNotEmpty()) {
                    writer.write("   Фото: ${item.photoPath}")
                    writer.newLine()
                }
                writer.newLine()
            }
        }

        Toast.makeText(context, "Файл сохранен: ${file.name}", Toast.LENGTH_LONG).show()

        return file
    }

    fun exportAsHtml(collection: Collection, items: List<CollectionItem>): File {
        val fileName = "${collection.name.replace(" ", "_")}.html"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        file.bufferedWriter().use { writer ->
            writer.write("<!DOCTYPE html>")
            writer.newLine()
            writer.write("<html dir='ltr'>")
            writer.newLine()
            writer.write("<head>")
            writer.newLine()
            writer.write("<meta charset='UTF-8'>")
            writer.newLine()
            writer.write("<title>${collection.name}</title>")
            writer.newLine()
            writer.write("<style>")
            writer.newLine()
            writer.write("""
                body { font-family: Arial, sans-serif; margin: 20px; }
                h1 { color: #1976D2; }
                .description { color: #666; margin-bottom: 20px; }
                .item { 
                    border: 1px solid #ddd; 
                    padding: 15px; 
                    margin-bottom: 10px; 
                    border-radius: 5px;
                }
                .item h3 { margin-top: 0; color: #333; }
                .item p { margin: 5px 0; color: #666; }
            """.trimIndent())
            writer.newLine()
            writer.write("</style>")
            writer.newLine()
            writer.write("</head>")
            writer.newLine()
            writer.write("<body>")
            writer.newLine()

            writer.write("<h1>Коллекция: ${collection.name}</h1>")
            writer.newLine()

            if (collection.description.isNotEmpty()) {
                writer.write("<p class='description'>Описание: ${collection.description}</p>")
                writer.newLine()
            }

            writer.write("<div class='items'>")
            writer.newLine()

            items.forEach { item ->
                writer.write("<div class='item'>")
                writer.newLine()
                writer.write("<h3>${item.name}</h3>")
                writer.newLine()

                if (item.model.isNotEmpty()) {
                    writer.write("<p><strong>Модель:</strong> ${item.model}</p>")
                    writer.newLine()
                }

                if (item.description.isNotEmpty()) {
                    writer.write("<p><strong>Описание:</strong> ${item.description}</p>")
                    writer.newLine()
                }

                if (item.location.isNotEmpty()) {
                    writer.write("<p><strong>Местоположение:</strong> ${item.location}</p>")
                    writer.newLine()
                }

                if (item.photoPath.isNotEmpty()) {
                    writer.write("<p><strong>Фото:</strong> ${item.photoPath}</p>")
                    writer.newLine()
                }

                writer.write("</div>")
                writer.newLine()
            }

            writer.write("</div>")
            writer.newLine()
            writer.write("</body>")
            writer.newLine()
            writer.write("</html>")
        }

        Toast.makeText(context, "Файл сохранен: ${file.name}", Toast.LENGTH_LONG).show()

        return file
    }
}