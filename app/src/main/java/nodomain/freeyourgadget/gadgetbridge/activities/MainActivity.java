/*  Copyright (C) 2016-2023 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Johannes Tysiak, Taavi Eomäe, vanous, Arjan Schrijver

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.BuildConfig;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class MainActivity extends AbstractGBActivity implements BottomNavigationView.OnNavigationItemSelectedListener, GBActivity {
    public static final String ACTION_REQUEST_PERMISSIONS
            = "nodomain.freeyourgadget.gadgetbridge.activities.controlcenter.requestpermissions";
    private boolean isLanguageInvalid = false;
    private static PhoneStateListener fakeStateListener;

    BottomNavigationView bottomNavigationView;
    DashboardFragment dashboardFragment = new DashboardFragment();
    ControlCenterv2 devicesFragment = new ControlCenterv2();
    MainMenuFragment mainMenuFragment = new MainMenuFragment();

    //needed for KK compatibility
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)) {
                case GBApplication.ACTION_LANGUAGE_CHANGE:
                    setLanguage(GBApplication.getLanguage(), true);
                    break;
                case GBApplication.ACTION_QUIT:
                    finish();
                    break;
                case DeviceManager.ACTION_DEVICES_CHANGED:
                case GBApplication.ACTION_NEW_DATA:
                    if (devicesFragment.isResumed()) {
                        devicesFragment.createRefreshTask("get activity data", getApplication()).execute();
//                        mGBDeviceAdapter.rebuildFolders();
                        devicesFragment.refreshPairedDevices();
                    }
                    break;
                case DeviceService.ACTION_REALTIME_SAMPLES:
                    handleRealtimeSample(intent.getSerializableExtra(DeviceService.EXTRA_REALTIME_SAMPLE));
                    break;
                case ACTION_REQUEST_PERMISSIONS:
                    checkAndRequestPermissions(false);
                    break;
            }
        }
    };
    private boolean pesterWithPermissions = true;
    private ActivitySample currentHRSample;

    public ActivitySample getCurrentHRSample() {
        return currentHRSample;
    }

    private void setCurrentHRSample(ActivitySample sample) {
        if (HeartRateUtils.getInstance().isValidHeartRateValue(sample.getHeartRate())) {
            currentHRSample = sample;
            if (devicesFragment.isResumed()) {
                devicesFragment.refreshPairedDevices();
            }
        }
    }

    private void handleRealtimeSample(Serializable extra) {
        if (extra instanceof ActivitySample) {
            ActivitySample sample = (ActivitySample) extra;
            setCurrentHRSample(sample);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        // TODO: read last view from savedInstanceState
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_devices);

        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(GBApplication.ACTION_LANGUAGE_CHANGE);
        filterLocal.addAction(GBApplication.ACTION_QUIT);
        filterLocal.addAction(GBApplication.ACTION_NEW_DATA);
        filterLocal.addAction(DeviceManager.ACTION_DEVICES_CHANGED);
        filterLocal.addAction(DeviceService.ACTION_REALTIME_SAMPLES);
        filterLocal.addAction(ACTION_REQUEST_PERMISSIONS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);

        /*
         * Ask for permission to intercept notifications on first run.
         */
        Prefs prefs = GBApplication.getPrefs();
        pesterWithPermissions = prefs.getBoolean("permission_pestering", true);

        Set<String> set = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (pesterWithPermissions) {
            if (!set.contains(this.getPackageName())) { // If notification listener access hasn't been granted
                // Put up a dialog explaining why we need permissions (Polite, but also Play Store policy)
                // When accepted, we open the Activity for Notification access
                DialogFragment dialog = new MainActivity.NotifyListenerPermissionsDialogFragment();
                dialog.show(getSupportFragmentManager(), "NotifyListenerPermissionsDialogFragment");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           /* In order to be able to set ringer mode to silent in GB's PhoneCallReceiver
           the permission to access notifications is needed above Android M
           ACCESS_NOTIFICATION_POLICY is also needed in the manifest */
            if (pesterWithPermissions) {
                if (!((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).isNotificationPolicyAccessGranted()) {
                    // Put up a dialog explaining why we need permissions (Polite, but also Play Store policy)
                    // When accepted, we open the Activity for Notification access
                    DialogFragment dialog = new MainActivity.NotifyPolicyPermissionsDialogFragment();
                    dialog.show(getSupportFragmentManager(), "NotifyPolicyPermissionsDialogFragment");
                }
            }

            if (!android.provider.Settings.canDrawOverlays(getApplicationContext())) {
                // If diplay over other apps access hasn't been granted
                // Put up a dialog explaining why we need permissions (Polite, but also Play Store policy)
                // When accepted, we open the Activity for permission to display over other apps.
                if (pesterWithPermissions) {
                    DialogFragment dialog = new MainActivity.DisplayOverOthersPermissionsDialogFragment();
                    dialog.show(getSupportFragmentManager(), "DisplayOverOthersPermissionsDialogFragment");
                }
            }

            // Check all the other permissions that we need to for Android M + later
            checkAndRequestPermissions(true);
        }

        GBApplication.deviceService().start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isLanguageInvalid) {
            isLanguageInvalid = false;
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean
    onNavigationItemSelected(@NonNull MenuItem item)
    {
        // TODO: save current view so we can restore it in onCreate()
        switch (item.getItemId()) {
            case R.id.bottom_nav_dashboard:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, dashboardFragment)
                        .commit();
                return true;

            case R.id.bottom_nav_devices:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, devicesFragment)
                        .commit();
                return true;

            case R.id.bottom_nav_menu:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, mainMenuFragment)
                        .commit();
                return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions(boolean showDialogFirst) {
        List<String> wantedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CONTACTS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.CALL_PHONE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALL_LOG);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.RECEIVE_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.SEND_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALENDAR);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MEDIA_CONTENT_CONTROL) == PackageManager.PERMISSION_DENIED)
                wantedPermissions.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        } catch (Exception ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (pesterWithPermissions) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED) {
                    wantedPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                wantedPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if (BuildConfig.INTERNET_ACCESS) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
                wantedPermissions.add(Manifest.permission.INTERNET);
            }
        }

        if (!wantedPermissions.isEmpty()) {
            Prefs prefs = GBApplication.getPrefs();
            // If this is not the first run, we can rely on
            // shouldShowRequestPermissionRationale(String permission)
            // and ignore permissions that shouldn't or can't be requested again
            if (prefs.getBoolean("permissions_asked", false)) {
                // Don't request permissions that we shouldn't show a prompt for
                // e.g. permissions that are "Never" granted by the user or never granted by the system
                Set<String> shouldNotAsk = new HashSet<>();
                for (String wantedPermission : wantedPermissions) {
                    if (!shouldShowRequestPermissionRationale(wantedPermission)) {
                        shouldNotAsk.add(wantedPermission);
                    }
                }
                wantedPermissions.removeAll(shouldNotAsk);
            } else if (!showDialogFirst) {
                // Permissions have not been asked yet, but now will be
                prefs.getPreferences().edit().putBoolean("permissions_asked", true).apply();
            }

            if (!wantedPermissions.isEmpty()) {
                if (showDialogFirst) {
                    // Show a dialog - thus will then call checkAndRequestPermissions(false)
                    DialogFragment dialog = new MainActivity.LocationPermissionsDialogFragment();
                    dialog.show(getSupportFragmentManager(), "LocationPermissionsDialogFragment");
                } else {
                    GB.toast(this, getString(R.string.permission_granting_mandatory), Toast.LENGTH_LONG, GB.ERROR);
                    ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[0]), 0);
                }
            }
        }

        // HACK: On Lineage we have to do this so that the permission dialog pops up
        if (fakeStateListener == null) {
            fakeStateListener = new PhoneStateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    public void setLanguage(Locale language, boolean invalidateLanguage) {
        if (invalidateLanguage) {
            isLanguageInvalid = true;
        }
        AndroidUtils.setLanguage(this, language);
    }

    /// Called from onCreate - this puts up a dialog explaining we need permissions, and goes to the correct Activity
    public static class NotifyPolicyPermissionsDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final Context context = getContext();
            builder.setMessage(context.getString(R.string.permission_notification_policy_access,
                            getContext().getString(R.string.app_name),
                            getContext().getString(R.string.ok)))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                            } catch (ActivityNotFoundException e) {
                                GB.toast(context, "'Notification Policy' activity not found", Toast.LENGTH_LONG, GB.ERROR);
                            }
                        }
                    });
            return builder.create();
        }
    }

    /// Called from onCreate - this puts up a dialog explaining we need permissions, and goes to the correct Activity
    public static class NotifyListenerPermissionsDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final Context context = getContext();
            builder.setMessage(context.getString(R.string.permission_notification_listener,
                            getContext().getString(R.string.app_name),
                            getContext().getString(R.string.ok)))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                            } catch (ActivityNotFoundException e) {
                                GB.toast(context, "'Notification Listener Settings' activity not found", Toast.LENGTH_LONG, GB.ERROR);
                            }
                        }
                    });
            return builder.create();
        }
    }

    /// Called from onCreate - this puts up a dialog explaining we need permissions, and goes to the correct Activity
    public static class DisplayOverOthersPermissionsDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Context context = getContext();
            builder.setMessage(context.getString(R.string.permission_display_over_other_apps,
                            getContext().getString(R.string.app_name),
                            getContext().getString(R.string.ok)))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent enableIntent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            startActivity(enableIntent);
                        }
                    }).setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
            return builder.create();
        }
    }

    /// Called from checkAndRequestPermissions - this puts up a dialog explaining we need permissions, and then calls checkAndRequestPermissions (via an intent) when 'ok' pressed
    public static class LocationPermissionsDialogFragment extends DialogFragment {
        ControlCenterv2 controlCenter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Context context = getContext();
            builder.setMessage(context.getString(R.string.permission_location,
                            getContext().getString(R.string.app_name),
                            getContext().getString(R.string.ok)))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(ACTION_REQUEST_PERMISSIONS);
                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                        }
                    });
            return builder.create();
        }
    }
}
