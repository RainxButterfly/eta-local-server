// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.sync;

import java.io.IOException;
import java.util.List;

public interface CloudStorageProvider {

    void upload(String key, byte[] data) throws IOException;

    byte[] download(String key) throws IOException;

    boolean exists(String key) throws IOException;

    void delete(String key) throws IOException;

    List<String> list(String prefix) throws IOException;
}
