package com.example.asusa455la.zxingembedded.utility;

/*import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.operator.ContentSigner;*/


import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ASUS A455LA on 04/12/2017.*/



//implements ContentSigner
public class JCESigner implements ContentSigner {
    private static Map<String, AlgorithmIdentifier> ALGOS = new HashMap<String, AlgorithmIdentifier>();

    static {
        ALGOS.put("SHA256withRSA".toLowerCase(), new AlgorithmIdentifier(
                new ASN1ObjectIdentifier("1.2.840.113549.1.1.11")));

    }

    private String mAlgo;
    private Signature signature;
    private ByteArrayOutputStream outputStream;

    public JCESigner(PrivateKey privateKey, String sigAlgo) {
        //Utils.throwIfNull(privateKey, sigAlgo);
        mAlgo = sigAlgo.toLowerCase();
        try {
            this.outputStream = new ByteArrayOutputStream();
            this.signature = Signature.getInstance(sigAlgo);
            this.signature.initSign(privateKey);
        } catch (GeneralSecurityException gse) {
            throw new IllegalArgumentException(gse.getMessage());
        }
    }

    @Override
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        AlgorithmIdentifier id = ALGOS.get(mAlgo);
        if (id == null) {
            throw new IllegalArgumentException("Does not support algo: " +
                    mAlgo);
        }
        return id;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public byte[] getSignature() {
        try {
            signature.update(outputStream.toByteArray());
            return signature.sign();
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            return null;
        }
    }

}
