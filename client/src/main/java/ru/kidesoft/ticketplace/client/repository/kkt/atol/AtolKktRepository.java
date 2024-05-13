package ru.kidesoft.ticketplace.client.repository.kkt.atol;

import ru.kidesoft.ticketplace.client.domain.dao.Kkt;
import ru.kidesoft.ticketplace.client.domain.models.exception.KktException;

public class AtolKktRepository implements Kkt {
    @Override
    public void printXReport() throws KktException {
        throw new KktException("Во время печати Х-отчета произошла ошибка", "Не удалось печатать Х-отчет", 0);
    }
}