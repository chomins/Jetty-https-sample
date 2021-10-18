import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

class MyHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/plain;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        PrintWriter out = response.getWriter();

        for (Enumeration<String> e = baseRequest.getParameterNames();
             e.hasMoreElements();) {
            String name = e.nextElement();
            out.format("server:  your %s -> %s%n", name, baseRequest.getParameter(name));
        }
    }
}

public class HttpsClientSendForm {
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

        HttpsClientSendForm smp = new HttpsClientSendForm();
        smp.startServer();
        smp.startClient();
        smp.stopClientServer();
    }
}
