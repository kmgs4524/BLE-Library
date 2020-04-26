package com.york.bluetoothlibrary

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import kotlin.concurrent.thread

class Command(
    private val data: ByteArray,
    private val bluetoothGatt: BluetoothGatt,
    private val writeCharacteristic: BluetoothGattCharacteristic,
    private val writeType: Int,
    private val verify: (ByteArray) -> Boolean
) {
    var sendDesiredData = false
    var verifyResponse = false

    private val sendLock = AutoReleaseLock(false)
    private val verifyLock = AutoReleaseLock(false)

    private var timeoutCanceled = false

    fun send(): Boolean {
        return when (writeType) {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE -> {
                // if command initiated fail, abort write operation progress
                sendData(data)
                waitForConcurrencyTaskFinish(Operation.SEND)
                sendDesiredData
            }
            else -> {
                sendData(data)
                waitForConcurrencyTaskFinish(Operation.SEND)

                if (sendDesiredData) {
                    waitForConcurrencyTaskFinish(Operation.VERIFY)
                }

                false
            }
        }
    }

    fun close() {
        sendLock.release()
        verifyLock.release()
        timeoutCanceled = true

    }

    private fun waitForConcurrencyTaskFinish(operationType: Operation) {
        val threadLock = when (operationType) {
            Operation.SEND -> sendLock
            Operation.VERIFY -> verifyLock
        }
        thread {
            var waitMilliSeconds = 0
            while (waitMilliSeconds <= 5000 && !timeoutCanceled) {
                Thread.sleep(25)
                waitMilliSeconds += 25
            }
            threadLock.release()
        }
        threadLock.waitFor(5000)
    }

    fun verifySendDesiredData(remoteData: ByteArray?) {
        if (remoteData == null) {
            sendDesiredData = false
            sendLock.release()
        } else {
            sendDesiredData = remoteData contentEquals data
        }
    }

    fun verifySendOperationResponse(data: ByteArray?) {
        if (data != null) {
            verifyResponse = verify(data)
        }
    }

    /**
     * @return if write operation was initiated successfully
     */
    private fun sendData(data: ByteArray): Boolean {
        return with(writeCharacteristic) {
            writeType = this@Command.writeType
            value = data
            bluetoothGatt.writeCharacteristic(writeCharacteristic)
        }
    }

    enum class Operation {
        SEND,
        VERIFY
    }
}