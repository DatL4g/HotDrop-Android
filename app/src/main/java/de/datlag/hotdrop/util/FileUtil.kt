package de.datlag.hotdrop.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.obsez.android.lib.filechooser.ChooserDialog
import de.datlag.hotdrop.R
import org.jetbrains.annotations.Contract
import org.zeroturnaround.zip.ZipUtil
import java.io.*
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

object FileUtil {
    private const val MIN_LARGE_HEAP_SIZE = 11000000
    private const val DEFAULT_BUFFER_SIZE = 1024 * 4

    fun chooseFile(activity: Activity?, fileFilter: FileFilter? = null, startingPath: String? = null, fileChooseCallback: FileChooseCallback) {
        ChooserDialog(activity, R.style.FileChooserStyle)
                .enableDpad(true)
                .withFilter(fileFilter)
                .withStartFile(startingPath)
                .withChosenListener { path: String?, file: File? -> fileChooseCallback.onChosen(path, file) }
                .withOnCancelListener { obj: DialogInterface -> obj.cancel() }
                .build()
                .show()
    }

    fun chooseFolder(activity: Activity?, startingPath: String? = null, folderChooseCallback: FolderChooseCallback) {
        ChooserDialog(activity, R.style.FileChooserStyle)
                .enableDpad(true)
                .withFilter(true, false)
                .withStartFile(startingPath)
                .withChosenListener { path: String?, pathFile: File? ->
                    try {
                        folderChooseCallback.onChosen(path, pathFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                .withOnCancelListener { obj: DialogInterface -> obj.cancel() }
                .build()
                .show()
    }

    fun chooseAny(activity: Activity?, anyChooseCallback: AnyChooseCallback) {
        ChooserDialog(activity, R.style.FileChooserStyle)
                .enableDpad(true)
                .withFilter(false, true)
                .withChosenListener { dir: String?, dirFile: File? ->
                    if (isFile(dir)) {
                        anyChooseCallback.onChosenFile(dir, dirFile)
                    } else if (isDirectory(dir)) {
                        anyChooseCallback.onChosenFolder(dir, dirFile)
                    }
                }
                .withOnCancelListener { obj: DialogInterface -> obj.cancel() }
                .withNegativeButton("Choose Folder") { _: DialogInterface?, _: Int ->
                    ChooserDialog.Result { dir, dirFile ->
                        if (isFile(dir)) {
                            anyChooseCallback.onChosenFile(dir, dirFile)
                        } else if (isDirectory(dir)) {
                            anyChooseCallback.onChosenFolder(dir, dirFile)
                        }
                    }
                }
                .build().show()
    }

    fun jsonObjectFromFile(context: Context, file: File, bytes: ArrayList<ByteArray>, pos: Int): JsonObject {
        val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
        val jsonObject = JsonObject()
        val base64List = ArrayList<String>()
        for (byteArray in bytes) {
            base64List.add(toBase64String(byteArray))
        }
        jsonObject.addProperty(context.getString(R.string.path), file.absolutePath)
        jsonObject.addProperty(context.getString(R.string.name), file.name)
        jsonObject.addProperty(context.getString(R.string.mime), MimeTypes.getMimeType(extension))
        jsonObject.addProperty(context.getString(R.string.extension), extension)
        jsonObject.addProperty(context.getString(R.string.base64), toBase64String(bytes[pos]))
        jsonObject.addProperty(context.getString(R.string.size), file.length())
        jsonObject.addProperty(context.getString(R.string.part), pos)
        jsonObject.addProperty(context.getString(R.string.maxParts), bytes.size)
        return jsonObject
    }

    fun jsonObjectToBytes(jsonObject: JsonObject): ByteArray {
        return jsonObject.toString().toByteArray()
    }

    private fun splitFileToBytes(file: File): ArrayList<ByteArray> {
        val returnValue = ArrayList<ByteArray>()
        val chunkSize = MIN_LARGE_HEAP_SIZE
        var `in`: BufferedInputStream? = null
        try {
            `in` = BufferedInputStream(FileInputStream(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val fileSize = file.length()
        var subfile = 0
        while (subfile < fileSize / chunkSize) {
            val outputStream = ByteArrayOutputStream()
            for (currentByte in 0 until chunkSize) {
                try {
                    outputStream.write(`in`!!.read())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            returnValue.add(outputStream.toByteArray())
            subfile++
        }
        if (fileSize != chunkSize * (subfile - 1).toLong()) {
            val outputStream = ByteArrayOutputStream()
            var b = 0
            while (true) {
                try {
                    if (`in`!!.read().also { b = it } == -1) break
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                outputStream.write(b)
            }
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            returnValue.add(outputStream.toByteArray())
        }
        try {
            `in`!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return returnValue
    }

    fun byteArraysFromFile(file: File): ArrayList<ByteArray> {
        var byteArrayList = ArrayList<ByteArray>()
        if (isParted(file)) {
            byteArrayList = splitFileToBytes(file)
        } else {
            byteArrayList.add(readFileToByteArray(file))
        }
        return byteArrayList
    }

    @Throws(IOException::class)
    fun openInputStream(file: File): FileInputStream {
        if (file.exists()) {
            if (file.isDirectory) {
                throw IOException("File '$file' exists but is a directory")
            }
            if (!file.canRead()) {
                throw IOException("File '$file' cannot be read")
            }
        } else {
            throw FileNotFoundException("File '$file' does not exist")
        }
        return FileInputStream(file)
    }

    private fun readFileToByteArray(file: File): ByteArray {
        var `in`: InputStream? = null
        return try {
            try {
                `in` = openInputStream(file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            toByteArray(`in`)
        } finally {
            closeQuietly(`in`)
        }
    }

    private fun toByteArray(input: InputStream?): ByteArray {
        val output = ByteArrayOutputStream()
        copy(input!!, output)
        return output.toByteArray()
    }

    private fun copy(input: InputStream, output: OutputStream): Int {
        val count = copyLarge(input, output)
        return if (count > Int.MAX_VALUE) {
            -1
        } else count.toInt()
    }

    private fun copyLarge(input: InputStream, output: OutputStream): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var count: Long = 0
        var n = 0
        while (true) {
            try {
                if (-1 == input.read(buffer).also { n = it }) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                output.write(buffer, 0, n)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            count += n.toLong()
        }
        return count
    }

    private fun closeQuietly(input: InputStream?) {
        try {
            input?.close()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

    fun readableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun isParted(file: File): Boolean {
        return file.length() > MIN_LARGE_HEAP_SIZE
    }

    fun writeBytesToFile(bytes: ByteArray?, file: String?) {
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file!!)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            out!!.write(bytes!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            out!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun createFile(path: String, filename: String) {
        makeDir(path)
        val file = File(path + File.separator + filename)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getMimeType(bytes: ByteArray?): String? {
        val `is`: InputStream = BufferedInputStream(ByteArrayInputStream(bytes))
        var mimeType: String? = null
        try {
            mimeType = URLConnection.guessContentTypeFromStream(`is`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mimeType
    }

    fun getMimeType(activity: Activity, uri: Uri): String? {
        var mimeType: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val path = Paths.get(uri.toString())
            try {
                mimeType = Files.probeContentType(path)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val cr = activity.contentResolver
                cr.getType(uri)
            } else {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
            }
        }
        return mimeType
    }

    private fun toBase64String(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun base64ToBytes(base64String: String?): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }

    fun jsonObjectFromBytes(bytes: ByteArray?): JsonObject {
        return Gson().fromJson(String(bytes!!), JsonObject::class.java)
    }

    private fun makeDir(path: String?) {
        if (!existsFile(path)) {
            val file = File(path!!)
            file.mkdirs()
        }
    }

    private fun existsFile(path: String?): Boolean {
        val file = File(path!!)
        return file.exists()
    }

    private fun existsFile(file: File): Boolean {
        return file.exists()
    }

    private fun isDirectory(path: String?): Boolean {
        return if (!existsFile(path)) {
            false
        } else File(path!!).isDirectory
    }

    private fun isFile(path: String?): Boolean {
        return if (!existsFile(path)) {
            false
        } else File(path!!).isFile
    }

    fun isDirectory(file: File): Boolean {
        return if (!existsFile(file)) {
            false
        } else file.isDirectory
    }

    fun isFile(file: File): Boolean {
        return if (!existsFile(file)) {
            false
        } else file.isFile
    }

    fun folderToFile(context: Context, file: File): File {
        val tempDir = context.cacheDir
        var tempFile: File? = null
        try {
            tempFile = File.createTempFile(file.name, ".zip", tempDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        ZipUtil.pack(file, tempFile)
        tempFile!!.deleteOnExit()
        return tempFile
    }

    fun folderToFile(context: Context, path: String?): File {
        val file = File(path!!)
        val tempDir = context.cacheDir
        var tempFile: File? = null
        try {
            tempFile = File.createTempFile(file.name, ".zip", tempDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        ZipUtil.pack(file, tempFile)
        tempFile!!.deleteOnExit()
        return tempFile
    }

    fun getFileExtension(file: File?): String {
        return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
    }

    interface FileChooseCallback {
        fun onChosen(path: String?, file: File?)
    }

    interface FolderChooseCallback {
        @Throws(IOException::class)
        fun onChosen(path: String?, file: File?)
    }

    interface AnyChooseCallback {
        fun onChosenFolder(path: String?, file: File?)
        fun onChosenFile(path: String?, file: File?)
    }
}