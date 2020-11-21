package callow.launcheragent;

import o.*;

import java.io.*;
import java.util.*;

public class Util {

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
        Aux config = aux.getConfig();
        CoM3 launcherRequest = new CoM3(config);
        COM3 response = (COM3) launcherRequest.request();

        List<AUX> serverProfiles = new ArrayList<>();
        for (Object profile : response.profiles) {
            cOm7 profileInfo = (cOm7) profile;
            serverProfiles.add((AUX) profileInfo.object);
        }
        AUX[] result = new AUX[serverProfiles.size()];
        serverProfiles.toArray(result);
        return result;
    }
}