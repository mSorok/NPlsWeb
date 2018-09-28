package de.unijena.cheminf.nplsweb.nplsweb.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class UserUploadedMoleculeFragmentCpd {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="umfc_id", nullable = true)
    private String umfc_id;


    private Integer umol_id;

    private Integer fragment_id;

    private Integer height;

    private Integer computed_with_sugar;


    public String getUmfc_id() {
        return umfc_id;
    }

    public void setUmfc_id(String umfc_id) {
        this.umfc_id = umfc_id;
    }

    public Integer getUmol_id() {
        return umol_id;
    }

    public void setUmol_id(Integer umol_id) {
        this.umol_id = umol_id;
    }

    public Integer getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(Integer fragment_id) {
        this.fragment_id = fragment_id;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getComputed_with_sugar() {
        return computed_with_sugar;
    }

    public void setComputed_with_sugar(Integer computed_with_sugar) {
        this.computed_with_sugar = computed_with_sugar;
    }
}
