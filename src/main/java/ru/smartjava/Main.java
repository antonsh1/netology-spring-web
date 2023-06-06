package ru.smartjava;

//http://localhost:8080/index.html

import ru.smartjava.server.Server;

public class Main {
    public static void main(String[] args) {

        Server server = new Server();
        server.start();

    }
}

