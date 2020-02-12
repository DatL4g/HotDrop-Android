package de.datlag.hotdrop.p2p

import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.util.FileUtil
import java.io.File

class FileTransfer(private val advancedActivity: AdvancedActivity) {
    fun send(host: Host, file: File) {
        val bytes = FileUtil.byteArraysFromFile(file)
        for (i in bytes.indices) {
            host.send(FileUtil.jsonObjectToBytes(FileUtil.jsonObjectFromFile(advancedActivity, file, bytes, i)))
        }
    }

    fun stop(host: Host) {
        host.stop()
    }
}