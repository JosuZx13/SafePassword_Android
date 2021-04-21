package pdm.safepassword.init;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class SecreKeySafePassword {

    private static Cipher mCipher;
    private static KeyPair mPair;
    private final static String KEY_NAME_CYPHER = "Biometric_SafePassword_Key";

    public SecreKeySafePassword()
            throws GeneralSecurityException, IOException {
        mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_NAME())) {
            generateKeyPair();
        }

        // Even if we just generated the key, always read it back to ensure we
        // can read it successfully.
        final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                KEY_NAME(), null);
        mPair = new KeyPair(getPublicKey(), getPrivateKey());

    }

    private static void generateKeyPair()
            throws GeneralSecurityException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        KEY_NAME_CYPHER,
                        KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                        .build());
        mPair = keyPairGenerator.generateKeyPair();
        Signature signature = Signature.getInstance("SHA256withRSA/PSS");
        signature.initSign(mPair.getPrivate());
    }

    public String KEY_NAME(){return KEY_NAME_CYPHER;}

    protected PrivateKey getPrivateKey()
            throws KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            UnrecoverableKeyException {

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        return (PrivateKey) keyStore.getKey(KEY_NAME_CYPHER, null);
    }

    protected PublicKey getPublicKey()
            throws KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException,
            IOException {

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        return keyStore.getCertificate(KEY_NAME_CYPHER).getPublicKey();
    }

}
