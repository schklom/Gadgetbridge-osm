package nodomain.freeyourgadget.gadgetbridge.devices.galaxy_buds;

import androidx.annotation.NonNull;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsCustomizer;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.SonyHeadphonesSettingsCustomizer;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryConfig;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;

public class GalaxyBudsProDeviceCoordinator extends GalaxyBudsGenericCoordinator {

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {

        String name = candidate.getName();

        if (name != null && (
                name.startsWith("Galaxy Buds Pro (")
        )) {
            return DeviceType.GALAXY_BUDS_PRO;
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public DeviceSpecificSettingsCustomizer getDeviceSpecificSettingsCustomizer(final GBDevice device) {
        return new GalaxyBudsSettingsCustomizer(device);
    }
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.GALAXY_BUDS_PRO;
    }

    @Override
    public int getBatteryCount() {
        return 3;
    }

    @Override
    public BatteryConfig[] getBatteryConfig() {
        BatteryConfig battery1 = new BatteryConfig(0, R.drawable.ic_buds_pro_case, R.string.battery_case);
        BatteryConfig battery2 = new BatteryConfig(1, R.drawable.ic_buds_pro_left, R.string.left_earbud);
        BatteryConfig battery3 = new BatteryConfig(2, R.drawable.ic_buds_pro_right, R.string.right_earbud);
        return new BatteryConfig[]{battery1, battery2, battery3};
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_galaxy_buds_pro,
        };
    }
}

// Menu map:
//
//Noise controls
//- off
//- active noise cancelling
//- ambient sound
//
//Voice detect on/off
//
//Touch controls:
//- block touches on/off
//- Touch and hold:
// - left:...
// - right:...
//
// ... switch noise controls...
// ... voice command
// ... volume down
// ... spotify
//
//- double tap earbud edge on/off
//
//Earbuds settings
//- equalizer
// - normal
// - bass boost
// - soft
// - dynamic
// - clear
// - treble boost
//
//- read notifications aloud
// - on/off
// - read aloud while using phone on/off
// - missed call (read aloud summary) on/off
//- seamless earbud connection on/off
//- in-ear detection on/off
//- use ambient sound during calls on/off
//
//
//Accessibility
//- balance left---center---right
//- noise control with one earbud on/off
//- customize ambient sound on/off
// - ambient sound volume:
//  - left 1-5
//  - right 1-5
// - ambient sound tone:
//  - soft 1-5 clear