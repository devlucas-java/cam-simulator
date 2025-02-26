package com.example.camarax

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class VideoSimulatedService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Inicializa o MediaPlayer para reproduzir o vídeo
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource("caminho/do/seu/video.mp4") // Caminho do arquivo de vídeo
        mediaPlayer?.prepare()
        mediaPlayer?.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
    return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Libere o MediaPlayer quando o serviço for destruído
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
