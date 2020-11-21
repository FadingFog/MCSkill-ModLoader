package callow.clientagent.patch;

import javassist.*;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import callow.common.IClassPatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HWIdPatcher implements IClassPatcher {

    private static final String getHWIdClass = "IIlIlIlIlIlIllIIIIlIIIIIIIIllIIlIlIIIIlllIlIIlIllIIllIllIIIIIlIIlIllIIIIllllIIIIlIllIllIIlIlIIIlIIlIIIllIlllllIlllIllIIIIIlIlIll.IIIIlIIlllIllIIIlIIlIlIIlIIIIIlllllIIIllllIIIIlllIIIllllIlIIIIlllllIIlIllIllIIlIIllIllllIlIlllllIlIlIIllIlIIlIllIIlIIIIlIllIllIl.IIlIlIlIlIlIllIIIIlIIIIIIIIllIIlIlIIIIlllIlIIlIllIIllIllIIIIIlIIlIllIIIIllllIIIIlIllIllIIlIlIIIlIIlIIIllIlllllIlllIllIIIIIlIlIll.llIlIlIlllIIIIIlIlIIlIlIllIlIIIlIIIllIlIlIIIlIIlllIIllIllIllIIIllIIIllIllIlllIIlIlIlIIllIlIIIIllllIlIlIIlIIIlIllIIIlIllIlIllIIlI";
    private static final String getHWIdMethod = "IIlIlIllllIIlllllIlIlllllIIIIlIIIIlllIIlIIllllllIIIIllIlllIIlIIIlIIlIIIIlIllIllllllIIllIIIlIIllIIIlIlIIlIlIlIlIlIIllIlIIlllIIIlI";
    private static final String AESEncoderMethod = "IIlIlIlIlIlIllIIIIlIIIIIIIIllIIlIlIIIIlllIlIIlIllIIllIllIIIIIlIIlIllIIIIllllIIIIlIllIllIIlIlIIIlIIlIIIllIlllllIlllIllIIIIIlIlIll.IIIIlIIlllIllIIIlIIlIlIIlIIIIIlllllIIIllllIIIIlllIIIllllIlIIIIlllllIIlIllIllIIlIIllIllllIlIlllllIlIlIIllIlIIlIllIIlIIIIlIllIllIl.IlIlllllIIIlIIllIlIlIlllIIlIlIlIIIIllIlIIIllIIlllIIIllIlIlIllIIllIIlllllIIlIIIIIIIIIIlIIlIllIlllIlIIIIlIIllIIlIIlIlIlIIllllIlIIl.llIlIlIlllIIIIIlIlIIlIlIllIlIIIlIIIllIlIlIIIlIIlllIIllIllIllIIIllIIIllIllIlllIIlIlIlIIllIlIIIIllllIlIlIIlIIIlIllIIIlIllIlIllIIlI.IIlIlIllllIIlllllIlIlllllIIIIlIIIIlllIIlIIllllllIIIIllIlllIIlIIIlIIlIIIIlIllIllllllIIllIIIlIIllIIIlIlIIlIlIlIlIlIIllIlIIlllIIIlI";

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (!ctClass.getName().equals(getHWIdClass))
            return false;

        try {
            CtMethod method = ctClass.getDeclaredMethod(getHWIdMethod);
            method.setBody(String.format("{ java.lang.String[] params = callow.clientagent.patch.HWIdPatcher.getRandomHWId();" +
                    "return %s(params[0]) + \"@\" + %s(params[1]) + \"@\" + %s(params[2]); }", AESEncoderMethod, AESEncoderMethod, AESEncoderMethod ));
            System.out.println("[+] Patcher | HWId patch created.");
        } catch (NotFoundException | CannotCompileException e) {
            System.out.println("[-] Patcher | Patch creation was failed.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String[] getRandomHWId() {
        Random random = new Random();
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        ComputerSystem systems = hardware.getComputerSystem();

        String[] HWIdStrings = new String[3];
        StringBuilder currentPartBuilder = new StringBuilder(systems.getBaseboard().getSerialNumber());

        for (Display display : hardware.getDisplays()){
            byte[] array = display.getEdid();
            random.nextBytes(array);
            currentPartBuilder.append(", ").append(Arrays.toString(array));
        }

        HWIdStrings[0] = currentPartBuilder.toString();

        currentPartBuilder = new StringBuilder(randomReplace(hardware.getProcessor().getProcessorIdentifier().getProcessorID(), true));
        for (HWDiskStore hWDiskStore : hardware.getDiskStores())
            currentPartBuilder.append(", ").append(randomReplace(hWDiskStore.getSerial(), true));
        HWIdStrings[1] = currentPartBuilder.toString();

        currentPartBuilder = new StringBuilder(getIP());

        String absolutePath = Paths.get("").toAbsolutePath().toString();
        Pattern pattern = Pattern.compile("Users[/\\\\]([^/\\\\]+)[/\\\\]");
        Matcher matcher = pattern.matcher(absolutePath);
        System.out.println(absolutePath);
        if (matcher.find()) {
            absolutePath = matcher.replaceFirst("Users/" + randomReplace(matcher.group(1), false) + "/");
        }
        currentPartBuilder.append(", ").append(Paths.get(absolutePath).toAbsolutePath());

        currentPartBuilder.append(", mac: ").append(randomReplace(hardware.getNetworkIFs().get(0).getMacaddr(), true));
        HWIdStrings[2] = currentPartBuilder.toString();

        System.out.println("[+] Sending random HWId: ");
        for (String part: HWIdStrings)
            System.out.println(part);

        return HWIdStrings;
    }

    public static String randomReplace(String string, boolean isHex) {
        byte[] encoded = string.getBytes(StandardCharsets.UTF_8);
        final int[][] randomRangesHex = { {48, 57}, {65, 70}, {97, 102} };
        final int[][] randomRanges = { {48, 57}, {65, 90}, {97, 122} };
        Random random = new Random();
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

    public static String getIP() {
        String str1 = null;
        String str2 = null;
        try {
            URL uRL = new URL("http://icanhazip.com/");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));
            String str;
            if ((str = bufferedReader.readLine()) != null)
                str2 = str;
            bufferedReader.close();
        } catch (IOException ignored) {}
        try {
            URL uRL = new URL("http://checkip.amazonaws.com/");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));
            String str;
            if ((str = bufferedReader.readLine()) != null)
                str1 = str;
            bufferedReader.close();
        } catch (IOException ignored) {}
        return "IPv4: " + str1 + ", IPv6: " + str2;
    }
}
