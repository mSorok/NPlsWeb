/*
 * Copyright (c) 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.unijena.cheminf.nplsweb.nplsweb.model;

import javax.persistence.*;


@Entity
@Table(name="molecule", indexes = {  @Index(name = "IDX1", columnList = "inchikey", unique = true) , @Index(name = "IDX2", columnList = "is_a_NP"), @Index(name = "IDX3", columnList = "mol_id"), @Index(name = "IDX4", columnList = "npl_score")  } )
public class Molecule implements IMolecule{


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer mol_id;

    private Integer is_a_NP;

    private Double npl_score;

    private Double npl_sugar_score;

    private Double npl_noh_score;


    @Column(length = 1200)
    private String inchi;

    @Column(length=30)
    private String inchikey;

    @Column(length = 1200)
    private String smiles;

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



    public Integer getContainsSugar() {
        return containsSugar;
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


    public Double getNpl_noh_score() {
        return npl_noh_score;
    }

    public void setNpl_noh_score(Double npl_noh_score) {
        this.npl_noh_score = npl_noh_score;
    }
}
