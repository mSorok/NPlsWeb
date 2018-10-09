package de.unijena.cheminf.nplsweb.nplsweb.reader;

import de.unijena.cheminf.nplsweb.nplsweb.misc.BeanUtil;
import de.unijena.cheminf.nplsweb.nplsweb.misc.MoleculeChecker;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;

public class MOLReader implements IReader {


    Hashtable<String, IAtomContainer> molecules;
    private IteratingSDFReader reader = null;

    MoleculeChecker moleculeChecker;


    public MOLReader(){
        this.molecules = new Hashtable<String, IAtomContainer>();
        moleculeChecker = BeanUtil.getBean(MoleculeChecker.class);

    }




    @Override
    public Hashtable<String, IAtomContainer> readMoleculesFromFile(File file) {

        int count = 1;

        try{

            reader = new IteratingSDFReader(new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
            reader.setSkip(true);

            while (reader.hasNext() && count <= 200) {

                try {
                    IAtomContainer molecule = reader.next();

                    molecule.setProperty("MOL_NUMBER_IN_FILE",  file.getName()+" " + Integer.toString(count) );
                    molecule.setProperty("FILE_ORIGIN", file.getName().replace(".mol", ""));


                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");

                    LocalDate localDate = LocalDate.now();

                    molecule.setProperty("ACQUISITION_DATE", dtf.format(localDate));


                    // **************************
                    // ID workaround
                    String id = "";
                    if (molecule.getID() == "" || molecule.getID() == null) {
                        for (Object p : molecule.getProperties().keySet()) {
                            if (p.toString().toLowerCase().contains("id")) {
                                molecule.setID(molecule.getProperty(p.toString()));
                                id = molecule.getProperty(p.toString());
                            }
                        }
                        if (molecule.getID() == "" || molecule.getID() == null) {
                            molecule.setID(molecule.getProperty("MOL_NUMBER_IN_FILE"));
                            id = molecule.getProperty("MOL_NUMBER_IN_FILE");
                        }


                    }
                    // **************************


                    molecule = moleculeChecker.checkMolecule(molecule);


                    if(molecule != null) {

                        this.molecules.put(id, molecule);
                    }
                    else{
                        this.molecules.put(id, null);
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                count++;

            }






        } catch (IOException ex) {
        System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
        ex.printStackTrace();
    }

        return this.molecules;
    }
}
