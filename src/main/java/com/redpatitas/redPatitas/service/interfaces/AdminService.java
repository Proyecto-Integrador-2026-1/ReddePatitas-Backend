package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.response.ReportedPublicationDto;
import java.util.List;
import java.util.Map;
import com.redpatitas.redPatitas.dto.response.ModerationActionDto;
import com.redpatitas.redPatitas.dto.response.UserMetricsResponse;
import java.util.UUID;

public interface AdminService {

    Map<String, Object> getMetrics();

    UserMetricsResponse getUserMetrics();

    List<ReportedPublicationDto> listReportedPublications();

    List<ReportedPublicationDto> listHiddenPublications();

    List<ReportedPublicationDto> listDeletedPublications();

    void ocultarPublicacion(UUID reportId, UUID adminId, String motivo);

    void eliminarPublicacion(UUID reportId, UUID adminId, String motivo);

    void ignorarReporte(UUID reportId, UUID adminId, String motivo);

    void restaurarPublicacion(UUID reportId, UUID adminId, String motivo);

    void blockUser(UUID userId, UUID adminId, String motivo);

    void unblockUser(UUID userId, UUID adminId, String motivo);

    void deactivateUser(UUID userId, UUID adminId, String motivo);

    void activateUser(UUID userId, UUID adminId, String motivo);

    List<ModerationActionDto> listModerationHistory(int page, int size);

}
