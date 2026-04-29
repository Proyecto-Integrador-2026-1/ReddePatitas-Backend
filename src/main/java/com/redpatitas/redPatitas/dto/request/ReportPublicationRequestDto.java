package com.redpatitas.redPatitas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportPublicationRequestDto {
    @NotNull
    private UUID reportId;

    @NotNull
    private String razon; // spam, datos_incorrectos, maltrato, otro

    private String descripcion; // optional detail

}

