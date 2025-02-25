package org.example.Assignment;

public class FailToSendSAPInvoiceException extends Exception {
    public FailToSendSAPInvoiceException() {
        super("Failed to send invoice to SAP");
    }
}