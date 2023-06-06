package ru.smartjava.server;

import ru.smartjava.handlers.RequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {

    private final Integer SERVER_SOCKET_TIMEOUT = 500;
    private final ServerSocket serverSocket;
    private final int SERVER_THREAD_POOL_SIZE = 64;
    private final int SERVER_PORT = 8080;
    private final ExecutorService requestProcessingPool = Executors.newFixedThreadPool(SERVER_THREAD_POOL_SIZE);

    public Server() {

        System.out.println("Старт сервера");
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Сервер запущен");
        } catch (IOException e) {
            System.out.println("Ошибка открытия сокета: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        try {
            this.serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT);
        } catch (IOException e1) {
            //
        }
        while (true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                RequestHandler clientHandler = new RequestHandler(clientSocket);
                requestProcessingPool.execute(clientHandler);
            } catch (SocketTimeoutException te) {
                // Срабатывает каждые SOCKET_TIMEOUT миллисекунд
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

