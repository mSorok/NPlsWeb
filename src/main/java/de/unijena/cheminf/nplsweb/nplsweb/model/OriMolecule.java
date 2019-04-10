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
import java.util.Date;


@Entity
@Table(name="ori_molecule" , indexes = {  @Index(name = "IDXI", columnList = "inchikey"  ), @Index(name = "IDX2", columnList = "source"), @Index(name = "IDX3", columnList = "status") } )
public class OriMolecule implements IMolecule{


    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Column(length = 70)
    private String ori_mol_id;

    @Column(length = 20)
    private String source;

    @Column(length=1200)
    private String inchi;

    @Column(length=30)
    private String inchikey;

    @Column(length=1200)
    private String smiles;

    private Integer heavy_atom_number;

    private Integer total_atom_number;

    private String status;

    private Integer unique_mol_id;

    private Date additionDate;


    @PrePersist
    protected void onCreate() {
        additionDate = new Date();
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOri_mol_id() {
        return ori_mol_id;
    }

    public void setOri_mol_id(String ori_mol_id) {
        this.ori_mol_id = ori_mol_id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUnique_mol_id() {
        return unique_mol_id;
    }

    public void setUnique_mol_id(Integer unique_mol_id) {
        this.unique_mol_id = unique_mol_id;
    }

    public Date getAdditionDate() {
        return additionDate;
    }

    public void setAdditionDate(Date additionDate) {
        this.additionDate = additionDate;
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

    public boolean isANP(){
        if(this.status.equals("NP")){
            return true;
        }
        return false;
    }

    public boolean isBIOGENIC(){
        if(this.status.equals("BIOGENIC")){
            return true;
        }
        return false;
    }

    public boolean isASM(){
        if(this.status.equals("SM")){
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return getOri_mol_id()+"  "+getInchi()+"  "+getStatus();

    }

}
