package de.unijena.cheminf.nplsweb.nplsweb.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository

public interface MoleculeRepository extends CrudRepository<Molecule, Integer> {


    List<Molecule> findAll();








    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE mol_id= :mol_id")
    Integer findAtomCountByMolId(@Param("mol_id") Integer mol_id);




    /* NPLS without sugar (classic NPLS) */

    @Query(nativeQuery = true, value = "SELECT MIN(npl_score) FROM molecule")
    List<Object[]> getMinNPLScore();

    @Query(nativeQuery = true, value = "SELECT MAX(npl_score) FROM molecule")
    List<Object[]> getMaxNPLScore();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(unique_mol_id=mol_id) WHERE is_a_np=1 AND npl_score !=0 ")
    List<Object[]> getNPLSinNP();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule WHERE is_a_np=0  AND npl_score !=0")
    List<Object[]> getNPLSinSM();

    // BY DATABASE
    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='CHEBI'")
    List<Object[]> getNPLSinCHEBI();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='TCMDB'")
    List<Object[]> getNPLSinTCMDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='ZINC' AND status='NP'")
    List<Object[]> getNPLSinZINCNP();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='PUBCHEM'")
    List<Object[]> getNPLSinPUBCHEM();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='CHEMBL'")
    List<Object[]> getNPLSinCHEMBL();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NPATLAS'")
    List<Object[]> getNPLSinNPATLAS();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NUBBE'")
    List<Object[]> getNPLSinNUBBE();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='SANCDB'")
    List<Object[]> getNPLSinSANCDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='SUPERNATURAL'")
    List<Object[]> getNPLSinSUPERNATURAL();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='AFRODB'")
    List<Object[]> getNPLSinAFRODB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='OLD2012'")
    List<Object[]> getNPLSinOLD2012();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE npl_score !=0 AND source='HMDB'")
    List<Object[]> getNPLSinHMDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE  npl_score !=0 AND source='DRUGBANK'")
    List<Object[]> getNPLSinDRUGBANK();


    //BY KINGDOM

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NPATLAS' AND ori_molecule.id LIKE 'b%'")
    List<Object[]> getNPLSinBACTERIA();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NPATLAS' AND ori_molecule.id LIKE 'g%'")
    List<Object[]> getNPLSinFUNGI();


    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND (source='TCMDB' OR source='NUBBE' OR source='AFRODB')")
    List<Object[]> getNPLSinPLANTS();




    /* NPLS with sugar */

    @Query(nativeQuery = true, value = "SELECT npl_sugar_score FROM molecule WHERE is_a_np=1 AND npl_score !=0")
    List<Object[]> getNPLSsugarSinNP();

    @Query(nativeQuery = true, value = "SELECT npl_sugar_score FROM molecule WHERE is_a_np=0 AND npl_score !=0")
    List<Object[]> getNPLSsugarSinSM();


    @Query(nativeQuery = true, value = "SELECT MIN(npl_sugar_score) FROM molecule")
    List<Object[]> getMinNPLSugarScore();

    @Query(nativeQuery = true, value = "SELECT MAX(npl_sugar_score) FROM molecule")
    List<Object[]> getMaxNPLSugarScore();





    /* not useful - SM scores*/
    @Query(nativeQuery = true, value = "SELECT sml_score FROM molecule WHERE is_a_np=1")
    List<Object[]> getSMLSinNP();

    @Query(nativeQuery = true, value = "SELECT sml_score FROM molecule WHERE is_a_np=0")
    List<Object[]> getSMLSinSM();

    @Query(nativeQuery = true, value = "SELECT sml_sugar_score FROM molecule WHERE is_a_np=1")
    List<Object[]> getSMLSsugarSinNP();

    @Query(nativeQuery = true, value = "SELECT sml_sugar_score FROM molecule WHERE is_a_np=0")
    List<Object[]> getSMLSsugarSinSM();


    @Query(nativeQuery = true, value = "SELECT MIN(sml_score) FROM molecule")
    List<Object[]> getMinSMLScore();

    @Query(nativeQuery = true, value = "SELECT MAX(sml_score) FROM molecule")
    List<Object[]> getMaxSMLScore();

    @Query(nativeQuery = true, value = "SELECT MIN(sml_sugar_score) FROM molecule")
    List<Object[]> getMinSMLSugarScore();

    @Query(nativeQuery = true, value = "SELECT MAX(sml_sugar_score) FROM molecule")
    List<Object[]> getMaxSMLSugarScore();





}
