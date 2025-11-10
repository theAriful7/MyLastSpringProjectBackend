package com.exampleOf.EcommerceApplication.Exception.CustomException;

public class AlreadyExistsException extends RuntimeException{
    public AlreadyExistsException(String message) {
        super(message);
    }
}
