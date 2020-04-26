package com.example.ble_library

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.ble_library.databinding.ActivitySampleBinding

class SampleActivity : AppCompatActivity() {

    private val viewModel by lazy { SampleViewModel(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivitySampleBinding>(
            this,
            R.layout.activity_sample
        )
        binding.viewModel = viewModel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission()
        }

        viewModel.startScan.observe(this, Observer {
            showScanResultDialog()
        })
        viewModel.notifyEnableBluetooth.observe(this, Observer {
            showEnableBluetoothNotification()
        })
        viewModel.connectSuccess.observe(this, Observer {
            binding.connectedDevice.text = it
            showToast("Connect success")
        })
        viewModel.connectFail.observe(this, Observer {
            binding.connectedDevice.text = "Failed"
            showToast("Connect failed")
        })
        viewModel.disconnectAction.observe(this, Observer {
            binding.connectedDevice.text = "--"
        })
    }

    @SuppressLint("NewApi")
    private fun checkLocationPermission() {
        val hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun showScanResultDialog() {
        ScanDialogFragment(viewModel).show(supportFragmentManager, ScanDialogFragment.TAG)
//        supportFragmentManager.executePendingTransactions()
    }

    private fun showEnableBluetoothNotification() {
        Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.scanDevice()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 9527
    }
}
