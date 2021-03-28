package test;

import com.google.gson.Gson;
import tech.itpark.tests.Cat;

import java.util.Arrays;

public class ParseRequest {
    public static void main(String[] args) {
        byte[] json = "{\"age\":10,\"name\":\"Sara\"}".getBytes();
//        System.out.println(new String(json));
        Gson gson = new Gson();
        Cat cat = gson.fromJson(new String(json), Cat.class);

    }
}
