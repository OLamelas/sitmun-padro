package com.sitmun.padro.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomiciliosModel {
    private Integer codigoMunicipioREQ;
    private Integer codigoIneEntidadSingularREQ;
    private Integer codigoIneEntidadColectivaREQ;
    private Integer codigoIneNucleoREQ;
    private Integer numeroDesdeREQ;
    private String codigoDomicilio;
    private String codigoIneVia;
    private String nombreVia;
    private String numeroDesde;
    private String letraDesde;
    private String numeroHasta;
    private String letraHasta;
    private String codigoBloque;
    private String codigoPortal;
    private String codigoEscalera;
    private String nombreEscalera;
    private String codigoPlanta;
    private String nombrePlanta;
    private String codigoPuerta;
    private String codigoPostal;
    private String referenciaCatastral;
    private String distrito;
    private String seccion;
    private String cadenaDomicilioCompleta;
    private String cadenaDomicilio;
    private Integer codigoIneNucleo;
    private String nombreNucleo;

    public String toJson(boolean withBrackets) {
        String result = "";
        if (withBrackets) {
            result += "{";
        }
        result += "\"codigoDomicilio\":" + "\"" + this.getCodigoDomicilio() + "\",";
        result += "\"codigoIneVia\":" + "\"" + this.getCodigoIneVia() + "\",";
        result += "\"cadenaDomicilioCompleta\":" + "\"" + this.getCadenaDomicilioCompleta() + "\",";
        result += "\"nombreVia\":" + "\"" + this.getNombreVia() + "\",";
        result += "\"numeroDesde\":" + "\"" + this.getNumeroDesde() + "\",";
        result += "\"codigoPlanta\":" + "\"" + this.getCodigoPlanta() + "\",";
        result += "\"nombrePlanta\":" + "\"" + this.getNombrePlanta() + "\",";
        result += "\"codigoPuerta\":" + "\"" + this.getCodigoPuerta() + "\",";
        result += "\"distrito\":" + "\"" + this.getDistrito() + "\",";
        result += "\"seccion\":" + "\"" + this.getSeccion() + "\",";
        result += "\"cadenaDomicilio\":" + "\"" + this.getCadenaDomicilio() + "\",";
        result += "\"codigoPortal\":" + "\"" + this.getCodigoPortal() + "\",";
        result += "\"codigoEscalera\":" + "\"" + this.getCodigoEscalera() + "\",";
        result += "\"nombreEscalera\":" + "\"" + this.getNombreEscalera() + "\",";
        result += "\"codigoPostal\":" + "\"" + this.getCodigoPostal() + "\",";
        result += "\"referenciaCatastral\":" + "\"" + this.getReferenciaCatastral() + "\"";

        if (withBrackets) {
            result += "}";
        }
        return result;
    }
}
