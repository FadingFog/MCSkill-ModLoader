package callow.clientagent.patch;

import callow.clientagent.IClientPatch;
import javassist.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HWIdPatch implements IClientPatch {

    private static final String newHWidClass = "com.luffy.mixedmod.client.utils.HWDetails";

    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add(newHWidClass);
        classes.add("com.luffy.mixedmod.common.utils.HWSerializer");
        return classes;
    }

    @Override
    public boolean isPatchRequired() {
        return false;
    }

    @Override
    public String getPatchName() {
        return "Patch hardware id mod";
    }

    @Override
    public List<String> getServersNames() {
        List<String> servers = new ArrayList<>();
        servers.add("HiTech 1.12.2");
        return servers;
    }

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (ctClass.getName().equals("com.luffy.mixedmod.common.utils.HWSerializer"))
        {
            try {
                CtMethod method = ctClass.getDeclaredMethod("serialize");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.onSerializeOut($_);");

            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            try {
                CtMethod method = ctClass.getDeclaredMethod("getMac");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");
                method = ctClass.getDeclaredMethod("getBaseboard");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");
                method = ctClass.getDeclaredMethod("isVM");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");
                method = ctClass.getDeclaredMethod("getHDD");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");
                method = ctClass.getDeclaredMethod("getGPU");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");
                method = ctClass.getDeclaredMethod("getDiscord");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");
                method = ctClass.getDeclaredMethod("getCPU");
                method.setBody("{return \"Intel(R) Core(TM) i5-2500K CPU @ 3.30GHz | BFEBFBFF000206A7\";}");
                method = ctClass.getDeclaredMethod("getIP");
                method.insertAfter("return callow.clientagent.patch.HWIdPatch.replaceDigits($_);");
            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }




    public static byte[] onSerializeOut(byte[] result) {
        System.out.println(result);
        return result;
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
