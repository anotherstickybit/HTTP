package tech.itpark.converter;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import tech.itpark.converter.BodyConverter;
import tech.itpark.http.Request;

import java.util.Arrays;

public class GsonBodyConverterImpl implements BodyConverter {
    Gson gson = new Gson();

    @Override
    public boolean canRead(Request request, Class<?> cls) {
        byte[] body = request.getBody();
        try {
            Object o = gson.fromJson(new String(body), cls);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    @Override
    public boolean canWrite(Class<?> cls) {
        try {
            String jsonString = gson.toJson(cls.getName());
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    @Override
    public <T> T convert(Request request, Class<T> cls) {
        if (canRead(request, cls)) {
            return gson.fromJson(new String(request.getBody()), cls);
        }
        return null;
    }

    @Override
    public String convert(Object response, Class<?> cls) {
        if (canWrite(cls)) {
            return gson.toJson(response, cls);
        }
        return null;
    }
}
