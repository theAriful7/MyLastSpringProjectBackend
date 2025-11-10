package com.exampleOf.EcommerceApplication.controller;


import com.exampleOf.EcommerceApplication.dto.requestdto.OrderRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.OrderResponseDTO;
import com.exampleOf.EcommerceApplication.enums.OrderStatus;
import com.exampleOf.EcommerceApplication.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
//@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping
    public OrderResponseDTO createOrder(@RequestBody OrderRequestDTO dto) {
        return orderService.save(dto);
    }


    @GetMapping
    public List<OrderResponseDTO> getAllOrders() {
        return orderService.findAll();
    }


    @GetMapping("/{id}")
    public OrderResponseDTO getOrderById(@PathVariable Long id) {
        return orderService.findById(id);
    }


    @PutMapping("/{id}/status")
    public OrderResponseDTO updateStatus(@PathVariable Long id,
                                         @RequestParam OrderStatus status) {
        return orderService.updateStatus(id, status);
    }


    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return "Order deleted successfully!";
    }


    @PostMapping("/checkout")
    public OrderResponseDTO checkout(@RequestParam Long userId,
                                     @RequestParam Long addressId) {
        return orderService.checkout(userId, addressId);
    }
}
