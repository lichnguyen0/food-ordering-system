package com.foodorderingsystem.service;

import com.foodorderingsystem.dto.MonthlyRevenue;
import com.foodorderingsystem.model.Order;
import com.foodorderingsystem.model.OrderStatus;
import com.foodorderingsystem.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonthlyStatService {

    private final OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MonthlyStatService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<MonthlyRevenue> getRevenueByMonth(LocalDateTime start, LocalDateTime end) {
        String jpql = """
            SELECT 
                FUNCTION('MONTH', o.orderDate),
                FUNCTION('YEAR', o.orderDate),
                SUM(o.totalAmount)
            FROM Order o
            WHERE o.status != :cancelled
              AND o.orderDate >= :start
              AND o.orderDate < :end
            GROUP BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate)
            ORDER BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate)
        """;

        Query query = entityManager.createQuery(jpql);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setParameter("start", start);
        query.setParameter("end", end);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<MonthlyRevenue> stats = new ArrayList<>();

        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            double revenue = ((Number) row[2]).doubleValue();
            stats.add(new MonthlyRevenue(month, year, revenue));
        }

        return stats;
    }
}
