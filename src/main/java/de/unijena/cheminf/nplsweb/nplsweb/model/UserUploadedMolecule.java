package de.unijena.cheminf.nplsweb.nplsweb.model;


import javax.persistence.*;
import java.util.Date;

@Entity
public class UserUploadedMolecule {


    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer umol_id;

    @Column(length=1200)
    private String smiles;

    private Date submissionDate;

    private Integer isInMolecule;

    private Integer addedToMolecule;


    private Double npl_score;

    private Double npl_sugar_score;

    private Double sml_score;

    private Double sml_sugar_score;

    private Integer atom_number;

    private Integer containsSugar;


    @PrePersist
    protected void onCreate() {
        submissionDate = new Date();
    }


    public Integer getUmol_id() {
        return umol_id;
    }

    public void setUmol_id(Integer umol_id) {
        this.umol_id = umol_id;
    }

    public String getSmiles() {
        return smiles;
    }

    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Integer getIsInMolecule() {
        return isInMolecule;
    }

    public void setIsInMolecule(Integer isInMolecule) {
        this.isInMolecule = isInMolecule;
    }

    public Integer getAddedToMolecule() {
        return addedToMolecule;
    }

    public void setAddedToMolecule(Integer addedToMolecule) {
        this.addedToMolecule = addedToMolecule;
    }

    public Double getNpl_score() {
        return npl_score;
    }

    public void setNpl_score(Double npl_score) {
        this.npl_score = npl_score;
    }

    public Double getNpl_sugar_score() {
        return npl_sugar_score;
    }

    public void setNpl_sugar_score(Double npl_sugar_score) {
        this.npl_sugar_score = npl_sugar_score;
    }

    public Double getSml_score() {
        return sml_score;
    }

    public void setSml_score(Double sml_score) {
        this.sml_score = sml_score;
    }

    public Double getSml_sugar_score() {
        return sml_sugar_score;
    }

    public void setSml_sugar_score(Double sml_sugar_score) {
        this.sml_sugar_score = sml_sugar_score;
    }

    public Integer getAtom_number() {
        return atom_number;
    }

    public void setAtom_number(Integer atom_number) {
        this.atom_number = atom_number;
    }

    public Integer getContainsSugar() {
        return containsSugar;
    }

    public void setContainsSugar(Integer containsSugar) {
        this.containsSugar = containsSugar;
    }
}
