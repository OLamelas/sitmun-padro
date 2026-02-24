package com.sitmun.padro.dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaModel {
    private String idFactura;
    private String dataFactura;
    private String idConcepte;
    private String desConcepte;
    private BigDecimal importFactura;
    private boolean pendent;
}
