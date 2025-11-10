package com.exampleOf.EcommerceApplication.service;


import com.exampleOf.EcommerceApplication.Exception.CustomException.OperationFailedException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ResourceNotFoundException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ValidationException;
import com.exampleOf.EcommerceApplication.dto.requestdto.PaymentRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.PaymentResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Order;
import com.exampleOf.EcommerceApplication.entity.Payment;
import com.exampleOf.EcommerceApplication.enums.PaymentStatus;
import com.exampleOf.EcommerceApplication.repository.OrderRepo;
import com.exampleOf.EcommerceApplication.repository.PaymentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final OrderRepo orderRepo;

    // ✅ DTO → Entity with validation
    public Payment toEntity(PaymentRequestDTO dto) {
        // Validate order exists
        Order order = orderRepo.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", dto.getOrderId()));

        // Validate amount
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("amount", "Payment amount must be greater than 0");
        }

        // Validate payment method
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().trim().isEmpty()) {
            throw new ValidationException("paymentMethod", "Payment method is required");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod().trim());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        return payment;
    }

    // ✅ Entity → DTO
    public PaymentResponseDTO toDto(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentDate(payment.getPaymentDate());
        return dto;
    }

    // ✅ Create Payment
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        try {
            // Check if payment already exists for this order
            boolean paymentExists = paymentRepo.findAll().stream()
                    .anyMatch(payment -> payment.getOrder().getId().equals(dto.getOrderId()));

            if (paymentExists) {
                throw new OperationFailedException(
                        "Create payment",
                        "Payment already exists for order ID: " + dto.getOrderId()
                );
            }

            Payment payment = toEntity(dto);
            Payment saved = paymentRepo.save(payment);
            return toDto(saved);
        } catch (Exception ex) {
            throw new OperationFailedException("Create payment", ex.getMessage());
        }
    }

    // ✅ Get Payment by ID
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return toDto(payment);
    }

    // ✅ Get Payment by Order ID
    public PaymentResponseDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepo.findAll().stream()
                .filter(p -> p.getOrder().getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return toDto(payment);
    }

    // ✅ Get All Payments
    public List<PaymentResponseDTO> getAllPayments() {
        try {
            return paymentRepo.findAll()
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve all payments", ex.getMessage());
        }
    }

    // ✅ Update Payment Status
    @Transactional
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatus status) {
        try {
            Payment payment = paymentRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

            payment.setPaymentStatus(status);

            // If status is COMPLETED, update payment date
            if (status == PaymentStatus.COMPLETED && payment.getPaymentDate() == null) {
                payment.setPaymentDate(LocalDateTime.now());
            }

            Payment updated = paymentRepo.save(payment);
            return toDto(updated);
        } catch (Exception ex) {
            throw new OperationFailedException("Update payment status", ex.getMessage());
        }
    }

    // ✅ Process Payment (Complete payment process)
    @Transactional
    public PaymentResponseDTO processPayment(Long paymentId, String transactionId) {
        try {
            Payment payment = paymentRepo.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

            // Simulate payment processing logic
            // In real app, you'd integrate with payment gateway here

            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());

            // You can store transaction ID if needed
            // payment.setTransactionId(transactionId);

            Payment processed = paymentRepo.save(payment);
            return toDto(processed);
        } catch (Exception ex) {
            throw new OperationFailedException("Process payment", ex.getMessage());
        }
    }

    // ✅ Get Payments by Status
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        try {
            return paymentRepo.findAll().stream()
                    .filter(payment -> payment.getPaymentStatus() == status)
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve payments by status", ex.getMessage());
        }
    }

    // ✅ Delete Payment
    @Transactional
    public void deletePayment(Long id) {
        try {
            Payment payment = paymentRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

            // Check if payment is already completed
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                throw new OperationFailedException(
                        "Delete payment",
                        "Cannot delete completed payment"
                );
            }

            paymentRepo.delete(payment);
        } catch (Exception ex) {
            throw new OperationFailedException("Delete payment", ex.getMessage());
        }
    }
}
