package com.exampleOf.EcommerceApplication.Exception.CustomException;

public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }
}