import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

public class MakeKeyStoreFromDERTest {

    @Before
    public void serverStart() throws Exception {
        Server server = new Server();

        byte[] privateKeyContent = Files.readAllBytes(Paths.get("key.der"));
        byte[] certContent = Files.readAllBytes(Paths.get("cert.der"));

        KeyStore keyStore  =MakeKeystoreUtil.createKeystore(privateKeyContent,certContent,"123456");
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
        System.out.println("serverStart");

    }

    @Test
    public void test() throws Exception {
        HttpClient client;
        SslContextFactory sslContextFactory = new SslContextFactory();

        sslContextFactory.setTrustStoreResource(Resource.newClassPathResource("trust"));

        client = new HttpClient(sslContextFactory);
        client.start();

        Fields.Field name = new Fields.Field("Name", "Test");
        Fields.Field age = new Fields.Field("Age", "27");
        Fields fields = new Fields();
        fields.put(name);
        fields.put(age);

        ContentResponse res = client.FORM("https://localhost:8443", fields);

        System.out.println(res.getContentAsString());
    }

}
