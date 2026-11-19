package com.pgms.model;

public final class Enums {
    private Enums() {}
    public enum Role { ADMIN, TENANT }
    public enum BedStatus { AVAILABLE, OCCUPIED }
    public enum InvoiceStatus { PENDING, OVERDUE, PAID }
    public enum ComplaintStatus { OPEN, IN_PROGRESS, RESOLVED }
}
