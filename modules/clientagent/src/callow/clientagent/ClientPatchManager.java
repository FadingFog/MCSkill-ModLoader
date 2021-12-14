package callow.clientagent;

import callow.common.IClassPatch;
import callow.common.PatchManager;

import java.util.Arrays;

public class ClientPatchManager extends PatchManager {
    public ClientPatchManager(IClientPatch[] patches, boolean loadDirs, String serverName) {
        super(Arrays.stream(patches).filter((patch) ->
                patch.getServersNames().size() == 0 || patch.getServersNames().contains(serverName))
                .toArray(IClassPatch[]::new), loadDirs);
    }
}
