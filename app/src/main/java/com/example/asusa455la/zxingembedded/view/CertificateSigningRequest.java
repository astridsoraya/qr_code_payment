/*
package com.example.asusa455la.zxingembedded.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;

*/
/*import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ocsp.Signature;
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.ExtensionsGenerator;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;*//*



import com.example.asusa455la.zxingembedded.R;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.regex.Pattern;

*/
/*
*
 * Created by ASUS A455LA on 04/12/2017.
*//*







public class CertificateSigningRequest extends AppCompatActivity{
*/
/*    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }*//*

    private final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";
    private final static String CN_PATTERN = "CN=%s";
    private PrivateKey privateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void createCertificateRequest(String commonName) throws NoSuchAlgorithmException, IOException, OperatorCreationException {
        KeyStore ks = null;
        KeyPair keyPair = null;
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = null;
        byte[] privateKeyBytes = null;

        try {
            keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
            keyPairGenerator.initialize(2048, secureRandom);
            keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();

            ks = KeyStore.getInstance("AndroidKeyStore");
            privateKeyBytes = this.privateKey.getEncoded();

            ks.setKeyEntry("client_key", privateKeyBytes, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);
        pemWriter.writeObject(new PemObject("CERTIFICATE REQUEST", privateKeyBytes));

        PKCS10CertificationRequest csr = generateCSR(keyPair, commonName);
        byte CSRder[] = csr.getEncoded();

        writer = new StringWriter();
        pemWriter = new PemWriter(writer);
        pemWriter.writeObject(new PemObject("CERTIFICATE REQUEST", CSRder));
        pemWriter.flush();
        pemWriter.close();
        String csrPEM = writer.toString();

        String path =
                Environment.getExternalStorageDirectory() + File.separator  + "CSR Folder";
        // Create the folder.
        File folder = new File(path);
        folder.mkdirs();

        // Create the file.
        File file = new File(folder, "client.csr");

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(csrPEM);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }

    private X509Certificate loadCertificate(int caCode){
        CertificateFactory certFactory;
        InputStream inStream;
        X509Certificate cert = null;
        try {
            certFactory = CertificateFactory
                    .getInstance("X.509");
            inStream = getResources().openRawResource(caCode);
            cert = (X509Certificate) certFactory.generateCertificate(inStream);
            inStream.close();
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

    private PKCS10CertificationRequest generateCSR(KeyPair keyPair, String cn) throws IOException,
            OperatorCreationException {
        String principal = String.format(CN_PATTERN, cn);

        ContentSigner signer = new JCESigner(keyPair.getPrivate(),DEFAULT_SIGNATURE_ALGORITHM);

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

}

*/
