package jemu.rest.security;

import java.util.Date;
import java.util.UUID;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class AuthorizationRequest {
    private String ip;
    private UUID uuid;
    private Date expires;

    public static final int EXPIRE_MIN = 10;

    public AuthorizationRequest(String ip, UUID uuid) {
        this.ip = ip;
        this.uuid = uuid;
        this.expires = new Date(System.currentTimeMillis() + EXPIRE_MIN * 60 * 1000);
    }

    public String getIp() {
        return ip;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Date getExpires() {
        return expires;
    }

    public boolean isExpired() {
        return expires.compareTo(new Date(System.currentTimeMillis())) < 0;
    }

    public String getSplitIp() {
        if (ip.length() < 32) {
            return ip;
        }
        int split = -1;
        int index = 0;
        while (index >= 0 && index < 30 && index < ip.length()) {
            split = index;
            index = ip.indexOf(":", index);
        }
        if (split < 0) {
            split = ip.length() / 2;
        }
        return ip.substring(0, split) + "\n" + ip.substring(split);
    }
}
