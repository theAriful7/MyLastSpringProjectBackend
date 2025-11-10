package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.dto.requestdto.PaymentRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.PaymentResponseDTO;
import com.exampleOf.EcommerceApplication.enums.PaymentStatus;
import com.exampleOf.EcommerceApplication.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PaymentController {


    private final PaymentService paymentService;

    // ✅ Create Payment - HTTP 201
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO response = paymentService.createPayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ Get Payment by ID - HTTP 200
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // ✅ Get Payment by Order ID - HTTP 200
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    // ✅ Get All Payments - HTTP 200
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // ✅ Get Payments by Status - HTTP 200
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    // ✅ Update Payment Status - HTTP 200
    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    // ✅ Process Payment - HTTP 200
    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String transactionId) {
        return ResponseEntity.ok(paymentService.processPayment(id, transactionId));
    }

    // ✅ Delete Payment - HTTP 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
