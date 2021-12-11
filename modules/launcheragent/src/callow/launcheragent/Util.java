package callow.launcheragent;

import launcher.*;

import java.io.*;
import java.util.*;

public class Util {

    public static Map<String, String> ClientDirToName;
    public static String readFile(File file) throws FileNotFoundException {
        Scanner myReader = new Scanner(file);
        StringBuilder builder = new StringBuilder();
        while (myReader.hasNextLine())
            builder.append(myReader.nextLine());
        myReader.close();
        return builder.toString();
    }

    public static void writeFile(File file, String data) throws IOException {
        Writer writer = new FileWriter(file);
        writer.write(data);
        writer.close();
    }

    public static AUX[] getServersProfiles() {
        COm4 launcherRequest = new COm4();
        CoM4 response = (CoM4) launcherRequest.request();

        List<AUX> serverProfiles = new ArrayList<>();
        ClientDirToName = new HashMap<>();
        for (Object profile : response.profiles) {
            AUX profileInfo = (AUX)((com8)profile).object;
            ClientDirToName.put(profileInfo.getDir(), profileInfo.getTitle());
            serverProfiles.add(profileInfo);
        }
        AUX[] result = new AUX[serverProfiles.size()];
        serverProfiles.toArray(result);
        return result;
    }
}