package com.foodorderingsystem.service;

import com.foodorderingsystem.model.payment.Invoice;
import com.foodorderingsystem.model.order.Order;
import com.foodorderingsystem.model.order.OrderStatus;
import com.foodorderingsystem.model.payment.PaymentStatus;
import com.foodorderingsystem.repository.payment.InvoiceRepository;
import com.foodorderingsystem.repository.order.OrderRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderRepository orderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Tự động đồng bộ hóa đơn cho tất cả đơn hàng cũ ngay khi ứng dụng khởi động.
     * Chạy 1 lần duy nhất, an toàn — không tạo trùng.
     */
    @PostConstruct
    @Transactional
    public void autoBackfillOnStartup() {
        List<Order> allOrders = orderRepository.findAll();
        int created = 0;

        for (Order order : allOrders) {
            if (invoiceRepository.findByOrder_OrderId(order.getOrderId()) != null) {
                continue; // Đã có hóa đơn → bỏ qua
            }

            Invoice invoice = new Invoice();
            invoice.setOrder(order);
            invoice.setInvoiceCode("INV-" + order.getOrderId() + "-" + System.currentTimeMillis());

            if (order.getUser() != null) {
                invoice.setCustomerName(order.getUser().getFullName());
                invoice.setCustomerPhone(order.getUser().getPhone());
            } else {
                invoice.setCustomerName("Khách hàng");
                invoice.setCustomerPhone("N/A");
            }

            invoice.setTotalAmount(order.getTotalAmount());
            invoice.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "CASH");

            // Ánh xạ trạng thái thanh toán theo trạng thái đơn thực tế
            if (order.getStatus() == OrderStatus.DELIVERED) {
                invoice.setPaymentStatus(PaymentStatus.PAID);
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                invoice.setPaymentStatus(PaymentStatus.CANCELLED);
            } else {
                invoice.setPaymentStatus(PaymentStatus.UNPAID);
            }

            invoiceRepository.save(invoice);
            created++;
        }

        if (created > 0) {
            log.info("[InvoiceService] Đã tự động tạo {} hóa đơn cho các đơn hàng cũ.", created);
        } else {
            log.info("[InvoiceService] Tất cả đơn hàng đã có hóa đơn. Không cần đồng bộ.");
        }
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll(
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
            )
        );
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    @Transactional
    public Invoice generateInvoiceForOrder(Order order) {
        // Kiểm tra xem đã có hóa đơn chưa
        Invoice existingInvoice = invoiceRepository.findByOrder_OrderId(order.getOrderId());
        if (existingInvoice != null) {
            return existingInvoice;
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceCode("INV-" + System.currentTimeMillis() + "-" + order.getOrderId());

        if (order.getUser() != null) {
            invoice.setCustomerName(order.getUser().getFullName());
            invoice.setCustomerPhone(order.getUser().getPhone());
        } else {
            invoice.setCustomerName("Khách hàng");
            invoice.setCustomerPhone("N/A");
        }

        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "CASH");
        invoice.setPaymentStatus(PaymentStatus.UNPAID);

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void updateInvoiceStatusBasedOnOrder(Long orderId, OrderStatus orderStatus) {
        Invoice invoice = invoiceRepository.findByOrder_OrderId(orderId);
        if (invoice != null) {
            if (orderStatus == OrderStatus.DELIVERED) {
                invoice.setPaymentStatus(PaymentStatus.PAID);
            } else if (orderStatus == OrderStatus.CANCELLED) {
                invoice.setPaymentStatus(PaymentStatus.CANCELLED);
            }
            invoiceRepository.save(invoice);
        }
    }
}
