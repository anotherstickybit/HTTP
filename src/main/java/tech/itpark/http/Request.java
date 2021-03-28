package tech.itpark.http;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Data
public class Request {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;

    public Request(String method, String path, String version, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = new byte[0];
    }
}
