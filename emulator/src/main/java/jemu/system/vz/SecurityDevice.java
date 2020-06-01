package jemu.system.vz;

import jemu.core.cpu.Z80;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;
import jemu.rest.security.AuthorizationRequest;
import jemu.rest.security.SecurityService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class SecurityDevice extends Device {
    private static final Logger log = LoggerFactory.getLogger(SecurityDevice.class);
    private final SecurityService securityService;
    private final VZ vz;

    private static final String TYPE = "VZ Security Device";
    public static final int PORT_MASK_DATA = 255;
    public static final int PORT_TEST_DATA = 249;

    public SecurityDevice(VZ vz, SecurityService securityService) {
        super(TYPE);
        this.securityService = securityService;
        this.vz = vz;
    }

    public void register(Z80 z80) {
        z80.addOutputDeviceMapping(new DeviceMapping(this, PORT_MASK_DATA, PORT_TEST_DATA));
        z80.addInputDeviceMapping(new DeviceMapping(this, PORT_MASK_DATA, PORT_TEST_DATA));
    }

    @Override
    public void writePort(int port, int value) {
        AuthorizationRequest request = securityService.getRequest(value);
        if (request == null) {
            vz.alert(String.format("ERROR GRANTING ACCESS FOR %d\n ", value));
            return;
        }
        if (!securityService.authorize(value)) {
            vz.alert(String.format("ERROR GRANTING ACCESS FOR %d\n%s", value, request.getSplitIp()));
            return;
        }
        vz.alert(String.format("ACCESS GRANTED FOR %d\n%s", value, request.getSplitIp()));
    }
}
