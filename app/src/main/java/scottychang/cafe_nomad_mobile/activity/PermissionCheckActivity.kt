package scottychang.cafe_nomad_mobile.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class PermissionCheckActivity : AppCompatActivity() {
    private val PERMISSION_ALL = 1
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        } else {
            MapActivity.go(this, false)
        }
    }

    private fun hasPermissions(): Boolean {
        for (permission in PERMISSIONS) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        for (grantResult:Int in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
                Handler().postDelayed({finish()}, 3000)
                return
            }
        }
        MapActivity.go(this, true)
    }
}