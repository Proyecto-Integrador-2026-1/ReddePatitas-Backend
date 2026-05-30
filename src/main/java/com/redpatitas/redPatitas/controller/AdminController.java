package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.response.ReportedPublicationDto;
import com.redpatitas.redPatitas.service.interfaces.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.security.access.prepost.PreAuthorize;

import com.redpatitas.redPatitas.dto.request.AdminActionRequestDto;
import com.redpatitas.redPatitas.dto.response.ModerationActionDto;
import com.redpatitas.redPatitas.dto.response.UserMetricsResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/metrics")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetrics(@RequestHeader(name = "X-User-Id", required = true) String userId) {
        // TODO: validate that `userId` corresponds to an ADMIN via AuthServiceClient
        Map<String, Object> metrics = adminService.getMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/user-metrics")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<
    
    UserMetricsResponse> getUserMetrics(@RequestHeader(name = "X-User-Id", required = true) String userId) {
        // TODO: validate that `userId` corresponds to an ADMIN via AuthServiceClient
        var metrics = adminService.getUserMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/reported-publications")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportedPublicationDto>> listReported(@RequestHeader(name = "X-User-Id", required = true) String userId) {
        // TODO: check admin role for `userId`
        List<ReportedPublicationDto> list = adminService.listReportedPublications();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/hidden-publications")
    
    public ResponseEntity<List<ReportedPublicationDto>> listHidden(@RequestHeader(name = "X-User-Id", required = true) String userId) {
        List<ReportedPublicationDto> list = adminService.listHiddenPublications();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/deleted-publications")
    
    public ResponseEntity<List<ReportedPublicationDto>> listDeleted(@RequestHeader(name = "X-User-Id", required = true) String userId) {
        List<ReportedPublicationDto> list = adminService.listDeletedPublications();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/reported-publications/{reportId}/ocultar")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ocultarPublicacion(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                                   @PathVariable("reportId") UUID reportId,
                                                   @RequestBody AdminActionRequestDto body) {
        // TODO: validar rol ADMIN
        adminService.ocultarPublicacion(reportId, java.util.UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reported-publications/{reportId}/eliminar")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarPublicacion(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                                    @PathVariable("reportId") UUID reportId,
                                                    @RequestBody AdminActionRequestDto body) {
        // TODO: validar rol ADMIN
        adminService.eliminarPublicacion(reportId, java.util.UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reported-publications/{reportId}/ignorar")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ignorarReporte(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                               @PathVariable("reportId") UUID reportId,
                                               @RequestBody AdminActionRequestDto body) {
        // TODO: validar rol ADMIN
        adminService.ignorarReporte(reportId, java.util.UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reported-publications/{reportId}/restaurar")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restaurarPublicacion(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                                     @PathVariable("reportId") UUID reportId,
                                                     @RequestBody AdminActionRequestDto body) {
        // TODO: validar rol ADMIN
        adminService.restaurarPublicacion(reportId, java.util.UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/moderation-history")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ModerationActionDto>> moderationHistory(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                                                        @RequestParam(name = "size", defaultValue = "50") int size) {
        // TODO: validar rol ADMIN
        List<ModerationActionDto> list = adminService.listModerationHistory(page, size);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                          @PathVariable("userId") UUID targetUserId,
                                          @RequestBody AdminActionRequestDto body) {
        adminService.blockUser(targetUserId, UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                            @PathVariable("userId") UUID targetUserId,
                                            @RequestBody AdminActionRequestDto body) {
        adminService.unblockUser(targetUserId, UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                               @PathVariable("userId") UUID targetUserId,
                                               @RequestBody AdminActionRequestDto body) {
        adminService.deactivateUser(targetUserId, UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(@RequestHeader(name = "X-User-Id", required = true) String userId,
                                             @PathVariable("userId") UUID targetUserId,
                                             @RequestBody AdminActionRequestDto body) {
        adminService.activateUser(targetUserId, UUID.fromString(userId), body.motivo());
        return ResponseEntity.ok().build();
    }

}