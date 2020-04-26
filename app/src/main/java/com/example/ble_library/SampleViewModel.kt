package com.example.ble_library

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import no.nordicsemi.android.support.v18.scanner.*

class SampleViewModel(application: Application) : AndroidViewModel(application) {

    val disconnectAction: LiveData<Unit>
        get() = _disconnectAction
    private val _disconnectAction = MutableLiveData<Unit>()

    val connectAction: LiveData<Event<Unit>>
        get() = _connectAction
    private val _connectAction = MutableLiveData<Event<Unit>>()

    val connectSuccess: LiveData<String>
        get() = _connectSuccess
    private val _connectSuccess = MutableLiveData<String>()

    val connectFail: LiveData<Unit>
        get() = _connectFail
    private val _connectFail = MutableLiveData<Unit>()

    val startScan: LiveData<Unit>
        get() = _startScan
    private val _startScan = MutableLiveData<Unit>()

    val finishBatchScan: LiveData<Unit>
        get() = _finishBatchScan
    private val _finishBatchScan = MutableLiveData<Unit>()

    val notifyEnableBluetooth: LiveData<Unit>
        get() = _notifyEnableBluetooth
    private val _notifyEnableBluetooth = MutableLiveData<Unit>()

    val devices = arrayListOf<BluetoothDevice>()
    private val scanner by lazy { BluetoothLeScannerCompat.getScanner() }

    private val cscManager by lazy { CscManager(application.applicationContext) }

    private val scanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.map {
                if (!devices.contains(it.device)) {
                    devices.add(it.device)
                }
            }
            _finishBatchScan.postValue(Unit)
        }
    }

    fun scanDevice() {
        val filter = ScanFilter.Builder()
//            .setServiceUuid(ParcelUuid(CSCManager.GPS200S_SERVICE_UUID))
            .setDeviceAddress("C2:46:9C:12:02:4F") // "C2:8C:FE:CC:8C:3B"
            .build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(false)
            .build()
        try {
            scanner.stopScan(scanCallback)
            scanner.startScan(mutableListOf(filter), scanSettings, scanCallback)
            _startScan.postValue(Unit)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            _notifyEnableBluetooth.postValue(Unit)
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        _connectAction.postValue(Event(Unit))
        Completable.fromCallable {
            cscManager.connectDevice(device, 5000)
        }.subscribeOn(Schedulers.io())
            .doFinally { scanner.stopScan(scanCallback) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _connectSuccess.postValue(device.name)
            }, {
                it.printStackTrace()
                _connectFail.postValue(Unit)
            })
    }

    fun disconnectDevice() {
        Completable.fromCallable {
            cscManager.disconnect()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _disconnectAction.postValue(Unit)
            }, {
                it.printStackTrace()
            })
    }
}