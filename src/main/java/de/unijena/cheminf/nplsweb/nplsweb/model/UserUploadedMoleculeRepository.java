package de.unijena.cheminf.nplsweb.nplsweb.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserUploadedMoleculeRepository extends CrudRepository<UserUploadedMolecule, Integer> {


    List<UserUploadedMolecule> findAllBySessionid(String sessionid);



}
