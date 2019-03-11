package de.unijena.cheminf.nplsweb.nplsweb.reader;

import de.unijena.cheminf.nplsweb.nplsweb.misc.BeanUtil;
import de.unijena.cheminf.nplsweb.nplsweb.misc.MoleculeChecker;
import net.sf.jniinchi.INCHI_OPTION;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SDFReader implements IReader {


    Hashtable<String, IAtomContainer> molecules;
    private IteratingSDFReader reader = null;

    MoleculeChecker moleculeChecker;


    public SDFReader(){
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

                    molecule.setProperty("MOL_NUMBER_IN_FILE", file.getName()+" " + Integer.toString(count));
                    molecule.setProperty("FILE_ORIGIN", file.getName().replace(".sdf", ""));


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

                        try {
                            List options = new ArrayList();
                            options.add(INCHI_OPTION.SNon);
                            options.add(INCHI_OPTION.ChiralFlagOFF);
                            options.add(INCHI_OPTION.AuxNone);
                            InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule, options );

                            molecule.setProperty("INCHIKEY", gen.getInchiKey());


                        } catch (CDKException e) {
                            Integer totalBonds = molecule.getBondCount();
                            Integer ib = 0;
                            while (ib < totalBonds) {

                                IBond b = molecule.getBond(ib);
                                if (b.getOrder() == IBond.Order.UNSET) {
                                    b.setOrder(IBond.Order.SINGLE);

                                }
                                ib++;
                            }
                            List options = new ArrayList();
                            options.add(INCHI_OPTION.SNon);
                            options.add(INCHI_OPTION.ChiralFlagOFF);
                            options.add(INCHI_OPTION.AuxNone);
                            InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule, options );

                            molecule.setProperty("INCHIKEY", gen.getInchiKey());

                        }

                        this.molecules.put(molecule.getID(), molecule);
                    }
                    else{
                        this.molecules.put(molecule.getID(), null);
                    }






                } catch (Exception ex) {
                    //ex.printStackTrace();
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
