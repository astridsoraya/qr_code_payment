package com.example.asusa455la.zxingembedded.model;

/**
 * Created by ASUS A455LA on 03/11/2017.*/




import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.*;
import java.security.*;

import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;


public class DigitalCertificate{
    private KeyStore keyStore;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public DigitalCertificate(){

    }

    public void generateKeys() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration<String> aliases = ks.aliases();

/*            if (!(ks instanceof KeyStore.PrivateKeyEntry)) {
                Log.w(TAG, "Not an instance of a PrivateKeyEntry");
                return;
            }*/
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        ks = null;
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(2048, secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();

            ks = KeyStore.getInstance("AndroidKeyStore");
            //ks.setKeyEntry();
            //ks.setKeyEntry("client_key", this.privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }



    }


}

