package com.example.cinema_app_lab25_lab26;

/** Eroare de acces la date, cu mesaj clar pentru utilizator. */
public class ProviderException extends RuntimeException {

    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}