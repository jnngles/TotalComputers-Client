package com.jnngl.client.exception;

public class PacketAlreadyExistsException extends Exception {

    public PacketAlreadyExistsException(int id) {
        super("Packet "+id+" already exists");
    }

}
