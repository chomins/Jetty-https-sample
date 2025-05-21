import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class HttpsClient {
    private HttpClient client;

    public void start() throws Exception {
        SslContextFactory sslContextFactory = new SslContextFactory();
        
        // Configure client certificate
        sslContextFactory.setKeyStoreResource(Resource.newClassPathResource("client"));
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");
        
        // Configure trust store for server certificate
        sslContextFactory.setTrustStoreResource(Resource.newClassPathResource("trust"));
        sslContextFactory.setTrustStorePassword("123456");

        client = new HttpClient(sslContextFactory);
        client.start();
    }

    public void sendGetRequest(String path) throws Exception {
        ContentResponse response = client.GET("https://localhost:8443" + path);
        System.out.println("GET " + path + " Response:");
        System.out.println(response.getContentAsString());
    }

    public void sendForm() throws Exception {
        Fields.Field name = new Fields.Field("Name", "Test");
        Fields.Field age = new Fields.Field("Age", "27");
        Fields fields = new Fields();
        fields.put(name);
        fields.put(age);

        ContentResponse res = client.FORM("https://localhost:8443", fields);
        System.out.println("POST Response:");
        System.out.println(res.getContentAsString());
    }

    public void stop() throws Exception {
        if (client != null) {
            client.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        HttpsClient client = new HttpsClient();
        client.start();
        
        // Test GET requests
        client.sendGetRequest("/");
        client.sendGetRequest("/hello");
        client.sendGetRequest("/info");
        client.sendGetRequest("/nonexistent");
        
        // // Test POST request
        // client.sendForm();
        
        client.stop();
    }
} 