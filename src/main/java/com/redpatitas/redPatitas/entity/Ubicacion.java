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
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ubicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_ubicacion")
    private UUID idUbicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reporte", nullable = false)
    private Report report;

    @Column(name = "lugar_desaparicion", length = 300)
    private String lugarDesaparicion;

    @Column(name = "latitud", precision = 12, scale = 9)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 12, scale = 9)
    private BigDecimal longitud;

    // geom no se mapea porque la base de datos lo maneja con trigger
}