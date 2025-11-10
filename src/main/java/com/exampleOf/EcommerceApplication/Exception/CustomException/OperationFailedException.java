package com.exampleOf.EcommerceApplication.Exception.CustomException;

public class OperationFailedException extends RuntimeException{
    public OperationFailedException(String operation, String reason) {
        super("💥 Operation '" + operation + "' failed miserably! Reason: " + reason +
                ". Try again, maybe the server had a bad day 😅.");
    }
}
