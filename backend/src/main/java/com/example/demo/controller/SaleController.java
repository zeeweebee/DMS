package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.PagedResponse;
import com.example.demo.dto.SaleDTO;
import com.example.demo.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    /**
     * GET /api/sales
     * Params: page, pageSize, sortBy, sortDirection, keyword, paymentStatus, dealerId
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<PagedResponse<SaleDTO>>> getAll(
            Authentication auth,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDirection,
            @RequestParam(required = false)           String keyword,
            @RequestParam(required = false)           String paymentStatus,
            @RequestParam(required = false)           Long dealerId) {

        return ResponseEntity.ok(ApiResponse.ok(
                saleService.getAll(auth.getName(), page, pageSize,
                        sortBy, sortDirection, keyword, paymentStatus, dealerId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<SaleDTO>> getById(@PathVariable Long id,
                                                         Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(saleService.getById(id, auth.getName())));
    }

    /**
     * POST /api/sales
     * Converts a CONFIRMED booking into a Sale, marks VIN as SOLD,
     * and mints a unique invoice number.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<SaleDTO>> create(@RequestBody CreateSaleRequest req,
                                                        Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(saleService.create(req, auth.getName())));
    }

    /**
     * GET /api/sales/{id}/invoice
     * Streams the invoice as a downloadable PDF.
     * Content-Disposition: attachment; filename="INV-YYYY-XXXXXX.pdf"
     */
    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id,
                                                   Authentication auth) {
        // Fetch metadata first so we can name the file correctly
        SaleDTO sale = saleService.getById(id, auth.getName());
        byte[] pdf   = saleService.generateInvoicePdf(id, auth.getName());

        String filename = sale.getInvoiceNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(pdf);
    }
}
