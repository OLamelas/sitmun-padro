package com.sitmun.padro.dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViviendaModel {
    private boolean existe;
    private String referenciaCatastral;
    private String codigoMunicipio;
    private String codigoNucleo;
    private String nombreNucleo;
    private String codigoBdmac;
    private String codigoIne;
    private String nombre;
    private String numero;
    private String complemento;
    private String bloque;
    private String cBloque;
    private String escalera;
    private String planta;
    private String puerta;
    private BigDecimal coord_x;
    private BigDecimal coord_y;
    private String url;
    private String referenciaCatastral20;

    public String toJson(boolean withBrackets) {
        String result = "";
        if (withBrackets) {
            result += "{";
        }

        if (this.isExiste()) {
            if (this.getReferenciaCatastral() != null) result += "\"referenciaCatastral\":" + "\"" + this.getReferenciaCatastral() + "\",";
            if (this.getReferenciaCatastral20() != null) result += "\"referenciaCatastral20\":" + "\"" + this.getReferenciaCatastral20() + "\",";
            if (this.getCodigoMunicipio() != null) result += "\"codigoMunicipio\":" + "\"" + this.getCodigoMunicipio() + "\",";
            if (this.getCodigoNucleo() != null) result += "\"codigoNucleo\":" + "\"" + this.getCodigoNucleo() + "\",";
            if (this.getNombreNucleo() != null) result += "\"nombreNucleo\":" + "\"" + this.getNombreNucleo() + "\",";
            if (this.getCodigoBdmac() != null) result += "\"codigoBdmac\":" + "\"" + this.getCodigoBdmac() + "\",";
            if (this.getCodigoIne() != null) result += "\"codigoIne\":" + "\"" + this.getCodigoIne() + "\",";
            if (this.getNombre() != null) result += "\"nombre\":" + "\"" + this.getNombre() + "\",";
            if (this.getNumero() != null) result += "\"numero\":" + "\"" + this.getNumero() + "\",";
            if (this.getComplemento() != null) result += "\"complemento\":" + "\"" + this.getComplemento() + "\",";
            if (this.getBloque() != null) result += "\"bloque\":" + "\"" + this.getBloque() + "\",";
            if (this.getCBloque() != null) result += "\"cBloque\":" + "\"" + this.getCBloque() + "\",";
            if (this.getEscalera() != null) result += "\"escalera\":" + "\"" + this.getEscalera() + "\",";
            if (this.getPlanta() != null) result += "\"planta\":" + "\"" + this.getPlanta() + "\",";
            if (this.getPuerta() != null) result += "\"puerta\":" + "\"" + this.getPuerta() + "\",";
            if (this.getCoord_x() != null) result += "\"x_etrs89\":" + "\"" + this.getCoord_x().toString() + "\",";
            if (this.getCoord_y() != null) result += "\"y_etrs89\":" + "\"" + this.getCoord_y().toString() + "\",";
            if (this.getUrl() != null) result += "\"url\":" + "\"" + this.getUrl() + "\",";

            result += "\"existe\":" + "\"Si\"";
        } else {
            result += "\"existe\":" + "\"No\"";
        }

        if (withBrackets) {
            result += "}";
        }
        return result;
    }
}
