package com.example.demo.exception;

public class InvalidStockStatusException extends RuntimeException {
    public InvalidStockStatusException(String message) {
        super(message);
    }
}