// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class DeviceIdProvider {

    private final String deviceId;

    public DeviceIdProvider(@Value("${eta.sync.device-id-file:./data/device-id}") String deviceIdFile) {
        Path path = Paths.get(deviceIdFile);
        String id;
        try {
            if (Files.exists(path)) {
                id = Files.readString(path).trim();
            } else {
                id = UUID.randomUUID().toString();
                Files.createDirectories(path.getParent());
                Files.writeString(path, id);
            }
        } catch (IOException e) {
            id = UUID.randomUUID().toString();
        }
        this.deviceId = id;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
