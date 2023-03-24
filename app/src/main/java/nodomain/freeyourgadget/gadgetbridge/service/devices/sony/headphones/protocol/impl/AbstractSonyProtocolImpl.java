/*  Copyright (C) 2021 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl;

import android.content.SharedPreferences;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.SonyHeadphonesCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AmbientSoundControl;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AmbientSoundControlButtonMode;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AudioUpsampling;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AutomaticPowerOff;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.ButtonModes;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.EqualizerCustomBands;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.EqualizerPreset;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.PauseWhenTakenOff;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.QuickAccess;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SoundPosition;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SpeakToChatConfig;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SpeakToChatEnabled;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SurroundMode;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.TouchSensor;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.VoiceNotifications;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.Request;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.MessageType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1.params.BatteryType;
import nodomain.freeyourgadget.gadgetbridge.util.DeviceHelper;

public abstract class AbstractSonyProtocolImpl {
    private final GBDevice device;

    public AbstractSonyProtocolImpl(final GBDevice device) {
        this.device = device;
    }

    protected GBDevice getDevice() {
        return this.device;
    }

    protected SonyHeadphonesCoordinator getCoordinator() {
        return (SonyHeadphonesCoordinator) DeviceHelper.getInstance().getCoordinator(getDevice());
    }

    public abstract Request getAmbientSoundControl();

    public abstract Request setAmbientSoundControl(final AmbientSoundControl config);

    public abstract Request setSpeakToChatEnabled(final SpeakToChatEnabled config);

    public abstract Request getSpeakToChatEnabled();

    public abstract Request setSpeakToChatConfig(final SpeakToChatConfig config);

    public abstract Request getSpeakToChatConfig();

    public abstract Request getNoiseCancellingOptimizerState();

    public abstract Request getAudioCodec();

    public abstract Request getBattery(final BatteryType batteryType);

    public abstract Request getFirmwareVersion();

    public abstract Request getAudioUpsampling();

    public abstract Request setAudioUpsampling(final AudioUpsampling config);

    public abstract Request getAutomaticPowerOff();

    public abstract Request setAutomaticPowerOff(final AutomaticPowerOff config);

    public abstract Request getButtonModes();

    public abstract Request setButtonModes(final ButtonModes config);

    public abstract Request getQuickAccess();

    public abstract Request setQuickAccess(final QuickAccess quickAccess);

    public abstract Request getAmbientSoundControlButtonMode();

    public abstract Request setAmbientSoundControlButtonMode(final AmbientSoundControlButtonMode ambientSoundControlButtonMode);

    public abstract Request getPauseWhenTakenOff();

    public abstract Request setPauseWhenTakenOff(final PauseWhenTakenOff config);

    public abstract Request getEqualizerPreset();

    public abstract Request setEqualizerPreset(final EqualizerPreset config);

    public abstract Request setEqualizerCustomBands(final EqualizerCustomBands config);

    public abstract Request getSoundPosition();

    public abstract Request setSoundPosition(final SoundPosition config);

    public abstract Request getSurroundMode();

    public abstract Request setSurroundMode(final SurroundMode config);

    public abstract Request getTouchSensor();

    public abstract Request setTouchSensor(final TouchSensor config);

    public abstract Request getVoiceNotifications();

    public abstract Request setVoiceNotifications(final VoiceNotifications config);

    public abstract Request startNoiseCancellingOptimizer(final boolean start);

    public abstract Request powerOff();

    public abstract List<? extends GBDeviceEvent> handlePayload(final MessageType messageType, final byte[] payload);

    public Request setAmbientSoundControl(final SharedPreferences prefs) {
        return setAmbientSoundControl(AmbientSoundControl.fromPreferences(prefs));
    }

    public Request setSpeakToChatEnabled(final SharedPreferences prefs) {
        return setSpeakToChatEnabled(SpeakToChatEnabled.fromPreferences(prefs));
    }

    public Request setSpeakToChatConfig(final SharedPreferences prefs) {
        return setSpeakToChatConfig(SpeakToChatConfig.fromPreferences(prefs));
    }

    public Request setAudioUpsampling(final SharedPreferences prefs) {
        return setAudioUpsampling(AudioUpsampling.fromPreferences(prefs));
    }

    public Request setAutomaticPowerOff(final SharedPreferences prefs) {
        return setAutomaticPowerOff(AutomaticPowerOff.fromPreferences(prefs));
    }

    public Request setButtonModes(final SharedPreferences prefs) {
        return setButtonModes(ButtonModes.fromPreferences(prefs));
    }

    public Request setQuickAccess(final SharedPreferences prefs) {
        return setQuickAccess(QuickAccess.fromPreferences(prefs));
    }

    public Request setAmbientSoundControlButtonMode(final SharedPreferences prefs) {
        return setAmbientSoundControlButtonMode(AmbientSoundControlButtonMode.fromPreferences(prefs))
    }

    public Request setPauseWhenTakenOff(final SharedPreferences prefs) {
        return setPauseWhenTakenOff(PauseWhenTakenOff.fromPreferences(prefs));
    }

    public Request setEqualizerPreset(final SharedPreferences prefs) {
        return setEqualizerPreset(EqualizerPreset.fromPreferences(prefs));
    }

    public Request setEqualizerCustomBands(final SharedPreferences prefs) {
        return setEqualizerCustomBands(EqualizerCustomBands.fromPreferences(prefs));
    }

    public Request setSoundPosition(final SharedPreferences prefs) {
        return setSoundPosition(SoundPosition.fromPreferences(prefs));
    }

    public Request setSurroundMode(final SharedPreferences prefs) {
        return setSurroundMode(SurroundMode.fromPreferences(prefs));
    }

    public Request setTouchSensor(final SharedPreferences prefs) {
        return setTouchSensor(TouchSensor.fromPreferences(prefs));
    }

    public Request setVoiceNotifications(final SharedPreferences prefs) {
        return setVoiceNotifications(VoiceNotifications.fromPreferences(prefs));
    }
}
