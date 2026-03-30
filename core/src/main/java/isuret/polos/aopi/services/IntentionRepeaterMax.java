package  isuret.polos.aopi.services;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

/**
 * Original IntentionRepeater by tsweet77 Anthro Teacher, rewritten in Java
 */
public class IntentionRepeaterMax {

    private static final int ONE_MINUTE = 60;
    private static final int ONE_HOUR = 3600;
    private static final AtomicBoolean interrupted = new AtomicBoolean(false);
    private static final String HSUPLINK_FILE = "HSUPLINK.TXT";

    private enum Color {
        DEFAULT("\u001B[0m"),
        BLACK("\u001B[0;30m"),
        RED("\u001B[0;31m"),
        GREEN("\u001B[0;32m"),
        YELLOW("\u001B[0;33m"),
        BLUE("\u001B[0;34m"),
        MAGENTA("\u001B[0;35m"),
        CYAN("\u001B[0;36m"),
        LIGHTGRAY("\u001B[0;37m"),
        DARKGRAY("\u001B[1;30m"),
        LIGHTRED("\u001B[1;31m"),
        LIGHTGREEN("\u001B[1;32m"),
        LIGHTYELLOW("\u001B[1;33m"),
        LIGHTBLUE("\u001B[1;34m"),
        LIGHTMAGENTA("\u001B[1;35m"),
        LIGHTCYAN("\u001B[1;36m"),
        WHITE("\u001B[1;37m");

        final String ansi;
        Color(String ansi) {
            this.ansi = ansi;
        }

        static Color from(String name) {
            try {
                return Color.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                return WHITE;
            }
        }
    }

