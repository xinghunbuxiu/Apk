package com.bt.sdk;

import java.io.IOException;

public class InvalidBtEncodingException extends IOException {
    public static final long serialVersionUID = -1;

    public InvalidBtEncodingException(String message) {
        super(message);
    }
}
