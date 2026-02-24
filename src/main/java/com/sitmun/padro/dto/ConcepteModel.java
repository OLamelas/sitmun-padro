package com.sitmun.padro.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcepteModel {
    private String idConcepte;
    private String desConcepte;
    private List<TarifaModel> tarifes;
}
