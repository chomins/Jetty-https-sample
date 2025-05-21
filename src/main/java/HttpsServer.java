import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

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

        // Handle GET requests
        if (request.getMethod().equals("GET")) {
            switch (target) {
                case "/":
                    out.println("Welcome to HTTPS Server!");
                    break;
                case "/hello":
                    out.println("Hello, World!");
                    break;
                case "/info":
                    out.println("Server Information:");
                    out.println("Protocol: " + request.getProtocol());
                    out.println("Remote Address: " + request.getRemoteAddr());
                    out.println("Remote Host: " + request.getRemoteHost());
                    out.println("Remote Port: " + request.getRemotePort());
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("404 - Not Found");
                    break;
            }
            return;
        }

        // Handle POST requests (existing form handling)
        for (Enumeration<String> e = baseRequest.getParameterNames();
             e.hasMoreElements();) {
            String name = e.nextElement();
            out.format("server:  your %s -> %s%n", name, baseRequest.getParameter(name));
        }
    }
}

public class HttpsServer {
    private Server server;

    public void start() throws Exception {
        server = new Server();
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(8443);
        httpConfiguration.setOutputBufferSize(32768);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStoreResource(Resource.newClassPathResource("server"));
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");
        
        // Enable client certificate authentication
        // sslContextFactory.setNeedClientAuth(true);
        // sslContextFactory.setTrustStoreResource(Resource.newClassPathResource("trust"));
        // sslContextFactory.setTrustStorePassword("123456");

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

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        HttpsServer server = new HttpsServer();
        server.start();
        
        // Keep the server running
        server.server.join();
    }
} 