package main.mcagent;

import o.AUX;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static main.mcagent.CustomMethods.serverProfiles;

public class Util {
    public static boolean checkOrCreateDirectories(Path path){
        if (Files.exists(path) && !Files.isDirectory(path)){
            try {
                Files.delete(path);
            } catch (IOException e) {
                System.out.println("[-] Couldn't delete file to create directory.");
                return false;
            }
        }

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                System.out.println("[-] Couldn't create directory.");
                return false;
            }
        }
        return true;
    }

    public static Boolean checkFileOrCreate(Path path){
        if (!Files.isRegularFile(PropertiesFields.excludeModsPath)) {
            try {
                Files.deleteIfExists(PropertiesFields.excludeModsPath);
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println("[-] File creation filed.");
                return false;
            }
        }
        return true;
    }

    public static String getServerName(String serverDir){
        for (AUX profile : serverProfiles)
        {
            if (profile.getDir().equals(serverDir))
                return profile.getTitle();
        }
        return null;
    }

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
}
