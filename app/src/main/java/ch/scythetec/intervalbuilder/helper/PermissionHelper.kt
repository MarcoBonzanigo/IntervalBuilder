package ch.scythetec.intervalbuilder.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ch.scythetec.intervalbuilder.R
import ch.scythetec.intervalbuilder.const.Constants.Companion.PERMISSION_REQUEST_ALL
import ch.scythetec.intervalbuilder.helper.PermissionHelper.Companion.PermissionGroups.*
import java.util.*

class PermissionHelper private constructor () {

    companion object {
        enum class PermissionGroups(val label: String, val title: Int, val description: Int) {
            NETWORK("Mobile Network", R.string.title,R.string.description),
            STORAGE("Storage", R.string.title,R.string.description),
            AUDIO("Audio", R.string.title,R.string.description),
            BLUETOOTH("Bluetooth", R.string.title,R.string.description),
            WIFI("Wi-Fi", R.string.title,R.string.description),
            NFC("NFC", R.string.title,R.string.description),
            SMS("SMS", R.string.title,R.string.description),
            GPS("GPS", R.string.title,R.string.description),
            TELEPHONY("Telephony", R.string.title,R.string.description),
            DEVICE("Device", R.string.title,R.string.description);

            fun getDescription(context: Context): String {
                return context.getString(description)
            }
            fun getTitle(context: Context): String {
                return context.getString(title)
            }
        }
        private val permissionHolder: HashMap<PermissionGroups, Array<String>> = hashMapOf(
                NETWORK to arrayOf(Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.CHANGE_NETWORK_STATE),
                STORAGE to arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                AUDIO to arrayOf(Manifest.permission.RECORD_AUDIO),
                BLUETOOTH to arrayOf(Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN),
                WIFI to arrayOf(Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.ACCESS_COARSE_LOCATION),
                NFC to arrayOf(Manifest.permission.NFC),
                SMS to arrayOf(Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS),
                GPS to arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
                TELEPHONY to arrayOf(Manifest.permission.READ_PHONE_STATE,Manifest.permission.CALL_PHONE),
                DEVICE   to arrayOf(Manifest.permission.READ_PHONE_STATE,Manifest.permission.CALL_PHONE)
        )
        fun getRequiredPermissions(context: Context): ArrayList<PermissionGroups>{
            val permissions = ArrayList<PermissionGroups>()
            permissionHolder.forEach { (key,value) -> if (!check(context,*value)){
                permissions.add(key)
            } }
            return permissions
        }
        fun check(context: Context, vararg permissions: String): Boolean{
            for (permission in permissions){
                val check = (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                if (!check) {
                    return false
                }
            }
            return true
        }
        fun check(context: Context, permissionGroup: PermissionGroups): Boolean{
            val permissions = permissionHolder[permissionGroup] ?: return false
            return check(context, *permissions)
        }
        fun request(activity: Activity, permissionGroup: PermissionGroups) {
            val permissions = permissionHolder[permissionGroup] ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(permissions,PERMISSION_REQUEST_ALL)
            }else{
                ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_ALL)
            }
        }
    }
}