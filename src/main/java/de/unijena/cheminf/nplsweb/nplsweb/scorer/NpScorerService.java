package de.unijena.cheminf.nplsweb.nplsweb.scorer;


import com.google.common.collect.Lists;
import de.unijena.cheminf.nplsweb.nplsweb.model.MoleculeRepository;
import de.unijena.cheminf.nplsweb.nplsweb.model.UserUploadedMolecule;
import de.unijena.cheminf.nplsweb.nplsweb.model.UserUploadedMoleculeRepository;
import org.javatuples.Quintet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class NpScorerService {



    @Autowired
    UserUploadedMoleculeRepository uumr;

    @Autowired
    MoleculeRepository mr;

    private ArrayList<Quintet< IAtomContainer, Double, Double, Double, Double>> moleculesWithScores;


    private ArrayList<String> moleculeIdWithScores;

    private Hashtable<String, IAtomContainer> molecules;

    private String sesstionId;


    private Integer numberOfThreads = 5 ;

    List<Future<?>> futures = new ArrayList<Future<?>>();



    public void doWork(){

        try{
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);


            List<List<String>>  moleculeListBatch =  Lists.partition(new ArrayList<String>(molecules.keySet()), 5);

            int taskcount = 0;

            List<Callable<Object>> todo = new ArrayList<Callable<Object>>(moleculeListBatch.size());

            System.out.println("Total number of tasks:" + moleculeListBatch.size());


            for(List<String> stringMolBatch : moleculeListBatch){
                NplsTask task = new NplsTask();


                Hashtable<String, IAtomContainer> molBatch = new Hashtable<String, IAtomContainer>();
                for(String s : stringMolBatch){
                    molBatch.put(s, molecules.get(s));
                }

                task.setSessionid(sesstionId);

                task.setMoleculesToCompute(molBatch);

                taskcount++;

                System.out.println("Task "+taskcount+" created");
                task.taskid=taskcount;

                // todo.add(Executors.callable(task));

                Future<?> f = executor.submit(task);

                futures.add(f);

                //executor.execute(task);

                System.out.println("Task "+taskcount+" executing");

            }






        } catch (Exception e) {
            e.printStackTrace();
        }



        return;
    }




    public ArrayList<String> returnResultsAsStrings(){
        this.moleculeIdWithScores = new ArrayList<String>();


        List<UserUploadedMolecule> results = uumr.findAllBySessionid(this.sesstionId);


        for(UserUploadedMolecule uum: results){

            String s = uum.getUu_id() + " : NPL score (sugar removal): " + uum.getNpl_score() + "; NPL score (full molecule): "+uum.getNpl_sugar_score();


            this.moleculeIdWithScores.add(s);


        }
        return this.moleculeIdWithScores;

    }

    public List<UserUploadedMolecule> returnResultsAsUserUploadedMolecules(String specificSessionId){



        List<UserUploadedMolecule> results = uumr.findAllBySessionid(specificSessionId);

        for(UserUploadedMolecule umol : results){
            umol.setSmiles( umol.getSmiles() );
        }


        return results;
    }


    public List<Double> returnxAxis(String plotType){

        Double minscore = 0.0;
        Double maxscore = 0.0;

        List<Double> axis = new ArrayList<>();

        if(plotType.equals("np")){
            Object obj1 = mr.getMinNPLScore().get(0);
            minscore = Double.parseDouble((obj1.toString()));

            Object obj2 = mr.getMaxNPLScore().get(0);
            maxscore = Double.parseDouble(obj2.toString());

        }
        else if(plotType.equals("np_sugar")){
            Object obj1 = mr.getMinNPLSugarScore().get(0);
            minscore = Double.parseDouble((obj1.toString()));

            Object obj2 = mr.getMaxNPLSugarScore().get(0);
            maxscore = Double.parseDouble(obj2.toString());

        }



        Integer xmin = (int)Math.floor(minscore) ;
        Integer xmax  = (int)Math.ceil(maxscore);


        for(double i=xmin;i<=xmax;i=i+0.1){
            i = (double)Math.round(i *10) /10 ;
            axis.add(i);

        }


        return axis;
    }


    public Hashtable<Double, Double> computeBins(List<Double> scores, List<Double> xaxis){


        Hashtable<Double, Integer> counts = new Hashtable<Double, Integer>();
        Hashtable<Double, Double> probs = new Hashtable<Double, Double>();

        //initialization of bin counts
        for(double i:xaxis){
            counts.put(i, 0);
        }

        for(Double score : scores){
            double roundedScore = (double) Math.round(score *10) /10 ;
            counts.put( roundedScore, counts.get(roundedScore) + 1);
        }


        if(scores.size()<100000){
            Hashtable<Double, Integer> countsBigBins = new Hashtable<Double, Integer>();

            for(double i=Collections.min(xaxis); i<=Collections.max(xaxis);i+=0.5){
                if(counts.get(i) != null && counts.get(i + 0.1) != null && counts.get(i + 0.2) != null && counts.get(i + 0.3)!= null && counts.get(i + 0.4) != null) {
                    countsBigBins.put(i+0.2, counts.get(i) + counts.get(i + 0.1) + counts.get(i + 0.2) + counts.get(i + 0.3) + counts.get(i + 0.4));
                }
                else if(counts.get(i) != null && counts.get(i + 0.1) != null && counts.get(i + 0.2) != null && counts.get(i + 0.3)!= null){
                    countsBigBins.put(i+0.2, counts.get(i) + counts.get(i + 0.1) + counts.get(i + 0.2) + counts.get(i + 0.3) );
                }
                else if(counts.get(i) != null && counts.get(i + 0.1) != null && counts.get(i + 0.2) != null){
                    countsBigBins.put(i+0.1, counts.get(i) + counts.get(i + 0.1) + counts.get(i + 0.2)  );
                }
                else if(counts.get(i) != null && counts.get(i + 0.1) != null){
                    countsBigBins.put(i, counts.get(i) + counts.get(i + 0.1) );
                }
                else if(counts.get(i) != null ){
                    countsBigBins.put(i, counts.get(i)  );
                }
            }

            counts.clear();
            counts.putAll(countsBigBins);

            for(Double bin : counts.keySet()){
                probs.put(bin, ((double)counts.get(bin)/ ((double)scores.size() ) ) / 5);
            }


        }
        else{


            for(Double bin : counts.keySet()){
                probs.put(bin, (double)counts.get(bin)/ (double)scores.size() );
            }

        }


        return probs;
    }





    // ALL SCORES FOR NP AND SM
    public Hashtable<Double, Double> returnAllNPLScoresNP(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinNP();

        return( computeBins(scores, returnxAxis("np")) );
    }


    public Hashtable<Double, Double>  returnAllNPLScoresSM(){
        List<Double> scores = (List<Double>)(Object) mr.getNPLSinSM();
        return( computeBins(scores, returnxAxis("np")) );
    }





    //SCORES BY DB

    public Hashtable<Double, Double> returnAllNPLScoresUEFS(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinUEFS();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresHIT(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinHIT();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresSANCDB(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinSANCDB();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresAFRODB(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinAFRODB();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresNPACT(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinNPACT();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresSELLECKCHEM(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinSELLECKCHEM();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresCHEMBL(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinCHEMBL();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresNUBBE(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinNUBBE();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresSTREPTOMEDB(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinSTREPTOMEDB();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresPUBCHEM(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinPUBCHEM();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresNANPDB(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinNANPDB();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresCHEBI(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinCHEBI();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresNPATLAS(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinNPATLAS();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresTCMDB(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinTCMDB();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresIBS(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinIBS();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresOLD2012(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinOLD2012();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresZINCNP(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinZINCNP();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresUNPD(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinUNPD();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresSUPENATURAL(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinSUPERNATURAL();

        return( computeBins(scores, returnxAxis("np")) );
    }






    public Hashtable<Double, Double> returnAllNPLScoresDRUGBANK(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinDRUGBANK();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresHMDB(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinHMDB();

        return( computeBins(scores, returnxAxis("np")) );
    }






    //SCORES BY ORGANISM

    public Hashtable<Double, Double> returnAllNPLScoresBACTERIA(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinBACTERIA();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresFUNGI(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinFUNGI();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresPLANTS(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSinPLANTS();

        return( computeBins(scores, returnxAxis("np")) );
    }


    //SCORES BY SIZE
    public Hashtable<Double, Double> returnAllNPLScoresACTranche1(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSacTranche1();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresACTranche2(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSacTranche2();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresACTranche3(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSacTranche3();

        return( computeBins(scores, returnxAxis("np")) );
    }

    public Hashtable<Double, Double> returnAllNPLScoresACTranche4(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSacTranche4();

        return( computeBins(scores, returnxAxis("np")) );
    }




    // ALL SCORES FOR NP AND SM - WITH SUGARS
    public Hashtable<Double, Double> returnAllNPLSugarScoresNP(){
        List<Double> scores = (List<Double>)(Object) mr.getNPLSsugarSinNP();

        return(computeBins(scores, returnxAxis("np_sugar")));
    }

    public Hashtable<Double, Double> returnAllNPLSugarScoresSM(){

        List<Double> scores = (List<Double>)(Object) mr.getNPLSsugarSinSM();

        return(computeBins(scores, returnxAxis("np_sugar")));
    }



    public boolean processFinished(){

        boolean allFuturesDone = true;

        for(Future<?> future : this.futures){

            allFuturesDone &= future.isDone();

        }



        return allFuturesDone;
    }



    public ArrayList getMoleculesWithScores() {
        return moleculesWithScores;
    }

    public void setMoleculesWithScores(ArrayList moleculesWithScores) {
        this.moleculesWithScores = moleculesWithScores;
    }

    public Hashtable<String, IAtomContainer>  getMolecules() {
        return molecules;
    }

    public void setMolecules(Hashtable<String, IAtomContainer>  molecules) {
        this.molecules = molecules;
    }

    public void setNumberOfThreads(Integer numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public ArrayList<String> getMoleculeIdWithScores() {
        return moleculeIdWithScores;
    }

    public void setMoleculeIdWithScores(ArrayList<String> moleculeIdWithScores) {
        this.moleculeIdWithScores = moleculeIdWithScores;
    }

    public String getSesstionId() {
        return sesstionId;
    }

    public void setSesstionId(String sesstionId) {
        this.sesstionId = sesstionId;
    }
}
