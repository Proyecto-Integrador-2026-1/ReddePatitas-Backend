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
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conversacion_id")
    private UUID conversacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_report", nullable = false)
    private Report report;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "user_id2")
    private UUID userId2;

    @Column(name = "creado_en")
    private Instant creadoEn;
}
