package ru.smartjava;

//http://localhost:8080/index.html

import ru.smartjava.server.Server;
import ru.smartjava.utils.RequestParametersGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;


public class Main {
    public static void main(String[] args) {

        Server server = new Server();
        server.addHandler("GET","/index.html",(request, out) -> {
            RequestParametersGenerator rpg = new RequestParametersGenerator(request);
            try {
                out.write((rpg.getHeader()).getBytes());
                Files.copy(rpg.getFilePath(), out);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.addHandler("GET","/classic.html",(request, out) -> {
            RequestParametersGenerator rpg = new RequestParametersGenerator(request);
            final var content = rpg.getFileContent().replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            try {
                out.write((rpg.getHeader()).getBytes());
                out.write(content);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.start();

    }
}

