package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CreateEnquiryRequest;
import com.example.demo.dto.EnquiryDTO;
import com.example.demo.dto.PagedResponse;
import com.example.demo.service.EnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;

    /**
     * GET /api/enquiries
     * Query params: page, pageSize, sortBy, sortDirection, keyword, status, dealerId
     * DEALER role: dealerId is always forced from JWT
     * ADMIN role: can filter by dealerId or see all
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<PagedResponse<EnquiryDTO>>> getAll(
            Authentication auth,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDirection,
            @RequestParam(required = false)           String keyword,
            @RequestParam(required = false)           String status,
            @RequestParam(required = false)           Long dealerId) {

        return ResponseEntity.ok(ApiResponse.ok(
                enquiryService.getAll(auth.getName(), page, pageSize,
                        sortBy, sortDirection, keyword, status, dealerId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<EnquiryDTO>> getById(@PathVariable Long id,
                                                            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(enquiryService.getById(id, auth.getName())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<EnquiryDTO>> create(@RequestBody CreateEnquiryRequest req,
                                                           Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(enquiryService.create(req, auth.getName())));
    }

    /**
     * PATCH /api/enquiries/{id}/status
     * Body: { "status": "CONTACTED" }
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<EnquiryDTO>> updateStatus(@PathVariable Long id,
                                                                  @RequestBody Map<String, String> body,
                                                                  Authentication auth) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("'status' field is required."));
        }
        return ResponseEntity.ok(ApiResponse.ok(enquiryService.updateStatus(id, newStatus, auth.getName())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
        enquiryService.delete(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Enquiry deleted", null));
    }
}
