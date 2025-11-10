package com.exampleOf.EcommerceApplication.Exception.CustomException;

public class UnauthorizedAccessException extends RuntimeException{
    public UnauthorizedAccessException(String action) {
        super("🚫 Access denied! You tried to " + action +
                " but you don't have the magic keys 🔑. Please login as a valid user.");
    }
}
