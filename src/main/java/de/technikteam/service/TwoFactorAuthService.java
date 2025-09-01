package de.technikteam.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import de.taimos.totp.TOTP;
import de.technikteam.api.v1.dto.TwoFactorSetupDTO;
import de.technikteam.dao.TwoFactorAuthDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.model.UserBackupCode;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class TwoFactorAuthService {

    private static final Logger logger = LogManager.getLogger(TwoFactorAuthService.class);
    private final TwoFactorAuthDAO twoFactorAuthDAO;
    private final UserDAO userDAO;
    private final AdminLogService adminLogService;
    private final SecretKeySpec encryptionKey;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public TwoFactorAuthService(TwoFactorAuthDAO twoFactorAuthDAO, UserDAO userDAO, ConfigurationService configService, AdminLogService adminLogService) {
        this.twoFactorAuthDAO = twoFactorAuthDAO;
        this.userDAO = userDAO;
        this.adminLogService = adminLogService;
        // NOTE: This uses the JWT secret for encryption. In a real-world scenario,
        // this should be a separate, dedicated encryption key.
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = configService.getProperty("jwt.secret");
        }
        // Use first 16 bytes for AES-128
        byte[] keyBytes = new byte[16];
        System.arraycopy(secret.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, Math.min(secret.length(), 16));
        this.encryptionKey = new SecretKeySpec(keyBytes, "AES");
    }

    public TwoFactorSetupDTO generateNewSecretAndQrCode(User user) throws Exception {
        String secret = generateSecretKey();
        String accountName = user.getEmail() != null && !user.getEmail().isBlank() ? user.getEmail() : user.getUsername();
        String qrCodeUri = getGoogleAuthenticatorBarCode(secret, accountName, "TechnikTeam");
        String qrCodeDataUri = createQRCode(qrCodeUri);
        return new TwoFactorSetupDTO(secret, qrCodeDataUri);
    }

    @Transactional
    public List<String> enableTotpForUser(int userId, String secret, String token) {
        if (!verifyCode(secret, token)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }
        String encryptedSecret = encrypt(secret);
        twoFactorAuthDAO.enableTotpForUser(userId, encryptedSecret);
        User user = userDAO.getUserById(userId);
        adminLogService.log(user.getUsername(), "2FA_ENABLED", "User enabled Two-Factor Authentication.");
        return generateAndStoreBackupCodes(userId);
    }

    @Transactional
    public void disableTotpForUser(int userId, String token) {
        User user = userDAO.getUserById(userId);
        if (user == null || !user.isTotpEnabled()) {
            throw new IllegalStateException("2FA is not enabled for this user.");
        }
        String decryptedSecret = decrypt(user.getTotpSecret());
        if (!verifyCode(decryptedSecret, token)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }
        twoFactorAuthDAO.disableTotpForUser(userId);
        adminLogService.log(user.getUsername(), "2FA_DISABLED", "User disabled Two-Factor Authentication.");
    }


    public boolean verifyCode(String secret, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }
        // The TOTP library requires a hex-encoded key, but Google Authenticator uses Base32.
        // We must decode the Base32 secret first, then hex-encode the resulting bytes.
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secret);
        String hexKey = Hex.encodeHexString(bytes);
        
        // TOTP.getOTP() generates the current code.
        String totp = TOTP.getOTP(hexKey);
        
        // Log for debugging purposes
        logger.debug("Verifying TOTP. Provided: {}, Generated: {}", code, totp);

        return totp.equals(code);
    }

    public boolean verifyBackupCode(int userId, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        List<UserBackupCode> unusedCodes = twoFactorAuthDAO.getUnusedBackupCodesForUser(userId);
        for (UserBackupCode backupCode : unusedCodes) {
            if (passwordEncoder.matches(code, backupCode.getCodeHash())) {
                twoFactorAuthDAO.markBackupCodeAsUsed(backupCode.getId());
                return true;
            }
        }
        return false;
    }

    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    private String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, StandardCharsets.UTF_8).replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8).replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createQRCode(String barCodeData) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(barCodeData, BarcodeFormat.QR_CODE, 250, 250);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
    }

    private List<String> generateAndStoreBackupCodes(int userId) {
        List<String> plainTextCodes = new ArrayList<>();
        List<String> hashedCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            byte[] bytes = new byte[6]; // 8 characters
            random.nextBytes(bytes);
            String code = new Base32().encodeToString(bytes).substring(0, 8);
            plainTextCodes.add(code);
            hashedCodes.add(passwordEncoder.encode(code));
        }
        twoFactorAuthDAO.storeBackupCodeHashes(userId, hashedCodes);
        return plainTextCodes;
    }

    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedValue)), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}