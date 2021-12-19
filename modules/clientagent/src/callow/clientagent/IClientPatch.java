package callow.clientagent;

import callow.common.IClassPatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IClientPatch extends IClassPatch {

    class ServerInfo {
        public final String serverName;
        public final HashMap<String, String> hashDependencies;
        public ServerInfo(String serverName, HashMap<String, String> hashDependencies) {
            this.serverName = serverName;
            this.hashDependencies = hashDependencies;
        }
    }

    /**
     * @return Name of servers for what patch working,
     * if returns empty list that means patch used for all servers
     */
    default List<ServerInfo> getServersInfo() {
        return new ArrayList<>();
    }
}
