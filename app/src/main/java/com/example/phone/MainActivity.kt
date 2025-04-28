package com.example.phone

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    // Lanzadores para permisos
    private val requestCallPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permiso CALL_PHONE concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso CALL_PHONE denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestReadPhoneStatePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permiso READ_PHONE_STATE denegado", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Revisar si tenemos permiso para leer el estado del telefono
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestReadPhoneStatePermission.launch(Manifest.permission.READ_PHONE_STATE)
        }

        // Mostrar info extra al usuario al abrir la app
        showSimInfoDialog()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DialerScreen { number ->
                        makePhoneCall(number)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Verifica si ya hay una llamada en curso
        if (isCallActive()) {
            Toast.makeText(this, "Ya hay una llamada activa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall(number: String) {
        // Verificar si el numero es valido
        if (number.isBlank()) {
            Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si ya hay una llamada en curso
        if (isCallActive()) {
            Toast.makeText(this, "Ya hay una llamada activa", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si tenemos permiso para hacer llamadas
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED -> {
                startCall(number)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                // Mostrar explicación de porque se necesita el permiso
                showPermissionExplanation()
            }

            else -> {
                // Solicitar permiso si no está concedido
                requestCallPermission.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    // Empezar la llamada
    private fun startCall(number: String) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(callIntent)
        } catch (e: SecurityException) {
            // Si no tenemos permiso, usar el marcador predeterminado
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
            }
            startActivity(dialIntent)
            Toast.makeText(this, "Usando marcador predeterminado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Si hubo un error al hacer la llamada
            Toast.makeText(this, "Error al realizar la llamada", Toast.LENGTH_SHORT).show()
        }
    }

    // Mostrar un cuadro de dialogo explicando el permiso necesario
    private fun showPermissionExplanation() {
        AlertDialog.Builder(this)
            .setTitle("Permiso necesario")
            .setMessage("Para realizar llamadas directas, necesitamos el permiso de teléfono")
            .setPositiveButton("Conceder permiso") { _, _ ->
                requestCallPermission.launch(Manifest.permission.CALL_PHONE)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Verificar si hay una llamada en curso
    private fun isCallActive(): Boolean {
        return try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                telecomManager.isInCall
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showSimInfoDialog() {
        val info = getSimAndNetworkInfo()
        AlertDialog.Builder(this)
            .setTitle("Información de SIM y Red")
            .setMessage(info)
            .setPositiveButton("OK", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getSimAndNetworkInfo(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Obtener el estado de la SIM
        val simState = telephonyManager.simState
        val simStateText = when (simState) {
            TelephonyManager.SIM_STATE_READY -> "SIM lista"
            TelephonyManager.SIM_STATE_ABSENT -> "Sin SIM"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN requerido"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK requerido"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Bloqueada por red"
            else -> "Estado desconocido"
        }

        var simOperator = "Desconocido"
        var phoneNumber = "Número no disponible"
        var networkTypeName = "Desconocido"
        var roaming = false
        var signalStrength = -1

        // Revisamos si tenemos el permiso para acceder a la informacion de la SIM
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            simOperator = telephonyManager.simOperatorName ?: "Desconocido"
            phoneNumber = telephonyManager.line1Number ?: "Número no disponible"
            networkTypeName = getNetworkTypeName(telephonyManager.networkType)
            roaming = telephonyManager.isNetworkRoaming
            signalStrength = getCurrentSignalStrength()
        }

        return """
        📶 Estado de la SIM: $simStateText
        🏢 Operador: $simOperator
        ☎️ Número: $phoneNumber
        🌐 Tipo de Red: $networkTypeName
        🚀 Nivel de Señal: $signalStrength dBm
        ✈️ Roaming: ${if (roaming) "Sí" else "No"}
    """.trimIndent()
    }

    // Obtener el tipo de red
    private fun getNetworkTypeName(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G+"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            else -> "Desconocido"
        }
    }

    //Informacion de la SIM
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getCurrentSignalStrength(): Int {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val allCellInfo = telephonyManager.allCellInfo
            if (allCellInfo != null && allCellInfo.isNotEmpty()) {
                return when (val cellInfo = allCellInfo[0]) {
                    is android.telephony.CellInfoLte -> cellInfo.cellSignalStrength.dbm
                    is android.telephony.CellInfoGsm -> cellInfo.cellSignalStrength.dbm
                    is android.telephony.CellInfoWcdma -> cellInfo.cellSignalStrength.dbm
                    is android.telephony.CellInfoNr -> cellInfo.cellSignalStrength.dbm
                    else -> -1
                }
            }
        }
        return -1
    }

}
