package com.masterwok.simplevlcplayer.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableFragment
import com.masterwok.simplevlcplayer.services.MediaPlayerService
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder

abstract class MediaPlayerServiceFragment : InjectableFragment() {

    protected var serviceBinder: MediaPlayerServiceBinder? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceBinder = null
        }

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            serviceBinder = binder as? MediaPlayerServiceBinder
            val startFrom :Int = 0
            onServiceConnected(startFrom)
        }
    }

    protected abstract fun onServiceConnected(startFrom: Int)

    private fun bindMediaPlayerService() = requireActivity().bindService(
            Intent(requireContext().applicationContext, MediaPlayerService::class.java)
            , serviceConnection
            , Context.BIND_AUTO_CREATE
    )

    override fun onStart() {
        super.onStart()

        bindMediaPlayerService()
    }

    override fun onStop() {
        activity?.unbindService(serviceConnection)

        super.onStop()
    }

}