package com.sitmun.padro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Getter
@Setter
@NoArgsConstructor
public class HabitantesModel {
    private String codigoDomicilio;
    private String distrito;
    private String session;
    private String nombreCompleto;
    private String nombre;
    private String particula1;
    private String apellido1;
    private String particula2;
    private String apellido2;
    private String nombreVia;
    private String numeroDesde;
    private String nombrePlanta;
    private String codigoPlanta;
    private String codigoPostal;
    private String codigoPortal;
    private String codigoPuerta;
    private String referenciaCatastral;
    private String cadenaDomicilio;
    private String cadenaDomicilioCompleta;
    private String codigoHabitante;
    private String letraDesde;
    private TreeMap<String, String> allFields = new TreeMap<>();

    public void setNewField(String key, String value) {
        allFields.put(key, value);
    }

    public List<String> getFieldNames() {
        List<String> resultList = new ArrayList<>();
        resultList.addAll(allFields.keySet());
        return resultList;
    }

    public String getFieldValue(String key) {
        return allFields.get(key);
    }

    public String toJson(boolean withBrackets) {
        String result = "";

        if (withBrackets) {
            result += "{";
        }

        List<String> keys = this.getFieldNames();
        for (int index = 0; index < keys.size(); index++) {
            String columnName = keys.get(index);
            String columnValue = this.getFieldValue(columnName);
            if (index > 0) {
                result += ",";
            }
            result += "\"" + columnName + "\":" + "\"" + columnValue + "\"";
        }

        if (withBrackets) {
            result += "}";
        }
        return result;
    }
}
