package de.unijena.cheminf.nplsweb.nplsweb.model;


import javax.persistence.*;

@Entity
@Table(name="molecule_fragment_cpd" ,
        indexes = {
        @Index(name = "IDX1", columnList = "mol_id, fragment_id" ) ,
                @Index(name="IDX2", columnList ="mol_id"),
                @Index(name="IDX3", columnList="fragment_id"),
                @Index(name="IDX4", columnList = "height"),
                @Index(name="IDX5", columnList = "computed_with_sugar")  })
public class MoleculeFragmentCpd {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="mfc_id", nullable = true)
    private Integer mfc_id;


    private Integer mol_id;

    private Integer fragment_id;

    private Integer height;

    private Integer computed_with_sugar;




    public Integer getMol_id() {
        return mol_id;
    }

    public void setMol_id(Integer mol_id) {
        this.mol_id = mol_id;
    }

    public Integer getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(Integer fragment_id) {
        this.fragment_id = fragment_id;
    }

    public Integer getMfc_id() {
        return mfc_id;
    }

    public void setMfc_id(Integer mfc_id) {
        this.mfc_id = mfc_id;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer isComputed_with_sugar() {
        return computed_with_sugar;
    }

    public void setComputed_with_sugar(Integer computed_with_sugar) {
        this.computed_with_sugar = computed_with_sugar;
    }
}
