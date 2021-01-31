package com.delivery.provider;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;
import com.facebook.stetho.urlconnection.ByteArrayRequestEntity;
import com.facebook.stetho.urlconnection.SimpleRequestEntity;
import com.facebook.stetho.urlconnection.StethoURLConnectionManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by darshan.kapasi on 29-10-2018.
 */
public class OkHttpStack extends BaseHttpStack {
    private static final int HTTP_CONTINUE = 100;

    /**
     * An interface for transforming URLs before use.
     */
    public interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        String rewriteUrl(String originalUrl);
    }

    private final com.android.volley.toolbox.HurlStack.UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;

    public OkHttpStack() {
        this(null);
    }

    /**
     * @param urlRewriter Rewriter to use for request URLs
     */
    public OkHttpStack(com.android.volley.toolbox.HurlStack.UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    /**
     * @param urlRewriter      Rewriter to use for request URLs
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     */
    public OkHttpStack(com.android.volley.toolbox.HurlStack.UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        String friendlyName = "TenoRequest:" + System.currentTimeMillis();
        StethoURLConnectionManager connectionManager = new StethoURLConnectionManager(friendlyName);
        try {
            String url = request.getUrl();
            HashMap<String, String> map = new HashMap<>();
            map.putAll(request.getHeaders());
            map.putAll(additionalHeaders);
            if (mUrlRewriter != null) {
                String rewritten = mUrlRewriter.rewriteUrl(url);
                if (rewritten == null) {
                    throw new IOException("URL blocked by rewriter: " + url);
                }
                url = rewritten;
            }
            URL parsedUrl = new URL(url);
            HttpURLConnection connection = openConnection(parsedUrl, request);
            for (String headerName : map.keySet()) {
                connection.addRequestProperty(headerName, map.get(headerName));
            }
            //pre action
            setConnectionParametersForRequest(connectionManager, connection, request);
            // Initialize HttpResponse with data from the HttpURLConnection.
            int responseCode = connection.getResponseCode();
            //post action
            if (responseCode == -1) {
                // -1 is returned by getResponseCode() if the response code could not be retrieved.
                // Signal to the caller that something was wrong with the connection.
                throw new IOException("Could not retrieve response code from HttpUrlConnection.");
            }
            postConnect(connectionManager);
            if (!hasResponseBody(request.getMethod(), responseCode)) {
                return new HttpResponse(responseCode, convertHeaders(connection.getHeaderFields()));
            }
            InputStream responseStream = inputStreamFromConnection(connection);
            //intercept response stream
            InputStream interpretResponseStream = connectionManager.interpretResponseStream(responseStream);
            return new HttpResponse(responseCode, convertHeaders(connection.getHeaderFields()),
                    connection.getContentLength(), interpretResponseStream);
        } catch (IOException e) {
            connectionManager.httpExchangeFailed(e);
            throw e;
        }
    }

    // VisibleForTesting
    static List<Header> convertHeaders(Map<String, List<String>> responseHeaders) {
        List<Header> headerList = new ArrayList<>(responseHeaders.size());
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            // HttpUrlConnection includes the status line as a header with a null key; omit it here
            // since it's not really a header and the rest of Volley assumes non-null keys.
            if (entry.getKey() != null) {
                for (String value : entry.getValue()) {
                    headerList.add(new Header(entry.getKey(), value));
                }
            }
        }
        return headerList;
    }

    /**
     * Checks if a response message contains a body.
     *
     * @param requestMethod request method
     * @param responseCode  response status code
     * @return whether the response has a body
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3">RFC 7230 section 3.3</a>
     */
    private static boolean hasResponseBody(int requestMethod, int responseCode) {
        return requestMethod != Method.HEAD
                && !(HTTP_CONTINUE <= responseCode && responseCode < HttpURLConnection.HTTP_OK)
                && responseCode != HttpURLConnection.HTTP_NO_CONTENT
                && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED;
    }

    /**
     * Initializes an {@link InputStream} from the given {@link HttpURLConnection}.
     *
     * @param connection
     * @return an HttpEntity populated with data from <code>connection</code>.
     */
    private static InputStream inputStreamFromConnection(HttpURLConnection connection) {
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        return inputStream;
    }

    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     */
    protected HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Workaround for the M release HttpURLConnection not observing the
        // HttpURLConnection.setFollowRedirects() property.
        // https://code.google.com/p/android/issues/detail?id=194495
        connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
        return connection;
    }

    /**
     * Opens an {@link HttpURLConnection} with parameters.
     *
     * @param url
     * @return an open connection
     * @throws IOException
     */
    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        HttpURLConnection connection = createConnection(url);
        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
        }
        return connection;
    }

    @SuppressWarnings("deprecation")
    /* package */ static void setConnectionParametersForRequest(
            StethoURLConnectionManager connectionManager, HttpURLConnection connection,
            Request<?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Method.DEPRECATED_GET_OR_POST:
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    connection.setRequestMethod("POST");
                    addBody(connectionManager, connection, request, postBody);
                }
                break;
            case Method.GET:
                // Not necessary to set the request method because connection defaults to GET but
                // being explicit here.
                connection.setRequestMethod("GET");
                preConnect(connectionManager, connection, null);
                break;
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                preConnect(connectionManager, connection, null);
                break;
            case Method.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connectionManager, connection, request);
                break;
            case Method.PUT:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connectionManager, connection, request);
                break;
            case Method.HEAD:
                connection.setRequestMethod("HEAD");
                preConnect(connectionManager, connection, null);
                break;
            case Method.OPTIONS:
                connection.setRequestMethod("OPTIONS");
                preConnect(connectionManager, connection, null);
                break;
            case Method.TRACE:
                connection.setRequestMethod("TRACE");
                preConnect(connectionManager, connection, null);
                break;
            case Method.PATCH:
                connection.setRequestMethod("PATCH");
                addBodyIfExists(connectionManager, connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(StethoURLConnectionManager connectionManager, HttpURLConnection connection, Request<?> request)
            throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            addBody(connectionManager, connection, request, body);
        }
    }

    private static void addBody(StethoURLConnectionManager connectionManager, HttpURLConnection connection, Request<?> request, byte[] body)
            throws IOException, AuthFailureError {
        // Prepare output. There is no need to set Content-Length explicitly,
        // since this is handled by HttpURLConnection using the size of the prepared
        // output stream.
        connection.setDoOutput(true);
		/*connection.addRequestProperty(
				HttpHeaderParser.HEADER_CONTENT_TYPE, request.getBodyContentType());*/
        connection.addRequestProperty("Content-Type", request.getBodyContentType());
        preConnect(connectionManager, connection, new ByteArrayRequestEntity(body));
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(body);
        out.close();
    }

    private static void preConnect(StethoURLConnectionManager manager, HttpURLConnection connection, SimpleRequestEntity requestEntity) {
        manager.preConnect(connection, requestEntity);
    }

    private void postConnect(StethoURLConnectionManager manager) {
        try {
            manager.postConnect();
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }
}