package test;

import tech.itpark.annotation.RequestBody;
import tech.itpark.annotation.RequestHeader;
import tech.itpark.http.HandleMethodResolver;
import tech.itpark.http.Request;
import tech.itpark.http.Server;
import tech.itpark.tests.Cat;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        server.register(HandleMethodResolver::handlerMethodResolver);
        server.register(HandleMethodResolver::singlePublicMethodResolver);
        server.POST("/api/users", ((request, responseStream) -> {
            System.out.println("Hello World");
        }));

        //local class
//        abstract class CustomHandler {
//            public abstract void handle(OutputStream stream);
//        }
//        server.POST("/api/users", new CustomHandler() {
//            @Override
//            public void handle(OutputStream stream) {
//                System.out.println("finish");
//            }
//        });

        abstract class AdvancedHandler {
            public abstract String handle(String contentType, Cat body, Request request);
        }
        server.POST("/api/users", new AdvancedHandler() {
            @Override
            public String handle(
                    @RequestHeader(value = "Content-Type") String contentType,
                    @RequestBody Cat body,
                    Request request
            ) {
                System.out.println("finish");
                return body.getName();
            }
        });

        abstract class NewHandler {
            public abstract Cat handle(Request request);
        }
        server.GET("/api/get", new NewHandler() {
            @Override
            public Cat handle(Request request) {
                return new Cat(10, "Sara");
            }
        });
        server.start(9999);
    }

}

