package com.exampleOf.EcommerceApplication.Exception.CustomException;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("😕 Oops! " + resourceName + " not found with " + fieldName + " : " + fieldValue +
                ". Are you sure it even exists?");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
