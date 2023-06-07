package ru.smartjava.interfaces;

import ru.smartjava.classes.Request;
import java.io.BufferedOutputStream;

public interface Handler {

    public void handle(Request request, BufferedOutputStream responseStream);

}
