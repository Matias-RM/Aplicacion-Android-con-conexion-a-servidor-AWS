package com.example.tallercomputacionconexionaws

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val baseUrl = "http://18.234.63.48:5000"

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGetUser: Button
    private lateinit var tvResult: TextView

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGetUser = findViewById(R.id.btnGetUser)
        tvResult = findViewById(R.id.tvResult)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            val jsonBody = JSONObject()
            jsonBody.put("username", user)
            jsonBody.put("password", pass)

            performHttpRequest("$baseUrl/api/login", "POST", jsonBody.toString())
        }

        btnGetUser.setOnClickListener {
            val user = etUsername.text.toString().trim()

            val requestUrl = if (user.isNotEmpty()) {
                "$baseUrl/api/user?username=$user"
            } else {
                "$baseUrl/api/user"
            }

            performHttpRequest(requestUrl, "GET", null)
        }
            //performHttpRequest("$baseUrl/api/user", "GET", null)

    }

    private fun performHttpRequest(urlString: String, method: String, jsonInput: String?) {
        tvResult.text = "Cargando..."

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = method
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")

                // Si es un POST y hay cuerpo JSON, enviarlo
                if (method == "POST" && jsonInput != null) {
                    connection.doOutput = true
                    val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                    outputStreamWriter.write(jsonInput)
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val reader = BufferedReader(InputStreamReader(stream))
                val stringBuilder = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                reader.close()

                val responseText = stringBuilder.toString()

                // Actualizar interfaz en el hilo principal
                mainHandler.post {
                    formatAndDisplayResponse(responseText)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post {
                    tvResult.text = "Error de conexión: ${e.localizedMessage}"
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun formatAndDisplayResponse(jsonRaw: String) {
        try {
            val jsonResponse = JSONObject(jsonRaw)
            val status = jsonResponse.optString("status")

            if (jsonResponse.has("data")) {
                val data = jsonResponse.getJSONObject("data")
                val name = data.getString("name")
                val email = data.getString("email")
                val rut = data.getString("rut")

                tvResult.text = "--- Datos de Usuario ---\nNombre: $name\nEmail: $email\nRUT: $rut"
            } else if (jsonResponse.has("user")) {
                val message = jsonResponse.getString("message")
                val user = jsonResponse.getJSONObject("user")
                val name = user.getString("name")

                tvResult.text = "¡$message!\nBienvenido, $name"
            } else {
                val message = jsonResponse.optString("message", "Respuesta recibida")
                tvResult.text = "Estado: $status\nMensaje: $message"
            }
        } catch (e: Exception) {
            tvResult.text = "Respuesta del servidor: $jsonRaw"
        }
    }
}