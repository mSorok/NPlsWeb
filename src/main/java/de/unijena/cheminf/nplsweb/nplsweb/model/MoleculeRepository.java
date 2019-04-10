package de.unijena.cheminf.nplsweb.nplsweb.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MoleculeRepository extends CrudRepository<Molecule, Integer> {


    List<Molecule> findAll();


    Molecule findByInchikey(String inchikey);

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

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='UEFS'")
    List<Object[]> getNPLSinUEFS();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='HIT'")
    List<Object[]> getNPLSinHIT();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='SANCDB'")
    List<Object[]> getNPLSinSANCDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='AFRODB'")
    List<Object[]> getNPLSinAFRODB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NPACT'")
    List<Object[]> getNPLSinNPACT();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='SELLECKCHEM'")
    List<Object[]> getNPLSinSELLECKCHEM();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='CHEMBL'")
    List<Object[]> getNPLSinCHEMBL();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NUBBE'")
    List<Object[]> getNPLSinNUBBE();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='STREPTOMEDB'")
    List<Object[]> getNPLSinSTREPTOMEDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='PUBCHEM'")
    List<Object[]> getNPLSinPUBCHEM();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NANPDB'")
    List<Object[]> getNPLSinNANPDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='CHEBI'")
    List<Object[]> getNPLSinCHEBI();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NPATLAS'")
    List<Object[]> getNPLSinNPATLAS();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='TCMDB'")
    List<Object[]> getNPLSinTCMDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='IBS'")
    List<Object[]> getNPLSinIBS();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='OLD2012'")
    List<Object[]> getNPLSinOLD2012();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='ZINC' AND status='NP'")
    List<Object[]> getNPLSinZINCNP();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='UNPD'")
    List<Object[]> getNPLSinUNPD();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='SUPERNATURAL'")
    List<Object[]> getNPLSinSUPERNATURAL();



    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE npl_score !=0 AND source='HMDB'")
    List<Object[]> getNPLSinHMDB();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE  npl_score !=0 AND source='DRUGBANK'")
    List<Object[]> getNPLSinDRUGBANK();





    //BY KINGDOM

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND ((source='NPATLAS' AND ori_molecule.ori_mol_id LIKE 'b%')  OR source='STREPTOMEDB' )"  )
    List<Object[]> getNPLSinBACTERIA();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND source='NPATLAS' AND ori_molecule.ori_mol_id LIKE 'f%'")
    List<Object[]> getNPLSinFUNGI();


    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND (source='TCMDB' OR (source='NUBBE' AND ori_molecule.ori_mol_id LIKE 'p%') OR source='AFRODB' OR source='SANCDB' OR source='HIT' OR source='NPACT')")
    List<Object[]> getNPLSinPLANTS();

    //BY SIZE

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule WHERE npl_score !=0 AND molecule.atom_number<30")
    List<Object[]> getNPLSacTranche1();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule  WHERE npl_score !=0 AND molecule.atom_number<35 AND molecule.atom_number>=30")
    List<Object[]> getNPLSacTranche2();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule  WHERE npl_score !=0 AND molecule.atom_number<40 AND molecule.atom_number>=35")
    List<Object[]> getNPLSacTranche3();

    @Query(nativeQuery = true, value = "SELECT npl_score FROM molecule  WHERE npl_score !=0 AND molecule.atom_number>=40")
    List<Object[]> getNPLSacTranche4();



    /* NPLS with sugar */

    @Query(nativeQuery = true, value = "SELECT npl_sugar_score FROM molecule WHERE is_a_np=1 AND npl_score !=0")
    List<Object[]> getNPLSsugarSinNP();

    @Query(nativeQuery = true, value = "SELECT npl_sugar_score FROM molecule WHERE is_a_np=0 AND npl_score !=0")
    List<Object[]> getNPLSsugarSinSM();


    @Query(nativeQuery = true, value = "SELECT MIN(npl_sugar_score) FROM molecule")
    List<Object[]> getMinNPLSugarScore();

    @Query(nativeQuery = true, value = "SELECT MAX(npl_sugar_score) FROM molecule")
    List<Object[]> getMaxNPLSugarScore();



    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE is_a_np=1")
    Integer countAllNP();


    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE is_a_np=0")
    Integer countAllSM();


    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE source= :source AND is_a_np=1")
    Integer countAllNPBySource(@Param("source") String source);



    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1 AND npl_score !=0 AND ((source='NPATLAS' AND ori_molecule.ori_mol_id LIKE 'b%') OR source='STREPTOMEDB' ) ")
    Integer countAllNPInBacteria();


    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1 AND npl_score !=0 AND source='NPATLAS' AND ori_molecule.ori_mol_id LIKE 'f%'")
    Integer countAllNPInFungi();


    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE is_a_np=1  AND npl_score !=0 AND (source='TCMDB' OR (source='NUBBE' AND ori_molecule.ori_mol_id LIKE 'p%') OR source='AFRODB' OR source='SANCDB' OR source='HIT' OR source='NPACT') ")
    Integer countAllNPInPlants();

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule INNER JOIN ori_molecule ON(mol_id = unique_mol_id) WHERE source='DRUGBANK' ")
    Integer countAllDrugbank();


    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE npl_score !=0 AND molecule.atom_number<30")
    Integer countAllTranche1();

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE npl_score !=0 AND molecule.atom_number<35 AND molecule.atom_number>=30")
    Integer countAllTranche2();

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE npl_score !=0 AND molecule.atom_number<40 AND molecule.atom_number>=35")
    Integer countAllTranche3();

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE npl_score !=0 AND molecule.atom_number>=40")
    Integer countAllTranche4();


}
