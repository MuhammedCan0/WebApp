package com.university.notenberechnung_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NotenEingabe {

    // Wir standardisieren auf "grades", akzeptieren aber auch "notenListe"/"notenliste"
    @JsonProperty("grades")
    @JsonAlias({"notenListe", "notenliste"})
    private List<EinzelNote> notenListe;

    public List<EinzelNote> getNotenListe() { return notenListe; }
    public void setNotenListe(List<EinzelNote> notenListe) { this.notenListe = notenListe; }

    public static class EinzelNote {
        private Integer modulId;
        private double note;
        private String createdAt;

        public Integer getModulId() { return modulId; }
        public void setModulId(Integer modulId) { this.modulId = modulId; }

        public double getNote() { return note; }
        public void setNote(double note) { this.note = note; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
