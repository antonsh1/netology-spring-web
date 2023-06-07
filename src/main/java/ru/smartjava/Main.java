package ru.smartjava;

//http://localhost:8080/index.html

import ru.smartjava.server.Server;
import ru.smartjava.utils.RequestParametersExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;


public class Main {
    public static void main(String[] args) {

        Server server = new Server();
        server.addHandler("GET","/index.html",(request, out) -> {
            RequestParametersExtractor rpe = new RequestParametersExtractor(request);
            try {
                out.write((rpe.getHeader()).getBytes());
                Files.copy(rpe.getFilePath(), out);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.addHandler("GET","/classic.html",(request, out) -> {
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

