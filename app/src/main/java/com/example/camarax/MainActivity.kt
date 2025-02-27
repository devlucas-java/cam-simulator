package com.example.camarax

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// A MainActivity é a tela principal do seu aplicativo.
// Ela gerencia a seleção de um vídeo e o controle de um serviço de webcam fake.

class MainActivity : AppCompatActivity() {

    // Declaração das variáveis que representam os componentes da interface do usuário.
    private lateinit var btnSelectVideo: Button // Botão para selecionar o vídeo
    private lateinit var btnStartService: Button // Botão para iniciar o serviço da webcam fake
    private lateinit var btnStopService: Button // Botão para parar o serviço da webcam fake
    private lateinit var textSelectedVideo: TextView // TextView para exibir o nome do vídeo selecionado
    private lateinit var videoPreview: VideoView // VideoView para exibir o preview do vídeo

    // Variável que armazenará o URI do vídeo selecionado
    private var selectedVideoUri: Uri? = null

    // Gerenciador de resultado para selecionar um vídeo da galeria
    private val selectVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedVideoUri = it // Armazena o URI do vídeo selecionado
            textSelectedVideo.text = "Vídeo selecionado: ${getVideoName(it)}" // Exibe o nome do vídeo selecionado

            // Configura o preview do vídeo no VideoView
            videoPreview.setVideoURI(it)
            videoPreview.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true // Faz o vídeo ficar em loop
                videoPreview.start() // Inicia a reprodução do vídeo
            }

            btnStartService.isEnabled = true // Habilita o botão de iniciar serviço
        }
    }

    // Gerenciador de resultado para solicitar permissão de overlay (sobreposição de tela)
    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(this)) { // Verifica se a permissão foi concedida
            startFakeWebcamService() // Se a permissão foi concedida, inicia o serviço da webcam fake
        } else {
            Toast.makeText(this, "Permissão de overlay negada", Toast.LENGTH_SHORT).show() // Se negado, exibe uma mensagem
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Define o layout da activity

        // Inicializa os componentes da interface
        btnSelectVideo = findViewById(R.id.btnSelectVideo)
        btnStartService = findViewById(R.id.btnStartService)
        btnStopService = findViewById(R.id.btnStopService)
        textSelectedVideo = findViewById(R.id.textSelectedVideo)
        videoPreview = findViewById(R.id.videoPreview)

        // Configura o click listener para o botão de selecionar vídeo
        btnSelectVideo.setOnClickListener {
            checkAndRequestPermissions() // Verifica e solicita as permissões necessárias
        }

        // Configura o click listener para o botão de iniciar serviço
        btnStartService.setOnClickListener {
            // Verifica se a permissão de overlay foi concedida
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                overlayPermissionLauncher.launch(intent) // Solicita permissão para sobreposição
            } else {
                startFakeWebcamService() // Se a permissão já foi concedida ou não é necessária, inicia o serviço
            }
        }

        // Configura o click listener para o botão de parar serviço
        btnStopService.setOnClickListener {
            stopFakeWebcamService() // Para o serviço da webcam fake
        }
    }

    // Função para verificar e solicitar as permissões necessárias
    private fun checkAndRequestPermissions() {
        // Define as permissões necessárias para o aplicativo
        val permissions = arrayOf(
            Manifest.permission.CAMERA, // Permissão para acessar a câmera
            Manifest.permission.READ_EXTERNAL_STORAGE, // Permissão para ler armazenamento externo
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Permissão para escrever no armazenamento externo
            Manifest.permission.RECORD_AUDIO, // Permissão para gravar áudio
            Manifest.permission.SYSTEM_ALERT_WINDOW, // Permissão para sobreposição de tela
            Manifest.permission.INTERNET, // Permissão para acessar a internet
            Manifest.permission.ACCESS_NETWORK_STATE // Permissão para acessar o estado da rede
        )

        // Filtra as permissões que ainda não foram concedidas
        val notGrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            // Se alguma permissão não foi concedida, solicita as permissões necessárias
            ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            // Se todas as permissões foram concedidas, lança o seletor de vídeo
            selectVideoLauncher.launch("video/*")
        }
    }

    // Callback que recebe o resultado da solicitação de permissões
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Se todas as permissões foram concedidas, lança o seletor de vídeo
                selectVideoLauncher.launch("video/*")
            } else {
                // Caso alguma permissão tenha sido negada, exibe uma mensagem
                Toast.makeText(this, "Permissões necessárias para funcionar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função que retorna o nome do vídeo a partir do URI
    private fun getVideoName(uri: Uri): String {
        val projection = arrayOf(android.provider.MediaStore.Video.Media.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return uri.lastPathSegment ?: "Vídeo desconhecido" // Se não encontrar o nome, retorna o caminho do URI
    }

    // Função que inicia o serviço da webcam fake com o vídeo selecionado
    @SuppressLint("ObsoleteSdkInt")
    private fun startFakeWebcamService() {
        selectedVideoUri?.let { uri ->
            val intent = Intent(this, FakeWebcamService::class.java).apply {
                putExtra(FakeWebcamService.EXTRA_VIDEO_URI, uri.toString()) // Passa o URI do vídeo para o serviço
            }

            // Inicia o serviço como um serviço em primeiro plano (para Android 8.0 ou superior)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent) // Para versões anteriores, usa startService
            }

            btnStartService.isEnabled = false // Desabilita o botão de iniciar o serviço
            btnStopService.isEnabled = true // Habilita o botão de parar o serviço
            Toast.makeText(this, "Serviço de webcam fake iniciado", Toast.LENGTH_SHORT).show() // Exibe mensagem de sucesso
        }
    }

    // Função que para o serviço da webcam fake
    private fun stopFakeWebcamService() {
        val intent = Intent(this, FakeWebcamService::class.java)
        stopService(intent) // Para o serviço da webcam fake

        btnStartService.isEnabled = selectedVideoUri != null // Habilita o botão de iniciar o serviço se um vídeo foi selecionado
        btnStopService.isEnabled = false // Desabilita o botão de parar o serviço
        Toast.makeText(this, "Serviço de webcam fake parado", Toast.LENGTH_SHORT).show() // Exibe mensagem de parada
    }

    // Código para controle de permissão
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100 // Código de solicitação de permissões
    }
}
