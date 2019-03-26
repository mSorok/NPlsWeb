package de.unijena.cheminf.nplsweb.nplsweb.misc;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class MoleculeChecker {



    private final String[] check = {"C", "H", "N", "O", "P", "S", "Cl", "F", "As", "Se", "Br", "I", "B"};
    private final HashSet<String> symbols2Check = new HashSet<String>(Arrays.asList(check));


    MoleculeConnectivityChecker mcc;





    public IAtomContainer checkMolecule(IAtomContainer molecule){

        mcc = BeanUtil.getBean(MoleculeConnectivityChecker.class);

        if(!containsStrangeElements(molecule)) {

            /**
             * Checking for connectivity and selecting the biggest component
             */

            List<IAtomContainer> listAC = mcc.checkConnectivity(molecule);
            if( listAC.size()>1 ){
                IAtomContainer biggestComponent = listAC.get(0);
                for(IAtomContainer partac : listAC){
                    if(partac.getAtomCount()>biggestComponent.getAtomCount()){
                        biggestComponent = partac;
                    }
                }
                molecule = biggestComponent;
                /*if(molecule.getAtomCount()<=6){
                    return null;
                }*/
            }




            // check ID

            if (molecule.getID() == "" || molecule.getID() == null) {
                for (Object p : molecule.getProperties().keySet()) {

                    if (p.toString().toLowerCase().contains("id")) {
                        molecule.setID(molecule.getProperty(p.toString()));

                    }

                }
                if (molecule.getID() == "" || molecule.getID() == null) {
                    molecule.setID(molecule.getProperty("MOL_NUMBER_IN_FILE"));
                    //this.molecule.setProperty("ID", this.molecule.getProperty("MOL_NUMBER_IN_FILE"));
                }


            }


            //ElectronDonation model = ElectronDonation.cdk();
            //CycleFinder cycles = Cycles.cdkAromaticSet();
            //Aromaticity aromaticity = new Aromaticity(model, cycles);



            //Remove aromaticity


            String smi;
            SmilesGenerator sg = new SmilesGenerator(SmiFlavor.Unique);
            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            Map<Object, Object> properties = molecule.getProperties();
            String id = molecule.getID();
            try {
                smi = sg.create(molecule);
                molecule = sp.parseSmiles(smi);
                molecule.setProperties(properties);
                molecule.setID(id);
            } catch (CDKException e) {
                e.printStackTrace();
            }



            //Homogenize pseudo atoms - all pseudo atoms (PA) as a "*"
            for (int u = 1; u < molecule.getAtomCount(); u++) {
                if (molecule.getAtom(u) instanceof IPseudoAtom) {

                    molecule.getAtom(u).setSymbol("*");
                    molecule.getAtom(u).setAtomTypeName("X");
                    ((IPseudoAtom) molecule.getAtom(u)).setLabel("*");

                }
            }


            // Addition of implicit hydrogens & atom typer
            CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molecule.getBuilder());
            for (int j = 0; j < molecule.getAtomCount(); j++) {
                IAtom atom = molecule.getAtom(j);
                IAtomType type = null;
                try {
                    type = matcher.findMatchingAtomType(molecule, atom);
                } catch (CDKException e) {
                    e.printStackTrace();
                }
                AtomTypeManipulator.configure(atom, type);
            }
            CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());

            try {
                adder.addImplicitHydrogens(molecule);
            } catch (CDKException e) {
                e.printStackTrace();
            }

            AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);


            /*
            //Adding aromaticity to molecules when needed
            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
                AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(molecule);
                aromaticity.apply(molecule);
            } catch (CDKException e) {
                e.printStackTrace();
            }

            */




            //Fixing molecular bonds
            try {
                Kekulization.kekulize(molecule);

            } catch (CDKException e1) {
                //e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                //System.out.println("Could not kekulize molecule "+ this.molecule.getID());
            }




            return molecule;
        }
        return null;
    }















    private boolean containsStrangeElements(IAtomContainer molecule) {
        if(molecule.getAtomCount()>0) {
            for (IAtom atom : molecule.atoms()) {
                if (!symbols2Check.contains(atom.getSymbol())) {
                    System.out.println("contains strange");
                    return true;
                }
            }
        }
        return false;
    }


}
