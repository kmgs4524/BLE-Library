# BLE-Library
可與 BLE 裝置連接、傳送/接收資料的 Android library，掃描藍牙裝置推薦使用 Nordic 推出的 Android BLE Scanner Compat library
## 使用方式
繼承 BaseManager 建立設備的 manager class 並宣告裝置的 service, characteristic UUID，以踏頻器為例：
```
class CSCManager : BaseManager {
  
  ...
  
  companion object {
    val CYCLING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
    val CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb")
  }
}
```
### 設定特徵值
若裝備具有回傳資料的 Notification Characteristic，請在取得特徵值後用 enableNotification(characteristic) 啟動通知開關，這樣 APP 才能接收到裝置回傳的資料。
```
class CSCManager : BaseManager {
 
  ...
  
  override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
    super.onServicesDiscovered(gatt, status)
    val service = gatt?.getService(UNKNOWN_SERVICE_UUID)
    if (service != null) {
        // 取得通知特徵值
        cscMeasurementCharacteristic = service.getCharacteristic(
            UNKNOWN_CHARACTERISTIC_UUID)
        // 啟動通知開關
        enableNotification(cscMeasurementCharacteristic)
        // 取得寫入特徵值
        writeCharacteristic = service.getCharacteristic(WRITE_CHARACTERISTIC_UUID)
    }
  }
}
```
### 連接設備
```
CSCManager.connectDevice(device)
```

### 傳送資料到設備
建立 command
```

```
