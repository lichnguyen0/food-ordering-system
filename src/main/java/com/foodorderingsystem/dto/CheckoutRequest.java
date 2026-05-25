package com.foodorderingsystem.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String address;
    private String detailAddress;
    private String note;
    private String paymentMethod;
    private String couponCode;
    private Long addressId; // New field for selecting saved address
}
