package ru.smartjava.init;

public class Const {
    public static final int requestLimit = 4096;
    public static final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};

    public static final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
}
