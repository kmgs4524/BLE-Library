# BLE-Library
可與BLE裝置連接、傳送/接收資料的 Android library，掃描藍牙裝置推薦使用 Nordic 推出的 Android BLE Scanner Compat library
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


### 連接設備
```
CSCManager.connectDevice()
```
