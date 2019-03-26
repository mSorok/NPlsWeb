package de.unijena.cheminf.nplsweb.nplsweb.model;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="user_uploaded_molecule" , indexes = {  @Index(name = "IDXI", columnList = "inchikey"  )})
public class UserUploadedMolecule {


    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer umol_id;

    private String uu_id;

    private String sessionid;

    @Column(length=1200)
    private String smiles;

    private String inchikey;

    private Date submissionDate;


    private Integer is_in_any_source;

    private String sources;

    private Integer addedToMolecule;

    private String depictionLocation;


    private Double npl_score;

    private Double npl_sugar_score;

    private Double npl_noh_score;

    private Integer heavy_atom_number;

    private Integer total_atom_number;

    private Integer sugar_free_total_atom_number;

    private Integer sugar_free_heavy_atom_number;



    private Integer containsSugar;

    private Integer numberOfRings;

    private String molecularFormula;

    private Integer numberOfCarbons;

    private Integer numberOfOxygens;

    private Integer numberOfNitrogens;

    private Double ratioCsize;

    private Integer numberRepeatedFragments;

    private Double molecularWeight;

    @Column(length=1200)
    private String unknown_fragments ;








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

    public Integer getHeavy_atom_number() {
        return heavy_atom_number;
    }

    public void setHeavy_atom_number(Integer heavy_atom_number) {
        this.heavy_atom_number = heavy_atom_number;
    }

    public Integer getTotal_atom_number() {
        return total_atom_number;
    }

    public void setTotal_atom_number(Integer total_atom_number) {
        this.total_atom_number = total_atom_number;
    }

    public Integer getSugar_free_total_atom_number() {
        return sugar_free_total_atom_number;
    }

    public void setSugar_free_total_atom_number(Integer sugar_free_total_atom_number) {
        this.sugar_free_total_atom_number = sugar_free_total_atom_number;
    }

    public Integer getSugar_free_heavy_atom_number() {
        return sugar_free_heavy_atom_number;
    }

    public void setSugar_free_heavy_atom_number(Integer sugar_free_heavy_atom_number) {
        this.sugar_free_heavy_atom_number = sugar_free_heavy_atom_number;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getUu_id() {
        return uu_id;
    }

    public void setUu_id(String uu_id) {
        this.uu_id = uu_id;
    }

    public String getDepictionLocation() {
        return depictionLocation;
    }

    public void setDepictionLocation(String depictionLocation) {
        this.depictionLocation = depictionLocation;
    }


    public Integer getIs_in_any_source() {
        return is_in_any_source;
    }

    public void setIs_in_any_source(Integer is_in_any_source) {
        this.is_in_any_source = is_in_any_source;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String getInchikey() {
        return inchikey;
    }

    public void setInchikey(String inchikey) {
        this.inchikey = inchikey;
    }

    public Double getNpl_noh_score() {
        return npl_noh_score;
    }

    public void setNpl_noh_score(Double npl_noh_score) {
        this.npl_noh_score = npl_noh_score;
    }

    public Integer getContainsSugar() {
        return containsSugar;
    }

    public void setContainsSugar(Integer containsSugar) {
        this.containsSugar = containsSugar;
    }

    public Integer getNumberOfRings() {
        return numberOfRings;
    }

    public void setNumberOfRings(Integer numberOfRings) {
        this.numberOfRings = numberOfRings;
    }

    public String getMolecularFormula() {
        return molecularFormula;
    }

    public void setMolecularFormula(String molecularFormula) {
        this.molecularFormula = molecularFormula;
    }

    public Integer getNumberOfCarbons() {
        return numberOfCarbons;
    }

    public void setNumberOfCarbons(Integer numberOfCarbons) {
        this.numberOfCarbons = numberOfCarbons;
    }

    public Integer getNumberOfOxygens() {
        return numberOfOxygens;
    }

    public void setNumberOfOxygens(Integer numberOfOxygens) {
        this.numberOfOxygens = numberOfOxygens;
    }

    public Integer getNumberOfNitrogens() {
        return numberOfNitrogens;
    }

    public void setNumberOfNitrogens(Integer numberOfNitrogens) {
        this.numberOfNitrogens = numberOfNitrogens;
    }

    public Double getRatioCsize() {
        return ratioCsize;
    }

    public void setRatioCsize(Double ratioCsize) {
        this.ratioCsize = ratioCsize;
    }

    public Integer getNumberRepeatedFragments() {
        return numberRepeatedFragments;
    }

    public void setNumberRepeatedFragments(Integer numberRepeatedFragments) {
        this.numberRepeatedFragments = numberRepeatedFragments;
    }

    public Double getMolecularWeight() {
        return molecularWeight;
    }

    public void setMolecularWeight(Double molecularWeight) {
        this.molecularWeight = molecularWeight;
    }

    public String getUnknown_fragments() {
        return unknown_fragments;
    }

    public void setUnknown_fragments(String unknown_fragments) {
        this.unknown_fragments = unknown_fragments;
    }
}
