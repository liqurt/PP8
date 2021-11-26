package com.ssafy.smartstore.activity

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.nfc.NfcAdapter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.ssafy.smartstore.*
import com.ssafy.smartstore.R
import com.ssafy.smartstore.api.OrderApi
import com.ssafy.smartstore.api.UserApi
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.databinding.DialogBeaconBinding
import com.ssafy.smartstore.dto.Grade
import com.ssafy.smartstore.dto.UserOrderDetail
import com.ssafy.smartstore.fragment.*
import com.ssafy.smartstore.response.LatestOrderResponse
import org.altbeacon.beacon.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import android.widget.Toast

import android.content.DialogInterface




private const val TAG = "MainActivity_싸피"
class MainActivity : AppCompatActivity(), BeaconConsumer {
    private lateinit var bottomNavigation : BottomNavigationView
    private val PERMISSIONS_CODE = 100
    private val shoppingListFragment = ShoppingListFragment()

    private lateinit var beaconManager: BeaconManager
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var needBLERequest = true
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.NFC
    )

    private var flag = false
    private val region = Region("altbeacon",null,null,null)
    private val BEACON_UUID = "fda50693-a4e2-4fb1-afcf-c6eb07647825"
    private val BEACON_MAJOR = "10004"
    private val BEACON_MINOR = "54480"
    private val STORE_DISTANCE = 0.5

    private lateinit var AccelometerSensor: Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorEventListener: SensorEventListener

    var lastTime: Long = 0
    val SHAKE_THRESHOLD = 500
    var DATA_X = SensorManager.DATA_X
    var DATA_Y = SensorManager.DATA_Y
    var DATA_Z = SensorManager.DATA_Z
    var lastX:Float = 0.0F
    var lastY:Float = 0.0F
    var lastZ:Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 가장 첫 화면은 홈 화면의 Fragment로 지정
        setNdef()
        setBeacon()
        initSensor()
        createNotificationChannel("ssafy_channel", "ssafy")
        checkPermissions()
        startScan()

        initUserStampLevel()


        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout_main, OrderFragment())
            .commit()

        bottomNavigation = findViewById(R.id.tab_layout_bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.navigation_page_2 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_main, OrderFragment())
                        .commit()
                    true
                }
                R.id.navigation_page_3 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_main, MypageFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            // 재선택시 다시 랜더링 하지 않기 위해 수정
            if(bottomNavigation.selectedItemId != item.itemId){
                bottomNavigation.selectedItemId = item.itemId
            }
        }
    }

    private fun initUserStampLevel() {
        val user = ApplicationClass.sharedPreferencesUtil.getUser()
        val service = ApplicationClass.retrofit.create(UserApi::class.java)

        service.getInfo(user).enqueue(object : Callback<HashMap<String, Any>> {
            override fun onResponse(
                call: Call<HashMap<String, Any>>,
                response: Response<HashMap<String, Any>>
            ) {
                val grade = Gson().fromJson(response.body()?.get("grade").toString(), Grade::class.java) //  서버에서 갖고와서
                ApplicationClass.sharedPreferencesUtil.setGradeTitle(grade.title) // SharedPrefs로 저장해줌
                ApplicationClass.sharedPreferencesUtil.setGradeStep(grade.step)
            }

            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }

        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("INFO", "onNewIntent called...")
        if (intent!!.action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) || intent.action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            shoppingListFragment.getNFCData(intent)
        }
    }


    fun openFragment(index:Int, key:String, value:Int){
        moveFragment(index, key, value)
    }

    fun openFragment(index: Int) {
        moveFragment(index, "", 0)
    }

    private fun moveFragment(index:Int, key:String, value:Int){
        val transaction = supportFragmentManager.beginTransaction()
        when(index){
            //장바구니
            1 -> transaction.replace(R.id.frame_layout_main, shoppingListFragment)
                .addToBackStack(null)
            //주문 상세 보기
            2 -> transaction.replace(R.id.frame_layout_main, OrderDetailFragment.newInstance(key, value))
                .addToBackStack(null)
            //메뉴 상세 보기
            3 -> transaction.replace(R.id.frame_layout_main, MenuDetailFragment.newInstance(key, value))
                .addToBackStack(null)
            //map으로 가기
            4 -> transaction.replace(R.id.frame_layout_main, MapFragment())
                .addToBackStack(null)
            //logout
            5 -> {
                logout()
            }
        }
        transaction.commit()
    }

    fun logout(){
        //preference 지우기
        ApplicationClass.sharedPreferencesUtil.deleteUser()

        //화면이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }

    fun hideBottomNav(state : Boolean){
        bottomNavigation = findViewById(R.id.tab_layout_bottom_navigation)
        if(state) bottomNavigation.visibility =  View.GONE
        else bottomNavigation.visibility = View.VISIBLE
    }

    private fun setNdef(){}

    private fun initSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        AccelometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorEventListener = AccelometerListener()

        sensorManager.unregisterListener(sensorEventListener)
    }

    private inner class AccelometerListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            var currentTime = System.currentTimeMillis();
            var gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 800) {
                lastTime = currentTime;
                var x = event.values[SensorManager.DATA_X];
                var y = event.values[SensorManager.DATA_Y];
                var z = event.values[SensorManager.DATA_Z];

                var speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    showMemberShipDialog()
//                    Toast.makeText(this@MainActivity, "가속도 센서 감지", Toast.LENGTH_SHORT).show()
                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    }

    fun showMemberShipDialog() {
        val builder = AlertDialog.Builder(this)

        val v1 = layoutInflater.inflate(R.layout.dialog_membership, null)
        builder.setView(v1)

        builder.setNegativeButton("닫기", null)
        builder.setNeutralButton("QR 체크인"
        ) { dialog, id ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout_main, WebViewFragment())
                .addToBackStack(null)
                .commit()
        }
        builder.show()
    }

    private fun setBeacon() {
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    // NotificationChannel 설정
    private fun createNotificationChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)

            val notificationManager: NotificationManager
                    = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                PERMISSIONS_CODE
            )
        }
    }

    private fun isYourBeacon(beacon: Beacon): Boolean {
        return (beacon.id2.toString() == BEACON_MAJOR &&
                beacon.id3.toString() == BEACON_MINOR &&
                beacon.distance <= STORE_DISTANCE
                )
    }

    private fun isEnableBLEService(): Boolean{
        if(!bluetoothAdapter!!.isEnabled){
            return false
        }
        return true
    }

    private fun requestEnableBLE(){
        val callBLEEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        requestBLEActivity.launch(callBLEEnableIntent)
        Log.d(TAG, "requestEnableBLE: ")
    }

    private val requestBLEActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        // 사용자의 블루투스 사용이 가능한지 확인
        if (isEnableBLEService()) {
            needBLERequest = false
            startScan()
        }
    }

    private fun startScan() {
        // 블루투스 Enable 확인
        if(!isEnableBLEService()){
            requestEnableBLE()
            Log.d(TAG, "startScan: 블루투스가 켜지지 않았습니다.")
            return
        }

        // 위치 정보 권한 허용 및 GPS Enable 여부 확인
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                PERMISSIONS_CODE
            )
        }
        Log.d(TAG, "startScan: beacon Scan start")

        // Beacon Service bind
        beaconManager.bind(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(object : MonitorNotifier {

            override fun didEnterRegion(region: Region?) {
                try {
                    Log.d(TAG, "비콘을 발견하였습니다.------------${region.toString()}")
                    beaconManager.startRangingBeaconsInRegion(region!!)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun didExitRegion(region: Region?) {
                try {
                    Log.d(TAG, "비콘을 찾을 수 없습니다.")
                    beaconManager.stopRangingBeaconsInRegion(region!!)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun didDetermineStateForRegion(i: Int, region: Region?) {}
        })

        beaconManager.addRangeNotifier { beacons, region ->
            for (beacon in beacons) {
                // Major, Minor로 Beacon 구별, 1미터 이내에 들어오면 메세지 출력

                if(isYourBeacon(beacon)){
                    // 한번만 띄우기 위한 조건
                    if (flag == false) {
                        Log.d(TAG, "distance: " + beacon.distance + " Major : " + beacon.id2 + ", Minor" + beacon.id3)


                        val order_service = ApplicationClass.retrofit.create(OrderApi::class.java)
                        order_service.getLastMonthOrder(ApplicationClass.sharedPreferencesUtil.getUser().id).enqueue(object : Callback<List<LatestOrderResponse>> {
                            override fun onResponse(call: Call<List<LatestOrderResponse>>, response: Response<List<LatestOrderResponse>>) {
                                if (response.isSuccessful) {
                                    val list = response.body() as List<LatestOrderResponse>
                                    val formatter = SimpleDateFormat("yyyy.MM.dd")
                                    val date = formatter.format(list[list.size - 1].orderDate)
                                    val userOrder = UserOrderDetail(list[list.size - 1].orderId, date, list[list.size - 1].orderCnt, list[list.size - 1].productPrice * list[list.size - 1].orderCnt, list[list.size - 1].img,  1, list[list.size - 1].productName)
                                    Log.d("최근 주문 불러오기", "완료 = ${userOrder}")
                                    val binding : DialogBeaconBinding = DialogBeaconBinding.inflate(layoutInflater)
                                    Glide.with(this@MainActivity)
                                        .load("${ApplicationClass.MENU_IMGS_URL}${userOrder.img}")
                                        .into(binding.dialogCoffeeImg)
                                    binding.dialogCoffeeMenu.text = userOrder.productName
                                    binding.dialogCoffeePrice.text = userOrder.sumPrice.toString()
                                    val dialog = AlertDialog.Builder(this@MainActivity).setView(binding.root)
                                        .setNegativeButton("확인") { dialog, which ->
                                            flag = false
                                            dialog.dismiss()
                                        }.create()
                                    dialog.show()
                                    flag = true
                                }
                            }
                            override fun onFailure(call: Call<List<LatestOrderResponse>>, t: Throwable) {
                                Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
        }
        try {
            beaconManager.startMonitoringBeaconsInRegion(region)
        } catch (e: RemoteException){
            e.printStackTrace()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_CODE -> {
                if(grantResults.isNotEmpty()) {
                    for((i, permission) in permissions.withIndex()) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //권한 획득 실패
                            Log.e(TAG, "$permission 권한 획득에 실패하였습니다.")
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(
            sensorEventListener,
            AccelometerSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }

}