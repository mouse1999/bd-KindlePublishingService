package com.amazon.ata.kindlepublishingservice.exceptions;

public class PublishingRecordFoundException extends RuntimeException{
    private static final long serialVersionUID = -8155479878535306164L;


    public PublishingRecordFoundException(String message) {

        super(message);
    }

    public PublishingRecordFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
