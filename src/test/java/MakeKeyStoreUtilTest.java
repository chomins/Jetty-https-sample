import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

public class MakeKeyStoreUtilTest {

    @Test
    public void test() throws Exception {

        Server server = new Server();
        File privateKey = new File("key.pem");
        File cert = new File("cert.pem");
        KeyStore keyStore = MakeKeystoreUtil.createKeystore(privateKey,cert,"123456");
        server = new Server();
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(8443);
        httpConfiguration.setOutputBufferSize(32768);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");

        HttpConfiguration https_config = new HttpConfiguration(httpConfiguration);
        https_config.addCustomizer(new SecureRequestCustomizer());
        ServerConnector https =
                new ServerConnector(
                        server,
                        new SslConnectionFactory(sslContextFactory,"http/1.1"),
                        new HttpConnectionFactory(https_config)
                );
        https.setPort(8443);
        https.setIdleTimeout(500000);

        server.setConnectors(new Connector[] { https });
        MyHandler myHandler = new MyHandler();
        server.setHandler(myHandler);
        server.start();
        server.join();

    }


}
