package isuret.polos.aopi.services;

import com.badlogic.gdx.Gdx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
 * Service-oriented version of IntentionRepeaterMax for libGDX usage.
 *
 * Features:
 * - runs in background thread
 * - progress callback for UI updates
 * - one public service method with all params
 * - can be stopped externally
 * - no console input/output required
 */
public class IntentionBroadcastService {

    private static final int ONE_MINUTE = 60;
    private static final int ONE_HOUR = 3600;
    private static final String HSUPLINK_FILE = "HSUPLINK.TXT";

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private Thread workerThread;

    public interface ProgressListener {
        void onPrepared(BroadcastProgress progress);
        void onProgress(BroadcastProgress progress);
        void onCompleted(BroadcastProgress progress);
        void onError(Throwable error);
    }

    public static class BroadcastProgress {
        public boolean running;
        public boolean finished;
        public boolean stopped;
        public boolean exactTimer;
        public boolean useHololink;
        public boolean hashingEnabled;
        public boolean compressionEnabled;

        public String intentionDisplay;
        public String runtimeFormatted;
        public String suffixMode;
        public String timerMode;

        public int elapsedSeconds;
        public int targetFrequency;
        public int restEverySeconds;
        public int restForSeconds;

        public long rawLoopFrequency;
        public long multiplier;
        public long hashMultiplier;
        public long effectiveFreeMemoryBytes;
        public long requestedMemoryBytes;
        public long allocatedIntentionBytes;
        public long compressionFactor;

        public double requestedMemoryGigabytes;
        public float completionRatio;

        public BigInteger totalIterations = BigInteger.ZERO;
        public BigInteger totalFrequency = BigInteger.ZERO;

        public String totalIterationsDisplay;
        public String totalFrequencyDisplay;
        public String scientificIterationsDisplay;
        public String scientificFrequencyDisplay;

