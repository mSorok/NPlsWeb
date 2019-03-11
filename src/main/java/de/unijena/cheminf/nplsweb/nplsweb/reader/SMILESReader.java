package de.unijena.cheminf.nplsweb.nplsweb.reader;

import de.unijena.cheminf.nplsweb.nplsweb.misc.BeanUtil;
import de.unijena.cheminf.nplsweb.nplsweb.misc.MoleculeChecker;
import net.sf.jniinchi.INCHI_OPTION;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SMILESReader implements IReader {


    Hashtable<String, IAtomContainer> molecules;
    private LineNumberReader smilesReader;

    MoleculeChecker moleculeChecker;


    public SMILESReader(){
        this.molecules = new Hashtable<String, IAtomContainer>();
        moleculeChecker = BeanUtil.getBean(MoleculeChecker.class);
    }

    @Override
    public Hashtable<String, IAtomContainer> readMoleculesFromFile(File file) {

        int count = 1;

        String line;

        try {
            smilesReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
            System.out.println("SMILES reader creation");


            while ((line = smilesReader.readLine()) != null  && count <= 200) {
                String smiles_names = line;
                if(!line.contains("smiles")) {
                    try {
                        String[] splitted = smiles_names.split("\\s+"); //splitting the canonical smiles format: SMILES \s mol name
                        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

                        IAtomContainer molecule = null;
                        try {
                            molecule = sp.parseSmiles(splitted[0]);


                            molecule.setProperty("MOL_NUMBER_IN_FILE", file.getName()+" " + Integer.toString(count));
                            molecule.setProperty("ID", splitted[1]);
                            molecule.setID(splitted[1]);

                            molecule.setProperty("FILE_ORIGIN", file.getName().replace(".smi", ""));


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




                        } catch (InvalidSmilesException e) {
                            e.printStackTrace();
                            smilesReader.skip(count - 1);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;

                }


            }
            smilesReader.close();



        } catch (IOException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            ex.printStackTrace();
        }

        System.out.println("Number of molecules in file : "+this.molecules.keySet().size());
        return this.molecules;
    }
}
