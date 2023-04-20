package com.rninappupdate

import android.content.IntentSender.SendIntentException
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class RnInAppUpdateModule(private var reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), InstallStateUpdatedListener, LifecycleEventListener {
  private var appUpdateManager: AppUpdateManager? = null

  init {
    reactContext.addLifecycleEventListener(this)
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun checkUpdate() {
    appUpdateManager = AppUpdateManagerFactory.create(reactContext)
    appUpdateManager!!.registerListener(this)
    val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo

    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
      if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.clientVersionStalenessDays() != null && appUpdateInfo.clientVersionStalenessDays()!! > UPDATE_STALE_DAYS && appUpdateInfo.isUpdateTypeAllowed(
          AppUpdateType.IMMEDIATE
        )
      ) {
        try {
          reactContext.currentActivity?.let {
            appUpdateManager!!.startUpdateFlowForResult(
              appUpdateInfo,
              AppUpdateType.IMMEDIATE,
              it,
              UPDATE_REQUEST_CODE
            )
          }
        } catch (e: SendIntentException) {
          e.printStackTrace()
        }
      } else {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
          && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        ) {
          try {
            reactContext.currentActivity?.let {
              appUpdateManager!!.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                it,
                UPDATE_REQUEST_CODE
              )
            }
          } catch (e: SendIntentException) {
            e.printStackTrace()
          }
        }
      }
    }
  }

  override fun onStateUpdate(state: InstallState) {
    if (state.installStatus() == InstallStatus.DOWNLOADED) {
      popupSnackbarForCompleteUpdate()
    }
  }

  private fun popupSnackbarForCompleteUpdate() {
    Snackbar.make(
      reactContext.currentActivity!!.window.decorView.rootView,
      "CS.MONEY has just downloaded an update.",
      Snackbar.LENGTH_INDEFINITE
    ).apply {
      setAction("RESTART") { appUpdateManager?.completeUpdate() }
      show()
    }
  }

  override fun onHostResume() {
    if (appUpdateManager != null) {
      appUpdateManager!!
        .appUpdateInfo
        .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
          if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
          }
          if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            try {
              reactContext.currentActivity?.let {
                appUpdateManager!!.startUpdateFlowForResult(
                  appUpdateInfo,
                  AppUpdateType.IMMEDIATE,
                  it,
                  UPDATE_REQUEST_CODE
                )
              }
            } catch (e: SendIntentException) {
              e.printStackTrace()
            }
          }
        }
    }
  }

  override fun onHostPause() {}
  override fun onHostDestroy() {
    if (appUpdateManager != null) {
      appUpdateManager!!.unregisterListener(this)
    }
  }


  companion object {
    private const val UPDATE_STALE_DAYS = 5
    private const val UPDATE_REQUEST_CODE = 0
    const val NAME = "RnInAppUpdate"
  }
}
