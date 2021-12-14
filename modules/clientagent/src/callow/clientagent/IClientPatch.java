package callow.clientagent;

import callow.common.IClassPatch;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public interface IClientPatch extends IClassPatch {
    /**
     * @return Name of servers for what patch working,
     * if returns empty list that means patch used for all servers
     */
    default List<String> getServersNames() {
        return new ArrayList<>();
    }
}
