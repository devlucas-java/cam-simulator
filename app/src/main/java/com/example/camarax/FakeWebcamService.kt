package com.example.camarax

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.core.app.NotificationCompat

// O FakeWebcamService é um serviço em segundo plano que simula uma webcam usando um vídeo como fonte.
// Ele exibe um vídeo em uma janela de sobreposição (overlay) e é executado em primeiro plano para
// garantir que o sistema não o finalize enquanto está em execução.

class FakeWebcamService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private lateinit var windowManager: WindowManager // Gerenciador de janelas para criar a sobreposição
    private lateinit var surfaceView: SurfaceView // SurfaceView para exibir o vídeo
    private var mediaPlayer: MediaPlayer? = null // MediaPlayer para reproduzir o vídeo
    private var videoUri: Uri? = null // URI do vídeo que será exibido

    override fun onCreate() {
        super.onCreate()

        // Cria o canal de notificação (necessário para Android 8.0 ou superior)
        createNotificationChannel()

        // Cria a notificação para o serviço em primeiro plano
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification) // Inicia o serviço em primeiro plano com a notificação

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager // Inicializa o gerenciador de janelas
        surfaceView = SurfaceView(this) // Inicializa o SurfaceView para exibir o vídeo

        // Configura o SurfaceHolder do SurfaceView para controlar a reprodução do vídeo
        setupSurfaceHolder()
    }

    // Configura o SurfaceHolder para gerenciar o vídeo exibido
    private fun setupSurfaceHolder() {
        val holder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // Quando o Surface for criado, inicializa o MediaPlayer com o vídeo
                videoUri?.let { uri ->
                    initMediaPlayer(uri, holder)
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Não é necessário implementar
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // Libera o MediaPlayer quando o Surface for destruído
                releaseMediaPlayer()
            }
        })
    }

    // Método chamado quando o serviço é iniciado
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Recupera o URI do vídeo da intenção
        intent?.getStringExtra(EXTRA_VIDEO_URI)?.let { uriString ->
            videoUri = Uri.parse(uriString)
            addOverlay() // Adiciona a janela de sobreposição
        }

        return START_STICKY // O serviço será reiniciado se o sistema o matar
    }

    // Adiciona a janela de sobreposição para exibir o vídeo
    private fun addOverlay() {
        // Configura os parâmetros da janela de sobreposição
        val params = WindowManager.LayoutParams(
            1, // Largura mínima (invisível para o usuário)
            1, // Altura mínima (invisível para o usuário)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, // Tipo da janela, depende da versão do Android
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or // A janela não recebe foco
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or // A janela não é tocável
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, // Aceleração por hardware
            PixelFormat.TRANSLUCENT // Transparente
        )

        params.gravity = Gravity.TOP or Gravity.START // Posiciona a janela no canto superior esquerdo

        try {
            windowManager.addView(surfaceView, params) // Adiciona a sobreposição à tela
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Inicializa o MediaPlayer para reproduzir o vídeo no SurfaceHolder
    private fun initMediaPlayer(uri: Uri, holder: SurfaceHolder) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri) // Define o URI do vídeo
                setDisplay(holder) // Define o SurfaceHolder para exibir o vídeo
                setOnPreparedListener(this@FakeWebcamService) // Define o listener para quando o vídeo estiver pronto
                setOnCompletionListener(this@FakeWebcamService) // Define o listener para quando o vídeo terminar
                isLooping = true // Faz o vídeo ficar em loop
                prepareAsync() // Prepara o MediaPlayer de forma assíncrona
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Método chamado quando o vídeo está pronto para ser reproduzido
    override fun onPrepared(mp: MediaPlayer) {
        mp.start() // Inicia a reprodução do vídeo
    }

    // Método chamado quando o vídeo é completado
    override fun onCompletion(mp: MediaPlayer) {
        // Como o vídeo está em loop, ele nunca vai parar, mas esse método é chamado em caso de erro
        mp.start() // Reinicia o vídeo se ele tiver sido interrompido
    }

    // Libera os recursos do MediaPlayer
    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            stop() // Para a reprodução
            release() // Libera os recursos do MediaPlayer
        }
        mediaPlayer = null
    }

    // Cria o canal de notificação (necessário para Android 8.0 ou superior)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fake Webcam Service", // Nome do canal
                NotificationManager.IMPORTANCE_LOW // Prioridade baixa para não ser intrusivo
            ).apply {
                description = "Serviço de simulação de webcam em execução" // Descrição do canal
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel) // Cria o canal de notificação
        }
    }

    // Cria a notificação que será exibida enquanto o serviço estiver em execução
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fake Webcam") // Título da notificação
            .setContentText("Simulação de webcam em execução") // Texto da notificação
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ícone da notificação
            .setPriority(NotificationCompat.PRIORITY_LOW) // Prioridade da notificação
            .build()
    }

    // Método chamado quando o serviço é destruído
    override fun onDestroy() {
        super.onDestroy()
        try {
            windowManager.removeView(surfaceView) // Remove a sobreposição da tela
        } catch (e: Exception) {
            e.printStackTrace()
        }
        releaseMediaPlayer() // Libera o MediaPlayer
    }

    // O método onBind não é utilizado, já que o serviço não é vinculado a uma atividade
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        // Chave para passar o URI do vídeo no Intent
        const val EXTRA_VIDEO_URI = "extra_video_uri"

        // ID do canal de notificação
        private const val CHANNEL_ID = "fake_webcam_channel"

        // ID da notificação
        private const val NOTIFICATION_ID = 1001
    }
}
