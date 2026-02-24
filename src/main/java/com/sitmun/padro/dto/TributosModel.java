package com.sitmun.padro.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TributosModel {
    private List<ConcepteModel> conceptes;
    private List<FacturaModel> factures;
    private String incidencia;
    private String domicili;

    public String toJson(boolean withBrackets) {
        String result = "";
        if (withBrackets) {
            result += "{";
        }

        if (withBrackets) {
            result += "}";
        }
        return result;
    }
}
