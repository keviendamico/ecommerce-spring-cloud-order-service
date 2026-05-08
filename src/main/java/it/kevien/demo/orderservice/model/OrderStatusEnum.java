package it.kevien.demo.orderservice.model;


import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    FAILED("failed");

    private final String value;

    OrderStatusEnum(String value) {
        this.value = value;
    }
}
