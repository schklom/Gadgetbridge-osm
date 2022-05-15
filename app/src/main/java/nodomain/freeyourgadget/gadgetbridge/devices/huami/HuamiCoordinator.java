/*  Copyright (C) 2016-2021 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, José Rebelo, Nephiel

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
package nodomain.freeyourgadget.gadgetbridge.devices.huami;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsCustomizer;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.DateTimeDisplay;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.DoNotDisturb;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBand2SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandPairingActivity;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandService;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.VibrationProfile;
import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.MiBandActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiVibrationPatternNotificationType;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public abstract class HuamiCoordinator extends AbstractDeviceCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(HuamiCoordinator.class);

    @Override
    public Class<? extends Activity> getPairingActivity() {
        return MiBandPairingActivity.class;
    }

    @NonNull
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        ParcelUuid mi2Service = new ParcelUuid(MiBandService.UUID_SERVICE_MIBAND2_SERVICE);
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(mi2Service).build();
        return Collections.singletonList(filter);
    }

    @Override
    protected void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException {
        Long deviceId = device.getId();
        QueryBuilder<?> qb = session.getMiBandActivitySampleDao().queryBuilder();
        qb.where(MiBandActivitySampleDao.Properties.DeviceId.eq(deviceId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    @Override
    public String getManufacturer() {
        return "Huami";
    }

    @Override
    public boolean supportsAppsManagement() {
        return false;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return null;
    }

    @Override
    public boolean supportsCalendarEvents() {
        return false;
    }

    @Override
    public boolean supportsRealtimeData() {
        return true;
    }

    @Override
    public int getAlarmSlotCount() {
        return 10;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Override
    public boolean supportsActivityTracking() {
        return true;
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{R.xml.devicesettings_pairingkey};
    }

    @Override
    public SampleProvider<? extends AbstractActivitySample> getSampleProvider(GBDevice device, DaoSession session) {
        return new MiBand2SampleProvider(device, session);
    }

    public static DateTimeDisplay getDateDisplay(Context context, String deviceAddress) throws IllegalArgumentException {
        SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);
        String dateFormatTime = context.getString(R.string.p_dateformat_time);
        if (dateFormatTime.equals(sharedPrefs.getString(MiBandConst.PREF_MI2_DATEFORMAT, dateFormatTime))) {
            return DateTimeDisplay.TIME;
        }
        return DateTimeDisplay.DATE_TIME;
    }

    public static ActivateDisplayOnLift getActivateDisplayOnLiftWrist(Context context, String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);

        String liftOff = context.getString(R.string.p_off);
        String liftOn = context.getString(R.string.p_on);
        String liftScheduled = context.getString(R.string.p_scheduled);

        String pref = prefs.getString(DeviceSettingsPreferenceConst.PREF_ACTIVATE_DISPLAY_ON_LIFT, liftOff);

        if (liftOn.equals(pref)) {
            return ActivateDisplayOnLift.ON;
        } else if (liftScheduled.equals(pref)) {
            return ActivateDisplayOnLift.SCHEDULED;
        }

        return ActivateDisplayOnLift.OFF;
    }

    public static Date getDisplayOnLiftStart(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_DISPLAY_ON_LIFT_START, "00:00", deviceAddress);
    }

    public static Date getDisplayOnLiftEnd(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_DISPLAY_ON_LIFT_END, "00:00", deviceAddress);
    }

    public static ActivateDisplayOnLiftSensitivity getDisplayOnLiftSensitivity(String deviceAddress) {
        final SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);

        final String pref = prefs.getString(DeviceSettingsPreferenceConst.PREF_DISPLAY_ON_LIFT_SENSITIVITY, "sensitive");

        return ActivateDisplayOnLiftSensitivity.valueOf(pref.toUpperCase(Locale.ROOT));
    }

    public static DisconnectNotificationSetting getDisconnectNotificationSetting(Context context, String deviceAddress) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));

        String liftOff = context.getString(R.string.p_off);
        String liftOn = context.getString(R.string.p_on);
        String liftScheduled = context.getString(R.string.p_scheduled);

        String pref = prefs.getString(DeviceSettingsPreferenceConst.PREF_DISCONNECT_NOTIFICATION, liftOff);

        if (liftOn.equals(pref)) {
            return DisconnectNotificationSetting.ON;
        } else if (liftScheduled.equals(pref)) {
            return DisconnectNotificationSetting.SCHEDULED;
        }

        return DisconnectNotificationSetting.OFF;
    }

    public static Date getDisconnectNotificationStart(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_DISCONNECT_NOTIFICATION_START, "00:00", deviceAddress);
    }

    public static Date getDisconnectNotificationEnd(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_DISCONNECT_NOTIFICATION_END, "00:00", deviceAddress);
    }

    public static boolean getUseCustomFont(String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);
        return prefs.getBoolean(HuamiConst.PREF_USE_CUSTOM_FONT, false);
    }

    public static boolean getGoalNotification(String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);
        return prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_USER_FITNESS_GOAL_NOTIFICATION, false);
    }

    public static boolean getRotateWristToSwitchInfo(String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);
        return prefs.getBoolean(MiBandConst.PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO, false);
    }

    public static boolean getInactivityWarnings(String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);
        return prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_ENABLE, false);
    }

    public static int getInactivityWarningsThreshold(String deviceAddress) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));
        return prefs.getInt(DeviceSettingsPreferenceConst.PREF_INACTIVITY_THRESHOLD, 60);
    }

    public static boolean getInactivityWarningsDnd(String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);
        return prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_DND, false);
    }

    public static Date getInactivityWarningsStart(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_INACTIVITY_START, "06:00", deviceAddress);
    }

    public static Date getInactivityWarningsEnd(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_INACTIVITY_END, "22:00", deviceAddress);
    }

    public static Date getInactivityWarningsDndStart(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_INACTIVITY_DND_START, "12:00", deviceAddress);
    }

    public static Date getInactivityWarningsDndEnd(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_INACTIVITY_DND_END, "14:00", deviceAddress);
    }

    public static Date getDoNotDisturbStart(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_START, "01:00", deviceAddress);
    }

    public static Date getDoNotDisturbEnd(String deviceAddress) {
        return getTimePreference(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_END, "06:00", deviceAddress);
    }

    public static boolean getBandScreenUnlock(String deviceAddress) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));
        return prefs.getBoolean(MiBandConst.PREF_SWIPE_UNLOCK, false);
    }

    public static boolean getExposeHRThirdParty(String deviceAddress) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));
        return prefs.getBoolean(HuamiConst.PREF_EXPOSE_HR_THIRDPARTY, false);
    }

    public static int getHeartRateMeasurementInterval(String deviceAddress) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));
        return GBApplication.getPrefs().getInt(DeviceSettingsPreferenceConst.PREF_HEARTRATE_MEASUREMENT_INTERVAL, 0) / 60;
    }

    public static boolean getBtConnectedAdvertising(String deviceAddress) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));
        return prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_BT_CONNECTED_ADVERTISEMENT, false);
    }

    public static VibrationProfile getVibrationProfile(String deviceAddress, HuamiVibrationPatternNotificationType notificationType) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));
        final String vibrationProfileId = prefs.getString(
                HuamiConst.PREF_HUAMI_VIBRATION_PROFILE_PREFIX + notificationType.name().toLowerCase(Locale.ROOT),
                VibrationProfile.ID_MEDIUM
        );
        final int vibrationProfileCount = prefs.getInt(HuamiConst.PREF_HUAMI_VIBRATION_COUNT_PREFIX + notificationType.name().toLowerCase(Locale.ROOT), 2);

        return VibrationProfile.getProfile(vibrationProfileId, (short) vibrationProfileCount);
    }

    protected static Date getTimePreference(String key, String defaultValue, String deviceAddress) {
        Prefs prefs;

        if (deviceAddress == null) {
            prefs = GBApplication.getPrefs();
        } else {
            prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(deviceAddress));
        }
        String time = prefs.getString(key, defaultValue);

        DateFormat df = new SimpleDateFormat("HH:mm");
        try {
            return df.parse(time);
        } catch (Exception e) {
            LOG.error("Unexpected exception in MiBand2Coordinator.getTime: " + e.getMessage());
        }

        return new Date();
    }

    protected static Date getTimePreference(String key, String defaultValue) {
        return getTimePreference(key, defaultValue, null);
    }

    public static MiBandConst.DistanceUnit getDistanceUnit() {
        Prefs prefs = GBApplication.getPrefs();
        String unit = prefs.getString(SettingsActivity.PREF_MEASUREMENT_SYSTEM, GBApplication.getContext().getString(R.string.p_unit_metric));
        if (unit.equals(GBApplication.getContext().getString(R.string.p_unit_metric))) {
            return MiBandConst.DistanceUnit.METRIC;
        } else {
            return MiBandConst.DistanceUnit.IMPERIAL;
        }
    }

    public static DoNotDisturb getDoNotDisturb(String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);

        String pref = prefs.getString(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB, DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_OFF);

        if (DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_AUTOMATIC.equals(pref)) {
            return DoNotDisturb.AUTOMATIC;
        } else if (DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_SCHEDULED.equals(pref)) {
            return DoNotDisturb.SCHEDULED;
        }

        return DoNotDisturb.OFF;
    }

    public static boolean getDoNotDisturbLiftWrist(String deviceAddress) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceAddress);

        return prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_LIFT_WRIST, false);
    }

    @Override
    public boolean supportsScreenshots() {
        return false;
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return false;
    }

    @Override
    public boolean supportsFindDevice() {
        return true;
    }

    @Override
    public boolean supportsAlarmSnoozing() {
        return true;
    }

    @Override
    public int getMaximumReminderMessageLength() {
        return 16;
    }

    @Override
    public int getReminderSlotCount() {
        return 22; // At least, Mi Fit still allows more
    }

    @Override
    public DeviceSpecificSettingsCustomizer getDeviceSpecificSettingsCustomizer(final GBDevice device) {
        return new HuamiSettingsCustomizer(device);
    }
}
