package com.example.camarax

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.camarax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicia o serviço para simular a câmera com um vídeo
        val serviceIntent = Intent(this, VideoSimulatedService::class.java)
        startService(serviceIntent)

        // Configuração da visualização da câmera
        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT) // Use LENS_FACING_BACK para a câmera traseira
            .build()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Obtém o CameraProvider
            val cameraProvider = cameraProviderFuture.get()

            // Configura a visualização da câmera (PreviewView)
            val previewView = binding.previewView // Usando o PreviewView da view binding
            preview.setSurfaceProvider(previewView.surfaceProvider)

            try {
                // Vincula a câmera ao ciclo de vida da Activity
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e("CameraX", "Falha ao iniciar a câmera", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
