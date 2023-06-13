package ru.smartjava;

//http://localhost:8080/index.html

import ru.smartjava.init.Methods;
import ru.smartjava.server.Server;
import ru.smartjava.utils.ResponseParametersGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;


public class Main {

    public static void main(String[] args) {

        Server server = new Server();
        server.addHandler(Methods.GET, "/index.html", (request, out) -> {
            ResponseParametersGenerator rpe = new ResponseParametersGenerator(request);
            try {
                out.write((rpe.getResponseHeader()).getBytes());
                Files.copy(rpe.getFilePath(), out);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/default-get", (request, out) -> {
            ResponseParametersGenerator rpe = new ResponseParametersGenerator(request);
            try {
                out.write((rpe.getResponseHeader()).getBytes());
                if (!rpe.getResponseHeader().contains("Not Found")) {
                    Files.copy(rpe.getFilePath(), out);
                }
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/default-post", (request, out) -> {
            ResponseParametersGenerator rpe = new ResponseParametersGenerator(request);
            try {
                out.write((rpe.getResponseHeader()).getBytes());
                if (!rpe.getResponseHeader().contains("Not Found")) {
                    Files.copy(rpe.getFilePath(), out);
                }
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.POST, "/messages", (request, out) -> {
            ResponseParametersGenerator rpe = new ResponseParametersGenerator(request);
            if (request.getQueryParams() != null) {
                System.out.println("URL Params = " + request.getQueryParams());
                System.out.println("URL Param = value " + request.getParams("value"));
            }
            if (request.getBodyQueryParams() != null) {
                System.out.println("BODY Params = " + request.getBodyQueryParams());
                System.out.println("BODY Param = value " + request.getBodyParams("value"));
            }
            request.test();
            try {
                out.write((rpe.getSimpleOkHeader()).getBytes());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/messages", (request, out) -> {
            ResponseParametersGenerator rpe = new ResponseParametersGenerator(request);
            if (request.getQueryParams() != null) {
                System.out.println("URL Params = " + request.getQueryParams());
                System.out.println("URL Param = title " + request.getParams("title"));
                System.out.println("URL Param = value " + request.getParams("value"));
            }
            try {
                out.write((rpe.getSimpleOkHeader()).getBytes());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler(Methods.GET, "/classic.html", (request, out) -> {
            ResponseParametersGenerator rpe = new ResponseParametersGenerator(request);
            final var content = rpe.getFileContent().replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            try {
                out.write((rpe.getResponseHeader()).getBytes());
                out.write(content);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.start();

    }
}

