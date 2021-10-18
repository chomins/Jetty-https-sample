import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;


public class HttpsClientSendFormReqAuth {
    private Server server;
    private HttpClient client;

    private void startServer() throws Exception {
        server = new Server();
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(8443);
        httpConfiguration.setOutputBufferSize(32768);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStoreResource(Resource.newClassPathResource("server"));
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");
        sslContextFactory.setTrustStoreResource(Resource.newClassPathResource("trust"));
        sslContextFactory.setNeedClientAuth(true);

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

    }

    private void startClient() throws Exception {

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustStoreResource(Resource.newClassPathResource("trust"));
        sslContextFactory.setKeyStoreResource(Resource.newClassPathResource("server"));
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");

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

    private void stopClientServer() throws Exception {
        client.stop();
        server.stop();
    }

    public static void main(String[] args) throws Exception {

        HttpsClientSendFormReqAuth smp = new HttpsClientSendFormReqAuth();
        smp.startServer();
        smp.startClient();
        smp.stopClientServer();
    }
}
