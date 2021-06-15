package ru.sstu.vak.emotionrecognition.eventlistener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        String name = args[0];
        String path = args[1];
        int port = Integer.parseInt(args[2]);

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        server.createContext(path, httpExchange -> {
            log.info("[{}] Received event: {}", name, requestToString(httpExchange));
            httpExchange.sendResponseHeaders(200, 0);
        });

        server.start();

        log.info("'{}' started listen on port {} and path {}", name, port, path);
    }

    private static String requestToString(HttpExchange httpExchange) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = httpExchange.getRequestBody().read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        byte[] byteArray = buffer.toByteArray();

        return new String(byteArray, UTF_8);
    }
}
