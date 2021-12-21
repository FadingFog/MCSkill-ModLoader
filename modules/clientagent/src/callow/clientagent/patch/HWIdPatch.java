package callow.clientagent.patch;

import callow.clientagent.IClientPatch;
import javassist.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

public class HWIdPatch implements IClientPatch {

    @Override
    public String getPatchName() {
        return "Patch hardware id mod";
    }

    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("com.luffy.mixedmod.client.utils.HWDetails");

        // classes.add("ru.sky_drive.dw.lllIIIllIIIlllllIIlIIIIIllIlIIlIIlIlIIllIIIlIlIIIllIIIllIlIIIIIlIIlIIlIIllIlIIllIlIllIlllIlIlllIllIIIlIIlIlIllIlIIIlIllllllllIlIIIIlIIlllIIlllllIIlIIllllllllIIIllIlIlIIllIIIIlIIllllIIllllIIllIIllIlIIIIIlIllIIll.HwidUtils");
        return classes;
    }

    @Override
    public List<ServerInfo> getServersInfo() {
        List<ServerInfo> servers = new ArrayList<>();

        ServerInfo htc112 = new ServerInfo("HiTech 1.12.2", new HashMap<>());
        htc112.hashDependencies.put("mods/MixedMod-1.0.9-client.jar",
                "8ea38b671ea9704d8459517706b706e7");
        servers.add(htc112);

//        ServerInfo htc1710 = new ServerInfo("HiTechCraft", new HashMap<>());
//        htc1710.hashDependencies.put("mods/DwCity-2.11-client-final.jar",
//                "d0b7a199361cf2eaa4fc0c26c4d08273");
//        servers.add(htc1710);

        return servers;
    }

    @Override
    public boolean isPatchRequired() {
        return false;
    }

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        try {
            CtMethod method = ctClass.getDeclaredMethod("getCPU");
            method.setBody("{return \"getCPU error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

            method = ctClass.getDeclaredMethod("getGPU");
            method.setBody("{return \"getGPU error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

            method = ctClass.getDeclaredMethod("getHDD");
            method.setBody("{return \"getHDD error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

            method = ctClass.getDeclaredMethod("getBaseboard");
            method.setBody("{return \"getBaseboard error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

            method = ctClass.getDeclaredMethod("getOS");
            method.setBody("{return \"getOS error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

            method = ctClass.getDeclaredMethod("getMac");
            method.setBody("{return \"getMac error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.randomReplace($_, true);");

            method = ctClass.getDeclaredMethod("getIP");
            method.setBody("{return \"getIP error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

            method = ctClass.getDeclaredMethod("isVM");
            method.setBody("{return \"isVM error\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

            method = ctClass.getDeclaredMethod("getDiscord");
            method.setBody("{return \"\";}");
            //method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");

        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String randomReplace(String string, boolean isHex) {
        byte[] encoded = string.getBytes(StandardCharsets.UTF_8);
        final int[][] randomRangesHex = { {48, 57}, {65, 70}, {97, 102} };
        final int[][] randomRanges = { {48, 57}, {65, 90}, {97, 122} };
        Random random = new Random(getIdentifier());
        StringBuilder randomBuilder = new StringBuilder();
        for (byte b: encoded) {
            int ch = b & 0xFF;
            char toAdd = (char)ch;
            for (int[] range: isHex ? randomRangesHex : randomRanges) {
                if (ch >= range[0] && ch <= range[1]) {
                    toAdd = (char) (range[0] + random.nextInt(range[1] - range[0]));
                    break;
                }
            }
            randomBuilder.append(toAdd);
        }
        return randomBuilder.toString();
    }

    public static String replaceDigits(String content) {
        Random random = new Random(getIdentifier());
        StringBuilder builder = new StringBuilder();
        for (byte ch: content.getBytes(StandardCharsets.UTF_8)) {
            char toAdd = (char) ch;
            if (ch >= 49 && ch <= 57) {
                toAdd = (char) (49 + random.nextInt(8));
            }
            builder.append(toAdd);
        }
        return builder.toString();
    }

    public static long getIdentifier() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(System.getenv("PLAYER_NAME").getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            byte[] digest = messageDigest.digest();
            for (int i = 0; i < 8; i++)
                buffer.put((byte) (digest[i] - digest[i + 8]));
            buffer.flip();
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            return 0;
        }
    }

    public static String getIP() {
        String str1 = null;
        try {
            URL uRL = new URL("http://checkip.amazonaws.com/");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));
            String str;
            if ((str = bufferedReader.readLine()) != null)
                str1 = str;
            bufferedReader.close();
        } catch (IOException ignored) {}
        return  str1;
    }
}
