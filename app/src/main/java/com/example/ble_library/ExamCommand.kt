package com.example.ble_library

sealed class ExamCommand(val type: Byte) {

    object START_UPLOAD_DATA : ExamCommand(0x01)

    fun create(dataByte: ByteArray): ByteArray {
        val headerByte = 0xA3.toByte()
        val dataIdByte = 0x01.toByte()
        val lengthBytes = 0x00.toByte()
        val endByte = 0xFF.toByte()

        val contentBytes = mutableListOf<Byte>().apply {
            add(headerByte)
            add(dataIdByte)
            add(lengthBytes)
            addAll(dataByte.toList())
            add(endByte)
        }
//        val content = byteArrayOf(
//            headerByte,
//            dataIdByte,
//            FILE_LIST_UPLOAD.type,
//            dataByte,
//            endByte
//        )
        return contentBytes.toByteArray()
    }
}