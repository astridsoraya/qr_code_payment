package com.example.asusa455la.zxingembedded.utility;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.example.asusa455la.zxingembedded.R;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

/**
 * Created by ASUS A455LA on 11/02/2018.
 */

public class Cryptography{

    public static String getDigitalSignature(String text, PrivateKey privateKey)  {
        String digitalSignature = "";
        try {
            // text to bytes
            byte[] data = text.getBytes("UTF-8");

            // signature
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(data);
            byte[] signatureBytes = sig.sign();

            digitalSignature = Base64.encodeToString(signatureBytes, Base64.DEFAULT);

        }catch(Exception e){
            e.printStackTrace();
        }
        return digitalSignature;
    }

    public static boolean verifyDigitalSignature(String message, String signature, PublicKey publicKey){
        Signature sign;
        boolean verification = false;
        try {
            sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(publicKey);
            sign.update(message.getBytes("UTF-8"));
            byte[] signBytes = Base64.decode(signature.getBytes("UTF-8"), Base64.DEFAULT);
            verification = sign.verify(signBytes);
        } catch (NoSuchAlgorithmException | SignatureException | UnsupportedEncodingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return verification;
    }

    public static String encrypt(String plaintext, SecretKey secretKey)
    {
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
            encryptedText = new String(Base64.encode(encryptedBytes, Base64.DEFAULT));

            System.out.println("GO GO: " + encryptedText + " " + Cryptography.decrypt(encryptedText, secretKey));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return encryptedText;
    }

    public static String decrypt(String ciphertext, SecretKey secretKey){
        String decryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = Base64.decode(ciphertext.getBytes("UTF-8"), Base64.DEFAULT);
            decryptedText = new String (cipher.doFinal(decryptedBytes), "UTF-8");
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return decryptedText;
    }

    public static String wrapKey(SecretKey secretKey, PublicKey publicKey)
    {
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.WRAP_MODE, publicKey);
            byte[] wrappedKey = cipher.wrap(secretKey);
            encryptedText = new String(Base64.encode(wrappedKey, Base64.DEFAULT));
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return encryptedText;
    }

    public static SecretKey unwrapKey(String wrappedKey, PrivateKey privateKey){
        SecretKey secretKey = null;

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.UNWRAP_MODE, privateKey);
            byte[] decryptedBytes = Base64.decode(wrappedKey.getBytes("UTF-8"), Base64.DEFAULT);
            secretKey = (SecretKey) cipher.unwrap(decryptedBytes,"AES/ECB/PKCS5Padding", Cipher.SECRET_KEY);
        } catch (InvalidKeyException | NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return secretKey;
    }

    private static KeyPair generateKeyPair(Context context, String commonName){
        PrivateKey privateKey;
        KeyPair keyPair = null;
        KeyPairGenerator keyPairGenerator;

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(
                        commonName,
                        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setKeySize(2048)
                        .build());
            }
            else{
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.MONTH, 12);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context).setKeySize(2048)
                        .setAlias(commonName)
                        .setSubject(new X500Principal("CN="+commonName))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                keyPairGenerator.initialize(spec);
            }

            keyPair = keyPairGenerator.generateKeyPair();

            privateKey = keyPair.getPrivate();

            Certificate[] chain = new Certificate[2];

            InputStream inputStream = context.getResources().openRawResource(R.raw.ca_cert);
            chain[0] = loadCertificate(inputStream);
            inputStream = context.getResources().openRawResource(R.raw.intermediate_cert);
            chain[1] = loadCertificate(inputStream);

            ks.load(null);
            ks.setKeyEntry(commonName, privateKey, null, chain);


        } catch (NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        return keyPair;
    }

    public static SecretKey generateSecretKey(){
        SecretKey secretKey = null;
        try {
            KeyGenerator keyGenerator = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            else
                keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            secretKey = keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }

        return secretKey;
    }

    public static void createCertificateRequestFile(Context context, String commonName) {
        KeyPair keyPair = generateKeyPair(context, commonName);

        try{
            PKCS10CertificationRequest csr = generateCSR(keyPair, commonName);
            byte CSRder[] = csr.getEncoded();

            StringWriter writer = new StringWriter();
            PemWriter pemWriter = new PemWriter(writer);
            pemWriter.writeObject(new PemObject("CERTIFICATE REQUEST", CSRder));
            pemWriter.flush();
            pemWriter.close();
            String csrPEM = writer.toString();

            String path =
                    Environment.getExternalStorageDirectory() + File.separator  + "CSR Folder";
            // Create the folder.
            File csrFolder = new File(path);

            if(!csrFolder.exists()){
                boolean created = csrFolder.mkdirs();
            }

            // Create the file.
            File csrFile = new File(csrFolder, commonName+".csr");
            boolean created = csrFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(csrFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(csrPEM);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }

    }

    private static PKCS10CertificationRequest generateCSR(KeyPair keyPair, String cn) throws IOException,
            OperatorCreationException {
        String principal = String.format("CN=%s", cn);

        ContentSigner signer = new JCESigner(keyPair.getPrivate(),"SHA256withRSA");

        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
                new X500Name(principal), keyPair.getPublic());
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        extensionsGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(
                false));
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                extensionsGenerator.generate());
        PKCS10CertificationRequest csr = csrBuilder.build(signer);

        return csr;
    }

    public static X509Certificate loadCertificate(File certFile){
        CertificateFactory certFactory;
        FileInputStream fileInputStream;
        X509Certificate cert = null;
        try {
            certFactory = CertificateFactory
                    .getInstance("X.509");
            fileInputStream = new FileInputStream(certFile);
            cert = (X509Certificate) certFactory.generateCertificate(fileInputStream);
            fileInputStream.close();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("SERVER CERTIFICATE","Unable to load certificate " + e.getMessage());
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Log.d("SERVER CERTIFICATE","Server certificate file missing " + e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cert;
    }

    public static X509Certificate loadCertificate(InputStream inputStream){
        CertificateFactory certFactory;
        X509Certificate cert = null;
        try {
            certFactory = CertificateFactory
                    .getInstance("X.509");
            cert = (X509Certificate) certFactory.generateCertificate(inputStream);
            inputStream.close();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("SERVER CERTIFICATE","Unable to load certificate " + e.getMessage());
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Log.d("SERVER CERTIFICATE","Server certificate file missing " + e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cert;
    }
}
