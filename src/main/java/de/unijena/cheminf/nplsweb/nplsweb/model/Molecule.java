package de.unijena.cheminf.nplsweb.nplsweb.model;

import javax.persistence.*;


@Entity
@Table(name="molecule", indexes = {  @Index(name = "IDX1", columnList = "inchikey", unique = true) , @Index(name = "IDX2", columnList = "is_a_NP")  } )
public class Molecule implements IMolecule{


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer mol_id;

    private Integer is_a_NP;

    private Double npl_score;

    private Double npl_sugar_score;

    private Double sml_score;

    private Double sml_sugar_score;

    @Column(length = 1200)
    private String inchi;

    @Column(length=30)
    private String inchikey;

    @Column(length = 1200)
    private String smiles;

    private Integer atom_number;

    private Integer sugar_free_atom_number;

    private Integer containsSugar;

    private Integer numberOfRings;

    private String molecularFormula;

    private Integer numberOfCarbons;

    private Integer numberOfOxygens;

    private Integer numberOfNitrogens;

    private Double ratioCsize;

    private Integer numberRepeatedFragments;

    private Double molecularWeight;







    public Integer getId() {
        return mol_id;
    }

    public void setId(Integer mol_id) {
        this.mol_id = mol_id;
    }

    public Integer getIs_a_NP() {
        return is_a_NP;
    }

    public void setIs_a_NP(Integer is_a_NP) {
        this.is_a_NP = is_a_NP;
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

    public String getInchi() {
        return this.inchi;
    }

    public void setInchi(String inchi) {
        this.inchi = inchi;
    }

    public String getInchikey() {
        return inchikey;
    }

    public void setInchikey(String inchikey) {
        this.inchikey = inchikey;
    }

    public String getSmiles() {
        return smiles;
    }

    public void setSmiles(String SMILES) {
        this.smiles = SMILES;
    }

    public Integer isContainsSugar() {
        return containsSugar;
    }

    public void setContainsSugar(Integer containsSugar) {
        this.containsSugar = containsSugar;
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

    public Integer getSugar_free_atom_number() {
        return sugar_free_atom_number;
    }

    public void setSugar_free_atom_number(Integer sugar_free_atom_number) {
        this.sugar_free_atom_number = sugar_free_atom_number;
    }

    public Integer getMol_id() {
        return mol_id;
    }

    public void setMol_id(Integer mol_id) {
        this.mol_id = mol_id;
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
}
