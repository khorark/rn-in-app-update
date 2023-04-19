package com.rninappupdate

import android.R
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.BaseActivityEventListener
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
import java.util.Objects

class RnInAppUpdateModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), InstallStateUpdatedListener, LifecycleEventListener {
  private var appUpdateManager: AppUpdateManager? = null
  private val mActivityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, intent: Intent?) {
      if (requestCode == UPDATE_REQUEST_CODE) {
        if (resultCode != Activity.RESULT_OK) {
        }
      }
    }
  }

  init {
    reactContext = context
    reactContext.addActivityEventListener(mActivityEventListener)
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

    val toast = Toast.makeText(applicationContext, "Start check update", Toast.LENGTH_SHORT).show()

    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
      if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.clientVersionStalenessDays() != null && appUpdateInfo.clientVersionStalenessDays()!! > UPDATE_STALE_DAYS && appUpdateInfo.isUpdateTypeAllowed(
          AppUpdateType.IMMEDIATE
        )
      ) {
        try {
          appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE,
            reactContext.getCurrentActivity(),
            UPDATE_REQUEST_CODE
          )
        } catch (e: SendIntentException) {
          e.printStackTrace()
        }
      } else {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
          && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        ) {
          try {
            appUpdateManager.startUpdateFlowForResult(
              appUpdateInfo,
              AppUpdateType.FLEXIBLE,
              reactContext.getCurrentActivity(),
              UPDATE_REQUEST_CODE
            )
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
    val snackbar: Snackbar = Snackbar.make(
      Objects.requireNonNull(
        reactContext
          .getCurrentActivity()
      )
        .findViewById(R.id.content)
        .getRootView(),
      "CS.MONEY has just downloaded an update",
      Snackbar.LENGTH_INDEFINITE
    )
    snackbar.setAction("RESTART") { view -> appUpdateManager!!.completeUpdate() }
    snackbar.show()
  }

  fun onHostResume() {
    if (appUpdateManager != null) {
      appUpdateManager!!
        .appUpdateInfo
        .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
          if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
          }
          if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            try {
              appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                reactContext.getCurrentActivity(),
                UPDATE_REQUEST_CODE
              )
            } catch (e: SendIntentException) {
              e.printStackTrace()
            }
          }
        }
    }
  }

  //  abstract method 'onHostPause()' in 'LifecycleEventListener'
  fun onHostPause() {}
  fun onHostDestroy() {
    if (appUpdateManager != null) {
      appUpdateManager!!.unregisterListener(this)
    }
  }

  companion object {
    private var reactContext: ReactApplicationContext
    private const val UPDATE_STALE_DAYS = 5
    private const val UPDATE_REQUEST_CODE = 0
    const val NAME = "RnInAppUpdate"
  }
}
