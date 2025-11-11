package com.exampleOf.EcommerceApplication.service;


import com.exampleOf.EcommerceApplication.Exception.CustomException.OperationFailedException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ResourceNotFoundException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ValidationException;
import com.exampleOf.EcommerceApplication.dto.requestdto.PaymentRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.PaymentResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Order;
import com.exampleOf.EcommerceApplication.entity.Payment;
import com.exampleOf.EcommerceApplication.enums.PaymentMethod;
import com.exampleOf.EcommerceApplication.enums.PaymentOption;
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


    public Payment toEntity(PaymentRequestDTO dto) {
        Order order = orderRepo.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", dto.getOrderId()));

        // Validate amount
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("amount", "Payment amount must be greater than 0");
        }

        // Validate payment method
        if (dto.getPaymentMethod() == null) {
            throw new ValidationException("paymentMethod", "Payment method is required");
        }

        // If Online Payment, must include type
        if (dto.getPaymentMethod() == PaymentMethod.ONLINE_PAYMENT && dto.getOnlinePaymentType() == null) {
            throw new ValidationException("onlinePaymentType", "Online payment type is required when method is ONLINE_PAYMENT");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setOnlinePaymentType(dto.getOnlinePaymentType());
        payment.setTransactionId(dto.getTransactionId());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());

        return payment;
    }


    public PaymentResponseDTO toDto(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setOnlinePaymentType(payment.getOnlinePaymentType());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentDate(payment.getPaymentDate());
        return dto;
    }


    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        try {
            // Check if payment already exists for this order
            boolean exists = paymentRepo.findAll().stream()
                    .anyMatch(p -> p.getOrder().getId().equals(dto.getOrderId()));

            if (exists) {
                throw new OperationFailedException("Create payment", "Payment already exists for this order");
            }

            Payment payment = toEntity(dto);
            Payment saved = paymentRepo.save(payment);
            return toDto(saved);
        } catch (Exception ex) {
            throw new OperationFailedException("Create payment", ex.getMessage());
        }
    }


    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return toDto(payment);
    }


    public PaymentResponseDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepo.findAll().stream()
                .filter(p -> p.getOrder().getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return toDto(payment);
    }


    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepo.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        payment.setPaymentStatus(status);
        if (status == PaymentStatus.COMPLETED && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        Payment updated = paymentRepo.save(payment);
        return toDto(updated);
    }


    @Transactional
    public PaymentResponseDTO processPayment(Long id, String transactionId) {
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        payment.setPaymentDate(LocalDateTime.now());

        Payment processed = paymentRepo.save(payment);
        return toDto(processed);
    }


    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepo.findAll()
                .stream()
                .filter(p -> p.getPaymentStatus() == status)
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new OperationFailedException("Delete payment", "Cannot delete completed payment");
        }

        paymentRepo.delete(payment);
    }

    // ✅ Get Payments by Payment Method
    public List<PaymentResponseDTO> getPaymentsByMethod(PaymentMethod method) {
        try {
            return paymentRepo.findAll().stream()
                    .filter(payment -> payment.getPaymentMethod() == method)
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve payments by method", ex.getMessage());
        }
    }

    // ✅ Get Payments by Payment Option (for online payments)
    public List<PaymentResponseDTO> getPaymentsByOption(PaymentOption option) {
        try {
            return paymentRepo.findAll().stream()
                    .filter(payment -> payment.getPaymentOption() != null && payment.getPaymentOption() == option)
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve payments by option", ex.getMessage());
        }
    }

}
