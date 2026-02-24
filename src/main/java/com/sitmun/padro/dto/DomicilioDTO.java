package com.sitmun.padro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para representar un domicilio.
 * Utilizado como ejemplo de mapeo con MapStruct.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioDTO {
    private String id;
    private String via;
    private String numero;
    private String codigoPostal;
}

