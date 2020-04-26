package com.york.bluetoothlibrary

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.util.Log
import java.lang.IllegalStateException
import java.util.*

abstract class BaseManager(context: Context) : BleConnectManager(context) {

    private val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val awaitingSendCommand = mutableListOf<Command>()

    private var defaultWriteCharacteristic: BluetoothGattCharacteristic? = null

    fun connectDevice(device: BluetoothDevice, timeoutCount: Int): Boolean {
        if (super.connect(device)) {
            var passedTime = 0
            while (passedTime < timeoutCount &&
                !notificationEnabled() &&
                !indicationEnabled()) {
                Thread.sleep(10)
                passedTime += 10
            }

            return if (notificationEnabled()) {
                Log.i(TAG, "Connect success")
                true
            } else {
                Log.i(TAG, "Connect fail")
                disconnect()
                false
            }
        }

        return false
    }

    protected fun isNotificationType(characteristic: BluetoothGattCharacteristic?): Boolean {
        if (characteristic == null) return false
        return characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    }

    protected fun createCommand(
        data: ByteArray,
        verify: (ByteArray) -> Boolean,
        writeCharacteristic: BluetoothGattCharacteristic? = defaultWriteCharacteristic): Command
    {
        val bluetoothGatt = this.bluetoothGatt ?: throw IllegalStateException()
        val notNullCharacteristic = writeCharacteristic ?: throw IllegalStateException()
        return Command(
            data,
            bluetoothGatt,
            notNullCharacteristic,
            notNullCharacteristic.writeType,
            verify
        ).apply {
            awaitingSendCommand.add(this)
        }
    }

    /**
     * Confirm notification for characteristic is enabled
     */
    protected fun isNotifying(characteristic: BluetoothGattCharacteristic?): Boolean {
        val descriptor = characteristic?.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID)
        if (descriptor?.value != null) {
            Log.d(TAG, "descriptor $descriptor value: ${descriptor.value} ENABLE_NOTIFICATION_VALUE: ${BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE}")
            return descriptor.value contentEquals BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        return false
    }

    protected fun enableNotification(characteristic: BluetoothGattCharacteristic?): Boolean {
        val gatt = bluetoothGatt ?: return false

        val notificationEnabled = bluetoothGatt?.setCharacteristicNotification(characteristic, true) ?: false    // 啟動 notification
        val clientCharacteristicConfigDescriptor = characteristic?.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID)
        if (notificationEnabled && clientCharacteristicConfigDescriptor != null) {
            clientCharacteristicConfigDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(clientCharacteristicConfigDescriptor)
        }

        return false
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        awaitingSendCommand.forEach {
            it.verifySendDesiredData(characteristic?.value)
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        awaitingSendCommand.forEach {
            it.verifySendOperationResponse(characteristic?.value)
        }
    }

    open fun notificationEnabled(): Boolean = false

    open fun indicationEnabled(): Boolean = false

    companion object {
        const val TAG = "BaseManager"
    }
}