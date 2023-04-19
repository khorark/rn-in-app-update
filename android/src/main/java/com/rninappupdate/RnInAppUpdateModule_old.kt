package com.rninappupdate

import android.content.IntentSender
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import java.util.Objects

class RnInAppUpdateModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun checkUpdate() {
    val appUpdateManager = AppUpdateManagerFactory.create(reactContext)

    // Returns an intent object that you use to check for an update.
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo

    // Checks that the platform will allow the specified type of update.
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
      if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
    ) {
        try {
          appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE,
            this,
            MY_REQUEST_CODE)
        } catch (e: IntentSender.SendIntentException) {
          e.printStackTrace()
        }
    } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
        try {
          appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            this,
            MY_REQUEST_CODE)
        } catch (e: IntentSender.SendIntentException) {
          e.printStackTrace()
        }
      }
    }
  }

  fun popupSnackbarForCompleteUpdate() {
    Snackbar.make(
      Objects.requireNonNull(reactContext
        .getCurrentActivity())
        .findViewById(R.id.activity_main_layout),
      "CS.MONEY has just downloaded an update.",
      Snackbar.LENGTH_INDEFINITE
    ).apply {
      setAction("RESTART") { appUpdateManager.completeUpdate() }
      setActionTextColor(resources.getColor(R.color.snackbar_action_text_color))
      show()
    }
  }

  companion object {
    const val NAME = "RnInAppUpdate"
  }
}
