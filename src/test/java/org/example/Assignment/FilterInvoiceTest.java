package org.example.Assignment;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

    @Test
    void filterInvoiceStubbedTest() {
        // Create mock/stub for Database and DAO
        Database mockDb = Mockito.mock(Database.class);
        QueryInvoicesDAO mockDao = Mockito.mock(QueryInvoicesDAO.class);

        // test data
        Invoice invoice1 = new Invoice("customer1", 50);    // Should pass filter
        Invoice invoice2 = new Invoice("customer2", 150);   // Should not pass filter
        List<Invoice> testInvoices = Arrays.asList(invoice1, invoice2);

        // mock DAO behavior
        when(mockDao.all()).thenReturn(testInvoices);

        // FilterInvoice using the injected dependencies
        FilterInvoice filter = new FilterInvoice(mockDb, mockDao);

        // Testing the filter
        List<Invoice> result = filter.lowValueInvoices();

        // make sure results are good
        assertEquals(1, result.size(), "Should only have one invoice under 100");
        assertTrue(result.contains(invoice1), "Should contain the invoice with value 50");
        assertFalse(result.contains(invoice2), "Should not contain the invoice with value 150");
    }
}