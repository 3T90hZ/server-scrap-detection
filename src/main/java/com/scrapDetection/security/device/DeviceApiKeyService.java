package com.scrapDetection.security.device;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class DeviceApiKeyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int KEY_BYTES      = 32;  // 256 bits of entropy
    private static final int PREFIX_LENGTH  = 8;

    /*
      Generate a new raw device key. This is shown to the caller exactly
      once (at creation/rotation time) and is never persisted anywhere —
      only its hash is stored via hash().
     */
    public String generateRawKey() {
        byte[] buf = new byte[KEY_BYTES];
        SECURE_RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /*
      Short, non-sensitive prefix of the raw key, safe to store/display
      for identification purposes.
     */
    public String extractPrefix(String rawKey) {
        return rawKey.substring(0, Math.min(PREFIX_LENGTH, rawKey.length()));
    }

    /* SHA-256 hash of the raw key, hex-encoded, for storage. */
    public String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every JVM — this is unreachable.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /*
      Constant-time comparison of a raw key against a stored hash, so a
      malicious client can't use response-timing differences to guess the
      key byte-by-byte.
     */
    public boolean matches(String rawKey, String storedHash) {
        if (rawKey == null || storedHash == null) {
            return false;
        }
        String computed = hash(rawKey);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
