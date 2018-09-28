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

    private Integer atom_number;

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

    public Integer getAtom_number() {
        return atom_number;
    }

    public void setAtom_number(Integer atom_number) {
        this.atom_number = atom_number;
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
