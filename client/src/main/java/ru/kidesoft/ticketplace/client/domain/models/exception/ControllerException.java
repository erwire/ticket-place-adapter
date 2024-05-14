package ru.kidesoft.ticketplace.client.domain.models.exception;

import ru.kidesoft.ticketplace.client.domain.presenter.ControllerType;

public class ControllerException extends AppException {
    ControllerType sourceType;
    public ControllerException(Exception e) {
        super(e);
    }
}
