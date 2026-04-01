package com.redpatitas.redPatitas.service.interfaces;

import java.math.BigDecimal;

public interface UbicacionService {
    void saveForReport(Long reportId, String lugarDesaparicion, BigDecimal latitud, BigDecimal longitud);
}
