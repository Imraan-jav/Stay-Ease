package com.pgms.dto;

import com.pgms.model.Enums;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class Dtos {
    private Dtos() {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record RoomRequest(@NotBlank String roomNumber, @NotNull Integer floor, @NotBlank String roomType,
                              @NotNull @Positive BigDecimal rentPerBed, @Min(1) @Max(10) int beds) {}
    public record TenantRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank String phone,
                                String idProofType, String idProofNumber, String guardianPhone, @Size(min = 6) String password) {}
    public record AllocationRequest(@NotNull Long tenantId, @NotNull Long bedId, BigDecimal monthlyRent,
                                    BigDecimal deposit, LocalDate startDate) {}
    public record TransferRequest(@NotNull Long newBedId) {}
    public record PaymentRequest(@NotBlank String method, String reference) {}
    public record ComplaintRequest(@NotBlank String title, String description, String category) {}
    public record StatusRequest(@NotNull Enums.ComplaintStatus status) {}
    public record NoticeRequest(@NotBlank String title, @NotBlank String message) {}
}
