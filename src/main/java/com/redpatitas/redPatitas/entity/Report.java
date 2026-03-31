package com.redpatitas.redPatitas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Column(nullable = false)
    private String tipoReporte;

    @Column(nullable = false)
    private String estadoReporte;

    @Column(nullable = false)
    private Instant fechaEvento;

    @Column(nullable = false)
    private Instant fechaCreacion;

    @Column(length = 300)
    private String lugarDesaparicion;

    @Column(precision = 12, scale = 9)
    private BigDecimal latitud;

    @Column(precision = 12, scale = 9)
    private BigDecimal longitud;

    @Column(length = 700)
    private String imagenUrl;

    @Column(length = 700)
    private String thumbnailUrl;
}