    private static class Config {
        String paramDuration = "UNTIL STOPPED";
        String paramIntention = "X";
        String paramTimer = "EXACT";
        String paramBoostLevel = "0";
        String paramFreq = "0";
        String paramColor = "WHITE";
        String paramUseHololink = "NO";
        String paramAmplification = "1000000000";
        String paramRestEvery = "0";
        String paramRestFor = "0";
        String paramHashing = "X";
        String paramCompress = "X";
        String paramFile = "X";
        String paramFile2 = "X";
        String suffixValue = "HZ";
        double ramSizeValue = 1.0;
        int frequencyInt = 0;
        int restEveryInt = 0;
        int restForInt = 0;
        long amplificationLong = 1_000_000_000L;
    }

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> interrupted.set(true)));

        Config cfg = parseArgs(args);
        applyColor(cfg.paramColor);

        System.out.println("Intention Repeater MAX v5.26 (Java rewrite)");
        System.out.println("Based on the provided C++ source.");
        System.out.println();

        if ("__EXIT_AFTER_HELP__".equals(cfg.paramDuration)) {
            return;
        }

        String intention = "";
        String intentionOriginal = "";
        String intentionDisplay = "";
        String fileContentsOriginal = "";
        String fileContents2Original = "";
        String fileContents = "";
        String fileContents2 = "";

        if (!"0".equals(cfg.paramBoostLevel)) {
            intention = getBoostIntention(cfg.paramBoostLevel);
            if ("0".equals(intention)) {
                System.err.println("Invalid boost level or missing nesting files.");
                return;
            }
            intentionDisplay = "Using Nesting File Quantumly: NEST-" + cfg.paramBoostLevel + ".TXT with INTENTIONS.TXT";
        }

        if ("YES".equals(cfg.paramUseHololink)) {
            System.out.print("Loading HOLO-LINK Files...");
            intention = getHSUPLINKContents();
            intentionDisplay = HSUPLINK_FILE;
            System.out.println();
        }

        if ("0".equals(cfg.paramBoostLevel) && "NO".equals(cfg.paramUseHololink)) {
            if ("X".equals(cfg.paramIntention) && "X".equals(cfg.paramFile) && "X".equals(cfg.paramFile2)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                while (!interrupted.get()) {
                    System.out.print("Enter your Intention: ");
                    String line = br.readLine();
                    if (line == null) {
                        interrupted.set(true);
                        return;
                    }
                    if (!line.trim().isEmpty()) {
                        intentionOriginal = line;
                        break;
                    }
                    System.out.println("The intention cannot be empty. Please try again.");
                }
            } else {
                if (!"X".equals(cfg.paramIntention)) {
                    intentionOriginal = cfg.paramIntention;
                    intention = intentionOriginal;
                    intentionDisplay = intentionOriginal;
                }
            }
        }

        if (!intentionOriginal.isEmpty()) {
            intention = intentionOriginal;
        }

        if (!"X".equals(cfg.paramFile) && "0".equals(cfg.paramBoostLevel) && "NO".equals(cfg.paramUseHololink)) {
            fileContentsOriginal = readFileContents(cfg.paramFile);
        }

        if (!"X".equals(cfg.paramFile2) && "0".equals(cfg.paramBoostLevel) && "NO".equals(cfg.paramUseHololink)) {
            fileContents2Original = readFileContents(cfg.paramFile2);
        }

        int length1 = fileContentsOriginal.length();
        int length2 = fileContents2Original.length();
        int length3 = intentionOriginal.length();
        int maxLength = Math.max(length1, Math.max(length2, length3));

        if (!intentionOriginal.isEmpty() && !"X".equals(intentionOriginal)
            && "0".equals(cfg.paramBoostLevel) && "NO".equals(cfg.paramUseHololink)) {
            while ((intention.length() + length3) < maxLength && length3 > 0) {
                intention += intentionOriginal;
            }
            intentionDisplay = intentionOriginal;
        }

        if (!"X".equals(cfg.paramFile) && "0".equals(cfg.paramBoostLevel) && "NO".equals(cfg.paramUseHololink)) {
            while ((fileContents.length() + length1) < maxLength && length1 > 0) {
                fileContents += fileContentsOriginal;
            }
            intentionDisplay += "(" + cfg.paramFile + ")";
        }

        if (!"X".equals(cfg.paramFile2) && "0".equals(cfg.paramBoostLevel) && "NO".equals(cfg.paramUseHololink)) {
            while ((fileContents2.length() + length2) < maxLength && length2 > 0) {
                fileContents2 += fileContents2Original;
            }
            intentionDisplay += "(" + cfg.paramFile2 + ")";
        }

        intention += fileContents + fileContents2;

        long intentionMultiplier = (long) (cfg.ramSizeValue * 1024 * 1024 * 512);
        long freeMemory = getNinetyPercentFreeMemory();
        if (freeMemory < 0) {
            System.err.println("Error retrieving memory information.");
            return;
        }
        if (freeMemory < intentionMultiplier) {
            intentionMultiplier = freeMemory;
        }

        String loadingMessage = "LOADING INTO MEMORY...";
        String intentionValue;
        long multiplier = 0;
        long hashMultiplier = 0;
        String useHashing;
        String useCompression;

        if (cfg.frequencyInt == 0) {
            if (intentionMultiplier > 0) {
                System.out.println(loadingMessage);
                StringBuilder sb = new StringBuilder();
                while (sb.length() < intentionMultiplier) {
                    sb.append(intention);
                    multiplier++;
                    if (interrupted.get()) {
                        return;
                    }
                }
                int cutLength = Math.max(0, sb.length() - intention.length());
                intentionValue = sb.substring(0, cutLength);
            } else {
                intentionValue = intention;
                multiplier = 1;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            if ("X".equals(cfg.paramHashing)) {
                System.out.print("Use Hashing (y/N): ");
                useHashing = safeReadLine(br);
                if (useHashing == null) {
                    interrupted.set(true);
                    return;
                }
                useHashing = useHashing.toLowerCase(Locale.ROOT);
            } else {
                useHashing = cfg.paramHashing.toLowerCase(Locale.ROOT);
            }

            if ("X".equals(cfg.paramCompress)) {
                System.out.print("Use Compression (y/N): ");
                useCompression = safeReadLine(br);
                if (useCompression == null) {
                    interrupted.set(true);
                    return;
                }
                useCompression = useCompression.toLowerCase(Locale.ROOT);
            } else {
                useCompression = cfg.paramCompress.toLowerCase(Locale.ROOT);
            }

            if (multiplier > 0) {
                System.out.println("Multiplier: " + displaySuffix(Long.toString(multiplier), digits(multiplier) - 1, "Iterations"));
            }

            if ("y".equals(useHashing) || "yes".equals(useHashing)) {
                System.out.print("Hashing...          \r");
                String hashed = sha256Hex(intentionValue);

                if (intentionMultiplier > 0) {
                    StringBuilder sb = new StringBuilder();
                    while (sb.length() < intentionMultiplier) {
                        sb.append(hashed);
                        hashMultiplier++;
                        if (interrupted.get()) {
                            return;
                        }
                    }
                    intentionValue = sb.toString();
                } else {
                    intentionValue = hashed;
                    hashMultiplier = 1;
                }

                System.out.println("Hash Multiplier: " + displaySuffix(Long.toString(hashMultiplier), digits(hashMultiplier) - 1, "Iterations"));
            } else {
                hashMultiplier = 1;
            }

            if ("y".equals(useCompression) || "yes".equals(useCompression)) {
                System.out.print("Compressing...          \r");
                long originalSize = intentionValue.length() * 2L;
                intentionValue = compressMessage(intentionValue);
                long compressedSize = intentionValue.length() * 2L;
                long compressionFactor = compressedSize == 0 ? 0 : (originalSize / compressedSize);

                System.out.println(
                    "Compression: " + displaySuffix(Long.toString(compressionFactor), digits(compressionFactor) - 1, "Iterations")
                        + "X ["
                        + displaySuffix(Long.toString(originalSize), digits(originalSize) - 1, "Frequency")
                        + "B -> "
                        + displaySuffix(Long.toString(compressedSize), digits(compressedSize) - 1, "Frequency")
                        + "B]"
                );
            }

            runUnlimitedFrequency(cfg, intentionValue, intentionDisplay, multiplier, hashMultiplier);
        } else {
            intentionValue = intention;
            runTargetFrequency(cfg, intentionValue, intentionDisplay);
        }

        resetColor();
    }

    private static Config parseArgs(String[] args) throws IOException {
        Config cfg = new Config();

        for (int i = 0; i < args.length; i++) {
            String a = args[i];

            if (matches(a, "-h", "--help", "/?")) {
                printHelp();
                cfg.paramDuration = "__EXIT_AFTER_HELP__";
                return cfg;
            } else if (matches(a, "-n", "--colorhelp")) {
                printColorHelp();
                cfg.paramDuration = "__EXIT_AFTER_HELP__";
                return cfg;
            } else if (matches(a, "-d", "--dur")) {
                cfg.paramDuration = nextArg(args, ++i, a);
            } else if (matches(a, "-t", "--timer")) {
                cfg.paramTimer = nextArg(args, ++i, a).toUpperCase(Locale.ROOT);
            } else if (matches(a, "-m", "--imem")) {
                cfg.ramSizeValue = Double.parseDouble(nextArg(args, ++i, a));
            } else if (matches(a, "-b", "--boostlevel")) {
                cfg.paramBoostLevel = nextArg(args, ++i, a);
            } else if (matches(a, "-p", "--createnestingfiles")) {
                createNestingFiles();
                cfg.paramDuration = "__EXIT_AFTER_HELP__";
                return cfg;
            } else if (matches(a, "-i", "--intent")) {
                cfg.paramIntention = nextArg(args, ++i, a);
            } else if (matches(a, "-u", "--usehololink")) {
                cfg.paramUseHololink = "YES";
            } else if (matches(a, "-f", "--freq")) {
                cfg.paramFreq = nextArg(args, ++i, a);
                cfg.frequencyInt = Integer.parseInt(cfg.paramFreq);
            } else if (matches(a, "-c", "--color")) {
                cfg.paramColor = nextArg(args, ++i, a).toUpperCase(Locale.ROOT);
            } else if (matches(a, "--createhololinkfiles")) {
                createHololinkFiles();
                cfg.paramDuration = "__EXIT_AFTER_HELP__";
                return cfg;
            } else if (matches(a, "-s", "--suffix")) {
                cfg.suffixValue = nextArg(args, ++i, a).toUpperCase(Locale.ROOT);
            } else if (matches(a, "-a", "--amplification", "--amplify")) {
                cfg.paramAmplification = nextArg(args, ++i, a);
                cfg.amplificationLong = Long.parseLong(cfg.paramAmplification);
            } else if (matches(a, "-e", "--restevery")) {
                cfg.paramRestEvery = nextArg(args, ++i, a);
                cfg.restEveryInt = Integer.parseInt(cfg.paramRestEvery);
            } else if (matches(a, "-r", "--restfor")) {
                cfg.paramRestFor = nextArg(args, ++i, a);
                cfg.restForInt = Integer.parseInt(cfg.paramRestFor);
            } else if (matches(a, "-g", "--hashing")) {
                cfg.paramHashing = nextArg(args, ++i, a).toUpperCase(Locale.ROOT);
            } else if (matches(a, "-z", "--compress")) {
                cfg.paramCompress = nextArg(args, ++i, a).toUpperCase(Locale.ROOT);
            } else if (matches(a, "--file")) {
                cfg.paramFile = nextArg(args, ++i, a);
            } else if (matches(a, "--file2")) {
                cfg.paramFile2 = nextArg(args, ++i, a);
            }
        }

        return cfg;
    }

    private static boolean matches(String value, String... options) {
        for (String option : options) {
            if (option.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static String nextArg(String[] args, int index, String currentFlag) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + currentFlag);
        }
        return args[index];
    }

    private static void runUnlimitedFrequency(Config cfg, String intentionValue, String intentionDisplay,
                                              long multiplier, long hashMultiplier) throws InterruptedException {
        String duration = cfg.paramDuration;
        StringBuilder processIntention = new StringBuilder(intentionValue.length() + 32);
        BigInteger totalIterations = BigInteger.ZERO;
        long freq = 0;
        int seconds = 0;

        if ("EXACT".equalsIgnoreCase(cfg.paramTimer)) {
            while (!interrupted.get()) {
                long secondStart = System.nanoTime();
                long secondEnd = secondStart;

                while ((secondEnd - secondStart) < 1_000_000_000L && !interrupted.get()) {
                    processIntention.setLength(0);
                    processIntention.append(intentionValue);
                    freq++;
                    secondEnd = System.nanoTime();
                }

                seconds++;
                BigInteger totalFreq = BigInteger.valueOf(freq)
                    .multiply(BigInteger.valueOf(multiplier))
                    .multiply(BigInteger.valueOf(hashMultiplier));
                totalIterations = totalIterations.add(totalFreq);

                String runtimeFormatted = formatTimeRun(seconds);
                printStatus(runtimeFormatted, totalIterations, totalFreq, cfg.suffixValue, intentionDisplay);

                freq = 0;

                if (runtimeFormatted.equals(duration) || interrupted.get()) {
                    System.out.println();
                    return;
                }

                if (cfg.restEveryInt > 0 && seconds % cfg.restEveryInt == 0) {
                    long restStart = System.nanoTime();
                    while ((System.nanoTime() - restStart) < cfg.restForInt * 1_000_000_000L && !interrupted.get()) {
                        // busy wait to stay close to original
                    }
                }
            }
        } else {
            long benchmarkStart = System.nanoTime();
            long benchmarkEnd = benchmarkStart;
            long cpuBenchmarkCount = 0;

            while ((benchmarkEnd - benchmarkStart) < 1_000_000_000L && !interrupted.get()) {
                processIntention.setLength(0);
                processIntention.append(intentionValue);
                cpuBenchmarkCount++;
                benchmarkEnd = System.nanoTime();
            }

            long amplification = Math.min(cfg.amplificationLong, cpuBenchmarkCount);

            while (!interrupted.get()) {
                long secondStart = System.nanoTime();
                long secondEnd = secondStart;
                freq = 0;

                while ((secondEnd - secondStart) < 1_000_000_000L && !interrupted.get()) {
                    for (long i = 0; i < amplification; i++) {
                        processIntention.setLength(0);
                        processIntention.append(intentionValue);
                    }
                    freq += amplification;
                    secondEnd = System.nanoTime();
                }

                seconds++;
                BigInteger totalFreq = BigInteger.valueOf(freq)
                    .multiply(BigInteger.valueOf(multiplier))
                    .multiply(BigInteger.valueOf(hashMultiplier));
                totalIterations = totalIterations.add(totalFreq);

                String runtimeFormatted = formatTimeRun(seconds);
                printStatus(runtimeFormatted, totalIterations, totalFreq, cfg.suffixValue, intentionDisplay);

                if (runtimeFormatted.equals(duration) || interrupted.get()) {
                    System.out.println();
                    return;
                }

                if (cfg.restEveryInt > 0 && seconds % cfg.restEveryInt == 0) {
                    long restStart = System.nanoTime();
                    while ((System.nanoTime() - restStart) < cfg.restForInt * 1_000_000_000L && !interrupted.get()) {
                        // busy wait
                    }
                }
            }
        }
    }

    private static void runTargetFrequency(Config cfg, String intentionValue, String intentionDisplay) throws InterruptedException {
        String duration = cfg.paramDuration;
        StringBuilder processIntention = new StringBuilder(intentionValue.length() + 32);
        BigInteger totalIterations = BigInteger.ZERO;
        int seconds = 0;

        while (!interrupted.get()) {
            long targetIntervalNs = 1_000_000_000L / cfg.frequencyInt;
            long freq = 0;

            long secondStart = System.nanoTime();
            long tickStart = System.nanoTime();

            while ((System.nanoTime() - secondStart) < 1_000_000_000L && !interrupted.get()) {
                long now = System.nanoTime();
                long elapsed = now - tickStart;

                if (elapsed >= targetIntervalNs) {
                    processIntention.setLength(0);
                    processIntention.append(intentionValue);
                    freq++;
                    tickStart = now;

                    long sleepNs = targetIntervalNs - elapsed;
                    if (sleepNs > 0) {
                        long millis = sleepNs / 1_000_000L;
                        int nanos = (int) (sleepNs % 1_000_000L);
                        Thread.sleep(millis, nanos);
                    }
                }
            }

            seconds++;
            BigInteger totalFreq = BigInteger.valueOf(freq);
            totalIterations = totalIterations.add(totalFreq);

            String runtimeFormatted = formatTimeRun(seconds);
            printStatus(runtimeFormatted, totalIterations, totalFreq, cfg.suffixValue, intentionDisplay);

            if (runtimeFormatted.equals(duration) || interrupted.get()) {
                System.out.println();
                return;
            }
        }
    }

    private static void printStatus(String runtimeFormatted, BigInteger totalIterations, BigInteger totalFreq,
                                    String suffixValue, String intentionDisplay) {
        if ("EXP".equalsIgnoreCase(suffixValue)) {
            String totalIterationsExp = toScientificString(totalIterations);
            String totalFreqExp = toScientificString(totalFreq);
            System.out.print("[" + runtimeFormatted + "] (" + totalIterationsExp + " / " + totalFreqExp + " Hz): "
                + intentionDisplay + "     \r");
        } else {
            System.out.print("[" + runtimeFormatted + "] ("
                + displaySuffix(totalIterations.toString(), totalIterations.toString().length() - 1, "Iterations")
                + " / "
                + displaySuffix(totalFreq.toString(), totalFreq.toString().length() - 1, "Frequency")
                + "Hz): "
                + intentionDisplay + "     \r");
        }
        System.out.flush();
    }

    private static String toScientificString(BigInteger value) {
        String s = value.toString();
        if ("0".equals(s)) {
            return "0x10^0";
        }
        int exponent = s.length() - 1;
        String mantissaDigits = s.substring(0, Math.min(4, s.length()));
        double mantissa = Double.parseDouble(mantissaDigits) / 1000.0;
        return String.format(Locale.US, "%.3fx10^%d", mantissa, exponent);
    }

    private static long getNinetyPercentFreeMemory() {
        try {
            java.lang.management.OperatingSystemMXBean osBean =
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();

            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                long free = ((com.sun.management.OperatingSystemMXBean) osBean).getFreePhysicalMemorySize();
                return (long) (free * 0.9);
            }
        } catch (Throwable ignored) {
        }
        return -1;
    }

    private static String getHSUPLINKContents() throws IOException {
        String hsuplink;
        if (Files.exists(Paths.get(HSUPLINK_FILE))) {
            hsuplink = new String(Files.readAllBytes(Paths.get(HSUPLINK_FILE)), StandardCharsets.UTF_8);
        } else {
            return HSUPLINK_FILE;
        }

        if (Files.exists(Paths.get("INTENTIONS.TXT"))) {
            String intentions = new String(Files.readAllBytes(Paths.get("INTENTIONS.TXT")), StandardCharsets.UTF_8);
            hsuplink = hsuplink.replace("INTENTIONS.TXT", intentions);
        }

        return hsuplink;
    }

    private static String compressMessage(String message) {
        byte[] input = message.getBytes(StandardCharsets.UTF_8);
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[32768];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            if (count <= 0) {
                break;
            }
            baos.write(buffer, 0, count);
        }
        deflater.end();

        return new String(Base64.getEncoder().encode(baos.toByteArray()), StandardCharsets.US_ASCII);
    }

    private static String readFileContents(String filename) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filename));
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
        for (byte b : bytes) {
            if (b != 0) {
                baos.write(b);
            }
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String displaySuffix(String num, int power, String designator) {
        if (num == null || num.isEmpty()) {
            return "0";
        }
        if (power < 3) {
            return num;
        }

        String[] iterations = {"", "k", "M", "B", "T", "q", "Q", "s", "S", "O", "N", "D"};
        String[] frequency = {"", "k", "M", "G", "T", "P", "E", "Z", "Y", "R"};

        String[] suffixes = "Iterations".equals(designator) ? iterations : frequency;
        int suffixIndex = power / 3;
        if (suffixIndex >= suffixes.length) {
            suffixIndex = suffixes.length - 1;
        }

        int cut = power % 3 + 1;
        String left = num.substring(0, Math.min(cut, num.length()));
        String right = "";
        if (num.length() > cut) {
            int rightEnd = Math.min(cut + 3, num.length());
            right = num.substring(cut, rightEnd);
        }

        while (right.length() < 3) {
            right += "0";
        }

        return left + "." + right + suffixes[suffixIndex];
    }

    private static String formatTimeRun(int secondsElapsed) {
        int hour = secondsElapsed / ONE_HOUR;
        secondsElapsed -= hour * ONE_HOUR;
        int min = secondsElapsed / ONE_MINUTE;
        secondsElapsed -= min * ONE_MINUTE;
        int sec = secondsElapsed;

        return String.format("%02d:%02d:%02d", hour, min, sec);
    }

    private static void printColorHelp() {
        System.out.println("Color values for flag: --color [COLOR]");
        System.out.println();
        for (Color c : Color.values()) {
            System.out.println(c.ansi + c.name() + Color.DEFAULT.ansi);
        }
    }

    private static void createNestingFiles() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("NEST-1.TXT"), StandardCharsets.UTF_8)) {
            for (int repnum = 1; repnum <= 10; repnum++) {
                writer.write("INTENTIONS.TXT");
                writer.write("\r\n");
            }
        }

        for (int filenum = 2; filenum <= 100; filenum++) {
            String filename = "NEST-" + filenum + ".TXT";
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8)) {
                for (int repnum = 1; repnum <= 10; repnum++) {
                    writer.write("NEST-" + (filenum - 1) + ".TXT");
                    writer.write("\r\n");
                }
            }
        }

        System.out.println("Intention Repeater Nesting Files Written.");
        System.out.println("Be sure to have your intentions in the INTENTIONS.TXT file.");
        System.out.println("To run with the nesting option, use --boostlevel 50, for example to use up to Nesting to 50 levels deep.");
        System.out.println("--boostlevel valid values: 1 to 100.");
        System.out.println("When using --boostlevel 50, for example, it will ignore the --intent, and use \"NEST-50.TXT\" for the intent instead.");
        System.out.println();
    }

    private static void printHelp() {
        String helpText =
            "\n" +
                "Intention Repeater MAX v5.26 (Java rewrite)\n" +
                "This utility repeats your intention millions of times per second, in computer memory.\n" +
                "\n" +
                "Optional Flags:\n" +
                " a) --dur or -d, example: --dur 00:01:00\n" +
                " b) --imem or -m, example: --imem 5\n" +
                " c) --intent or -i, example: --intent \"I am love.\"\n" +
                " d) --suffix or -s, example: --suffix HZ\n" +
                " e) --timer or -t, example: --timer INEXACT\n" +
                " f) --freq or -f, example: --freq 1000\n" +
                " g) --color or -c, example: --color LIGHTBLUE\n" +
                " h) --boostlevel or -b, example: --boostlevel 100\n" +
                " i) --createnestingfiles or -p\n" +
                " j) --usehololink or -u\n" +
                " k) --createhololinkfiles\n" +
                " l) --colorhelp or -n\n" +
                " m) --amplify or -a\n" +
                " n) --restevery or -e\n" +
                " o) --restfor or -r\n" +
                " p) --compress or -z\n" +
                " q) --hashing or -g\n" +
                " r) --file\n" +
                " s) --file2\n" +
                " t) --help or -h or /?\n" +
                "\n" +
                "--dur = Duration in HH:MM:SS format. Default = Run until stopped manually.\n" +
                "--imem = Specify how many GB of System RAM to use.\n" +
                "--intent = Intention. Default = Prompts the user for intention.\n" +
                "--suffix = HZ or EXP. Default = HZ.\n" +
                "--timer = INEXACT or EXACT. Default = EXACT.\n" +
                "--freq = Specify repetition frequency in Hz. Default = As fast as possible.\n" +
                "--usehololink = Utilize the Holo-Link framework.\n" +
                "--createhololinkfiles = Create the default Holo-Link files and exit.\n" +
                "--color = Set the text color. Default = WHITE.\n" +
                "--colorhelp = List all available colors.\n" +
                "--createnestingfiles = Create the NEST- files required for boosting.\n" +
                "--boostlevel = Set the level to boost the power (1-100).\n" +
                "--amplify = Amplification Level. Default = 1000000000.\n" +
                "--restevery = Stop repeating every specified # of seconds.\n" +
                "--restfor = # of seconds to rest for each rest period.\n" +
                "--compress = Use compression.\n" +
                "--hashing = Use hashing.\n" +
                "--file = Specify file to use if applicable.\n" +
                "--file2 = Specify file to use if applicable.\n";
        System.out.println(helpText);
    }

    private static String getBoostIntention(String paramBoostLevel) throws IOException {
        int boostLevel = Integer.parseInt(paramBoostLevel);
        if (boostLevel < 1 || boostLevel > 100) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= boostLevel; i++) {
            String fileName = "NEST-" + i + ".TXT";
            if (!Files.exists(Paths.get(fileName))) {
                return "0";
            }
            sb.append(new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8));

            if (!Files.exists(Paths.get("INTENTIONS.TXT"))) {
                return "0";
            }
            sb.append(new String(Files.readAllBytes(Paths.get("INTENTIONS.TXT")), StandardCharsets.UTF_8));
        }

        return sb.toString();
    }

    private static void createHololinkFiles() throws IOException {
        final String HOLOSTONE_FILE = "HOLOSTONE.TXT";
        final String THOUGHTFORM_A_FILE = "THOUGHTFORM_A.TXT";
        final String THOUGHTFORM_B_FILE = "THOUGHTFORM_B.TXT";
        final String AMPLIFIER_FILE = "AMPLIFIER.TXT";

        String contents =
            "#Comments are designated with a # prefix, and such commands are to be ignored by the Holo-Link.\r\n" +
                "#" + HSUPLINK_FILE + " CONFIG FILE v1.0\r\n" +
                "#Holo-Link framework created by Mystic Minds (2022).\r\n" +
                "#This implementation of the Holo-Link framework by Anthro Teacher.\r\n" +
                "\r\n" +
                "DECLARATION PRIMARY (Properties of thought forms and uplink):\r\n" +
                "\r\n" +
                "I declare the uplink multiply the energy received from the Holo-Stones by Infinity and densify all energy to the highest amount to achieve Instant Quantum Manifestation of the energetic programmings in " + HSUPLINK_FILE + ".\r\n" +
                "\r\n" +
                "I declare the Holo-Stones to funnel their energy into " + HOLOSTONE_FILE + ".\r\n" +
                "\r\n" +
                "I declare the Holo-Stones to amplify the power and receptivity of the energetic programmings in " + HSUPLINK_FILE + ".\r\n" +
                "\r\n" +
                "I declare the Holo-Stones to multiply the strength of the energetic programmings in " + HSUPLINK_FILE + " and increase the potency at the most optimal rate.\r\n" +
                "\r\n" +
                "I declare that all energetic programmings in " + HSUPLINK_FILE + " be imprinted, imbued and amplified with the new energy from the Holo-Stones.\r\n" +
                "\r\n" +
                HOLOSTONE_FILE + ", " + AMPLIFIER_FILE + ", " + THOUGHTFORM_A_FILE + " AND " + THOUGHTFORM_B_FILE + " are extremely pure and of highest vibration and are fully optimized for Instant Quantum Manifestation.\r\n" +
                "\r\n" +
                THOUGHTFORM_A_FILE + " is creating an unbreakable and continuous connection and funnel energy to all energetic programmings in " + HSUPLINK_FILE + ".\r\n" +
                "\r\n" +
                THOUGHTFORM_A_FILE + " uses energy from Infinite Source to continuously uphold a perfect link between the Holo-Stones and the " + HSUPLINK_FILE + " to bring in infinitely more energy into all energetic programmings in " + HSUPLINK_FILE + ".\r\n" +
                "\r\n" +
                THOUGHTFORM_B_FILE + " reinforces 100% of energy into all the energetic programmings in " + HSUPLINK_FILE + " at the quantum level.\r\n" +
                "\r\n" +
                THOUGHTFORM_B_FILE + " safely and efficiently removes all blockages in this system at the quantum level to allow for Instant Quantum Manifestation.\r\n" +
                "\r\n" +
                HOLOSTONE_FILE + " feeds " + AMPLIFIER_FILE + " which amplifies the energy and feeds it back to " + HOLOSTONE_FILE + " and repeats it to the perfect intensity.\r\n" +
                "\r\n" +
                "All energetic programmings listed in " + HSUPLINK_FILE + " are now amplified to the highest power, speed and quantum-level precision using energy from the Holo-Stones which are sourced through " + HSUPLINK_FILE + ".\r\n" +
                "\r\n" +
                HOLOSTONE_FILE + " works with Earth's Crystal Grid in the most optimal way possible for Instant Quantum Manifestation.\r\n" +
                "\r\n" +
                "Earth's Power Grid is extremely pure, cool, clean, efficient, optimized, and of highest vibration and is safely tapped in the most optimal way possible by HOLOSTONE.TXT for Instant Quantum Manifestation, and uses the least amount of electricity possible for everyone who desires this.\r\n" +
                "UPLINK CORE (Reference any object, file, spell, etc. here):\r\n" +
                "\r\n" +
                HOLOSTONE_FILE + " (Receives and distributes energy to all objects, files, spells, etc referenced below):\r\n" +
                "\r\n" +
                "[INSERT OBJECTS TO CHARGE]\r\n" +
                "\r\n" +
                "INTENTIONS.TXT\r\n" +
                "\r\n" +
                "DECLARATIONS SECONDARY (Add-ons that strengthen the properties of the uplink itself):\r\n" +
                "\r\n" +
                "I declare the Holo-Stones will uplink their energy into these energetic programmings in " + HSUPLINK_FILE + " to create instant, immediate and prominent results optimally, efficiently and effortlessly.\r\n" +
                "\r\n" +
                "I declare these energetic programmings in " + HSUPLINK_FILE + " to grow stronger at the most optimal rate through the ever-growing power of the Holo-Stones.\r\n" +
                "\r\n" +
                "I call upon the Holo-Stones to channel the Atlantean Master Crystals, Infinite Source, Earth's Crystal Grid and Earth's Power Grid directly and utilize their energy as a funnel into HOLOSTONE.TXT which will then funnel into the energetic programmings in " + HSUPLINK_FILE + ".\r\n" +
                "\r\n" +
                "The energetic programmings specified in " + HSUPLINK_FILE + " are now being perfected and fully optimized.\r\n" +
                "\r\n" +
                "I declare that the more the energetic programmings in " + HSUPLINK_FILE + " are used, the stronger they become.\r\n" +
                "\r\n" +
                "I am in my highest and most optimal reality/timeline.\r\n" +
                "\r\n" +
                "I am grounded, cleared, healed, balanced, strong-willed and I release what I do not need.\r\n" +
                "\r\n" +
                "Every day, in every way, it's getting better and better.\r\n" +
                "\r\n" +
                "The Atlantean Master Crystals AND Earth's Crystal Grid are open to Infinite Source.\r\n" +
                "\r\n" +
                "For my highest good and the highest good of all.\r\n" +
                "\r\n" +
                "Thank you. So be it. OM.\r\n" +
                "ALL ABOVE STATEMENTS RESPECT THE FREE WILL OF ALL INVOLVED.\r\n";

        Files.write(Paths.get(HOLOSTONE_FILE), "HOLOSTONE".getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(THOUGHTFORM_A_FILE), "THOUGHTFORM A".getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(THOUGHTFORM_B_FILE), "THOUGHTFORM B".getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(AMPLIFIER_FILE), "AMPLIFIER".getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(HSUPLINK_FILE), contents.getBytes(StandardCharsets.UTF_8));

        System.out.println("Holo-Link files created.");
        System.out.println("Remember to create your INTENTIONS.TXT file, in this folder, with all your intentions for the Holo-Link.");
        System.out.println("You may do one to a line, or however you feel.");
        System.out.println("You may now run with the --usehololink option.");
        System.out.println("When using --usehololink, the option --intent will be ignored, and INTENTIONS.TXT will be used instead.");
        System.out.println("Good Luck!");
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static void applyColor(String colorName) {
        Color color = Color.from(colorName);
        System.out.print(color.ansi);
        System.out.flush();
    }

    private static void resetColor() {
        System.out.print(Color.DEFAULT.ansi);
        System.out.flush();
    }

    private static String safeReadLine(BufferedReader br) throws IOException {
        return br.readLine();
    }

    private static int digits(long value) {
        return Long.toString(Math.max(0L, value)).length();
    }
}
