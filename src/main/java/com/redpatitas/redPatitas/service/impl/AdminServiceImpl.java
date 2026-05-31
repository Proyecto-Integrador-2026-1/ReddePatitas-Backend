package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.response.ReportedPublicationDto;
import com.redpatitas.redPatitas.repository.ReportPublicationRepository;
import com.redpatitas.redPatitas.client.AuthServiceClient;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.repository.ModerationActionRepository;
import com.redpatitas.redPatitas.entity.ModerationAction;
import com.redpatitas.redPatitas.dto.response.ModerationActionDto;
import com.redpatitas.redPatitas.service.interfaces.AdminService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
//import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.redpatitas.redPatitas.dto.response.UserMetricsResponse;

@Service
public class AdminServiceImpl implements AdminService {

    private final ReportRepository reportRepository;
    private final ReportPublicationRepository reportPublicationRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final AuthServiceClient authServiceClient;
    private final EntityManager em;

    public AdminServiceImpl(ReportRepository reportRepository,
                            ReportPublicationRepository reportPublicationRepository,
                            ModerationActionRepository moderationActionRepository,
                            AuthServiceClient authServiceClient,
                            EntityManager em) {
        this.reportRepository = reportRepository;
        this.reportPublicationRepository = reportPublicationRepository;
        this.moderationActionRepository = moderationActionRepository;
        this.authServiceClient = authServiceClient;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    //@PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getMetrics() {
        Map<String, Object> m = new HashMap<>();
        long totalPublications = reportRepository.count();
        long totalReportedPublications = reportRepository.countByEstadoIn(java.util.List.of("REPORTADA", "EN_REVISION"));
        long totalResolvedPublications = reportRepository.countByEstado("RESUELTO");
        long totalDeletedPublications = reportRepository.countByEstado("ELIMINADO");
        long totalHiddenPublications = reportRepository.countByOcultoTrue();
        // Pending = publications that are ACTIVO, REPORTADA, EN_REVISION or OCULTADO (exclude ELIMINADO/RESUELTO)
        long pendingReports = reportRepository.countByEstadoIn(java.util.List.of("ACTIVO", "REPORTADA", "EN_REVISION", "OCULTADO"));
        m.put("total_Publicaciones", totalPublications);
        m.put("total_Publicaciones_Reportadas", totalReportedPublications);
        m.put("total_Publicaciones_Resueltas", totalResolvedPublications);
        m.put("total_Publicaciones_Eliminadas", totalDeletedPublications);
        m.put("total_Publicaciones_Ocultas", totalHiddenPublications);
        m.put("reports_Pendientes", pendingReports);
        return m;
    }

