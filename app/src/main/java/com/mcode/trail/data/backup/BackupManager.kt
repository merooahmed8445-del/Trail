package com.mcode.trail.data.backup

import android.content.Context
import android.net.Uri
import com.mcode.trail.data.local.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * مدير النسخ الاحتياطي — تصدير واستيراد
 *
 * الصيغة: JSON
 * - الإصدار: 1
 * - التاريخ: timestamp
 * - الذكريات: array of bookmarks
 */
object BackupManager {

    private const val FORMAT_VERSION = 1

    /**
     * يحوّل قائمة الـ bookmarks لـ JSON String
     */
    fun exportToJson(bookmarks: List<Bookmark>): String {
        val root = JSONObject()
        root.put("version", FORMAT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("count", bookmarks.size)

        val array = JSONArray()
        bookmarks.forEach { bookmark ->
            val obj = JSONObject()
            obj.put("url", bookmark.url)
            obj.put("title", bookmark.title)
            obj.put("note", bookmark.note)
            obj.put("type", bookmark.type)
            obj.put("imageUrl", bookmark.imageUrl ?: JSONObject.NULL)
            obj.put("createdAt", bookmark.createdAt)
            obj.put("isFavorite", bookmark.isFavorite)
            array.put(obj)
        }
        root.put("bookmarks", array)

        return root.toString(2)  // pretty-print
    }

    /**
     * يقرأ JSON String ويرجّع قائمة bookmarks
     */
    fun importFromJson(json: String): ImportResult {
        return try {
            val root = JSONObject(json)

            // تحقق من الإصدار
            val version = root.optInt("version", 0)
            if (version == 0) {
                return ImportResult.Error("ملف غير صالح — مفيش version")
            }
            if (version > FORMAT_VERSION) {
                return ImportResult.Error("الملف من نسخة أحدث من التطبيق")
            }

            val array = root.optJSONArray("bookmarks")
                ?: return ImportResult.Error("مفيش بيانات في الملف")

            val bookmarks = mutableListOf<Bookmark>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                bookmarks.add(
                    Bookmark(
                        // id = 0 → Room هيعمل auto-generate
                        url = obj.getString("url"),
                        title = obj.optString("title", ""),
                        note = obj.optString("note", ""),
                        type = obj.optString("type", "article"),
                        imageUrl = obj.optString("imageUrl", null)
                            .takeIf { it != "null" && it.isNotBlank() },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                )
            }

            ImportResult.Success(bookmarks)
        } catch (e: Exception) {
            ImportResult.Error("خطأ في قراءة الملف: ${e.message}")
        }
    }

    /**
     * يكتب JSON في ملف عبر Uri
     */
    suspend fun writeToUri(context: Context, uri: Uri, json: String) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(json.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("مش قادر أفتح الملف للكتابة")
    }

    /**
     * يقرأ JSON من ملف عبر Uri
     */
    suspend fun readFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: throw IllegalStateException("مش قادر أفتح الملف للقراءة")
    }

    /**
     * نتيجة الاستيراد
     */
    sealed class ImportResult {
        data class Success(val bookmarks: List<Bookmark>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}