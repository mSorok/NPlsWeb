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
import java.util.*;

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

    @Autowired
    @Transient
    OriMoleculeRepository omr;


    private String sessionid;




    final LinearSugars linearSugarChains = LinearSugars.getInstance();


    Hashtable<String,IAtomContainer> moleculesToCompute;


    public int taskid = 0;

    public boolean computeWithSugar = true;
    public boolean computeWithoutSugar = true;


    @Override
    public void run(){

        this.fr =  BeanUtil.getBean(FragmentWithSugarRepository.class);
        this.fro = BeanUtil.getBean(FragmentWithoutSugarRepository.class);
        this.uumr = BeanUtil.getBean(UserUploadedMoleculeRepository.class);
        this.uumfcpd = BeanUtil.getBean(UserUploadedMoleculeFragmentCpdRepository.class);
        this.omr = BeanUtil.getBean(OriMoleculeRepository.class);

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

                uum.setInchikey(ac.getProperty("INCHIKEY").toString());

                uum.setTotal_atom_number(ac.getAtomCount());

                int heavyAtomCount = 0;
                for(IAtom a : ac.atoms()){
                    if(!a.getSymbol().equals("H")){
                        heavyAtomCount=heavyAtomCount+1;
                    }
                }

                uum.setHeavy_atom_number(heavyAtomCount);

                uum = uumr.save(uum);



                // Computing for molecules WITHOUT sugar removal

                int height = 2;

                if(computeWithSugar){

                    List<String> allFragments = generateAtomSignatures(ac, height);
                    Double sugarScoreNP=0.0;


                    HashSet<String> unknownFragmentsWithSugar = new HashSet<>();


                    for (String f : allFragments) {
                        List<FragmentWithSugar> inDBlist = fr.findBySignatureAndHeight(f, height);
                        if(inDBlist.isEmpty()){
                            //means it's the first time we see this fragment
                            unknownFragmentsWithSugar.add(f);
                            //DO THINGS WITH UNKNOWN FRAGMENT WITH SUGAR

                        }
                        else{
                            FragmentWithSugar inDB = inDBlist.get(0);

                            sugarScoreNP+=inDB.getScoreNP();

                            UserUploadedMoleculeFragmentCpd uumFrag = new UserUploadedMoleculeFragmentCpd();
                            uumFrag.setUmol_id(uum.getUmol_id());
                            uumFrag.setUu_id(uum.getUu_id());
                            uumFrag.setSignature(f);
                            uumFrag.setHeight(height);
                            uumFrag.setComputed_with_sugar(1);
                            uumfcpd.save(uumFrag);


                        }

                        Double nplsScoreSugar = sugarScoreNP/(double)uum.getTotal_atom_number();

                        uum.setNpl_sugar_score(nplsScoreSugar);



                    }



                }



                /**
                 * Computing for molecules with sugar removal
                 */
                IAtomContainer sugarlessMolecule = null;
                if(computeWithoutSugar){


                    Double sugarFreeScoreNP=0.0;
                    Double sugarFreeHfreeScoreNP=0.0;

                    sugarlessMolecule = removeSugars(ac);

                    uum.setSugar_free_total_atom_number(sugarlessMolecule.getAtomCount());


                    int sugarFreeHeavyAtomCount = 0;
                    for(IAtom a : sugarlessMolecule.atoms()){
                        if(!a.getSymbol().equals("H")){
                            sugarFreeHeavyAtomCount=sugarFreeHeavyAtomCount+1;
                        }
                    }
                    uum.setSugar_free_heavy_atom_number(sugarFreeHeavyAtomCount);



                    if (sugarlessMolecule != null) {
                        // run the fragments computation on it!

                        List<String> allFragments = generateAtomSignatures(sugarlessMolecule, height);
                        Double scoreNP=0.0;
                        Double scoreNPnoH= 0.0;

                        HashSet<String> unknownFragments = new HashSet<>();


                        if(allFragments != null) {

                            for (String f : allFragments) {

                                List<FragmentWithoutSugar> inDBlist = fro.findBySignatureAndHeight(f, height);

                                if(inDBlist.isEmpty()) {

                                    // add to unknown fragments to a list to plot

                                    unknownFragments.add(f);


                                }
                                else {
                                    FragmentWithoutSugar inDB = inDBlist.get(0);

                                    scoreNP = scoreNP+ inDB.getScoreNP() ;

                                    UserUploadedMoleculeFragmentCpd uumFrag = new UserUploadedMoleculeFragmentCpd();
                                    uumFrag.setUmol_id(uum.getUmol_id());
                                    uumFrag.setUu_id(uum.getUu_id());
                                    uumFrag.setSignature(f);
                                    uumFrag.setHeight(height);
                                    uumFrag.setComputed_with_sugar(0);

                                    //computing the score without fragments centered on H
                                    String signature = inDB.getSignature() ;
                                    if(!signature.startsWith("[H]")){


                                        scoreNPnoH = scoreNPnoH + scoreNP ;


                                    }

                                    uumfcpd.save(uumFrag);

                                }
                            }
                        }

                        sugarFreeScoreNP=scoreNP/ (double)uum.getSugar_free_total_atom_number() ;

                        sugarFreeHfreeScoreNP = scoreNPnoH / (double)uum.getSugar_free_heavy_atom_number();


                        if(unknownFragments.isEmpty()){
                            uum.setUnknown_fragments("no");
                        }
                        else{

                            uum.setUnknown_fragments("");
                            for(String uf: unknownFragments){
                                uum.setUnknown_fragments( uum.getUnknown_fragments()+uf+"; " );
                            }
                        }

                    }





                    uum.setNpl_score(sugarFreeScoreNP);
                    uum.setNpl_noh_score(sugarFreeHfreeScoreNP);



                }



                ac = AtomContainerManipulator.suppressHydrogens(ac);


                dg.depict(ac).writeTo("./molimg/"+uum.getUmol_id()+".png");

                uum.setDepictionLocation("./molimg/"+uum.getUmol_id()+".png");




                if(uum.getSugar_free_total_atom_number() == null || uum.getSugar_free_total_atom_number() == 0){
                    uum.setSugar_free_total_atom_number(uum.getTotal_atom_number());
                }


                uum.setSmiles(smilesGenerator.create(ac));



                List<Object[]>  oriMolecules = omr.findSourcesByInchikey(uum.getInchikey());

                if(oriMolecules != null && !oriMolecules.isEmpty()){
                    uum.setIs_in_any_source(1);


                    String omsources = "";
                    for(Object[] obj : oriMolecules){
                        String source = obj[0].toString();
                        String ori_mol_id = obj[1].toString();
                        if(!source.equals("OLD2012")) {
                            if(source.equals("CHEBI")) {
                                omsources = omsources + ori_mol_id + ";";
                            }
                            else if(source.equals("NUBBE")){
                                String [] tab = ori_mol_id.split("\\$");
                                omsources = omsources + source + ": " + tab[0] + ";";
                            }
                            else if(source.equals("PUBCHEM") && ori_mol_id.contains(" ")){
                                String [] tab = ori_mol_id.split(" ");
                                omsources = omsources + source + ": " + tab[0] + ";";
                            }
                            else{
                                omsources = omsources + source + ": " + ori_mol_id + ";";
                            }
                        }

                    }
                    uum.setSources(omsources);
                    if(omsources.equals("")){
                        uum.setIs_in_any_source(0);
                    }
                }
                else{
                    uum.setIs_in_any_source(0);
                }

                System.out.println(uum.getUnknown_fragments());


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

        IAtomContainer newMolecule = null;
        try {
            newMolecule = molecule.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        try {

            IRingSet ringset = Cycles.sssr(newMolecule).toRingSet();

            // RING SUGARS
            for (IAtomContainer one_ring : ringset.atomContainers()) {
                try {
                    IMolecularFormula molecularFormula = MolecularFormulaManipulator.getMolecularFormula(one_ring);
                    String formula = MolecularFormulaManipulator.getString(molecularFormula);
                    IBond.Order bondorder = AtomContainerManipulator.getMaximumBondOrder(one_ring);

                    if (formula.equals("C5O") | formula.equals("C4O") | formula.equals("C6O")) {
                        if (IBond.Order.SINGLE.equals(bondorder)) {
                            if (shouldRemoveRing(one_ring, newMolecule, ringset) == true) {
                                for (IAtom atom : one_ring.atoms()) {
                                    {

                                        newMolecule.removeAtom(atom);
                                    }
                                }
                            }

                        }
                    }
                }catch(NullPointerException e){
                    return null;
                }
            }
            Map<Object, Object> properties = newMolecule.getProperties();
            IAtomContainerSet molset = ConnectivityChecker.partitionIntoMolecules(newMolecule);
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
