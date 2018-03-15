/*
package com.example.asusa455la.zxingembedded.view;

import android.os.Bundle;

*
 * Created by ASUS A455LA on 05/12/2017.



public class Archive {
    protected void onCreate(Bundle savedInstanceState) {
        try {
            inputTrustedCertificate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PublicKey publicKey = null;
        PrivateKey privateKey = null;
        String encoded = null;
        String decoded = null;
        try {
            publicKey = this.getPublicKey();
            privateKey = this.getPrivateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cipher cipher = null; //or try with "RSA"
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal("JULIUS CAESAR".getBytes());
            encoded = Base64.encodeToString(encrypted, Base64.DEFAULT);

            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.decode(encoded, Base64.DEFAULT));
            decoded = new String(decrypted);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Public Key RSA");
        String message = "Plaintext: JULIUS CAESAR \nCiphertext: " + encoded + "\nDecrypted text: " + decoded;
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                endActivity();
            }
        });
        builder.setCancelable(true);
        AlertDialog alert1 = builder.create();
        alert1.show();



    }



    public void inputTrustedCertificate() throws Exception{
        // Load CAs from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(R.raw.cacert);
        X509Certificate ca = (X509Certificate) cf.generateCertificate(caInput);
        System.out.println("ca=" + ca.getSubjectDN());
        caInput.close();

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in out KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
        trustManagerFactory.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagerFactory.getTrustManagers(), null);

        // Tell the URLConnection to use a SocketFactory from our SSLContext

*URL url = new URL("localhost");
     HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
     urlConnection.setSSLSocketFactory(context.getSocketFactory());
     InputStream in = urlConnection.getInputStream();
     copyInputStreamToOutputStream(in, System.out);

    }

    private void copyInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk= new byte[1024];
        int nbyte;
        while(  (nbyte=in.read(chunk)) >-1) {
            out.write(chunk,0,nbyte);
        };
        in.close();
    }

    public PrivateKey getPrivateKey() throws Exception {
        InputStream keyInputStream = getResources().openRawResource(R.raw.myprivatekey);
        DataInputStream dis = new DataInputStream(keyInputStream);
        byte[] keyBytes = new byte[keyInputStream.available()];
        dis.readFully(keyBytes);
        keyInputStream.close();
        dis.close();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public PublicKey getPublicKey() throws Exception{
        InputStream certificateStream = getResources().openRawResource(R.raw.mycert);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certificateStream);
        PublicKey publicKey = certificate.getPublicKey();
        certificateStream.close();
        return publicKey;
    }

}
*/
