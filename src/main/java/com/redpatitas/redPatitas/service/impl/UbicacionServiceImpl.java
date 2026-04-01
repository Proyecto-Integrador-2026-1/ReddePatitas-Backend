package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.Ubicacion;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.repository.UbicacionRepository;
import com.redpatitas.redPatitas.service.interfaces.UbicacionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional
    public void saveForReport(Long reportId, String lugarDesaparicion, BigDecimal latitud, BigDecimal longitud) {
        if (lugarDesaparicion == null && latitud == null && longitud == null) return;
        Report report = reportRepository.getReferenceById(reportId);
        Ubicacion ubicacion = Ubicacion.builder()
                .report(report)
                .lugarDesaparicion(lugarDesaparicion)
                .latitud(latitud)
                .longitud(longitud)
                .build();
        ubicacionRepository.save(ubicacion);
    }
}
