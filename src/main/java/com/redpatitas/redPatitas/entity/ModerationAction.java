package com.redpatitas.redPatitas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "moderation_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tipo_accion", nullable = false)
    private String tipoAccion;

    @Column(name = "tipo_objetivo", nullable = false)
    private String tipoObjetivo;

    @Column(name = "id_objetivo", nullable = false)
    private UUID idObjetivo;

    @Column(name = "realizado_por", nullable = false)
    private UUID realizadoPor;

    @Column(name = "motivo", length = 1000)
    private String motivo;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadatos;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
