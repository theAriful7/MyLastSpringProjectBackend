package com.exampleOf.EcommerceApplication.Exception.CustomException;

public class ForbiddenActionException extends RuntimeException{
    public ForbiddenActionException(String message) {
        super("🛑 Forbidden! " + message +
                ". You can’t do that unless you’re the boss 😎.");
    }
}
