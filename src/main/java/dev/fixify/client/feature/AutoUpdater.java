package dev.fixify.client.feature;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import dev.fixify.client.FixifyClient;
import dev.fixify.client.FixifyConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

public final class AutoUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger("Fixify AutoUpdater");
    private static final Gson GSON = new Gson();
    private static final long CHECK_INTERVAL_MS = 15L * 60L * 1000L;
    private static final AtomicBoolean CHECKING = new AtomicBoolean();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final String PENDING_UPDATE_SUFFIX = ".fixify-update";
    private static final String UPDATE_REPO = "AkuTheKing/Fixify";
    private static final String EXPECTED_MOD_ID = FixifyClient.MOD_ID;
    private static final String EXPECTED_ASSET_PREFIX = "fixify-";
    private static final String INSTALLER_MAIN_CLASS = "dev.fixify.client.feature.UpdateInstaller";
    private static final String INSTALLER_CLASS_RESOURCE = "/" + INSTALLER_MAIN_CLASS.replace('.', '/') + ".class";
    private static final TextColor INITIAL_UPDATE_MESSAGE_COLOR = TextColor.fromRgb(0xFFFF55);
    private static final int INITIAL_UPDATE_MESSAGE_DELAY_TICKS = 3;

    private static volatile long lastCheckStartedMs;
    private static int initialUpdateMessageTicksRemaining = INITIAL_UPDATE_MESSAGE_DELAY_TICKS;
    private static volatile String statusLine = "Auto updater enabled.";

    private AutoUpdater() {
    }

    public static void init() {
        if (isEnabled()) {
            checkForUpdatesAsync(false);
        } else {
            statusLine = "Auto updater is disabled. Use /fixify auto on to enable it.";
        }
    }

    public static void onClientTick(Minecraft client) {
        if (!isEnabled()) {
            return;
        }
        maybeShowInitialUpdateMessage(client);
        if (CHECKING.get()) {
            return;
        }
        long now = Util.getMillis();
        if (lastCheckStartedMs == 0L || now - lastCheckStartedMs >= CHECK_INTERVAL_MS) {
            checkForUpdatesAsync(false);
        }
    }

    public static void onClientStopping() {
        if (!isEnabled()) {
            return;
        }
        trySchedulePendingInstall();
    }

    public static String getStatusLine() {
        return statusLine;
    }

    public static boolean isEnabled() {
        return FixifyConfig.INSTANCE.getAutoUpdateEnabled();
    }

    public static void setEnabledState(boolean enabled) {
        FixifyConfig.INSTANCE.setAutoUpdateEnabled(enabled);
        FixifyConfig.INSTANCE.setAutoUpdateConsentGiven(enabled);
        if (!enabled) {
            FixifyConfig.INSTANCE.setInitialUpdateMessageShown(true);
        }
        FixifyConfig.INSTANCE.save();
        statusLine = enabled ? "Auto updater enabled." : "Auto updater disabled. Use /fixify auto on to enable it.";
        if (!enabled) {
            clearPendingUpdate();
        }
    }

    public static void clearPendingUpdate() {
        resolveCurrentJarPath()
                .map(AutoUpdater::pendingPathFor)
                .ifPresent(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        LOGGER.warn("Failed to clear pending update {}", path, e);
                    }
                });
    }

    public static void checkForUpdatesAsync(boolean manual) {
        if (!isEnabled()) {
            statusLine = "Auto updater is disabled. Use /fixify auto on to enable it.";
            return;
        }
        if (!CHECKING.compareAndSet(false, true)) {
            statusLine = "Already checking GitHub for updates...";
            return;
        }
        lastCheckStartedMs = Util.getMillis();
        statusLine = "Checking GitHub releases...";

        Thread.startVirtualThread(() -> {
            try {
                performCheck(manual);
            } catch (Exception e) {
                statusLine = "Update check failed. See log for details.";
                LOGGER.error("Failed to check for updates", e);
            } finally {
                CHECKING.set(false);
            }
        });
    }

    private static void maybeShowInitialUpdateMessage(Minecraft client) {
        if (FixifyConfig.INSTANCE.getInitialUpdateMessageShown() || client == null || client.player == null) {
            return;
        }
        if (initialUpdateMessageTicksRemaining > 0) {
            initialUpdateMessageTicksRemaining--;
            return;
        }
        FixifyConfig.INSTANCE.setInitialUpdateMessageShown(true);
        FixifyConfig.INSTANCE.save();
        client.player.sendSystemMessage(Component.literal("FIXIFY: Autoupdater is on, click here to disable")
                .withStyle(style -> style
                        .withColor(INITIAL_UPDATE_MESSAGE_COLOR)
                        .withClickEvent(new ClickEvent.RunCommand("/fixify auto off"))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Disable Fixify automatic updates")))));
    }

    private static void performCheck(boolean manual) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/" + UPDATE_REPO + "/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "Fixify AutoUpdater")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> releaseResponse = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (releaseResponse.statusCode() != 200) {
            statusLine = "GitHub responded with HTTP " + releaseResponse.statusCode() + '.';
            return;
        }

        GithubRelease release = GSON.fromJson(releaseResponse.body(), GithubRelease.class);
        if (release == null || release.assets == null || release.assets.length == 0) {
            statusLine = "No downloadable release jar found yet.";
            return;
        }

        Optional<GithubAsset> chosenAsset = Arrays.stream(release.assets)
                .filter(asset -> asset != null && asset.browserDownloadUrl != null && asset.name != null)
                .filter(asset -> asset.name.endsWith(".jar"))
                .filter(asset -> !asset.name.contains("-sources"))
                .filter(asset -> asset.name.startsWith(EXPECTED_ASSET_PREFIX))
                .findFirst();
        if (chosenAsset.isEmpty()) {
            statusLine = "Release exists, but there is no jar asset to download.";
            return;
        }

        String currentVersion = FabricLoader.getInstance().getModContainer(EXPECTED_MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        String latestVersion = normalizeVersion(release.tagName);
        String normalizedCurrentVersion = normalizeVersion(currentVersion);
        GithubAsset asset = chosenAsset.get();

        Optional<Path> currentJar = resolveCurrentJarPath();
        Path destination;
        if (currentJar.isPresent()) {
            destination = pendingPathFor(currentJar.get());
        } else {
            Path updateDir = Path.of("config", "Fixify", "updates");
            Files.createDirectories(updateDir);
            destination = updateDir.resolve(asset.name);
        }

        if (latestVersion.equals(normalizedCurrentVersion)) {
            currentJar.map(AutoUpdater::pendingPathFor).ifPresent(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    LOGGER.debug("Failed to clear stale pending update {}", path, e);
                }
            });
            statusLine = "You're up to date on " + currentVersion + ".";
            return;
        }

        if (Files.exists(destination) && (asset.size <= 0 || Files.size(destination) == asset.size)) {
            statusLine = currentJar.isPresent()
                    ? "Update " + release.tagName + " is ready. Restart the game to apply it."
                    : "Update " + release.tagName + " is already downloaded to config/Fixify/updates.";
            return;
        }

        if (currentJar.isPresent()) {
            cleanupSiblingPending(currentJar.get(), destination);
        } else {
            cleanupFallbackDownloads(destination.getParent(), destination);
        }

        HttpRequest downloadRequest = HttpRequest.newBuilder(URI.create(asset.browserDownloadUrl))
                .header("User-Agent", "Fixify AutoUpdater")
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        HttpResponse<InputStream> downloadResponse = HTTP.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (downloadResponse.statusCode() != 200) {
            statusLine = "Download failed with HTTP " + downloadResponse.statusCode() + '.';
            return;
        }

        Path tempFile = destination.resolveSibling(destination.getFileName() + ".part");
        try (InputStream inputStream = downloadResponse.body()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        if (!verifyDownloadedJar(tempFile)) {
            Files.deleteIfExists(tempFile);
            statusLine = "Downloaded file failed verification.";
            return;
        }
        try {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        statusLine = currentJar.isPresent()
                ? "Downloaded " + release.tagName + ". Restart the game to install it automatically."
                : "Downloaded " + release.tagName + " to config/Fixify/updates.";
        LOGGER.info("Downloaded {} to {}", asset.name, destination.toAbsolutePath());
    }

    private static Optional<Path> resolveCurrentJarPath() {
        try {
            Path path = Path.of(FixifyClient.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
            if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar")) {
                return Optional.of(path);
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to resolve current jar path", e);
        }
        return Optional.empty();
    }

    private static Path pendingPathFor(Path currentJar) {
        return currentJar.resolveSibling(currentJar.getFileName().toString() + PENDING_UPDATE_SUFFIX);
    }

    private static void trySchedulePendingInstall() {
        Optional<Path> currentJar = resolveCurrentJarPath();
        if (currentJar.isEmpty()) {
            return;
        }
        Path stagedFile = pendingPathFor(currentJar.get());
        if (!Files.exists(stagedFile)) {
            return;
        }
        try {
            launchInstaller(currentJar.get(), stagedFile);
            statusLine = "Update is queued and will be installed as the game closes.";
        } catch (IOException e) {
            statusLine = "Update downloaded, but failed to schedule install.";
            LOGGER.error("Failed to launch installer", e);
        }
    }

    private static void launchInstaller(Path currentJar, Path stagedFile) throws IOException {
        String javaExe = Path.of(
                System.getProperty("java.home"),
                "bin",
                System.getProperty("os.name", "").toLowerCase().contains("win") ? "javaw.exe" : "java"
        ).toString();

        Path installerRoot = Files.createTempDirectory("fixify-updater-").toAbsolutePath();
        Path installerClass = installerRoot.resolve(INSTALLER_MAIN_CLASS.replace('.', '/') + ".class");
        Files.createDirectories(installerClass.getParent());
        try (InputStream in = AutoUpdater.class.getResourceAsStream(INSTALLER_CLASS_RESOURCE)) {
            if (in == null) {
                throw new IOException("Unable to locate " + INSTALLER_CLASS_RESOURCE + " in mod jar");
            }
            Files.copy(in, installerClass, StandardCopyOption.REPLACE_EXISTING);
        }

        new ProcessBuilder(
                javaExe,
                "-cp",
                installerRoot.toString(),
                INSTALLER_MAIN_CLASS,
                Long.toString(ProcessHandle.current().pid()),
                stagedFile.toAbsolutePath().toString(),
                currentJar.toAbsolutePath().toString(),
                installerRoot.toString()
        )
                .start();
    }

    private static boolean verifyDownloadedJar(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            var entry = jarFile.getEntry("fabric.mod.json");
            if (entry == null) {
                return false;
            }

            try (InputStream in = jarFile.getInputStream(entry)) {
                String json = new String(in.readAllBytes());
                return json.contains("\"id\": \"" + EXPECTED_MOD_ID + "\"")
                        || json.contains("\"id\":\"" + EXPECTED_MOD_ID + "\"");
            }
        } catch (Exception e) {
            LOGGER.warn("Downloaded jar failed verification", e);
            return false;
        }
    }

    private static void cleanupSiblingPending(Path currentJar, Path keepFile) {
        Path siblingDir = currentJar.getParent();
        String currentJarName = currentJar.getFileName().toString();
        try (var files = Files.list(siblingDir)) {
            files.filter(path -> !path.equals(keepFile))
                    .filter(path -> path.getFileName().toString().startsWith(currentJarName) && path.getFileName().toString().contains(PENDING_UPDATE_SUFFIX))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete old pending update {}", path, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to clean pending updates near {}", currentJar, e);
        }
    }

    private static void cleanupFallbackDownloads(Path updateDir, Path keepFile) {
        try (var files = Files.list(updateDir)) {
            files.filter(path -> !path.equals(keepFile))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete old update file {}", path, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to clean update directory {}", updateDir, e);
        }
    }

    private static String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }
        String normalized = version.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static final class GithubRelease {
        @SerializedName("tag_name")
        String tagName;
        GithubAsset[] assets;
    }

    private static final class GithubAsset {
        String name;
        long size;
        @SerializedName("browser_download_url")
        String browserDownloadUrl;
    }
}
