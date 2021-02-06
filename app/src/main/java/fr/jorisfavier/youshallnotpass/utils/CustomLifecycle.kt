package fr.jorisfavier.youshallnotpass.utils

import androidx.lifecycle.*

class CustomLifecycle(parentLifecycleOwner: LifecycleOwner) : LifecycleOwner, LifecycleObserver {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        parentLifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun doOnStart() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun doOnResume() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun doOnPause() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun doOnStop() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun doOnDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}