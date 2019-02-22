package me.darksidecode.kantanj.networking;

import me.darksidecode.kantanj.types.Check;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class Networking {

    private Networking() {}

    /**
     * By default, any user agents in Java are silently updated with a String
     * of format "Java/1.8.0". This is often not desired.
     */
    public static void removeJavaUserAgentSuffixGlobally() {
        System.setProperty("http.agent", "");
    }

    public static String resolveIPv4(String host) {
        try {
            Check.notNull(host, "host cannot be null");

            for (InetAddress inetAddress : InetAddress.getAllByName(host))
                if (inetAddress instanceof Inet4Address)
                    return inetAddress.getHostAddress();

            throw new UnknownHostException("unknown host " + host);
        } catch (Exception ex) {
            throw new RuntimeException("failed to resolve IPv4 of " + host, ex);
        }
    }

    public static final class Http {
        private Http() {}

        public static String get(GetHttpRequest request) {
            try {
                HttpURLConnection con = openConnection(request);
                StringBuilder response = new StringBuilder();

                InputStream inputStream = con.getInputStream();

                for (String line : IOUtils.readLines(inputStream, StandardCharsets.UTF_8))
                    response.append(line).append('\n');

                inputStream.close();
                con.disconnect();

                if ((response.length() > 0) && (response.charAt(response.length() - 1) == '\n'))
                    response.deleteCharAt(response.length() - 1);
                return response.toString();
            } catch (Exception ex) {
                throw new RuntimeException("http GET request failed", ex);
            }
        }

        public static HttpURLConnection openConnection(HttpRequest request) {
            Check.notNull(request, "request cannot be null");

            try {
                URL url = new URL(request.getURL());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod(request.getRequestMethod().name());
                Map<String, String> requestProps = request.getRequestProperties();

                for (String prop : requestProps.keySet())
                    con.setRequestProperty(prop, requestProps.get(prop));

                con.setRequestProperty("User-Agent", request.getUserAgent());
                con.setInstanceFollowRedirects(request.shouldFollowRedirects());

                con.setConnectTimeout(request.getConnectTimeout());
                con.setReadTimeout(request.getReadTimeout());

                return con;
            } catch (Exception ex) {
                throw new RuntimeException("failed to establish a new connection", ex);
            }
        }
    }

}