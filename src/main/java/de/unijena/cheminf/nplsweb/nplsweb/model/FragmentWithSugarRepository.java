package de.unijena.cheminf.nplsweb.nplsweb.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FragmentWithSugarRepository extends CrudRepository<FragmentWithSugar, Integer> {

    List<FragmentWithSugar> findAll();

    List<FragmentWithSugar> findBySignatureAndHeight(String atom_signature, Integer height);

    List<FragmentWithSugar> findAllByHeight(Integer height);

    @Query(nativeQuery = true, value = " SELECT signature, GROUP_CONCAT(fragment_id SEPARATOR ' '), count(*) nb FROM  fragment_with_sugar GROUP BY signature HAVING nb>1 ")
    List<Object[]> findRedundantSignatures(@Param("height") Integer height);



    @Override
    void deleteById(Integer integer);

}
