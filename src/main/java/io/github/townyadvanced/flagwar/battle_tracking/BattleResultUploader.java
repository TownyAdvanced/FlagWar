package io.github.townyadvanced.flagwar.battle_tracking;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.database.TrackerDatabase.BattleResultPackage;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;

/** Uploads completed battle packages to the configured Battria website without blocking the server thread. */
public final class BattleResultUploader {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /** Uploads the summary, archive, and publish request in the website's required order. */
    public CompletableFuture<Void> upload(BattleResultPackage battlePackage) {
        if (!FlagWarConfig.isBattleResultUploadEnabled()) return CompletableFuture.completedFuture(null);

        String baseUrl = FlagWarConfig.getBattleResultWebsiteUrl().replaceAll("/+$", "");
        String key = FlagWarConfig.getBattleResultHmacKey();
        if (baseUrl.isEmpty() || key.isEmpty()) {
            FlagWar.getInstance().getLogger().warning("Battle result upload is enabled without a website URL or HMAC key.");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                String basePath = "/api/internal/battles/" + battlePackage.battleId();
                send(baseUrl, basePath + "/summary", "PUT", battlePackage.summaryJson().getBytes(StandardCharsets.UTF_8), "application/json", key);
                sendArchive(baseUrl, basePath + "/damage", battlePackage.archivePath(), key);
                send(baseUrl, basePath + "/complete", "POST", new byte[0], "application/json", key);
            } catch (IOException | InterruptedException | GeneralSecurityException e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                throw new IllegalStateException("Failed to upload battle result " + battlePackage.battleId(), e);
            }
        }).exceptionally(ex -> {
            FlagWar.getInstance().getLogger().warning(ex.getMessage());
            return null;
        });
    }

    private static void send(String baseUrl, String path, String method, byte[] body, String contentType, String key)
        throws IOException, InterruptedException, GeneralSecurityException {
        long timestamp = System.currentTimeMillis();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        String signature = sign(timestamp + "\n" + method + "\n" + path + "\n" + hash, key);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
            .header("Content-Type", contentType)
            .header("X-Battria-Timestamp", Long.toString(timestamp))
            .header("X-Battria-Signature", signature)
            .method(method, HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
        HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() / 100 != 2)
            throw new IOException(method + " " + path + " returned HTTP " + response.statusCode());
    }

    private static void sendArchive(String baseUrl, String path, java.nio.file.Path archive, String key)
        throws IOException, InterruptedException, GeneralSecurityException {
        long timestamp = System.currentTimeMillis();
        String hash = sha256(archive);
        String signature = sign(timestamp + "\nPUT\n" + path + "\n" + hash, key);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
            .header("Content-Type", "application/gzip")
            .header("X-Battria-Timestamp", Long.toString(timestamp))
            .header("X-Battria-Signature", signature)
            .PUT(HttpRequest.BodyPublishers.ofFile(archive))
            .build();
        HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() / 100 != 2)
            throw new IOException("PUT " + path + " returned HTTP " + response.statusCode());
    }

    private static String sign(String payload, String key) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private static String sha256(java.nio.file.Path file) throws IOException, GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new DigestInputStream(Files.newInputStream(file), digest)) {
            input.transferTo(java.io.OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
