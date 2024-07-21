package fr.jorisfavier.youshallnotpass.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class CustomLifecycle(
    parentLifecycle: Lifecycle,
) : LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle = lifecycleRegistry

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        parentLifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
            }

            override fun onResume(owner: LifecycleOwner) {
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }

            override fun onPause(owner: LifecycleOwner) {
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
            }

            override fun onStop(owner: LifecycleOwner) {
                lifecycleRegistry.currentState = Lifecycle.State.CREATED
            }

            override fun onDestroy(owner: LifecycleOwner) {
                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            }
        })
    }

    fun pause() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun resume() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }


}