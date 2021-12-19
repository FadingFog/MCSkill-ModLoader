package callow.common;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utils {
    public static void copyResourceFile(String resource, Path destination) throws IOException {
        InputStream is = Utils.class.getResourceAsStream("/" + resource);

        if (is == null) {
            System.out.println("[-] Cannot find LauncherAgent.jar file withing .jar classpath. " +
                    "Maybe you have ran program with IDEA?");
            System.out.println("[-] File path: "
                    + Thread.currentThread().getContextClassLoader().getResource("/LauncherAgent.jar"));
            return;
        }

        if (!Files.exists(destination))
            Files.createFile(destination);
        OutputStream os = new FileOutputStream(destination.toFile());

        byte[] buffer = new byte[2048];
        while (is.available() > 0){
            int len = is.read(buffer);
            os.write(buffer, 0, len);
        }
        is.close();
        os.close();
    }

    public static Path findMCSkillDir() {
        Path launcher = Paths.get(System.getenv("APPDATA")).resolve("McSkill/updates");
        if (!Files.isDirectory(launcher))
            return null;
        return launcher;
    }

    public static byte[] createChecksum(String filename) throws IOException {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete;
        try {
            complete = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) {
        try {
            byte[] b = createChecksum(filename);
            if (b == null)
                return null;

            StringBuilder result = new StringBuilder();
            for (byte value : b) {
                result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
            }
            return result.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static String readFile(File file) throws FileNotFoundException {
        Scanner myReader = new Scanner(file);
        StringBuilder builder = new StringBuilder();
        while (myReader.hasNextLine())
            builder.append(myReader.nextLine());
        myReader.close();
        return builder.toString();
    }

//    HashSet<String> llIlllllIIlllll = (HashSet<String>)Stream.concat(getClasses().stream(), getRuntimeClasses().stream()).sorted().collect(Collectors.toCollection(java.util.LinkedHashSet::new));
//    byte[] llIlllllIIllllI = String.join("\n", (Iterable)llIlllllIIlllll).getBytes(StandardCharsets.UTF_8);
//    byte[][] llIlllllIIlllIl = CommonUtils.divideBytes(llIlllllIIllllI, 31743);
//    NetworkManager.network.sendToServer((IMessage)new DetectorReplyPacket(anInt, llIlllllIIlllIl.length));
//    for (int llIlllllIlIIIIl = 0; llIlllllIlIIIIl < llIlllllIIlllIl.length; llIlllllIlIIIIl++)
//            NetworkManager.network.sendToServer((IMessage)new DetectChunkPacket(anInt, llIlllllIlIIIIl, llIlllllIIlllIl[llIlllllIlIIIIl]));
//}
//
//    public DetectorUtil(Exception llIlllllIlllllI) {
//        anInt = llIlllllIlllllI;
//    }
//
//    private HashSet<String> getRuntimeClasses() throws IllegalAccessException, NoSuchFieldException {
//        Field llIlllllIllIlII = ClassLoader.class.getDeclaredField("classes");
//        llIlllllIllIlII.setAccessible(true);
//        Vector<Class<?>> llIlllllIllIIll = (Vector<Class<?>>)llIlllllIllIlII.get(ClasspathHelper.contextClassLoader());
//        return (HashSet<String>)llIlllllIllIIll.stream().map(Class::getName).filter(llIlllllIIlIIlI -> !llIlllllIIlIIlI.contains("$")).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
//    }
//
//    private HashSet<String> getClasses() {
//        ConfigurationBuilder configurationBuilder = (new ConfigurationBuilder()).setScanners(new Scanner[] { (Scanner)Scanners.SubTypes.filterResultsBy(llIlllllIIlIlII -> true) }).setUrls(ClasspathHelper.forClassLoader(new ClassLoader[] { ClasspathHelper.contextClassLoader() }));
//        Reflections llIlllllIlIlIll = new Reflections((Configuration)configurationBuilder);
//        Map<String, Set<String>> llIlllllIlIlIlI = (Map<String, Set<String>>)((Map.Entry)llIlllllIlIlIll.getStore().entrySet().iterator().next()).getValue();
//        return (HashSet<String>)Stream.concat(llIlllllIlIlIlI.keySet().stream(), llIlllllIlIlIlI.values().stream().flatMap(Collection::stream)).filter(llIlllllIIlIlIl -> !llIlllllIIlIlIl.contains("$")).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
//    }
}
