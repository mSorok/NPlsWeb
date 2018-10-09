package de.unijena.cheminf.nplsweb.nplsweb.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository

public interface MoleculeRepository extends CrudRepository<Molecule, Integer> {


    List<Molecule> findAll();


    @Query(nativeQuery = true, value = "SELECT MIN(npl_score) FROM molecule")
    List<Object[]> getMinNPLScore();

    @Query(nativeQuery = true, value = "SELECT MAX(npl_score) FROM molecule")
    List<Object[]> getMaxNPLScore();

    @Query(nativeQuery = true, value = "SELECT MIN(npl_sugar_score) FROM molecule")
    List<Object[]> getMinNPLSugarScore();

    @Query(nativeQuery = true, value = "SELECT MAX(npl_sugar_score) FROM molecule")
    List<Object[]> getMaxNPLSugarScore();

    @Query(nativeQuery = true, value = "SELECT MIN(sml_score) FROM molecule")
    List<Object[]> getMinSMLScore();

    @Query(nativeQuery = true, value = "SELECT MAX(sml_score) FROM molecule")
    List<Object[]> getMaxSMLScore();

    @Query(nativeQuery = true, value = "SELECT MIN(sml_sugar_score) FROM molecule")
    List<Object[]> getMinSMLSugarScore();

    @Query(nativeQuery = true, value = "SELECT MAX(sml_sugar_score) FROM molecule")
    List<Object[]> getMaxSMLSugarScore();


    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE mol_id= :mol_id")
    Integer findAtomCountByMolId(@Param("mol_id") Integer mol_id);


    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule WHERE is_a_np=1")
    List<Object[]> getNPLSinNP();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule WHERE is_a_np=0")
    List<Object[]> getNPLSinSM();

    @Query(nativeQuery = true, value = "SELECT npl_sugar_score FROM molecule WHERE is_a_np=1")
    List<Object[]> getNPLSsugarSinNP();

    @Query(nativeQuery = true, value = "SELECT npl_sugar_score FROM molecule WHERE is_a_np=0")
    List<Object[]> getNPLSsugarSinSM();


    @Query(nativeQuery = true, value = "SELECT sml_score FROM molecule WHERE is_a_np=1")
    List<Object[]> getSMLSinNP();

    @Query(nativeQuery = true, value = "SELECT sml_score FROM molecule WHERE is_a_np=0")
    List<Object[]> getSMLSinSM();

    @Query(nativeQuery = true, value = "SELECT sml_sugar_score FROM molecule WHERE is_a_np=1")
    List<Object[]> getSMLSsugarSinNP();

    @Query(nativeQuery = true, value = "SELECT sml_sugar_score FROM molecule WHERE is_a_np=0")
    List<Object[]> getSMLSsugarSinSM();






}
