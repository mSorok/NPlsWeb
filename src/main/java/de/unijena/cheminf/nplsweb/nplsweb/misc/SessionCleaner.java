package de.unijena.cheminf.nplsweb.nplsweb.misc;


import de.unijena.cheminf.nplsweb.nplsweb.model.UserUploadedMolecule;
import de.unijena.cheminf.nplsweb.nplsweb.model.UserUploadedMoleculeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionCleaner {



    @Autowired
    UserUploadedMoleculeRepository uumr;


    public void clearSession(String sessionId){

        List<UserUploadedMolecule> uums = uumr.findAllBySessionid(sessionId);
        for(UserUploadedMolecule mol : uums){
            mol.setSessionid("deleted$"+sessionId);
            uumr.save(mol);
        }

    }


}
