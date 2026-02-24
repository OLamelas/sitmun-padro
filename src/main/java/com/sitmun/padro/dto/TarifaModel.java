package com.sitmun.padro.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaModel {
    private String idTarifa;
    private String desTarifa;
    private String unitats;
}
