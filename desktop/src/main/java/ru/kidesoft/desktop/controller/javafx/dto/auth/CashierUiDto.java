package ru.kidesoft.desktop.controller.javafx.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashierUiDto {
    private String fullName;
    private Long inn;
}