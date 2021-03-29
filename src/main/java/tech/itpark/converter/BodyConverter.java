package tech.itpark.converter;

import tech.itpark.http.Request;

public interface BodyConverter {
    // content-type -> application/json GsonConverter
    boolean canRead(String contentType, Class<?> cls);
    <T> T convert(Request request, Class<T> cls);
    String convert(Object response, Class<?> cls);
}
