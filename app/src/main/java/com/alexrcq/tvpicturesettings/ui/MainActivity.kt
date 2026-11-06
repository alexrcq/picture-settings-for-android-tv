package com.alexrcq.tvpicturesettings.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.platform.Device
import com.alexrcq.tvpicturesettings.platform.android.PermissionGranter
import com.alexrcq.tvpicturesettings.service.MtkWhiteBalanceLocker
import com.alexrcq.tvpicturesettings.service.ScreenManager
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.LoadingDialog
import com.alexrcq.tvpicturesettings.ui.viewmodel.SetupSideEffect
import com.alexrcq.tvpicturesettings.ui.viewmodel.SetupState
import com.alexrcq.tvpicturesettings.ui.viewmodel.SetupViewModel
import com.alexrcq.tvpicturesettings.util.hasPermission
import com.alexrcq.tvpicturesettings.util.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: SetupViewModel by viewModels {
        val app = application as App
        SetupViewModel.provideFactory(
            PermissionGranter(applicationContext, app.adbShell), app.developerSettings
        )
    }

    private val requiredPermissions = listOfNotNull(
        Manifest.permission.WRITE_SECURE_SETTINGS,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.SCHEDULE_EXACT_ALARM else null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Device.isSupported(this)) {
            showDeviceNotSupported()
            return
        }
        setContentView(R.layout.activity_main)
        setupWindow()
        setupObservers()
        startServices()
        ensurePermissionsGranted()
    }

    private fun setupWindow() {
        window.setGravity(Gravity.END)
        window.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::handleSetupState) }
                launch { viewModel.sideEffects.collect(::handleSetupSideEffect) }
            }
        }
    }

    private fun handleSetupState(state: SetupState) {
        when (state) {
            SetupState.Idle -> hideLoading()
            SetupState.Processing -> showLoading()
        }
    }

    private fun handleSetupSideEffect(sideEffect: SetupSideEffect) {
        when (sideEffect) {
            is SetupSideEffect.AdbRequired -> showAdbRequiredDialog()
            is SetupSideEffect.PermissionsGranted -> onPermissionsGranted()
            is SetupSideEffect.GrantPermissionsTimeout -> showGrantPermissionsRetryDialog(sideEffect.permissions)
            is SetupSideEffect.Error -> showToast(sideEffect.message)
        }
    }

    private fun onPermissionsGranted() {
        showToast(getString(R.string.permissions_granted))
        lifecycleScope.launch {
            delay(3000L)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                showUsbDebuggingImpactDialog()
            }
        }
    }

    private fun ensurePermissionsGranted() {
        val missingPermissions = getMissingPermissions()
        if (missingPermissions.isNotEmpty()) {
            showGrantPermissionsDialog(missingPermissions)
        }
    }

    private fun getMissingPermissions(): List<String> = requiredPermissions.filter { !hasPermission(it) }

    private fun startServices() {
        ScreenManager.startForeground(this)
        val isWhiteBalanceLocked = (application as App).videoPreferences.isWhiteBalanceLocked
        if (isWhiteBalanceLocked) {
            MtkWhiteBalanceLocker.startForeground(this)
        }
    }

    private fun showLoading() {
        if (supportFragmentManager.findFragmentByTag(LoadingDialog.TAG) == null) {
            LoadingDialog().show(supportFragmentManager, LoadingDialog.TAG)
        }
    }

    private fun hideLoading() {
        (supportFragmentManager.findFragmentByTag(LoadingDialog.TAG) as DialogFragment?)?.dismiss()
    }

    private fun showDeviceNotSupported() {
        val dialog = AlertDialog.Builder(this)
            .setMessage(R.string.device_not_supported)
            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus()
    }

    private fun showAdbRequiredDialog() {
        val dialog = AlertDialog.Builder(this)
            .setMessage(R.string.adb_debugging_required)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus()
    }

    private fun showGrantPermissionsDialog(permissions: List<String>) {
        val dialog = AlertDialog.Builder(this)
            .setMessage(R.string.wait_for_debug_window)
            .setPositiveButton(R.string.grant_permissions) { _, _ ->
                viewModel.grantPermissions(permissions)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus()
    }

    private fun showGrantPermissionsRetryDialog(permissions: List<String>) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.adb_timeout_title)
            .setMessage(R.string.adb_timeout_message)
            .setPositiveButton(R.string.retry) { _, _ ->
                viewModel.grantPermissions(permissions)
            }
            .setNegativeButton(R.string.exit) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus()
    }

    private fun showUsbDebuggingImpactDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.usb_debugging_impact_warning))
            .setPositiveButton(getString(R.string.skip)) { _, _ -> }
            .setNegativeButton(getString(R.string.disable_usb_debugging)) { _, _ ->
                viewModel.setAdbEnabled(false)
            }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus()
    }
}
