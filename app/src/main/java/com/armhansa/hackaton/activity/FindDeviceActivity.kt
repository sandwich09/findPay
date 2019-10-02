package com.armhansa.hackaton.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.armhansa.hackaton.R
import com.armhansa.hackaton.adapter.BluetoothDeviceAdapter
import com.armhansa.hackaton.data.TokenModel
import com.armhansa.hackaton.extension.makeToast
import com.armhansa.hackaton.listener.OnBluetoothDeviceItemClick
import com.example.myapplication.basic_api.data.ExampleData
import com.example.myapplication.basic_api.service.SCBManager
import kotlinx.android.synthetic.main.activity_find_device.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindDeviceActivity : AppCompatActivity(), OnBluetoothDeviceItemClick {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, FindDeviceActivity::class.java))
        }
    }

    private val bluetoothRvAdapter: BluetoothDeviceAdapter by lazy { BluetoothDeviceAdapter(this) }
    private val bluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private lateinit var mReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_device)

        setView()
    }

    private fun setView() {
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        discoverBluetoothDevice()
        rvBluetoothDevice.run {
            adapter = bluetoothRvAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
        }
        pullToRefresh.setOnRefreshListener {
            discoverBluetoothDevice()
        }
    }

    private fun discoverBluetoothDevice() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        makeToast("Started", Toast.LENGTH_SHORT)
//                        pbScanning.isVisible = true
                        bluetoothRvAdapter.reset()

                        pullToRefresh.isRefreshing = true
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            it.name?.let { name ->
                                if (name.startsWith("szb_") && bluetoothRvAdapter.isNew(name.toLowerCase())) {
                                    bluetoothRvAdapter.addBluetoothDevice(
                                        name.toLowerCase().slice(
                                            IntRange(4, name.count() - 1)
                                        )
                                    )
                                }
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        makeToast("Finished", Toast.LENGTH_SHORT)
//                        pbScanning.isVisible = false
                        pullToRefresh.isRefreshing = false
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        try {
            bluetoothAdapter.cancelDiscovery()
            unregisterReceiver(mReceiver)
        } catch (t: Throwable) {
        }
        super.onDestroy()
    }

    override fun onClickBluetoothItem(btDeviceUsername: String) {
        val accountTo = when (btDeviceUsername) {
            "vittaya" -> "1111111111"
            "naringg" -> "2222222222"
            "sandwish" -> "1100400881"
            else -> ""
        }
        makeToast("accountTo is $accountTo", Toast.LENGTH_LONG)
        Handler().postDelayed({ gotoDeepLink("https://www.google.co.th") }, 1000)
    }

    private fun getToken() {
        val ex = ExampleData(
            "l7f031d768df40465ba05ae327022a5220",
            "8e25c09a6e0a4adeabfd6f50742c969d"
        )
        SCBManager().createService().getToken(exampleData = ex).enqueue(object : Callback<TokenModel> {
            override fun onFailure(call: Call<TokenModel>, t: Throwable) {
                makeToast("Error! Can not get token!", Toast.LENGTH_LONG)
            }

            override fun onResponse(call: Call<TokenModel>, response: Response<TokenModel>) {
                makeToast("Response Successful", Toast.LENGTH_LONG)
                Log.d(com.armhansa.hackaton.constant.TAG, response.body().toString())
                response.body()?.apply {
                    makeToast("${this.data1}", Toast.LENGTH_LONG)
                }
            }
        })
    }

    private fun gotoDeepLink(deepLink: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)))
    }

}