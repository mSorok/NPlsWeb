package de.unijena.cheminf.nplsweb.nplsweb.model;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface OriMoleculeRepository extends CrudRepository<OriMolecule, Integer> {
    //implement search by id
    // search by source
    // search by is a NP



    List<OriMolecule> findBySource(String source);

    @Query(nativeQuery = true, value = "SELECT source, ori_mol_id FROM ori_molecule WHERE inchikey=:inchikey")
    List<Object[]> findSourcesByInchikey(@Param("inchikey") String inchikey);

    List<OriMolecule> findByInchikeyAndSource(String inchikey, String source);

    List<OriMolecule> findBySourceAndStatus(String source, String status);


    @Query(nativeQuery = true, value = "SELECT DISTINCT inchikey FROM ori_molecule")
    List<String> findDistinctInchikey();


    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT id) FROM ori_molecule WHERE inchikey= :inchikey")
    Integer countDistinctByInchikey(@Param("inchikey") String inchikey);


    @Query(nativeQuery = true, value="SELECT inchikey, COUNT(DISTINCT id) count FROM ori_molecule WHERE status = 'NP' OR status='SM' GROUP BY inchikey HAVING count >1")
    List<Object[]> findRedundantInchikey();

    @Query(nativeQuery = true, value="SELECT inchikey, COUNT(DISTINCT id) count FROM ori_molecule WHERE status = 'NP' OR status='SM' GROUP BY inchikey HAVING count =1")
    List<Object[]> findUniqueInchikey();



    @Query(nativeQuery = true, value = "SELECT inchikey, COUNT(DISTINCT id) count FROM ori_molecule WHERE source = 'ZINC' GROUP BY inchikey HAVING count >1")
    List<Object[]> findRedundantInchikeyInZinc();


    void deleteAllByInchikeyAndStatusAndSource(String inchikey, String status, String source);

    @Query(nativeQuery = true, value = "SELECT inchikey FROM ori_molecule WHERE source = 'ZINC' AND status= 'NP' GROUP BY inchikey")
    List<Object > findNPInchikeyInZinc();

    @Query(nativeQuery = true, value = "SELECT inchikey FROM ori_molecule WHERE source = 'ZINC' AND status= 'BIOGENIC' GROUP BY inchikey")
    List<Object> findBIOGENICInchikeyInZinc();






}
