package com.example.ble_library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ble_library.databinding.DeviceItemBinding

class DevicesAdapter(private val viewModel: SampleViewModel) : RecyclerView.Adapter<DevicesAdapter.DeviceHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val binding = DataBindingUtil.inflate<DeviceItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.device_item,
            parent,
            false
        )
        return DeviceHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.binding.apply {
            viewModel = this@DevicesAdapter.viewModel
            device = this@DevicesAdapter.viewModel.devices[position]
        }
    }

    override fun getItemCount() = viewModel.devices.size

    class DeviceHolder(val binding: DeviceItemBinding) : RecyclerView.ViewHolder(binding.root)
}