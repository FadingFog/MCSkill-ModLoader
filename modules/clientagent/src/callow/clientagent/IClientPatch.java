package callow.clientagent;

import callow.common.IClassPatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IClientPatch extends IClassPatch {

    /**
     * Server info
     * Contains information for about server and its file hashes to check.
     */
    class ServerInfo {
        /**
         * Name of server, must be in format giving by mcskill.
         */
        public final String serverName;


        /**
         * Filepath -> Hash for files what's need be checked.
         * Pay attention that Filepath may be relative to server directory.
         */
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
