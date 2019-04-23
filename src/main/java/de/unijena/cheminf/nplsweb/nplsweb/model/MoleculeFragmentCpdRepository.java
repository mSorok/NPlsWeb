package de.unijena.cheminf.nplsweb.nplsweb.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MoleculeFragmentCpdRepository  extends CrudRepository<MoleculeFragmentCpd, Integer> {

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar  ")
    List<Object[]> countDistinctMoleculesByFragmentIdAndHeightAndAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar AND is_a_np=1 ")
    List<Object[]> countDistinctNPMoleculesByFragmentIdAndHeightAndAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar AND is_a_np=0 ")
    List<Object[]> countDistinctSMMoleculesByFragmentIdAndHeightAndAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);


    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd WHERE height= :height AND computed_with_sugar=1")
    List<Object[]> countTotalMoleculesWithSugar(@Param("height") Integer height);

    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd WHERE height= :height AND computed_with_sugar=0")
    List<Object[]> countTotalMoleculesWithoutSugar(@Param("height") Integer height);

    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE is_a_np=1")
    List<Object[]> countTotalNPMolecules();

    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE is_a_np=0")
    List<Object[]> countTotalSMMolecules();

    @Query(nativeQuery = true, value="SELECT * FROM molecule_fragment_cpd WHERE fragment_id= :fragment_id")
    List<MoleculeFragmentCpd> findByfragment_id(@Param("fragment_id") Integer fragment_id);


    @Query(nativeQuery = true, value="SELECT f.fragment_id, f.scorenp " +
            "FROM molecule_fragment_cpd cpd INNER JOIN fragment_with_sugar f USING(fragment_id) " +
            "WHERE cpd.computed_with_sugar=1 AND f.height= :height AND cpd.mol_id= :mol_id")
    List<Object[]> findAllSugarFragmentsByMolid(@Param("mol_id") Integer mol_id, @Param("height") Integer height);

    @Query(nativeQuery = true, value="SELECT f.fragment_id, f.scorenp " +
            "FROM molecule_fragment_cpd cpd INNER JOIN fragment_without_sugar f USING(fragment_id) " +
            "WHERE cpd.computed_with_sugar=0 AND f.height= :height AND cpd.mol_id= :mol_id")
    List<Object[]> findAllSugarfreeFragmentsByMolid(@Param("mol_id") Integer mol_id, @Param("height") Integer height);




}
