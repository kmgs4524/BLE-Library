package com.york.bluetoothlibrary

import android.bluetooth.*
import android.content.Context


/**
 * This class is only responsible for connecting the BLE device.
 */
open class BleConnectManager(private val context: Context) : BluetoothGattCallback() {

    var bluetoothGatt: BluetoothGatt? = null
    private var isConnected = false
    var connectLock = Object()
    var disconnectLock = Object()

    protected fun connect(device: BluetoothDevice): Boolean {
        synchronized(connectLock) {
            bluetoothGatt = device.connectGatt(context, false, this)
            connectLock.wait(350000)
        }
        return isConnected
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        // 連線時發生 133 錯誤，可能原因為與硬體設備的連線沒斷乾淨
        // 直接結束連線流程
        if (status == 133) {
            synchronized(connectLock) {
                connectLock.notify()
            }
        }

        isConnected = newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS
        if (!isConnected) {
            closeGatt()
        } else {
            gatt?.discoverServices()
            bluetoothGatt = gatt
        }

        // After disconnect result callback, release waiting thread
        synchronized(disconnectLock) {
            disconnectLock.notifyAll()
        }
    }

    fun disconnect(): Boolean {
        bluetoothGatt?.disconnect()
        synchronized(disconnectLock) {
            // Disconnect operation takes at most 10s
            disconnectLock.wait(10000)
        }
        return !isConnected
    }

    private fun closeGatt() {
        onCloseNotification()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    /**
    *   After disconnect BLE device and before  close GATT, close all [BluetoothGattCharacteristic]'s notifications and set variable to null.
     *  Device manager class should override this function if device has notification characteristics.
     */
    open fun onCloseNotification() {}

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        // 連線流程結束，釋放進入等待的 thread
        synchronized(connectLock) {
            connectLock.notifyAll()
        }
    }
}