        public String preparedIntentionPreview;
        public int preparedIntentionLength;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void stopBroadcast() {
        stopRequested.set(true);
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    /**
     * One public service method with all params.
     *
     * @param intention direct intention text; can be null if using file/hololink/boost
     * @param durationHHMMSS null or "UNTIL STOPPED" for endless
     * @param memoryGigabytes desired preparation size similar to --imem
     * @param timerMode EXACT or INEXACT
     * @param targetFrequencyHz 0 = as fast as possible, otherwise throttle to target Hz
     * @param useHololink true to use HSUPLINK.TXT / INTENTIONS.TXT
     * @param boostLevel 0..100
     * @param amplification used only for INEXACT mode
     * @param restEverySeconds 0 = disabled
     * @param restForSeconds 0 = disabled
     * @param hashingEnabled true = SHA-256 expand
     * @param compressionEnabled true = compress prepared payload
     * @param suffixMode HZ or EXP
     * @param file optional extra file content
     * @param file2 optional extra file content
     * @param listener callback for UI progress
     */
    public void broadcastRepetition(
        final String intention,
        final String durationHHMMSS,
        final double memoryGigabytes,
        final String timerMode,
        final int targetFrequencyHz,
        final boolean useHololink,
        final int boostLevel,
        final long amplification,
        final int restEverySeconds,
        final int restForSeconds,
        final boolean hashingEnabled,
        final boolean compressionEnabled,
        final String suffixMode,
        final String file,
        final String file2,
        final ProgressListener listener
    ) {
        if (running.get()) {
            throw new IllegalStateException("Broadcast already running");
        }

        running.set(true);
        stopRequested.set(false);

        workerThread = new Thread(() -> {
            BroadcastProgress progress = new BroadcastProgress();
            try {
                progress.running = true;
                progress.finished = false;
                progress.stopped = false;
                progress.exactTimer = "EXACT".equalsIgnoreCase(timerMode);
                progress.useHololink = useHololink;
                progress.hashingEnabled = hashingEnabled;
                progress.compressionEnabled = compressionEnabled;
                progress.timerMode = normalize(timerMode, "EXACT");
                progress.suffixMode = normalize(suffixMode, "HZ");
                progress.requestedMemoryGigabytes = memoryGigabytes;
                progress.restEverySeconds = Math.max(0, restEverySeconds);
                progress.restForSeconds = Math.max(0, restForSeconds);
                progress.targetFrequency = Math.max(0, targetFrequencyHz);

                String preparedIntention = prepareIntention(
                    intention,
                    useHololink,
                    boostLevel,
                    file,
                    file2,
                    progress
                );

                String intentionDisplay = progress.intentionDisplay != null && !progress.intentionDisplay.isEmpty()
                    ? progress.intentionDisplay
                    : preparedIntention;

                long requestedMemoryBytes = (long) (memoryGigabytes * 1024d * 1024d * 512d);
                long freeMemory = getNinetyPercentFreeMemory();
                if (freeMemory < 0) {
                    throw new IllegalStateException("Error retrieving memory information");
                }

                progress.effectiveFreeMemoryBytes = freeMemory;
                progress.requestedMemoryBytes = requestedMemoryBytes;

                long intentionMultiplier = requestedMemoryBytes;
                if (freeMemory < intentionMultiplier) {
                    intentionMultiplier = freeMemory;
                }

                String preparedValue;
                long multiplier = 0L;
                long hashMultiplier = 1L;

                if (targetFrequencyHz == 0) {
                    if (intentionMultiplier > 0) {
                        StringBuilder sb = new StringBuilder();
                        while (sb.length() < intentionMultiplier && !stopRequested.get()) {
                            sb.append(preparedIntention);
                            multiplier++;
                        }
                        int cutLength = Math.max(0, sb.length() - preparedIntention.length());
                        preparedValue = sb.substring(0, cutLength);
                    } else {
                        preparedValue = preparedIntention;
                        multiplier = 1L;
                    }

                    progress.multiplier = multiplier;
                    progress.allocatedIntentionBytes = preparedValue.length() * 2L;

                    if (hashingEnabled && !stopRequested.get()) {
                        String hashed = sha256Hex(preparedValue);
                        if (intentionMultiplier > 0) {
                            StringBuilder sb = new StringBuilder();
                            hashMultiplier = 0L;
                            while (sb.length() < intentionMultiplier && !stopRequested.get()) {
                                sb.append(hashed);
                                hashMultiplier++;
                            }
                            preparedValue = sb.toString();
                        } else {
                            preparedValue = hashed;
                            hashMultiplier = 1L;
                        }
                    }

                    progress.hashMultiplier = hashMultiplier;

                    if (compressionEnabled && !stopRequested.get()) {
                        long originalBytes = preparedValue.length() * 2L;
                        preparedValue = compressMessage(preparedValue);
                        long compressedBytes = preparedValue.length() * 2L;
                        progress.compressionFactor = compressedBytes == 0 ? 0L : (originalBytes / compressedBytes);
                        progress.allocatedIntentionBytes = compressedBytes;
                    }
                } else {
                    preparedValue = preparedIntention;
                    multiplier = 1L;
                    hashMultiplier = 1L;
                    progress.multiplier = multiplier;
                    progress.hashMultiplier = hashMultiplier;
                    progress.allocatedIntentionBytes = preparedValue.length() * 2L;
                }

                progress.preparedIntentionLength = preparedValue.length();
                progress.preparedIntentionPreview = preparedValue.substring(0, Math.min(120, preparedValue.length()));
                notifyPrepared(listener, snapshot(progress));

                runLoop(
                    preparedValue,
                    intentionDisplay,
                    normalize(durationHHMMSS, "UNTIL STOPPED"),
                    progress,
                    multiplier,
                    hashMultiplier,
                    Math.max(1L, amplification),
                    listener
                );

                progress.running = false;
                progress.finished = !stopRequested.get();
                progress.stopped = stopRequested.get();
                notifyCompleted(listener, snapshot(progress));

            } catch (Throwable t) {
                progress.running = false;
                progress.finished = false;
                progress.stopped = stopRequested.get();
                notifyError(listener, t);
            } finally {
                running.set(false);
                stopRequested.set(false);
                workerThread = null;
            }
        }, "IntentionBroadcastService-Worker");

        workerThread.setDaemon(true);
        workerThread.start();
    }

    private void runLoop(
        String intentionValue,
        String intentionDisplay,
        String duration,
        BroadcastProgress progress,
        long multiplier,
        long hashMultiplier,
        long amplification,
        ProgressListener listener
    ) throws InterruptedException {
        StringBuilder processIntention = new StringBuilder(intentionValue.length() + 32);
        BigInteger totalIterations = BigInteger.ZERO;
        int seconds = 0;

        if (progress.targetFrequency == 0) {
            if (progress.exactTimer) {
                while (!stopRequested.get()) {
                    long secondStart = System.nanoTime();
                    long secondEnd = secondStart;
                    long freq = 0L;

                    while ((secondEnd - secondStart) < 1_000_000_000L && !stopRequested.get()) {
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
                    updateProgress(progress, seconds, duration, freq, totalFreq, totalIterations, intentionDisplay);
                    notifyProgress(listener, snapshot(progress));

                    if (isDurationReached(progress.runtimeFormatted, duration)) {
                        return;
                    }

                    doRestIfNeeded(progress);
                }
            } else {
                long benchmarkStart = System.nanoTime();
                long benchmarkEnd = benchmarkStart;
                long cpuBenchmarkCount = 0L;

                while ((benchmarkEnd - benchmarkStart) < 1_000_000_000L && !stopRequested.get()) {
                    processIntention.setLength(0);
                    processIntention.append(intentionValue);
                    cpuBenchmarkCount++;
                    benchmarkEnd = System.nanoTime();
                }

                long effectiveAmplification = Math.min(amplification, Math.max(1L, cpuBenchmarkCount));

                while (!stopRequested.get()) {
                    long secondStart = System.nanoTime();
                    long secondEnd = secondStart;
                    long freq = 0L;

                    while ((secondEnd - secondStart) < 1_000_000_000L && !stopRequested.get()) {
                        for (long i = 0; i < effectiveAmplification; i++) {
                            processIntention.setLength(0);
                            processIntention.append(intentionValue);
                        }
                        freq += effectiveAmplification;
                        secondEnd = System.nanoTime();
                    }

                    seconds++;
                    BigInteger totalFreq = BigInteger.valueOf(freq)
                        .multiply(BigInteger.valueOf(multiplier))
                        .multiply(BigInteger.valueOf(hashMultiplier));

                    totalIterations = totalIterations.add(totalFreq);
                    updateProgress(progress, seconds, duration, freq, totalFreq, totalIterations, intentionDisplay);
                    notifyProgress(listener, snapshot(progress));

                    if (isDurationReached(progress.runtimeFormatted, duration)) {
                        return;
                    }

                    doRestIfNeeded(progress);
                }
            }
        } else {
            while (!stopRequested.get()) {
                long targetIntervalNs = 1_000_000_000L / progress.targetFrequency;
                long freq = 0L;
                long secondStart = System.nanoTime();
                long tickStart = System.nanoTime();

                while ((System.nanoTime() - secondStart) < 1_000_000_000L && !stopRequested.get()) {
                    long now = System.nanoTime();
                    long elapsed = now - tickStart;

                    if (elapsed >= targetIntervalNs) {
                        processIntention.setLength(0);
                        processIntention.append(intentionValue);
                        freq++;
                        tickStart = now;
                    } else {
                        long remaining = targetIntervalNs - elapsed;
                        if (remaining > 1_000_000L) {
                            Thread.sleep(0, 250_000);
                        }
                    }
                }

                seconds++;
                BigInteger totalFreq = BigInteger.valueOf(freq);
                totalIterations = totalIterations.add(totalFreq);

                updateProgress(progress, seconds, duration, freq, totalFreq, totalIterations, intentionDisplay);
                notifyProgress(listener, snapshot(progress));

                if (isDurationReached(progress.runtimeFormatted, duration)) {
                    return;
                }

                doRestIfNeeded(progress);
            }
        }
    }

    private void updateProgress(
        BroadcastProgress progress,
        int seconds,
        String duration,
        long rawFreq,
        BigInteger totalFreq,
        BigInteger totalIterations,
        String intentionDisplay
    ) {
        progress.elapsedSeconds = seconds;
        progress.runtimeFormatted = formatTimeRun(seconds);
        progress.rawLoopFrequency = rawFreq;
        progress.totalFrequency = totalFreq;
        progress.totalIterations = totalIterations;
        progress.intentionDisplay = intentionDisplay;

        progress.totalIterationsDisplay = displaySuffix(
            totalIterations.toString(),
            totalIterations.toString().length() - 1,
            "Iterations"
        );
        progress.totalFrequencyDisplay = displaySuffix(
            totalFreq.toString(),
            totalFreq.toString().length() - 1,
            "Frequency"
        );
        progress.scientificIterationsDisplay = toScientificString(totalIterations);
        progress.scientificFrequencyDisplay = toScientificString(totalFreq);

        if (duration != null && !"UNTIL STOPPED".equalsIgnoreCase(duration)) {
            int totalSeconds = parseDurationToSeconds(duration);
            if (totalSeconds > 0) {
                progress.completionRatio = Math.min(1f, (float) seconds / (float) totalSeconds);
            } else {
                progress.completionRatio = 0f;
            }
        } else {
            progress.completionRatio = 0f;
        }
    }

    private void doRestIfNeeded(BroadcastProgress progress) throws InterruptedException {
        if (progress.restEverySeconds > 0
            && progress.restForSeconds > 0
            && progress.elapsedSeconds % progress.restEverySeconds == 0) {
            long restStart = System.nanoTime();
            while ((System.nanoTime() - restStart) < progress.restForSeconds * 1_000_000_000L && !stopRequested.get()) {
                Thread.sleep(1L);
            }
        }
    }

    private boolean isDurationReached(String runtimeFormatted, String duration) {
        return duration != null
            && !"UNTIL STOPPED".equalsIgnoreCase(duration)
            && runtimeFormatted.equals(duration);
    }

    private String prepareIntention(
        String intention,
        boolean useHololink,
        int boostLevel,
        String file,
        String file2,
        BroadcastProgress progress
    ) throws IOException {
        String baseIntention = "";
        String intentionOriginal = intention == null ? "" : intention;
        String fileContentsOriginal = "";
        String fileContents2Original = "";
        String fileContents = "";
        String fileContents2 = "";

        if (boostLevel > 0) {
            baseIntention = getBoostIntention(Integer.toString(boostLevel));
            if ("0".equals(baseIntention)) {
                throw new IllegalStateException("Invalid boost level or missing nesting files");
            }
            progress.intentionDisplay = "Using Nesting File Quantumly: NEST-" + boostLevel + ".TXT with INTENTIONS.TXT";
            return baseIntention;
        }

        if (useHololink) {
            baseIntention = getHSUPLINKContents();
            progress.intentionDisplay = HSUPLINK_FILE;
            return baseIntention;
        }

        if (file != null && !file.trim().isEmpty()) {
            fileContentsOriginal = readFileContents(file);
        }
        if (file2 != null && !file2.trim().isEmpty()) {
            fileContents2Original = readFileContents(file2);
        }

        int length1 = fileContentsOriginal.length();
        int length2 = fileContents2Original.length();
        int length3 = intentionOriginal.length();
        int maxLength = Math.max(length1, Math.max(length2, length3));

        baseIntention = intentionOriginal;

        if (!intentionOriginal.isEmpty()) {
            while ((baseIntention.length() + length3) < maxLength && length3 > 0) {
                baseIntention += intentionOriginal;
            }
            progress.intentionDisplay = intentionOriginal;
        }

        if (!fileContentsOriginal.isEmpty()) {
            while ((fileContents.length() + length1) < maxLength && length1 > 0) {
                fileContents += fileContentsOriginal;
            }
            progress.intentionDisplay = (progress.intentionDisplay == null ? "" : progress.intentionDisplay) + "(" + file + ")";
        }

        if (!fileContents2Original.isEmpty()) {
            while ((fileContents2.length() + length2) < maxLength && length2 > 0) {
                fileContents2 += fileContents2Original;
            }
            progress.intentionDisplay = (progress.intentionDisplay == null ? "" : progress.intentionDisplay) + "(" + file2 + ")";
        }

        return baseIntention + fileContents + fileContents2;
    }

    private static String normalize(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static int parseDurationToSeconds(String duration) {
        try {
            String[] parts = duration.split(":");
            if (parts.length != 3) {
                return 0;
            }
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int s = Integer.parseInt(parts[2]);
            return h * 3600 + m * 60 + s;
        } catch (Exception e) {
            return 0;
        }
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

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String formatTimeRun(int secondsElapsed) {
        int hour = secondsElapsed / ONE_HOUR;
        secondsElapsed -= hour * ONE_HOUR;
        int min = secondsElapsed / ONE_MINUTE;
        secondsElapsed -= min * ONE_MINUTE;
        int sec = secondsElapsed;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hour, min, sec);
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

    private BroadcastProgress snapshot(BroadcastProgress src) {
        BroadcastProgress p = new BroadcastProgress();
        p.running = src.running;
        p.finished = src.finished;
        p.stopped = src.stopped;
        p.exactTimer = src.exactTimer;
        p.useHololink = src.useHololink;
        p.hashingEnabled = src.hashingEnabled;
        p.compressionEnabled = src.compressionEnabled;
        p.intentionDisplay = src.intentionDisplay;
        p.runtimeFormatted = src.runtimeFormatted;
        p.suffixMode = src.suffixMode;
        p.timerMode = src.timerMode;
        p.elapsedSeconds = src.elapsedSeconds;
        p.targetFrequency = src.targetFrequency;
        p.restEverySeconds = src.restEverySeconds;
        p.restForSeconds = src.restForSeconds;
        p.rawLoopFrequency = src.rawLoopFrequency;
        p.multiplier = src.multiplier;
        p.hashMultiplier = src.hashMultiplier;
        p.effectiveFreeMemoryBytes = src.effectiveFreeMemoryBytes;
        p.requestedMemoryBytes = src.requestedMemoryBytes;
        p.allocatedIntentionBytes = src.allocatedIntentionBytes;
        p.compressionFactor = src.compressionFactor;
        p.requestedMemoryGigabytes = src.requestedMemoryGigabytes;
        p.completionRatio = src.completionRatio;
        p.totalIterations = src.totalIterations;
        p.totalFrequency = src.totalFrequency;
        p.totalIterationsDisplay = src.totalIterationsDisplay;
        p.totalFrequencyDisplay = src.totalFrequencyDisplay;
        p.scientificIterationsDisplay = src.scientificIterationsDisplay;
        p.scientificFrequencyDisplay = src.scientificFrequencyDisplay;
        p.preparedIntentionPreview = src.preparedIntentionPreview;
        p.preparedIntentionLength = src.preparedIntentionLength;
        return p;
    }

    private void notifyPrepared(ProgressListener listener, BroadcastProgress progress) {
        if (listener == null) return;
        if (Gdx.app != null) {
            Gdx.app.postRunnable(() -> listener.onPrepared(progress));
        } else {
            listener.onPrepared(progress);
        }
    }

    private void notifyProgress(ProgressListener listener, BroadcastProgress progress) {
        if (listener == null) return;
        if (Gdx.app != null) {
            Gdx.app.postRunnable(() -> listener.onProgress(progress));
        } else {
            listener.onProgress(progress);
        }
    }

    private void notifyCompleted(ProgressListener listener, BroadcastProgress progress) {
        if (listener == null) return;
        if (Gdx.app != null) {
            Gdx.app.postRunnable(() -> listener.onCompleted(progress));
        } else {
            listener.onCompleted(progress);
        }
    }

    private void notifyError(ProgressListener listener, Throwable error) {
        if (listener == null) return;
        if (Gdx.app != null) {
            Gdx.app.postRunnable(() -> listener.onError(error));
        } else {
            listener.onError(error);
        }
    }
}
