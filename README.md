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
建立 manager class 後，執行 connectDevice 便可連線設備並同時完成啟動通知開關動作。由於連線是同步耗時工作，請在 background thread 執行這個 fucntion，connectDevice 會阻塞當前 thread 直到回傳連線結果。
```
CSCManager.connectDevice(device)
```
### 傳送資料到設備
傳送資料到裝置需要建立 command，在 createCommand(data, verifyResponse, writeCharacteristc) 依序放入
* data: ByteArray，要傳送的資料
* verifyResponse: 接收裝置回傳資料的 callback，必須在這裡判斷回傳資料是否正確並回傳 Boolean
* writeCharacteristic: 裝置開放傳送資料的寫入特徵值
command.send() 會隨著 verifyResponse 回傳 true 或 false，回傳這次傳送成功或失敗
```
val command = createCommand(
            commandBytes,
            { responseBytes ->
                if (responseBytes[0] == 0xA2.toByte()
                    && responseBytes[1] == 0x08.toByte()
                    && responseBytes.size == 20) {
                    totalRecordCount = responseBytes[3].toInt()
                    return@createCommand true
                }

                return@createCommand false
            },
            writeCharacteristic
        )
command.send()
```
