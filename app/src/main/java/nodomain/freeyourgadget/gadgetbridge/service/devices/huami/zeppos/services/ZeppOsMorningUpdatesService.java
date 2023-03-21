/*  Copyright (C) 2023 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppos.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.Huami2021Support;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppos.AbstractZeppOsService;

public class ZeppOsMorningUpdatesService extends AbstractZeppOsService {
    private static final Logger LOG = LoggerFactory.getLogger(ZeppOsMorningUpdatesService.class);

    private static final short ENDPOINT = 0x003f;

    private static final byte CMD_ENABLED_GET = 0x03;
    private static final byte CMD_ENABLED_RET = 0x04;
    private static final byte CMD_ENABLED_SET = 0x07;
    private static final byte CMD_ENABLED_SET_ACK = 0x08;
    private static final byte CMD_CONTENT_REQUEST = 0x05;
    private static final byte CMD_CONTENT_RESPONSE = 0x06;
    private static final byte CMD_CONTENT_SET = 0x09;
    private static final byte CMD_CONTENT_SET_ACK = 0x0a;

    public ZeppOsMorningUpdatesService(Huami2021Support support) {
        super(support);
    }

    @Override
    public short getEndpoint() {
        return ENDPOINT;
    }

    @Override
    public boolean isEncrypted() {
        return false;
    }

    @Override
    public void handlePayload(byte[] payload) {
        switch (payload[0]) {
            case CMD_ENABLED_RET:

                return;
            case CMD_ENABLED_SET_ACK:
                LOG.info("Morning updates enabled set ack ACK, status = {}", payload[1]);
                return;
            case CMD_CONTENT_RESPONSE:

                return;
            case CMD_CONTENT_SET_ACK:
                LOG.info("Morning updates content set ack ACK, status = {}", payload[1]);
                return;
            default:
                LOG.warn("Unexpected morning updates byte {}", String.format("0x%02x", payload[0]));
        }
    }

    public void setEnabled(final boolean enabled) {

    }

    public void setCategories(final List<String> categories) {

    }
}
