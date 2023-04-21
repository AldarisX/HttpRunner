package cn.misakanet.util;

import cn.misakanet.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class RequestUtil {
    private static RequestUtil requestUtil;
    private CloseableHttpClient httpClient;

    private RequestUtil() {
    }

    public static RequestUtil getInstance() {
        if (requestUtil == null) {
            requestUtil = new RequestUtil();
            requestUtil.init();
        }
        return requestUtil;
    }

    public void init() {
        httpClient = null;
        httpClient = create();
    }

    public CloseableHttpClient create() {
        try {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("https", sslSF)
                            .register("http", new PlainConnectionSocketFactory())
                            .build();

            var config = Config.getInstance();
            var proxyHost = config.get("http.proxy.host");
            var proxyPort = Integer.parseInt(config.get("http.proxy.port", "0"));
            var proxyType = config.get("http.proxy.type", "http");

            var defaultRequestConfig = RequestConfig.custom();
            if (StringUtils.isNotBlank(proxyHost)) {
                HttpHost proxy = new HttpHost(proxyHost, proxyPort, proxyType);
                defaultRequestConfig.setProxy(proxy);
            }

            BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
            return HttpClients.custom()
                    .setSSLSocketFactory(sslSF)
                    .setConnectionManager(connectionManager)
                    .setRedirectStrategy(new DefaultRedirectStrategy(new String[]{}))
                    .setDefaultRequestConfig(defaultRequestConfig.build())
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public CloseableHttpClient getClient() {
        return httpClient;
    }

    public <T> CloseableHttpResponse get(String url, List<NameValuePair> params) throws IOException, URISyntaxException {
        HttpGet httpGet = new HttpGet(url);
        if (params != null) {
            URI uri = new URIBuilder(httpGet.getURI())
                    .addParameters(params)
                    .build();
            httpGet.setURI(uri);
        }

        return httpClient.execute(httpGet);
    }

    public <T> CloseableHttpResponse post(String url, String data) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(data, StandardCharsets.UTF_8);
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        return httpClient.execute(httpPost);
    }

    public void close() throws IOException {
        httpClient.close();
        httpClient = create();
    }
}
