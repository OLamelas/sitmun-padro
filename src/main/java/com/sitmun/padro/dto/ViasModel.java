package com.sitmun.padro.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViasModel {
    private Integer codigoMunicipioREQ;
    private Integer codigoVia;
    private Integer codigoIneVia;
    private String nombreVia;
    private String situacionVia;
    private String codigoTipoVia;
    private String nombreTipoVia;
    private Integer codigoIneNucleo;
    private Integer codigoNucleo;
    private String nombreNucleo;
}
