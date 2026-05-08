package it.kevien.demo.orderservice.exception;

public class InventoryQuantityNotEnoughException extends RuntimeException {

    public InventoryQuantityNotEnoughException(String message) {
        super(message);
    }
}
