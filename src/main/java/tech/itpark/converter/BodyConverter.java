package tech.itpark.converter;

import tech.itpark.http.Request;

public interface BodyConverter {
    // content-type -> application/json GsonConverter
    boolean canRead(Request request, Class<?> cls);
    boolean canWrite(Class<?> cls);
    <T> T convert(Request request, Class<T> cls);
    String convert(Object response, Class<?> cls);
}
