package tech.itpark.converter;

import com.google.gson.Gson;
import tech.itpark.http.Request;

public class GsonBodyConverterImpl implements BodyConverter {
    Gson gson = new Gson();

    @Override
    public boolean canRead(String contentType, Class<?> cls) {
        return contentType.equals("application/json");
    }

    @Override
    public <T> T convert(Request request, Class<T> cls) {
        return gson.fromJson(new String(request.getBody()), cls);
    }

    @Override
    public String convert(Object response, Class<?> cls) {
        return gson.toJson(response, cls);
    }
}
