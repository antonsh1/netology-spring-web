package ru.smartjava;

//http://localhost:8080/index.html

import ru.smartjava.init.Methods;
import ru.smartjava.server.Server;
import ru.smartjava.utils.RequestParametersExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;


public class Main {

    public static void main(String[] args) {

        Server server = new Server();
        server.addHandler(Methods.GET, "/index.html", (request, out) -> {
            RequestParametersExtractor rpe = new RequestParametersExtractor(request);
            try {
                out.write((rpe.getHeader()).getBytes());
                Files.copy(rpe.getFilePath(), out);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/default-get", (request, out) -> {
            RequestParametersExtractor rpe = new RequestParametersExtractor(request);
            try {
                out.write((rpe.getHeader()).getBytes());
                Files.copy(rpe.getFilePath(), out);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/default-post", (request, out) -> {
            RequestParametersExtractor rpe = new RequestParametersExtractor(request);
            try {
                out.write((rpe.getHeader()).getBytes());
                if (!rpe.getHeader().contains("Not Found")) {
                    Files.copy(rpe.getFilePath(), out);
                }
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.POST, "/messages", (request, out) -> {
            RequestParametersExtractor rpe = new RequestParametersExtractor(request);

            if (request.getQueryParams() != null) {
                System.out.println("GET Params = " + request.getQueryParams());
                System.out.println("GET value = " + request.getParams("value"));
            }
            if (request.getBodyQueryParams() != null) {
                System.out.println("POST Params = " + request.getBodyQueryParams());
                System.out.println("POST value = " + request.getBodyParams("value"));
            }
//            System.out.println("Body: " + request.getBody());

            try {
                out.write((rpe.getEmptyHeader()).getBytes());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/messages", (request, out) -> {
            RequestParametersExtractor rpe = new RequestParametersExtractor(request);
            if (request.getQueryParams() != null) {
                System.out.println("GET Params = " + request.getQueryParams());
                System.out.println("GET value = " + request.getParams("value"));
            }
            try {
                out.write((rpe.getEmptyHeader()).getBytes());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/classic.html", (request, out) -> {
            RequestParametersExtractor rpe = new RequestParametersExtractor(request);
            final var content = rpe.getFileContent().replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            try {
                out.write((rpe.getHeader()).getBytes());
                out.write(content);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.start();

    }
}

