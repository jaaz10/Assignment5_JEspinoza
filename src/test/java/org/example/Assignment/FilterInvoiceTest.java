package org.example.Assignment;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FilterInvoiceTest {

    // Integration test that checks the actual filtering
    // This verifies:
    // The result is not null
    // All returned invoices have vals less than 100
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

    // Unit test which uses stubs/mocks to test invoice filtering in iso
    // This creates mock db's and DAO to avoid real db access
    // Sets up test data w/ one invoice less than 100 and one above it
    // Then it changes mock DAO to return the test data
    // Then it checks that only invoices less than 100 are in the the result
    @Test
    void filterInvoiceStubbedTest() {
        // Create mock/stub for Database and DAO
        Database mockDb = mock(Database.class);
        QueryInvoicesDAO mockDao = mock(QueryInvoicesDAO.class);

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

    // Tests when there are low val invoices to be sent to SAP
    // Mocks both filter & SAP dependencies
    // Makes test invoices and stubs filters to return them
    // verifies SAP.send is called exactly only once for every invoice
    // Makes sure the correct invoices are sent to SAP
    @Test
    void testWhenLowInvoicesSent() throws FailToSendSAPInvoiceException {
        // Set up mocks
        FilterInvoice mockFilter = mock(FilterInvoice.class);
        SAP mockSap = mock(SAP.class);

        // Makes test invoices
        Invoice invoice1 = new Invoice("customer1", 50);
        Invoice invoice2 = new Invoice("customer2", 75);
        List<Invoice> lowValueInvoices = Arrays.asList(invoice1, invoice2);

        // Stub filter to return test invoices
        when(mockFilter.lowValueInvoices()).thenReturn(lowValueInvoices);

        // Makes SAP sender with mocked dependencies
        SAP_BasedInvoiceSender sapSender = new SAP_BasedInvoiceSender(mockFilter, mockSap);

        // Executes the method we're testing
        sapSender.sendLowValuedInvoices();

        // Verifies SAP connections
        verify(mockSap).send(invoice1);
        verify(mockSap).send(invoice2);
        verify(mockSap, times(2)).send(any(Invoice.class));
    }

    // Tests when there are no low vals to send
    // Mocks both filter and SAP depens
    // Stubs filter to return empty list
    // verifies SAP.send is not called
    // Makes sure system handles empty case with no error
    @Test
    void testWhenNoInvoices() throws FailToSendSAPInvoiceException {
        // Set up mocks
        FilterInvoice mockFilter = mock(FilterInvoice.class);
        SAP mockSap = mock(SAP.class);

        // Stub filter to return empty list
        when(mockFilter.lowValueInvoices()).thenReturn(new ArrayList<>());

        // Makes SAP sender with mocked depens
        SAP_BasedInvoiceSender sapSender = new SAP_BasedInvoiceSender(mockFilter, mockSap);

        // Execute the method being tested
        sapSender.sendLowValuedInvoices();

        // Verify SAP will not be called
        verify(mockSap, never()).send(any(Invoice.class));
    }


      // Tests error handling when sending invoice to SAP fails
      // Sets up mock depens
      // changes SAP mock to throw exception for certain invoice
      // Verifies failed invoice is stored and returned
      // makes sure system continues processing other invoices
    @Test
    void testThrowExceptionWhenBadInvoice() throws FailToSendSAPInvoiceException {
        // Set up mocks
        FilterInvoice mockFilter = mock(FilterInvoice.class);
        SAP mockSap = mock(SAP.class);

        // Create test invoice that will fail
        Invoice badInvoice = new Invoice("customer1", 50);
        List<Invoice> invoices = Arrays.asList(badInvoice);

        // Stub filter to return test invoice
        when(mockFilter.lowValueInvoices()).thenReturn(invoices);

        // Configure SAP to throw exception for specific invoice
        doThrow(new FailToSendSAPInvoiceException())
                .when(mockSap).send(badInvoice);

        // Create sender with mocked dependencies
        SAP_BasedInvoiceSender sapSender = new SAP_BasedInvoiceSender(mockFilter, mockSap);

        // Execute and get failed invoices
        List<Invoice> failedInvoices = sapSender.sendLowValuedInvoices();

        // Verify the invoice failed and was stored
        assertEquals(1, failedInvoices.size());
        assertTrue(failedInvoices.contains(badInvoice));
    }

}