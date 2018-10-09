package de.unijena.cheminf.nplsweb.nplsweb.reader;


import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

@Service
public class ReaderService {


    public Hashtable<String, IAtomContainer> molecules;

    public File molecularFile;

    private boolean acceptFileFormat = false;

    private String submittedFileFormat ;



    public boolean startService(String file){
        this.molecularFile = new File(file);

        System.out.println("\n\n Working on: "+this.molecularFile.getAbsolutePath() + "\n\n");

        this.acceptFileFormat = acceptFile(file);

        if(this.acceptFileFormat){
            return true;
        }
        else{
            return false;
        }
    }





    private boolean acceptFile(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith("sdf") || filename.toLowerCase().contains("sdf".toLowerCase())) {
            this.submittedFileFormat="sdf";
            return true;
        } else if (filename.endsWith("smi")  ||
                filename.toLowerCase().contains("smi".toLowerCase()) ||
                filename.toLowerCase().contains("smiles".toLowerCase()) ||
                filename.toLowerCase().contains("smile".toLowerCase())) {
            this.submittedFileFormat="smi";
            return true;
        } else if (filename.endsWith("json")) {
            return false;
        }
        else if (filename.endsWith("mol")  ||
                filename.toLowerCase().contains("mol".toLowerCase())
                || filename.toLowerCase().contains("molfile".toLowerCase())) {
            this.submittedFileFormat="mol";
            return true;
        }


        return false;
    }


    public void doWorkWithFile(){

        IReader reader = null;
        if(this.submittedFileFormat.equals("mol")){
            reader = new MOLReader();
        }
        else if(this.submittedFileFormat.equals("sdf")){
            reader = new SDFReader();
        }
        else if(this.submittedFileFormat.equals("smi")){
            reader = new SMILESReader();
        }

        this.molecules = reader.readMoleculesFromFile(this.molecularFile);

    }


    public Hashtable<String, IAtomContainer> getMolecules() {
        return molecules;
    }
}
