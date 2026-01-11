package com.prashantpizza.nofsdotaca.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

object AppStateTracker : Application.ActivityLifecycleCallbacks {
    
    private const val TAG = "AppStateTracker"
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    
    var isAppInForeground = false
        private set
    
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: ${activity.localClassName}")
    }
    
    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            isAppInForeground = true
            Log.d(TAG, "App entered foreground")
        }
    }
    
    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, "onActivityResumed: ${activity.localClassName}")
    }
    
    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, "onActivityPaused: ${activity.localClassName}")
    }
    
    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            isAppInForeground = false
            Log.d(TAG, "App entered background")
        }
    }
    
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d(TAG, "onActivitySaveInstanceState: ${activity.localClassName}")
    }
    
    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG, "onActivityDestroyed: ${activity.localClassName}")
    }
}
