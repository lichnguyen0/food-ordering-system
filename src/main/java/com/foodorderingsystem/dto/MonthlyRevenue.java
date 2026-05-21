package com.foodorderingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyRevenue {
    private int month;
    private int year;
    private double revenue;
}
