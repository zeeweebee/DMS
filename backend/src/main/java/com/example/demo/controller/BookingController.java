package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BookingDTO;
import com.example.demo.dto.CancelBookingRequest;
import com.example.demo.dto.CreateBookingRequest;
import com.example.demo.dto.PagedResponse;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * GET /api/bookings
     * Query params: page, pageSize, sortBy, sortDirection, status, dealerId
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDTO>>> getAll(
            Authentication auth,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDirection,
            @RequestParam(required = false)           String status,
            @RequestParam(required = false)           Long dealerId) {

        return ResponseEntity.ok(ApiResponse.ok(
                bookingService.getAll(auth.getName(), page, pageSize,
                        sortBy, sortDirection, status, dealerId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<BookingDTO>> getById(@PathVariable Long id,
                                                            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getById(id, auth.getName())));
    }

    /**
     * POST /api/bookings
     * Creates a booking; allocates VIN via FIFO if vin not supplied.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<BookingDTO>> create(@RequestBody CreateBookingRequest req,
                                                           Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.create(req, auth.getName())));
    }

    /**
     * PATCH /api/bookings/{id}/cancel
     * Cancels a booking, reverts VIN to AVAILABLE, reverts enquiry to NEGOTIATING.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<BookingDTO>> cancel(@PathVariable Long id,
                                                           @RequestBody CancelBookingRequest req,
                                                           Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.cancel(id, req, auth.getName())));
    }
}
