package com.jnngl.client.exception;

public class IncompatibleAPIException extends Exception {

    public IncompatibleAPIException(int apiVersion, int minVersion) {
        super("API version "+apiVersion+" is not supported. Required at least "+minVersion+'.');
    }

}
