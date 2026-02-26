package io.quickledger.controllers;

import io.quickledger.dto.StatsDto;
import io.quickledger.services.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<StatsDto.MonthlyRevenue>> getMonthlyRevenue(@PathVariable Long companyId) {
        return ResponseEntity.ok(statsService.getMonthlyRevenue(companyId));
    }

    @GetMapping("/outstanding")
    public ResponseEntity<StatsDto.OutstandingInvoices> getOutstandingInvoices(@PathVariable Long companyId) {
        return ResponseEntity.ok(statsService.getOutstandingInvoices(companyId));
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<StatsDto.TopCustomer>> getTopCustomers(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(statsService.getTopCustomers(companyId, limit));
    }
}
