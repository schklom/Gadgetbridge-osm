/*  Copyright (C) 2022 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.devices.huami.miband7;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.Huami2021Coordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;

public class MiBand7Coordinator extends Huami2021Coordinator {
    private static final Logger LOG = LoggerFactory.getLogger(MiBand7Coordinator.class);

    @NonNull
    @Override
    public DeviceType getSupportedType(final GBDeviceCandidate candidate) {
        try {
            final BluetoothDevice device = candidate.getDevice();
            final String name = device.getName();
            if (name != null && name.startsWith(HuamiConst.XIAOMI_SMART_BAND7_NAME)) {
                return DeviceType.MIBAND7;
            }
        } catch (final Exception e) {
            LOG.error("unable to check device support", e);
        }

        return DeviceType.UNKNOWN;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MIBAND7;
    }

    @Override
    public InstallHandler findInstallHandler(final Uri uri, final Context context) {
        final MiBand7FWInstallHandler handler = new MiBand7FWInstallHandler(uri, context);
        return handler.isValid() ? handler : null;
    }
}
