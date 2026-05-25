package com.foodorderingsystem.repository.payment;

import com.foodorderingsystem.model.payment.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByInvoiceCode(String invoiceCode);
    Invoice findByOrder_OrderId(Long orderId);
}
