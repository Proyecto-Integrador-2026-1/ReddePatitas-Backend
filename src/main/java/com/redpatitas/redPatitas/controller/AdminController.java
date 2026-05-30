package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.request.AdminActionRequestDto;
import com.redpatitas.redPatitas.dto.response.ModerationActionDto;
import com.redpatitas.redPatitas.dto.response.ReportedPublicationDto;
import com.redpatitas.redPatitas.dto.response.UserMetricsResponse;
import com.redpatitas.redPatitas.security.JwtPrincipal;
import com.redpatitas.redPatitas.service.interfaces.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(adminService.getMetrics());
    }

    @GetMapping("/user-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserMetricsResponse> getUserMetrics() {
        return ResponseEntity.ok(adminService.getUserMetrics());
    }

    @GetMapping("/reported-publications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportedPublicationDto>> listReported() {
        return ResponseEntity.ok(adminService.listReportedPublications());
    }

    @GetMapping("/hidden-publications")
    public ResponseEntity<List<ReportedPublicationDto>> listHidden() {
        return ResponseEntity.ok(adminService.listHiddenPublications());
    }

    @GetMapping("/deleted-publications")
    public ResponseEntity<List<ReportedPublicationDto>> listDeleted() {
        return ResponseEntity.ok(adminService.listDeletedPublications());
    }

    @PostMapping("/reported-publications/{reportId}/ocultar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ocultarPublicacion(@PathVariable UUID reportId,
                                                   @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.ocultarPublicacion(reportId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reported-publications/{reportId}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarPublicacion(@PathVariable UUID reportId,
                                                     @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.eliminarPublicacion(reportId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reported-publications/{reportId}/ignorar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ignorarReporte(@PathVariable UUID reportId,
                                               @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.ignorarReporte(reportId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reported-publications/{reportId}/restaurar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restaurarPublicacion(@PathVariable UUID reportId,
                                                      @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.restaurarPublicacion(reportId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/moderation-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ModerationActionDto>> moderationHistory(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                        @RequestParam(name = "size", defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.listModerationHistory(page, size));
    }

    @PostMapping("/users/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockUser(@PathVariable("userId") UUID targetUserId,
                                          @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.blockUser(targetUserId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockUser(@PathVariable("userId") UUID targetUserId,
                                            @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.unblockUser(targetUserId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable("userId") UUID targetUserId,
                                               @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.deactivateUser(targetUserId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable("userId") UUID targetUserId,
                                             @RequestBody AdminActionRequestDto body) {
        var principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.activateUser(targetUserId, UUID.fromString(principal.userId()), body.motivo());
        return ResponseEntity.ok().build();
    }
}
