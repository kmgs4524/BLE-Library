package com.example.ble_library

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import byteextensions.toHexString
import com.york.bluetoothlibrary.BaseManager
import com.york.bluetoothlibrary.Command
import java.util.*

class CscManager(context: Context) : BaseManager(context) {

    private var cscMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null


    fun startUploadRecord() {
        val dataBytes = Array<Byte>(16) { 0x00 }.toByteArray()
        val commandBytes = ExamCommand.START_UPLOAD_DATA.create(dataBytes)
        var totalRecordCount = 0
        val command = createCommand(
            commandBytes,
            { responseBytes ->
                if (responseBytes[0] == 0xA4.toByte()
                    && responseBytes[1] == 0x01.toByte()
                    && responseBytes.size == 20) {
                    totalRecordCount = responseBytes[3].toInt()
                    return@createCommand true
                }

                return@createCommand false
            },
            writeCharacteristic
        )

        val isSuccess = sendCommand(command)
        Log.d(TAG, "Send result: $isSuccess")
    }

    private fun sendCommand(command: Command): Boolean {
        val isSuccess = command.send()
        command.close()
        return isSuccess
    }

    override fun notificationEnabled(): Boolean {
        val isNotifying = isNotifying(cscMeasurementCharacteristic)
        Log.d(TAG, "isNotifying: $isNotifying")
        return cscMeasurementCharacteristic != null && isNotifying
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        val service = gatt?.getService(UNKNOWN_SERVICE_UUID)
        if (service != null) {
            cscMeasurementCharacteristic = service.getCharacteristic(
                UNKNOWN_CHARACTERISTIC_UUID)
            enableNotification(cscMeasurementCharacteristic)
            writeCharacteristic = service.getCharacteristic(WRITE_CHARACTERISTIC_UUID)
        }
    }

    companion object {
        val CYCLING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
        val CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb")
        val UNKNOWN_SERVICE_UUID = UUID.fromString("0000a7e0-0000-1000-8000-00805f9b34fb")
        val UNKNOWN_CHARACTERISTIC_UUID = UUID.fromString("0000a7e0-0000-1000-8000-00805f9b34fb")
        val WRITE_CHARACTERISTIC_UUID = UUID.fromString("0000a7e1-0000-1000-8000-00805f9b34fb")

        const val TAG = "CscManager"
    }
}