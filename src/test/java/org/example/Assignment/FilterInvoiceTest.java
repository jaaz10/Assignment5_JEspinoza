package org.example.Assignment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class FilterInvoiceTest {

    @Test
    void filterInvoiceTest() {

        FilterInvoice filter = new FilterInvoice();


        List<Invoice> result = filter.lowValueInvoices();

        
        assertNotNull(result, "Result should not be null");
        for (Invoice invoice : result) {
            assertTrue(invoice.getValue() < 100,
                    "Each invoice should have value less than 100");
        }
    }
}