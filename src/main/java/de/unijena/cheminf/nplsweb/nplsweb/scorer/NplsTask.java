package de.unijena.cheminf.nplsweb.nplsweb.scorer;


import de.unijena.cheminf.nplsweb.nplsweb.misc.BeanUtil;
import de.unijena.cheminf.nplsweb.nplsweb.misc.LinearSugars;
import de.unijena.cheminf.nplsweb.nplsweb.model.*;

import org.openscience.cdk.Atom;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class NplsTask implements Runnable{



    @Autowired
    @Transient
    FragmentWithSugarRepository fr;

    @Autowired
    @Transient
    FragmentWithoutSugarRepository fro;

    @Autowired
    @Transient
    UserUploadedMoleculeRepository uumr;

    @Autowired
    @Transient
    UserUploadedMoleculeFragmentCpdRepository uumfcpd;


    private String sessionid;




    ElectronDonation model = ElectronDonation.cdk();
    CycleFinder cycles = Cycles.cdkAromaticSet();
    Aromaticity aromaticity = new Aromaticity(model, cycles);


    final LinearSugars linearSugarChains = LinearSugars.getInstance();


    Hashtable<String,IAtomContainer> moleculesToCompute;


    public int taskid = 0;

    public boolean computeWithSugar = true;
    public boolean computeWithoutSugar = true;


    @Override
    public void run(){

        this.fr =  BeanUtil.getBean(FragmentWithSugarRepository.class);
        this.fro = BeanUtil.getBean(FragmentWithoutSugarRepository.class);
        //this.cpdRepository = BeanUtil.getBean(MoleculeFragmentCpdRepository.class);
        this.uumr = BeanUtil.getBean(UserUploadedMoleculeRepository.class);
        this.uumfcpd = BeanUtil.getBean(UserUploadedMoleculeFragmentCpdRepository.class);

        SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Absolute |
                SmiFlavor.UseAromaticSymbols);

        DepictionGenerator dg = new DepictionGenerator().withSize(120, 120).withAtomColors();

        try{

            for(String keyAc : this.moleculesToCompute.keySet()){

                IAtomContainer ac = this.moleculesToCompute.get(keyAc);

                System.out.println("NPL score computation for one molecule batch");


                UserUploadedMolecule uum = new UserUploadedMolecule();

                uum.setUu_id(ac.getID());

                uum.setSessionid(this.sessionid);

                //uum.setSmiles(smilesGenerator.create(ac));







                uum = uumr.save(uum);





                /**
                 * Computing for molecules without sugar removal
                 */
                int height = 2;

                if(computeWithSugar){

                    List<String> allFragments = generateAtomSignatures(ac, height);
                    Double sugarScoreNP=0.0;
                    Double sugarScoreSM=0.0;

                    for (String f : allFragments) {
                        List<FragmentWithSugar> inDBlist = fr.findBySignatureAndHeight(f, height);
                        if(inDBlist.isEmpty()){
                            //means it's the first time we see this fragment
                            // TODO FIND WHAT TO DO!

                        }
                        else{
                            FragmentWithSugar inDB = inDBlist.get(0);

                            sugarScoreNP+=inDB.getScoreNP();
                            sugarScoreSM+=inDB.getScoreSM();

                            UserUploadedMoleculeFragmentCpd uumFrag = new UserUploadedMoleculeFragmentCpd();
                            uumFrag.setUmol_id(uum.getUmol_id());
                            uumFrag.setUu_id(uum.getUu_id());
                            uumFrag.setSignature(f);
                            uumFrag.setHeight(height);
                            uumFrag.setComputed_with_sugar(1);
                            uumfcpd.save(uumFrag);


                        }

                        Double nplsScoreSugar = sugarScoreNP/ac.getAtomCount();
                        Double smlsScoreSugar = sugarScoreSM/ac.getAtomCount();

                        uum.setNpl_sugar_score(nplsScoreSugar);
                        uum.setSml_sugar_score(smlsScoreSugar);



                    }



                }





                /**
                 * Computing for molecules with sugar removal
                 */
                IAtomContainer sugarlessMolecule = null;
                if(computeWithoutSugar){


                    Double sugarFreeScoreNP=0.0;
                    Double sugarFreeScoreSM=0.0;

                    sugarlessMolecule = removeSugars(ac);



                    if (sugarlessMolecule != null) {
                        // run the fragments computation on it!

                        List<String> allFragments = generateAtomSignatures(sugarlessMolecule, height);
                        Double scoreNP=0.0;
                        Double scoreSM=0.0;

                        if(allFragments != null) {

                            for (String f : allFragments) {

                                List<FragmentWithoutSugar> inDBlist = fro.findBySignatureAndHeight(f, height);

                                if(inDBlist.isEmpty()) {

                                    //TODO new fragment - what to do?


                                }
                                else {
                                    FragmentWithoutSugar inDB = inDBlist.get(0);
                                    scoreNP+=inDB.getScoreNP();
                                    scoreSM+=inDB.getScoreSM();

                                    UserUploadedMoleculeFragmentCpd uumFrag = new UserUploadedMoleculeFragmentCpd();
                                    uumFrag.setUmol_id(uum.getUmol_id());
                                    uumFrag.setUu_id(uum.getUu_id());
                                    uumFrag.setSignature(f);
                                    uumFrag.setHeight(height);
                                    uumFrag.setComputed_with_sugar(0);

                                    uumfcpd.save(uumFrag);

                                }
                            }
                        }

                        sugarFreeScoreNP=scoreNP/sugarlessMolecule.getAtomCount();
                        sugarFreeScoreSM=scoreSM/sugarlessMolecule.getAtomCount();
                    }



                    uum.setNpl_score(sugarFreeScoreNP);
                    uum.setSml_score(sugarFreeScoreSM);


                    sugarlessMolecule = AtomContainerManipulator.copyAndSuppressedHydrogens(sugarlessMolecule);
                    uum.setSugar_free_atom_number(sugarlessMolecule.getAtomCount());

                }







                ac = AtomContainerManipulator.copyAndSuppressedHydrogens(ac);

                dg.depict(ac).writeTo("./molimg/"+uum.getUmol_id()+".png");

                uum.setDepictionLocation("/molimg/"+uum.getUmol_id()+".png");

                uum.setAtom_number(ac.getAtomCount());

                if(uum.getSugar_free_atom_number() == null || uum.getSugar_free_atom_number() == 0){
                    uum.setSugar_free_atom_number(uum.getAtom_number());
                }


                uum.setSmiles(smilesGenerator.create(ac));

                uum.setSugar_free_atom_number(sugarlessMolecule.getAtomCount());

                uumr.save(uum);
            }

        }catch(Exception e){
            e.printStackTrace();
        }


        System.out.println("Task "+taskid+" finished");






    }


    public Hashtable<String,IAtomContainer> getMoleculesToCompute() {
        return moleculesToCompute;
    }

    public void setMoleculesToCompute(Hashtable<String,IAtomContainer> moleculesToCompute) {
        this.moleculesToCompute = moleculesToCompute;
    }

    public List<String> generateAtomSignatures(IAtomContainer atomContainer, Integer height) {

        List<String> atomSignatures = new ArrayList<>();



        //atomContainer = calculateAromaticity(atomContainer);

        if(!atomContainer.isEmpty()) {

            for (IAtom atom : atomContainer.atoms()) {
                try {
                    AtomSignature atomSignature = new AtomSignature(atom, height, atomContainer);
                    atomSignatures.add(atomSignature.toCanonicalString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return atomSignatures;
        }
        else{
            return null;
        }
    }




    private IAtomContainer removeSugars(IAtomContainer molecule){

        try {

            IRingSet ringset = Cycles.sssr(molecule).toRingSet();

            // RING SUGARS
            for (IAtomContainer one_ring : ringset.atomContainers()) {
                try {
                    IMolecularFormula molecularFormula = MolecularFormulaManipulator.getMolecularFormula(one_ring);
                    String formula = MolecularFormulaManipulator.getString(molecularFormula);
                    IBond.Order bondorder = AtomContainerManipulator.getMaximumBondOrder(one_ring);

                    if (formula.equals("C5O") | formula.equals("C4O") | formula.equals("C6O")) {
                        if (IBond.Order.SINGLE.equals(bondorder)) {
                            if (shouldRemoveRing(one_ring, molecule, ringset) == true) {
                                for (IAtom atom : one_ring.atoms()) {
                                    {

                                        molecule.removeAtom(atom);
                                    }
                                }
                            }

                        }
                    }
                }catch(NullPointerException e){
                    return null;
                }
            }
            Map<Object, Object> properties = molecule.getProperties();
            IAtomContainerSet molset = ConnectivityChecker.partitionIntoMolecules(molecule);
            for (int i = 0; i < molset.getAtomContainerCount(); i++) {
                molset.getAtomContainer(i).setProperties(properties);
                int size = molset.getAtomContainer(i).getBondCount();
                if (size >= 5) {
                    if (!linearSugarChains.hasSugarChains(molset.getAtomContainer(i), ringset.getAtomContainerCount())) {

                        return (IAtomContainer) molset.getAtomContainer(i);
                    }
                }
            }
            //
        } catch (NullPointerException e) {
        } catch (CDKException e) {
        }
        return null;

    }





    private boolean shouldRemoveRing(IAtomContainer possibleSugarRing, IAtomContainer molecule, IRingSet sugarRingsSet) {

        boolean shouldRemoveRing = false;
        List<IAtom> allConnectedAtoms = new ArrayList<IAtom>();
        List<IBond> bonds = new ArrayList<IBond>();
        int oxygenAtomCount = 0;

        IRingSet connectedRings = sugarRingsSet.getConnectedRings((IRing) possibleSugarRing);

        /*
         * get bonds to check for bond order of connected atoms in a sugar ring
         *
         */
        for (IAtom atom : possibleSugarRing.atoms()) {
            bonds.addAll(molecule.getConnectedBondsList(atom));
        }

        if (IBond.Order.SINGLE.equals(BondManipulator.getMaximumBondOrder(bonds))
                && connectedRings.getAtomContainerCount() == 0) {

            /*
             * get connected atoms of all atoms in sugar ring to check for glycoside bond
             */
            for (IAtom atom : possibleSugarRing.atoms()) {
                List<IAtom> connectedAtoms = molecule.getConnectedAtomsList(atom);
                allConnectedAtoms.addAll(connectedAtoms);
            }

            for (IAtom connected_atom : allConnectedAtoms) {
                if (!possibleSugarRing.contains(connected_atom)) {
                    if (connected_atom.getSymbol().matches("O")) {
                        oxygenAtomCount++;
                    }
                }
            }
            if (oxygenAtomCount > 0) {
                return true;
            }
        }
        return shouldRemoveRing;
    }


    public boolean isComputeWithSugar() {
        return computeWithSugar;
    }

    public void setComputeWithSugar(boolean computeWithSugar) {
        this.computeWithSugar = computeWithSugar;
    }

    public boolean isComputeWithoutSugar() {
        return computeWithoutSugar;
    }

    public void setComputeWithoutSugar(boolean computeWithoutSugar) {
        this.computeWithoutSugar = computeWithoutSugar;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
}
