package io.quickledger.services;

import io.quickledger.dto.StatsDto;
import io.quickledger.repositories.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final Logger logger = LoggerFactory.getLogger(StatsService.class);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final InvoiceRepository invoiceRepository;

    public StatsService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Get monthly revenue for the last 6 months.
     * Returns all 6 months including months with zero revenue.
     */
    public List<StatsDto.MonthlyRevenue> getMonthlyRevenue(Long companyId) {
        logger.debug("Getting monthly revenue for company {}", companyId);

        List<Object[]> results = invoiceRepository.getMonthlyRevenueByCompanyId(companyId);

        // Create a map of existing data
        Map<String, StatsDto.MonthlyRevenue> revenueMap = results.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> new StatsDto.MonthlyRevenue(
                    (String) row[0],
                    row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO,
                    row[2] != null ? ((Number) row[2]).intValue() : 0
                )
            ));

        // Build list with all 6 months (including zeros)
        List<StatsDto.MonthlyRevenue> monthlyRevenue = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            String monthStr = month.format(MONTH_FORMATTER);

            if (revenueMap.containsKey(monthStr)) {
                monthlyRevenue.add(revenueMap.get(monthStr));
            } else {
                monthlyRevenue.add(new StatsDto.MonthlyRevenue(monthStr, BigDecimal.ZERO, 0));
            }
        }

        return monthlyRevenue;
    }

    /**
     * Get outstanding invoices summary (SENT status = unpaid).
     */
    public StatsDto.OutstandingInvoices getOutstandingInvoices(Long companyId) {
        logger.debug("Getting outstanding invoices for company {}", companyId);

        List<Object[]> results = invoiceRepository.getOutstandingInvoicesByCompanyId(companyId);

        if (results == null || results.isEmpty()) {
            return new StatsDto.OutstandingInvoices(0, BigDecimal.ZERO);
        }

        Object[] result = results.get(0);
        int count = result[0] != null ? ((Number) result[0]).intValue() : 0;
        BigDecimal totalAmount = result[1] != null ? new BigDecimal(result[1].toString()) : BigDecimal.ZERO;

        return new StatsDto.OutstandingInvoices(count, totalAmount);
    }

    /**
     * Get top customers by revenue.
     */
    public List<StatsDto.TopCustomer> getTopCustomers(Long companyId, int limit) {
        logger.debug("Getting top {} customers for company {}", limit, companyId);

        List<Object[]> results = invoiceRepository.getTopCustomersByCompanyId(companyId, limit);

        return results.stream()
            .map(row -> new StatsDto.TopCustomer(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                row[1] != null ? ((String) row[1]).trim() : "Unknown",
                row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO,
                row[3] != null ? ((Number) row[3]).intValue() : 0
            ))
            .collect(Collectors.toList());
    }
}
