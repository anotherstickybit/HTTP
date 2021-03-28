package tech.itpark.http;

import tech.itpark.annotation.RequestBody;
import tech.itpark.annotation.RequestHeader;
import tech.itpark.converter.BodyConverter;
import tech.itpark.converter.GsonBodyConverterImpl;
import tech.itpark.http.exception.HttpException;
import tech.itpark.http.exception.MalformedRequestException;
import tech.itpark.http.exception.NoHandlerException;
import tech.itpark.http.exception.UnresolvedHandlerParametersException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {
    private final Map<String, Map<String, Object>> routers = new HashMap<>();
    private final List<HandleMethodResolver> handleMethodResolvers = new LinkedList<>();
    BodyConverter bodyConverter = new GsonBodyConverterImpl();

//    public void register(Object object) {
//        if (!object.getClass().isAnnotationPresent(Controller.class)) {}
//
//    }

    public void register(HandleMethodResolver resolver) {
        handleMethodResolvers.add(resolver);
    }

    public void register(String method, String path, Object handler) {
//        final var map = Optional.ofNullable(routers.get(method))
//                .orElse(new HashMap<>());
//        map.put(path, handler);
//        routers.put(method, map);

        Optional.ofNullable(routers.get(method))
                .ifPresentOrElse(
                        map -> map.put(path, handler),
                        () -> routers.put(method, new HashMap<>(Map.of(path, handler))));
    }

    public void GET(String path, Object handler) {
        register("GET", path, handler);
    }

    public void GET(String path, Handler handler) {
        register("GET", path, handler);
    }

    public void POST(String path, Object handler) {
        register("POST", path, handler);
    }

    public void POST(String path, Handler handler) {
        register("POST", path, handler);
    }

    public void DELETE(String path, Object handler) {
        register("DELETE", path, handler);
    }

    public void DELETE(String path, Handler handler) {
        register("DELETE", path, handler);
    }

    public void start(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                handleConnection(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    Map<String, String> requestMap = new HashMap<>();
    Map<String, String> headersMap = new HashMap<>();

    private void handleConnection(Socket socket) {
        try (
                socket;
                final var out = new BufferedOutputStream(socket.getOutputStream());
                final var in = new BufferedInputStream(socket.getInputStream());
        ) {
            try {
                final var request = createRequest(in, out);

                final var handler = routers.get(request.getMethod()).get(request.getPath());
                callHandler(handler, request, out);
            } catch (HttpException e) {
                writeError(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request createRequest(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        final var limit = 1000000;
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        final var CRLF = new byte[]{'\r', '\n'};
        final var filled = new byte[read];
        System.arraycopy(buffer, 0, filled, 0, read);

        int previousEndPosition = 0;
        while (true) {
            final var currentEndPosition = indexOf(filled, CRLF, previousEndPosition) + CRLF.length;
            if (currentEndPosition == -1) {
                throw new MalformedRequestException("Content-Length not found");
            }
            final var header = new byte[currentEndPosition - previousEndPosition];
            System.arraycopy(
                    filled,
                    previousEndPosition,
                    header,
                    0,
                    currentEndPosition - previousEndPosition);
            final var headerString = new String(header);
            previousEndPosition = currentEndPosition;

            if (headerString.contains("HTTP/")) {
                final var split = headerString.split(" ");
                requestMap.put("method", split[0]);
                requestMap.put("path", split[1]);
                requestMap.put("version", split[2].substring(0, split[2].length() - 2).split("/")[1]);
                continue;
            }
            if (headerString.equals("\r\n")) {
                break;
            }
            final var split = headerString.split(":");
            headersMap.put(split[0].trim(), split[1].trim());
        }

        if (headersMap.containsKey("Content-Length")) {
            final var contentLength = Integer.parseInt(headersMap.get("Content-Length"));
            if (contentLength > 0) {
                final var body = new byte[contentLength];
                System.arraycopy(filled, previousEndPosition, body, 0, contentLength);
                return new Request(
                        requestMap.get("method"),
                        requestMap.get("path"),
                        requestMap.get("version"),
                        headersMap,
                        body
                );
            }
        }
        return new Request(
                requestMap.get("method"),
                requestMap.get("path"),
                requestMap.get("version"),
                headersMap
        );
    }

    private void writeError(OutputStream out) {
        try {
            out.write(("HTTP/1.1 400\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void callHandler(Object handler, Request request, OutputStream responseStream) {
        try {
            for (final var resolver : handleMethodResolvers) {
                final var method = resolver.resolve(handler);
                if (method.isPresent()) {
                    callAdapter(method.get(), handler, request, responseStream);
                    return;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
        throw new NoHandlerException();
    }

    private void callAdapter(
            Method method,
            Object handler,
            Request request,
            OutputStream responseStream)
            throws InvocationTargetException, IllegalAccessException {
        final var params = method.getParameters();
        final var args = new LinkedList<>();
        boolean shouldHandleResponse = true;
        for (Parameter param : params) {
            if (param.isAnnotationPresent(RequestHeader.class)) {
                final var annotation = param.getAnnotation(RequestHeader.class);
                final var value = annotation.value();

                final var required = annotation.required();
                final String defaultValue = annotation.defaultValue();
                final String header = request.getHeaders().get(value);
                if (header != null) {
                    args.add(header);
                    continue;
                }
            }
            if (param.isAnnotationPresent(RequestBody.class)) {
                if (param.getType().equals(String.class)) {
                    args.add(new String(request.getBody()));
                    continue;
                }
                // []byte
                if (bodyConverter.canRead(request, param.getType())) {
                    args.add(bodyConverter.convert(request, param.getType()));
                    continue;
                }
                args.add(request.getBody());
                continue;
            }
            final var parameterType = param.getType();
            if (parameterType.equals(Request.class)) {
                args.add(request);
                continue;
            }
            if (parameterType.equals(OutputStream.class)) {
                args.add(responseStream);
                shouldHandleResponse = false;
                continue;
            }
        }
        if (params.length != args.size()) {
            throw new UnresolvedHandlerParametersException();
        }
        method.setAccessible(true);

        final Class<?> responseType = method.getReturnType();
        final Object response = method.invoke(handler, args.toArray());
        if (shouldHandleResponse) {
            handleResponse(response, responseStream, responseType);
        }
    }

    private void handleResponse(Object response, OutputStream responseStream, Class<?> responseType) {
        if (responseType.getName().equals("void")) {
            writeResponseStream(responseStream);
            return;
        }
        if (response instanceof String) {
            String stringResponse = (String) response;
            writeResponseStream(responseStream, stringResponse, "text/plain");
            return;
        }
        if (bodyConverter.canWrite(responseType)) {
            String convert = bodyConverter.convert(response, responseType);
            writeResponseStream(responseStream, convert, "application/json");
            return;
        }
    }

    private void writeResponseStream(OutputStream responseStream) {
        try {
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n").getBytes()
            );
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeResponseStream(OutputStream responseStream, String body, String contentType) {
        try {
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: " + body.length() + "\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n" +
                            body).getBytes()
            );
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //from Guava: Bytes.java
    public static int indexOf(byte[] array, byte[] target, int start) {
        if (target.length == 0) {
            return 0;
        }

        outer:
        for (int i = start; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
