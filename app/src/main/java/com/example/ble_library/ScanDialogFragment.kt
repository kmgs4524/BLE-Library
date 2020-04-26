package com.example.ble_library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ble_library.databinding.DialogScannedDeviceBinding

class ScanDialogFragment(private val viewModel: SampleViewModel) : DialogFragment() {

    private lateinit var binding: DialogScannedDeviceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_scanned_device,
            container,
            false
        )
        viewModel.connectAction.observe(this, EventObserver {
            dismiss()
            Toast.makeText(context, "Start connect ...", Toast.LENGTH_SHORT).show()
        })
        viewModel.finishBatchScan.observe(this, Observer {
            binding.devices.adapter?.notifyDataSetChanged()
        })
        return with(binding) {
            this.devices.apply {
                adapter = DevicesAdapter(viewModel)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            binding.root
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        const val TAG = "ScanDialogFragment"
    }
}