    @Override
    @Transactional(readOnly = true)
    //@PreAuthorize("hasRole('ADMIN')")
    public List<ReportedPublicationDto> listReportedPublications() {
        String sql = "select r.id as reportId, r.user_id as userId, r.pet_id as petId, p.nombre as petName, r.tipo_reporte as tipoReporte, r.fecha_creacion as fechaCreacion, " +
            "count(rp.*) as reportCount, max(rp.fecha_creacion) as lastReportAt, img.imagen_url as imagenUrl, img.thumbnail_url as thumbnailUrl, " +
            "rp_l.user_id as reporterId, rp_l.razon as reporterRazon " +
            "from reports r join report_publications rp on rp.report_id = r.id " +
            "left join pets p on p.id = r.pet_id " +
            "left join lateral (select i.imagen_url, i.thumbnail_url from imagen i where i.id_reporte = r.id order by i.creado_en desc limit 1) img on true " +
            "left join lateral (select rp2.user_id, rp2.razon from report_publications rp2 where rp2.report_id = r.id order by rp2.fecha_creacion desc limit 1) rp_l on true " +
            "where r.estado in ('REPORTADA','EN_REVISION') and r.oculto = false and r.eliminado = false " +
            "group by r.id, r.user_id, r.pet_id, p.nombre, r.tipo_reporte, r.fecha_creacion, img.imagen_url, img.thumbnail_url, rp_l.user_id, rp_l.razon " +
            "order by lastReportAt desc";

        var q = em.createNativeQuery(sql);
        List<?> rawResults = q.getResultList();

        // First map raw rows to a temporary structure including reporter id and reason
        var intermediate = rawResults.stream().map(r -> (Object[]) r).map(row -> {
            UUID reportId = (UUID) row[0];
            UUID userId = (UUID) row[1];
            UUID petId = (UUID) row[2];
            String petName = row[3] != null ? (String) row[3] : null;
            String tipo = (String) row[4];

            Instant fechaCreacion = null;
            Object fechaObj = row[5];
            if (fechaObj != null) {
                if (fechaObj instanceof java.time.Instant) {
                    fechaCreacion = (java.time.Instant) fechaObj;
                } else if (fechaObj instanceof java.sql.Timestamp) {
                    fechaCreacion = ((java.sql.Timestamp) fechaObj).toInstant();
                } else if (fechaObj instanceof java.util.Date) {
                    fechaCreacion = ((java.util.Date) fechaObj).toInstant();
                }
            }

            long count = ((Number) row[6]).longValue();

            Instant last = null;
            Object lastObj = row[7];
            if (lastObj != null) {
                if (lastObj instanceof java.time.Instant) {
                    last = (java.time.Instant) lastObj;
                } else if (lastObj instanceof java.sql.Timestamp) {
                    last = ((java.sql.Timestamp) lastObj).toInstant();
                } else if (lastObj instanceof java.util.Date) {
                    last = ((java.util.Date) lastObj).toInstant();
                }
            }

            String imagenUrl = row[8] != null ? (String) row[8] : null;
            String thumbnailUrl = row[9] != null ? (String) row[9] : null;

            UUID reporterId = row[10] != null ? (UUID) row[10] : null;
            String reporterRazon = row[11] != null ? (String) row[11] : null;

            Map<String, Object> item = new HashMap<>();
            item.put("reportId", reportId);
            item.put("userId", userId);
            item.put("petId", petId);
            item.put("petName", petName);
            item.put("tipo", tipo);
            item.put("fechaCreacion", fechaCreacion);
            item.put("count", count);
            item.put("last", last);
            item.put("imagenUrl", imagenUrl);
            item.put("thumbnailUrl", thumbnailUrl);
            item.put("reporterId", reporterId);
            item.put("reporterRazon", reporterRazon); // Assuming the report publication date is the same as the report creation date for this example
            return item;
        }).collect(Collectors.toList());

        // Resolve names for publisher (userId) and reporter (reporterId) via AuthServiceClient
        var userIds = intermediate.stream()
            .flatMap(m1 -> java.util.stream.Stream.of((UUID) m1.get("userId"), (UUID) m1.get("reporterId")))
            .filter(id -> id != null)
            .collect(Collectors.toSet())
            .stream().toList();

        java.util.Map<UUID, com.redpatitas.redPatitas.dto.response.ContactInfoResponse> contacts = new HashMap<>();
        try {
            var fetched = authServiceClient.getBatchContactInfo(userIds);
            if (fetched != null) contacts.putAll(fetched);
        } catch (Exception ignored) {
        }

        return intermediate.stream().map(m1 -> {
            UUID reportId = (UUID) m1.get("reportId");
            UUID userId = (UUID) m1.get("userId");
            UUID petId = (UUID) m1.get("petId");
            String petName = (String) m1.get("petName");
            String tipo = (String) m1.get("tipo");
            Instant fechaCreacion = (Instant) m1.get("fechaCreacion");
            long count = m1.get("count") != null ? ((Number) m1.get("count")).longValue() : 0L;
            Instant last = (Instant) m1.get("last");
            String imagenUrl = (String) m1.get("imagenUrl");
            String thumbnailUrl = (String) m1.get("thumbnailUrl");
            UUID reporterId = (UUID) m1.get("reporterId");
            String reporterRazon = (String) m1.get("reporterRazon");

            String publisherName = null;
            String reporterName = null;
            try {
                if (userId != null && contacts.containsKey(userId)) {
                    var c = contacts.get(userId);
                    publisherName = (c.nombre() != null ? c.nombre() : "") + (c.apellido() != null ? " " + c.apellido() : "");
                }
                if (reporterId != null && contacts.containsKey(reporterId)) {
                    var c2 = contacts.get(reporterId);
                    reporterName = (c2.nombre() != null ? c2.nombre() : "") + (c2.apellido() != null ? " " + c2.apellido() : "");
                }
            } catch (Exception ignored) {
            }

            return new ReportedPublicationDto(reportId, userId, petId, petName, tipo, fechaCreacion, count, last, imagenUrl, thumbnailUrl, publisherName, reporterName, reporterRazon);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportedPublicationDto> listHiddenPublications() {
        String sql = "select r.id as reportId, r.user_id as userId, r.pet_id as petId, p.nombre as petName, r.tipo_reporte as tipoReporte, r.fecha_creacion as fechaCreacion, " +
            "count(rp.*) as reportCount, max(rp.fecha_creacion) as lastReportAt, img.imagen_url as imagenUrl, img.thumbnail_url as thumbnailUrl, " +
            "rp_l.user_id as reporterId, rp_l.razon as reporterRazon " +
            "from reports r join report_publications rp on rp.report_id = r.id " +
            "left join pets p on p.id = r.pet_id " +
            "left join lateral (select i.imagen_url, i.thumbnail_url from imagen i where i.id_reporte = r.id order by i.creado_en desc limit 1) img on true " +
            "left join lateral (select rp2.user_id, rp2.razon from report_publications rp2 where rp2.report_id = r.id order by rp2.fecha_creacion desc limit 1) rp_l on true " +
            "where r.oculto = true " +
            "group by r.id, r.user_id, r.pet_id, p.nombre, r.tipo_reporte, r.fecha_creacion, img.imagen_url, img.thumbnail_url, rp_l.user_id, rp_l.razon " +
            "order by lastReportAt desc";

        var q = em.createNativeQuery(sql);
        List<?> rawResults = q.getResultList();

        var intermediate = rawResults.stream().map(r -> (Object[]) r).map(row -> {
            java.util.UUID reportId = (java.util.UUID) row[0];
            java.util.UUID userId = (java.util.UUID) row[1];
            java.util.UUID petId = (java.util.UUID) row[2];
            String petName = row[3] != null ? (String) row[3] : null;
            String tipo = (String) row[4];

            Instant fechaCreacion = null;
            Object fechaObj = row[5];
            if (fechaObj != null) {
                if (fechaObj instanceof java.time.Instant) {
                    fechaCreacion = (java.time.Instant) fechaObj;
                } else if (fechaObj instanceof java.sql.Timestamp) {
                    fechaCreacion = ((java.sql.Timestamp) fechaObj).toInstant();
                } else if (fechaObj instanceof java.util.Date) {
                    fechaCreacion = ((java.util.Date) fechaObj).toInstant();
                }
            }

            long count = ((Number) row[6]).longValue();

            Instant last = null;
            Object lastObj = row[7];
            if (lastObj != null) {
                if (lastObj instanceof java.time.Instant) {
                    last = (java.time.Instant) lastObj;
                } else if (lastObj instanceof java.sql.Timestamp) {
                    last = ((java.sql.Timestamp) lastObj).toInstant();
                } else if (lastObj instanceof java.util.Date) {
                    last = ((java.util.Date) lastObj).toInstant();
                }
            }

            String imagenUrl = row[8] != null ? (String) row[8] : null;
            String thumbnailUrl = row[9] != null ? (String) row[9] : null;

            java.util.UUID reporterId = row[10] != null ? (java.util.UUID) row[10] : null;
            String reporterRazon = row[11] != null ? (String) row[11] : null;

            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("reportId", reportId);
            item.put("userId", userId);
            item.put("petId", petId);
            item.put("petName", petName);
            item.put("tipo", tipo);
            item.put("fechaCreacion", fechaCreacion);
            item.put("count", count);
            item.put("last", last);
            item.put("imagenUrl", imagenUrl);
            item.put("thumbnailUrl", thumbnailUrl);
            item.put("reporterId", reporterId);
            item.put("reporterRazon", reporterRazon);
            return item;
        }).collect(Collectors.toList());

        var userIds = intermediate.stream()
            .flatMap(m1 -> java.util.stream.Stream.of((java.util.UUID) m1.get("userId"), (java.util.UUID) m1.get("reporterId")))
            .filter(id -> id != null)
            .collect(Collectors.toSet())
            .stream().toList();

        java.util.Map<java.util.UUID, com.redpatitas.redPatitas.dto.response.ContactInfoResponse> contacts = new java.util.HashMap<>();
        try {
            var fetched = authServiceClient.getBatchContactInfo(userIds);
            if (fetched != null) contacts.putAll(fetched);
        } catch (Exception ignored) {}

        return intermediate.stream().map(m1 -> {
            java.util.UUID reportId = (java.util.UUID) m1.get("reportId");
            java.util.UUID userId = (java.util.UUID) m1.get("userId");
            java.util.UUID petId = (java.util.UUID) m1.get("petId");
            String petName = (String) m1.get("petName");
            String tipo = (String) m1.get("tipo");
            Instant fechaCreacion = (Instant) m1.get("fechaCreacion");
            long count = m1.get("count") != null ? ((Number) m1.get("count")).longValue() : 0L;
            Instant last = (Instant) m1.get("last");
            String imagenUrl = (String) m1.get("imagenUrl");
            String thumbnailUrl = (String) m1.get("thumbnailUrl");
            java.util.UUID reporterId = (java.util.UUID) m1.get("reporterId");
            String reporterRazon = (String) m1.get("reporterRazon");

            String publisherName = null;
            String reporterName = null;
            try {
                if (userId != null && contacts.containsKey(userId)) {
                    var c = contacts.get(userId);
                    publisherName = (c.nombre() != null ? c.nombre() : "") + (c.apellido() != null ? " " + c.apellido() : "");
                }
                if (reporterId != null && contacts.containsKey(reporterId)) {
                    var c2 = contacts.get(reporterId);
                    reporterName = (c2.nombre() != null ? c2.nombre() : "") + (c2.apellido() != null ? " " + c2.apellido() : "");
                }
            } catch (Exception ignored) {}

            return new ReportedPublicationDto(reportId, userId, petId, petName, tipo, fechaCreacion, count, last, imagenUrl, thumbnailUrl, publisherName, reporterName, reporterRazon);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportedPublicationDto> listDeletedPublications() {
        String sql = "select r.id as reportId, r.user_id as userId, r.pet_id as petId, p.nombre as petName, r.tipo_reporte as tipoReporte, r.fecha_creacion as fechaCreacion, " +
            "count(rp.*) as reportCount, max(rp.fecha_creacion) as lastReportAt, img.imagen_url as imagenUrl, img.thumbnail_url as thumbnailUrl, " +
            "rp_l.user_id as reporterId, rp_l.razon as reporterRazon " +
            "from reports r join report_publications rp on rp.report_id = r.id " +
            "left join pets p on p.id = r.pet_id " +
            "left join lateral (select i.imagen_url, i.thumbnail_url from imagen i where i.id_reporte = r.id order by i.creado_en desc limit 1) img on true " +
            "left join lateral (select rp2.user_id, rp2.razon from report_publications rp2 where rp2.report_id = r.id order by rp2.fecha_creacion desc limit 1) rp_l on true " +
            "where r.eliminado = true " +
            "group by r.id, r.user_id, r.pet_id, p.nombre, r.tipo_reporte, r.fecha_creacion, img.imagen_url, img.thumbnail_url, rp_l.user_id, rp_l.razon " +
            "order by lastReportAt desc";

        var q = em.createNativeQuery(sql);
        List<?> rawResults = q.getResultList();

        var intermediate = rawResults.stream().map(r -> (Object[]) r).map(row -> {
            java.util.UUID reportId = (java.util.UUID) row[0];
            java.util.UUID userId = (java.util.UUID) row[1];
            java.util.UUID petId = (java.util.UUID) row[2];
            String petName = row[3] != null ? (String) row[3] : null;
            String tipo = (String) row[4];

            Instant fechaCreacion = null;
            Object fechaObj = row[5];
            if (fechaObj != null) {
                if (fechaObj instanceof java.time.Instant) {
                    fechaCreacion = (java.time.Instant) fechaObj;
                } else if (fechaObj instanceof java.sql.Timestamp) {
                    fechaCreacion = ((java.sql.Timestamp) fechaObj).toInstant();
                } else if (fechaObj instanceof java.util.Date) {
                    fechaCreacion = ((java.util.Date) fechaObj).toInstant();
                }
            }

            long count = ((Number) row[6]).longValue();

            Instant last = null;
            Object lastObj = row[7];
            if (lastObj != null) {
                if (lastObj instanceof java.time.Instant) {
                    last = (java.time.Instant) lastObj;
                } else if (lastObj instanceof java.sql.Timestamp) {
                    last = ((java.sql.Timestamp) lastObj).toInstant();
                } else if (lastObj instanceof java.util.Date) {
                    last = ((java.util.Date) lastObj).toInstant();
                }
            }

            String imagenUrl = row[8] != null ? (String) row[8] : null;
            String thumbnailUrl = row[9] != null ? (String) row[9] : null;

            java.util.UUID reporterId = row[10] != null ? (java.util.UUID) row[10] : null;
            String reporterRazon = row[11] != null ? (String) row[11] : null;

            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("reportId", reportId);
            item.put("userId", userId);
            item.put("petId", petId);
            item.put("petName", petName);
            item.put("tipo", tipo);
            item.put("fechaCreacion", fechaCreacion);
            item.put("count", count);
            item.put("last", last);
            item.put("imagenUrl", imagenUrl);
            item.put("thumbnailUrl", thumbnailUrl);
            item.put("reporterId", reporterId);
            item.put("reporterRazon", reporterRazon);
            return item;
        }).collect(Collectors.toList());

        var userIds = intermediate.stream()
            .flatMap(m1 -> java.util.stream.Stream.of((java.util.UUID) m1.get("userId"), (java.util.UUID) m1.get("reporterId")))
            .filter(id -> id != null)
            .collect(Collectors.toSet())
            .stream().toList();

        java.util.Map<java.util.UUID, com.redpatitas.redPatitas.dto.response.ContactInfoResponse> contacts = new java.util.HashMap<>();
        try {
            var fetched = authServiceClient.getBatchContactInfo(userIds);
            if (fetched != null) contacts.putAll(fetched);
        } catch (Exception ignored) {}

        return intermediate.stream().map(m1 -> {
            java.util.UUID reportId = (java.util.UUID) m1.get("reportId");
            java.util.UUID userId = (java.util.UUID) m1.get("userId");
            java.util.UUID petId = (java.util.UUID) m1.get("petId");
            String petName = (String) m1.get("petName");
            String tipo = (String) m1.get("tipo");
            Instant fechaCreacion = (Instant) m1.get("fechaCreacion");
            long count = m1.get("count") != null ? ((Number) m1.get("count")).longValue() : 0L;
            Instant last = (Instant) m1.get("last");
            String imagenUrl = (String) m1.get("imagenUrl");
            String thumbnailUrl = (String) m1.get("thumbnailUrl");
            java.util.UUID reporterId = (java.util.UUID) m1.get("reporterId");
            String reporterRazon = (String) m1.get("reporterRazon");

            String publisherName = null;
            String reporterName = null;
            try {
                if (userId != null && contacts.containsKey(userId)) {
                    var c = contacts.get(userId);
                    publisherName = (c.nombre() != null ? c.nombre() : "") + (c.apellido() != null ? " " + c.apellido() : "");
                }
                if (reporterId != null && contacts.containsKey(reporterId)) {
                    var c2 = contacts.get(reporterId);
                    reporterName = (c2.nombre() != null ? c2.nombre() : "") + (c2.apellido() != null ? " " + c2.apellido() : "");
                }
            } catch (Exception ignored) {}

            return new ReportedPublicationDto(reportId, userId, petId, petName, tipo, fechaCreacion, count, last, imagenUrl, thumbnailUrl, publisherName, reporterName, reporterRazon);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserMetricsResponse getUserMetrics() {
        try {
            return authServiceClient.getUserMetrics();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user metrics from auth service", e);
        }
    }

    @Override
    @Transactional
    public void blockUser(UUID userId, UUID adminId, String motivo) {
        try {
            authServiceClient.blockUser(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to block user in auth service", e);
        }

        ModerationAction action = ModerationAction.builder()
                .tipoAccion("BLOQUEAR_USUARIO")
                .tipoObjetivo("USUARIO")
                .idObjetivo(userId)
                .realizadoPor(adminId)
                .motivo(motivo != null ? motivo : "")
                .creadoEn(Instant.now())
                .build();
        moderationActionRepository.save(action);
    }

    @Override
    @Transactional
    public void unblockUser(UUID userId, UUID adminId, String motivo) {
        try {
            authServiceClient.unblockUser(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to unblock user in auth service", e);
        }

        ModerationAction action = ModerationAction.builder()
                .tipoAccion("DESBLOQUEAR_USUARIO")
                .tipoObjetivo("USUARIO")
                .idObjetivo(userId)
                .realizadoPor(adminId)
                .motivo(motivo != null ? motivo : "")
                .creadoEn(Instant.now())
                .build();
        moderationActionRepository.save(action);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId, UUID adminId, String motivo) {
        try {
            authServiceClient.deactivateUser(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deactivate user in auth service", e);
        }

        ModerationAction action = ModerationAction.builder()
                .tipoAccion("DESACTIVAR_USUARIO")
                .tipoObjetivo("USUARIO")
                .idObjetivo(userId)
                .realizadoPor(adminId)
                .motivo(motivo != null ? motivo : "")
                .creadoEn(Instant.now())
                .build();
        moderationActionRepository.save(action);
    }

    @Override
    @Transactional
    public void activateUser(UUID userId, UUID adminId, String motivo) {
        try {
            authServiceClient.activateUser(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to activate user in auth service", e);
        }

        ModerationAction action = ModerationAction.builder()
                .tipoAccion("ACTIVAR_USUARIO")
                .tipoObjetivo("USUARIO")
                .idObjetivo(userId)
                .realizadoPor(adminId)
                .motivo(motivo != null ? motivo : "")
                .creadoEn(Instant.now())
                .build();
        moderationActionRepository.save(action);
    }

    @Override
    @Transactional
    //@PreAuthorize("hasRole('ADMIN')")
    public void ocultarPublicacion(UUID reportId, UUID adminId, String motivo) {
    var report = reportRepository.findByIdWithPet(reportId)
        .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
    report.setOculto(true);
    report.setEstado("OCULTADO");
    report.setMensajeResolucion(motivo != null ? "Oculto por admin: " + motivo : "Oculto por admin");
    reportRepository.save(report);

    ModerationAction action = ModerationAction.builder()
        .tipoAccion("OCULTAR_PUBLICACION")
        .tipoObjetivo("REPORTE")
        .idObjetivo(reportId)
        .realizadoPor(adminId)
        .motivo(motivo != null ? motivo : "")
        .creadoEn(Instant.now())
        .build();
    try {
        var insert = em.createNativeQuery("insert into moderation_action (creado_en,id_objetivo,motivo,realizado_por,tipo_accion,tipo_objetivo) values (?,?,?,?,?,?)");
        insert.setParameter(1, java.sql.Timestamp.from(action.getCreadoEn()));
        insert.setParameter(2, action.getIdObjetivo());
        insert.setParameter(3, action.getMotivo());
        insert.setParameter(4, action.getRealizadoPor());
        insert.setParameter(5, action.getTipoAccion());
        insert.setParameter(6, action.getTipoObjetivo());
        insert.executeUpdate();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }

    @Override
    @Transactional
    //@PreAuthorize("hasRole('ADMIN')")
    public void eliminarPublicacion(UUID reportId, UUID adminId, String motivo) {
    var report = reportRepository.findByIdWithPet(reportId)
        .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
    report.setEliminado(true);
    report.setEstado("ELIMINADO");
    report.setFechaResuelta(Instant.now());
    report.setMensajeResolucion(motivo != null ? "Eliminado por admin: " + motivo : "Eliminado por admin");
    reportRepository.save(report);

    ModerationAction action = ModerationAction.builder()
        .tipoAccion("ELIMINAR_PUBLICACION")
        .tipoObjetivo("REPORTE")
        .idObjetivo(reportId)
        .realizadoPor(adminId)
        .motivo(motivo != null ? motivo : "")
        .creadoEn(Instant.now())
        .build();
    try {
        var insert = em.createNativeQuery("insert into moderation_action (creado_en,id_objetivo,motivo,realizado_por,tipo_accion,tipo_objetivo) values (?,?,?,?,?,?)");
        insert.setParameter(1, java.sql.Timestamp.from(action.getCreadoEn()));
        insert.setParameter(2, action.getIdObjetivo());
        insert.setParameter(3, action.getMotivo());
        insert.setParameter(4, action.getRealizadoPor());
        insert.setParameter(5, action.getTipoAccion());
        insert.setParameter(6, action.getTipoObjetivo());
        insert.executeUpdate();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }

    @Override
    @Transactional
    //@PreAuthorize("hasRole('ADMIN')")
    public void ignorarReporte(UUID reportId, UUID adminId, String motivo) {
    var report = reportRepository.findByIdWithPet(reportId)
        .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        // Al ignorar un reporte por parte del admin, la publicación vuelve a estar activa
        report.setEstado("ACTIVO");
        report.setFechaResuelta(null);
        report.setMensajeResolucion(motivo != null ? "Ignorado por admin: " + motivo : "Ignorado por admin");
    reportRepository.save(report);

    ModerationAction action = ModerationAction.builder()
        .tipoAccion("IGNORAR_REPORTE")
        .tipoObjetivo("REPORTE")
        .idObjetivo(reportId)
        .realizadoPor(adminId)
        .motivo(motivo != null ? motivo : "")
        .creadoEn(Instant.now())
        .build();
    try {
        var insert = em.createNativeQuery("insert into moderation_action (creado_en,id_objetivo,motivo,realizado_por,tipo_accion,tipo_objetivo) values (?,?,?,?,?,?)");
        insert.setParameter(1, java.sql.Timestamp.from(action.getCreadoEn()));
        insert.setParameter(2, action.getIdObjetivo());
        insert.setParameter(3, action.getMotivo());
        insert.setParameter(4, action.getRealizadoPor());
        insert.setParameter(5, action.getTipoAccion());
        insert.setParameter(6, action.getTipoObjetivo());
        insert.executeUpdate();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }

    @Override
    @Transactional
    public void restaurarPublicacion(UUID reportId, UUID adminId, String motivo) {
        var report = reportRepository.findByIdWithPet(reportId)
            .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        // Only restore if it was hidden and not deleted
        if (report.isEliminado()) {
            throw new RuntimeException("No se puede restaurar una publicación eliminada");
        }
        report.setOculto(false);
        report.setEstado("ACTIVO");
        report.setFechaResuelta(null);
        report.setMensajeResolucion(motivo != null ? "Restaurado por admin: " + motivo : "Restaurado por admin");
        reportRepository.save(report);

        ModerationAction action = ModerationAction.builder()
            .tipoAccion("RESTAURAR_PUBLICACION")
            .tipoObjetivo("REPORTE")
            .idObjetivo(reportId)
            .realizadoPor(adminId)
            .motivo(motivo != null ? motivo : "")
            .creadoEn(Instant.now())
            .build();
        try {
            var insert = em.createNativeQuery("insert into moderation_action (creado_en,id_objetivo,motivo,realizado_por,tipo_accion,tipo_objetivo) values (?,?,?,?,?,?)");
            insert.setParameter(1, java.sql.Timestamp.from(action.getCreadoEn()));
            insert.setParameter(2, action.getIdObjetivo());
            insert.setParameter(3, action.getMotivo());
            insert.setParameter(4, action.getRealizadoPor());
            insert.setParameter(5, action.getTipoAccion());
            insert.setParameter(6, action.getTipoObjetivo());
            insert.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    //@PreAuthorize("hasRole('ADMIN')")
    public List<ModerationActionDto> listModerationHistory(int page, int size) {
    var p = PageRequest.of(page, size, Sort.by("creadoEn").descending());
    var actions = moderationActionRepository.findAll(p);

    // collect admin IDs who executed actions
    var adminIds = actions.stream()
        .map(ModerationAction::getRealizadoPor)
        .filter(id -> id != null)
        .distinct()
        .toList();

    java.util.Map<java.util.UUID, com.redpatitas.redPatitas.dto.response.ContactInfoResponse> contacts = new java.util.HashMap<>();
    try {
        var fetched = authServiceClient.getBatchContactInfo(adminIds);
        if (fetched != null) contacts.putAll(fetched);
    } catch (Exception ignored) {}

    return actions.stream().map(a -> {
        String actorName = null;
        try {
            var info = contacts.get(a.getRealizadoPor());
            if (info != null) {
                actorName = (info.nombre() != null ? info.nombre() : "") + (info.apellido() != null ? " " + info.apellido() : "");
                if (actorName.isBlank()) actorName = null;
            }
        } catch (Exception ignored) {}

        return new ModerationActionDto(
            a.getId(),
            a.getTipoAccion(),
            a.getTipoObjetivo(),
            a.getIdObjetivo(),
            a.getRealizadoPor(),
            actorName,
            a.getMotivo(),
            a.getCreadoEn()
        );
    }).toList();
    }
}