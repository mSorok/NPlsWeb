package de.unijena.cheminf.nplsweb.nplsweb.model;

import javax.persistence.*;

@Entity
public class UserUploadedMoleculeFragmentCpd {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer umfc_id;


    private Integer umol_id; // automatic mol id - is unique

    private String uu_id; //user submitted mol id - human-readable - can be non-unique

    private Integer fragment_id;

    @Column(length = 300)
    private String signature;

    private Integer height;

    private Integer computed_with_sugar;






    public Integer getUmfc_id() {
        return umfc_id;
    }

    public void setUmfc_id(Integer umfc_id) {
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


    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getUu_id() {
        return uu_id;
    }

    public void setUu_id(String uu_id) {
        this.uu_id = uu_id;
    }
}
