import sun.security.validator.TrustStoreUtil;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;


public class MakeKeystoreUtil {


    public static KeyStore createKeystore(File privateKey, File certificate, final String password)throws Exception{
        final X509Certificate[] cert = createCertificates(certificate);
        final KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null);
        // Import private key
        final PrivateKey key = createPrivateKey(privateKey);
        keystore.setKeyEntry(privateKey.getName(), key, password.toCharArray(), cert);
        return keystore;
    }

    public static KeyStore createKeystore(byte[] privateKey, byte[] cert, final String password)throws Exception{
        final X509Certificate certs = generateCertificateFromDER(cert);
        final PrivateKey key = generatePrivateKeyFromDER(privateKey);

        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null);
        keyStore.setKeyEntry("privatekey",key,password.toCharArray(), new X509Certificate[]{certs});
        return keyStore;
    }

    private static PrivateKey createPrivateKey(File privateKey) throws Exception{
        final BufferedReader r = new BufferedReader(new FileReader(privateKey));
        String s = r.readLine();
        if (s == null || !s.contains("BEGIN PRIVATE KEY")){
            r.close();
            throw new IllegalArgumentException("No PRIVATE KEY");
        }
        final StringBuffer b = new StringBuffer();
        s= "";
        while (s != null){
            if(s.contains("END PRIVATE KEY")){
                break;
            }
            b.append(s);
            s = r.readLine();
        }
        r.close();
        final String hexString = b.toString();
        final byte[] bytes = DatatypeConverter.parseBase64Binary(hexString);

        return generatePrivateKeyFromDER(bytes);
    }

    private static X509Certificate[] createCertificates(File certificate) throws Exception{
        final List<X509Certificate> result = new ArrayList<X509Certificate>();
        final BufferedReader r = new BufferedReader(new FileReader(certificate));
        String s = r.readLine();
        if (s == null || !s.contains("BEGIN CERTIFICATE")) {
            r.close();
            throw new IllegalArgumentException("No CERTIFICATE found");
        }
        StringBuffer b = new StringBuffer();
        while (s != null) {
            if (s.contains("END CERTIFICATE")) {
                String hexString = b.toString();
                final byte[] bytes = DatatypeConverter.parseBase64Binary(hexString);
                X509Certificate cert = generateCertificateFromDER(bytes);
                result.add(cert);
                b = new StringBuffer();
            } else {
                if (!s.startsWith("----")) {
                    b.append(s);
                }
            }
            s = r.readLine();
        }
        r.close();

        return result.toArray(new X509Certificate[result.size()]);
    }

    public static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        final KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        final CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }





}
