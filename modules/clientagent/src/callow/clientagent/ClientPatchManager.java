package callow.clientagent;

import callow.common.IClassPatch;
import callow.common.PatchManager;

import javax.swing.*;
import java.util.*;

import static callow.common.Utils.getMD5Checksum;

public class ClientPatchManager extends PatchManager {
    private final HashMap<IClientPatch, IClientPatch.ServerInfo> patchServerInfo;

    public ClientPatchManager(IClientPatch[] patches, boolean loadDirs, String serverName) {
        super(Arrays.stream(patches).filter((patch) ->
                patch.getServersInfo().size() == 0 ||
                        patch.getServersInfo().stream()
                                .anyMatch(info -> info.serverName.equals("*") || info.serverName.equals(serverName)))
                .toArray(IClassPatch[]::new), loadDirs);

        patchServerInfo = new HashMap<>();
        for (IClientPatch patch: patches)
        {
            patch.getServersInfo().stream()
                    .filter(info -> info.serverName.equals("*") || info.serverName.equals(serverName))
                    .forEach(serverInfo -> patchServerInfo.put(patch, serverInfo));
        }
    }

    public void ShowDependenciesHashMismatch() {
        HashMap<IClientPatch, String> mismatches = new HashMap<>();

        for (Map.Entry<IClientPatch, IClientPatch.ServerInfo> entryPatch: patchServerInfo.entrySet()) {
            for (Map.Entry<String, String> entry : entryPatch.getValue().hashDependencies.entrySet())
                if (!Objects.equals(getMD5Checksum(entry.getKey()), entry.getValue())) {
                    mismatches.put(entryPatch.getKey(), entry.getKey());
                    break;
                }
        }

        if (mismatches.size() == 0)
            return;

        StringBuilder builder = new StringBuilder();
        builder.append("Found hash mismatch, some patches may be unsuitable:\n");
        for (Map.Entry<IClientPatch, String> entry: mismatches.entrySet())
            builder.append(String.format("\t%s: %s\n", entry.getKey().getPatchName(), entry.getValue()));
        JOptionPane.showMessageDialog(null, builder.toString(),
                "Hash mismatch detected", JOptionPane.ERROR_MESSAGE);
    }
}
