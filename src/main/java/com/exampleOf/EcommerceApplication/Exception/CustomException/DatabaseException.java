package com.exampleOf.EcommerceApplication.Exception.CustomException;

public class DatabaseException extends RuntimeException{
    public DatabaseException(String message) {
        super("🧨 Database glitch detected! " + message +
                ". Check your SQL, or maybe the DB just needs some coffee ☕");
    }
}
