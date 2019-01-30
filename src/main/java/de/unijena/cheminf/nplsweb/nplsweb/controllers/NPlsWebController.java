package de.unijena.cheminf.nplsweb.nplsweb.controllers;

import de.unijena.cheminf.nplsweb.nplsweb.misc.PlotData;
import de.unijena.cheminf.nplsweb.nplsweb.reader.ReaderService;
import de.unijena.cheminf.nplsweb.nplsweb.reader.UserInputMoleculeReaderService;
import de.unijena.cheminf.nplsweb.nplsweb.scorer.NpScorerService;
import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageFileNotFoundException;
import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class NPlsWebController {

    private boolean newPlot = false;

    private PlotData plotData = new PlotData();



    private final StorageService storageService;



    @Autowired
    HttpServletRequest request;

    @Autowired
    ReaderService readerService;

    @Autowired
    NpScorerService npScorerService;

    @Autowired
    UserInputMoleculeReaderService userInputMoleculeReaderService;

    @Autowired
    public NPlsWebController(StorageService storageService) {
        this.storageService = storageService;
    }



    //  ******************************************
    // ******* READING MOLECULE FROM STRING upload
    @PostMapping(value="/smiles", consumes = {MediaType.TEXT_PLAIN_VALUE} )
    public String readMoleculeFromSMILES(@RequestBody String smiles, RedirectAttributes redirectAttributes){



        smiles = smiles.split("smiles=")[1];
        //System.out.println(smiles);



        boolean acceptMolecule = userInputMoleculeReaderService.verifySMILES(smiles);

        //System.out.println("molecule accepted "+acceptMolecule);

        //need to include a validation step from the reader service
        if(acceptMolecule) {
            System.out.println("here smiles : "+smiles);

            String smifile = userInputMoleculeReaderService.transformToSMI(smiles);

            readerService.startService(smifile);
            readerService.doWorkWithFile();
            npScorerService.setMolecules(readerService.getMolecules());
            String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getRemoteAddr();
            npScorerService.setSesstionId(sessionId);
            npScorerService.doWork();
            redirectAttributes.addFlashAttribute("numberOfProcessedMolecules", "1");

            return "redirect:/results";
        }
        else{

            redirectAttributes.addFlashAttribute("notSmiles", "");
            return "redirect:/";
        }
    }




    //  ******************************************
    // ******* READING MOLECULE FROM DRAWING

    @PostMapping(value="/drawing", consumes = {MediaType.APPLICATION_JSON_VALUE} )
    public String readMoleculeFromSketcher(@RequestBody String mol, RedirectAttributes redirectAttributes){
        System.out.println("in drawn function");

        System.out.println(mol);
        //do things on molecule
        //save as molfile

        String molfile = userInputMoleculeReaderService.transformToMOL(mol);


        boolean acceptMolecule = true; //accept molecule function to write

        //need to include a validation step from the reader service
        if(acceptMolecule) {

            readerService.startService(molfile);

            readerService.doWorkWithFile();

            npScorerService.setMolecules(readerService.getMolecules());



            String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getRemoteAddr();

            //System.out.println("Session id "+sessionId);


            npScorerService.setSesstionId(sessionId);

            npScorerService.doWork();
            redirectAttributes.addFlashAttribute("numberOfProcessedMolecules", "1");

            return "redirect:/results";
        }
        else{
            return("redirect:/");
        }
        //return new RedirectView("/results");


    }



    //  ******************************************
    // ******* READING MOLECULES FROM FILE

    @PostMapping("/file")
    public String readMoleculesFromFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes){



        if(!file.isEmpty()) {


            storageService.store(file);
            String loadedFile = "upload-dir/" + file.getOriginalFilename();

            if (readerService.startService(loadedFile)) {

                readerService.doWorkWithFile();

                npScorerService.setMolecules(readerService.getMolecules());

                String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getRemoteAddr();

                npScorerService.setSesstionId(sessionId);

                npScorerService.doWork();

                return "redirect:/results";

                // read molecules
                //launch computation

            } else {
                storageService.deleteFile(file);
                redirectAttributes.addFlashAttribute("badFileType",
                        "Bad file type! Accepted formats: SDF, MOL & SMI (SMILES)");
                return "redirect:/";
            }



        }
        else{

            redirectAttributes.addFlashAttribute("noFileError", "You need to load a file!");


            return "redirect:/";

        }
    }





    @GetMapping("/results")
    public String showResults(Model model) throws IOException{

        model.addAttribute("numberOfProcessedMolecules", ""+readerService.getMolecules().keySet().size()+"");



        if(!npScorerService.processFinished()){
            System.out.println("process not finished");
            model.addAttribute("numberOfProcessedMolecules", ""+readerService.getMolecules().keySet().size()+"");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return("redirect:/results");
        }
        else {

            // sending all user molecules with all scores
            model.addAttribute("scores", npScorerService.returnResultsAsUserUploadedMolecules());
            System.out.println("in results");


            //fillPlotData();


            model.addAttribute( "npXdataNP" ,  this.plotData.npXnp );
            model.addAttribute( "npYdataNP" ,  this.plotData.npYnp );

            model.addAttribute( "npXdataSM" ,  this.plotData.npXsm );
            model.addAttribute( "npYdataSM" ,  this.plotData.npYsm );


            model.addAttribute( "npXbacteria" ,  this.plotData.npXbacteria );
            model.addAttribute( "npYbacteria" ,  this.plotData.npYbacteria );


            model.addAttribute( "npXfungi" ,  this.plotData.npXfungi );
            model.addAttribute( "npYfungi" ,  this.plotData.npYfungi );


            model.addAttribute( "npXplants" ,  this.plotData.npXplants );
            model.addAttribute( "npYplants" ,  this.plotData.npYplants );



            model.addAttribute( "npXchebi" ,  this.plotData.npXchebi );
            model.addAttribute( "npYchebi" ,  this.plotData.npYchebi );



            model.addAttribute( "npXtcmdb" ,  this.plotData.npXtcmdb );
            model.addAttribute( "npYtcmdb" ,  this.plotData.npYtcmdb );



            model.addAttribute( "npXzincnp" ,  this.plotData.npXzincnp );
            model.addAttribute( "npYzincnp" ,  this.plotData.npYzincnp );


            model.addAttribute( "npXpubchem" ,  this.plotData.npXpubchem );
            model.addAttribute( "npYpubchem" ,  this.plotData.npYpubchem );



            model.addAttribute( "npXchembl" ,  this.plotData.npXchembl );
            model.addAttribute( "npYchembl" ,  this.plotData.npYchembl );


            model.addAttribute( "npXnpatlas" ,  this.plotData.npXnpatlas );
            model.addAttribute( "npYnpatlas" ,  this.plotData.npYnpatlas );



            model.addAttribute( "npXnubbe" ,  this.plotData.npXnubbe );
            model.addAttribute( "npYnubbe" ,  this.plotData.npYnubbe );


            model.addAttribute( "npXsancdb" ,  this.plotData.npXsancdb );
            model.addAttribute( "npYsancdb" ,  this.plotData.npYsancdb );


            model.addAttribute( "npXdrugbank" ,  this.plotData.npXdrugbank );
            model.addAttribute( "npYdrugbank" ,  this.plotData.npYdrugbank );



            model.addAttribute( "npXhmdb" ,  this.plotData.npXhmdb );
            model.addAttribute( "npYhmdb" ,  this.plotData.npYhmdb );



            model.addAttribute( "npXold2012" ,  this.plotData.npXold2012 );
            model.addAttribute( "npYold2012" ,  this.plotData.npYold2012 );


            model.addAttribute( "npXsupernatural" ,  this.plotData.npXsupernatural );
            model.addAttribute( "npYsupernatural" ,  this.plotData.npYsupernatural );


            model.addAttribute( "npXafrodb" ,  this.plotData.npXafrodb );
            model.addAttribute( "npYafrodb" ,  this.plotData.npYafrodb );

            model.addAttribute("npXac100", this.plotData.npXac100);
            model.addAttribute("npYac100", this.plotData.npYac100);

            model.addAttribute("npXac100200", this.plotData.npXac100200);
            model.addAttribute("npYac100200", this.plotData.npYac100200);

            model.addAttribute("npXac200300", this.plotData.npXac200300);
            model.addAttribute("npYac200300", this.plotData.npYac200300);

            model.addAttribute("npXac300", this.plotData.npXac300);
            model.addAttribute("npYac300", this.plotData.npYac300);




        }

        return "results";

    }








    // ******************************************
    // ***** FILE HANDLING **********************
    /**
     *
     * @param model
     * @return
     * @throws IOException
     *
     * from the index / page, is there are files that have been submitted, lists them, without launching the NPLS computation
     */
    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getRemoteAddr();

        List<String> fileList =storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(NPlsWebController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()) ;

        List<String> filesToServe = new ArrayList<>();
        for(String f : fileList){
            if(f.contains(sessionId)){
                filesToServe.add(f);
            }
        }

        //FINDING data for the plot
        fillPlotData();

        model.addAttribute( "npXdataNP" ,  this.plotData.npXnp );
        model.addAttribute( "npYdataNP" ,  this.plotData.npYnp );

        model.addAttribute( "npXdataSM" ,  this.plotData.npXsm );
        model.addAttribute( "npYdataSM" ,  this.plotData.npYsm );


        model.addAttribute( "npXbacteria" ,  this.plotData.npXbacteria );
        model.addAttribute( "npYbacteria" ,  this.plotData.npYbacteria );


        model.addAttribute( "npXfungi" ,  this.plotData.npXfungi );
        model.addAttribute( "npYfungi" ,  this.plotData.npYfungi );


        model.addAttribute( "npXplants" ,  this.plotData.npXplants );
        model.addAttribute( "npYplants" ,  this.plotData.npYplants );



        model.addAttribute( "npXchebi" ,  this.plotData.npXchebi );
        model.addAttribute( "npYchebi" ,  this.plotData.npYchebi );



        model.addAttribute( "npXtcmdb" ,  this.plotData.npXtcmdb );
        model.addAttribute( "npYtcmdb" ,  this.plotData.npYtcmdb );



        model.addAttribute( "npXzincnp" ,  this.plotData.npXzincnp );
        model.addAttribute( "npYzincnp" ,  this.plotData.npYzincnp );


        model.addAttribute( "npXpubchem" ,  this.plotData.npXpubchem );
        model.addAttribute( "npYpubchem" ,  this.plotData.npYpubchem );



        model.addAttribute( "npXchembl" ,  this.plotData.npXchembl );
        model.addAttribute( "npYchembl" ,  this.plotData.npYchembl );


        model.addAttribute( "npXnpatlas" ,  this.plotData.npXnpatlas );
        model.addAttribute( "npYnpatlas" ,  this.plotData.npYnpatlas );



        model.addAttribute( "npXnubbe" ,  this.plotData.npXnubbe );
        model.addAttribute( "npYnubbe" ,  this.plotData.npYnubbe );


        model.addAttribute( "npXsancdb" ,  this.plotData.npXsancdb );
        model.addAttribute( "npYsancdb" ,  this.plotData.npYsancdb );


        model.addAttribute( "npXdrugbank" ,  this.plotData.npXdrugbank );
        model.addAttribute( "npYdrugbank" ,  this.plotData.npYdrugbank );



        model.addAttribute( "npXhmdb" ,  this.plotData.npXhmdb );
        model.addAttribute( "npYhmdb" ,  this.plotData.npYhmdb );



        model.addAttribute( "npXold2012" ,  this.plotData.npXold2012 );
        model.addAttribute( "npYold2012" ,  this.plotData.npYold2012 );


        model.addAttribute( "npXsupernatural" ,  this.plotData.npXsupernatural );
        model.addAttribute( "npYsupernatural" ,  this.plotData.npYsupernatural );


        model.addAttribute( "npXafrodb" ,  this.plotData.npXafrodb );
        model.addAttribute( "npYafrodb" ,  this.plotData.npYafrodb );

        model.addAttribute("npXac100", this.plotData.npXac100);
        model.addAttribute("npYac100", this.plotData.npYac100);

        model.addAttribute("npXac100200", this.plotData.npXac100200);
        model.addAttribute("npYac100200", this.plotData.npYac100200);

        model.addAttribute("npXac200300", this.plotData.npXac200300);
        model.addAttribute("npYac200300", this.plotData.npYac200300);

        model.addAttribute("npXac300", this.plotData.npXac300);
        model.addAttribute("npYac300", this.plotData.npYac300);




        model.addAttribute("files", filesToServe);



        return "index";
    }

    public void fillPlotData(){

        if(this.newPlot==true){
            //new plot - query all data and serialize

            this.plotData.npScoresNPall = npScorerService.returnAllNPLScoresNP();

            //Get general plot for all SM
            this.plotData.npScoresSM = npScorerService.returnAllNPLScoresSM();

            //Get subplots by DB
            this.plotData.npScoresCHEBI = npScorerService.returnAllNPLScoresCHEBI();
            this.plotData.npScoresTCMDB = npScorerService.returnAllNPLScoresTCMDB();
            this.plotData.npScoresZINCNP = npScorerService.returnAllNPLScoresZINCNP();
            this.plotData.npScoresPUBCHEM = npScorerService.returnAllNPLScoresPUBCHEM();
            this.plotData.npScoresCHEMBL = npScorerService.returnAllNPLScoresCHEMBL();
            this.plotData.npScoresNPATLAS = npScorerService.returnAllNPLScoresNPATLAS();
            this.plotData.npScoresNUBBE = npScorerService.returnAllNPLScoresNUBBE();
            this.plotData.npScoresSANCDB = npScorerService.returnAllNPLScoresSANCDB();
            this.plotData.npScoresDRUGBANK = npScorerService.returnAllNPLScoresDRUGBANK();
            this.plotData.npScoresHMDB = npScorerService.returnAllNPLScoresHMDB();
            this.plotData.npScoresSUPERNATURAL = npScorerService.returnAllNPLScoresSUPENATURAL();
            this.plotData.npScoresAFRODB = npScorerService.returnAllNPLScoresAFRODB();
            this.plotData.npScoresOLD2012 = npScorerService.returnAllNPLScoresOLD2012();


            //Get subplots by organism

            this.plotData.npScoresBACTERIA = npScorerService.returnAllNPLScoresBACTERIA();
            this.plotData.npScoresFUNGI = npScorerService.returnAllNPLScoresFUNGI();
            this.plotData.npScoresPLANTS = npScorerService.returnAllNPLScoresPLANTS();


            //Get subplots by size
            this.plotData.npScoresAC100 = npScorerService.returnAllNPLScoresAC100();
            this.plotData.npScoresAC100200 = npScorerService.returnAllNPLScoresAC100200();
            this.plotData.npScoresAC200300 = npScorerService.returnAllNPLScoresAC200300();
            this.plotData.npScoresAC300 = npScorerService.returnAllNPLScoresAC300();

            //SERIALIZE
            try{
                // ALL
                FileOutputStream fos = new FileOutputStream("archive/npScoresNP.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresNPall);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresSM.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresSM);
                oos.close();
                fos.close();



                //BY DB

                fos = new FileOutputStream("archive/npScoresCHEBI.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresCHEBI);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresTCMDB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresTCMDB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresZINCNP.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresZINCNP);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresPUBCHEM.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresPUBCHEM);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresCHEMBL.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresCHEMBL);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresNPATLAS.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresNPATLAS);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresNUBBE.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresNUBBE);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresSANCDB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresSANCDB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresDRUGBANK.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresDRUGBANK);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresHMDB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresHMDB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresSUPERNATURAL.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresSUPERNATURAL);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresAFRODB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresAFRODB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresOLD2012.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresOLD2012);
                oos.close();
                fos.close();


                // BY ORGANISM

                fos = new FileOutputStream("archive/npScoresBACTERIA.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresBACTERIA);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresFUNGI.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresFUNGI);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresPLANTS.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresPLANTS);
                oos.close();
                fos.close();


                // BY SIZE

                fos = new FileOutputStream("archive/npScoresAC100.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresAC100);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresAC100200.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresAC100200);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresAC200300.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresAC200300);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresAC300.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresAC300);
                oos.close();
                fos.close();




            }catch(IOException ioe) { ioe.printStackTrace(); }

        }
        else{
            // only deserialize

            try
            {
                FileInputStream fis = new FileInputStream("archive/npScoresNP.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                this.plotData.npScoresNPall = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresSM.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresSM = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresCHEBI.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresCHEBI = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresTCMDB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData. npScoresTCMDB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresZINCNP.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresZINCNP = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresPUBCHEM.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresPUBCHEM = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresCHEMBL.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresCHEMBL = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresNPATLAS.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresNPATLAS = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresNUBBE.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresNUBBE = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresSANCDB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresSANCDB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresDRUGBANK.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresDRUGBANK = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresHMDB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresHMDB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresSUPERNATURAL.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresSUPERNATURAL = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresAFRODB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresAFRODB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresOLD2012.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresOLD2012 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();


                fis = new FileInputStream("archive/npScoresBACTERIA.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresBACTERIA = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresFUNGI.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresFUNGI = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresPLANTS.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresPLANTS = (Hashtable) ois.readObject();
                ois.close();
                fis.close();


                fis = new FileInputStream("archive/npScoresAC100.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresAC100 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresAC100200.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresAC100200 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresAC200300.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresAC200300 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresAC300.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresAC300 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();



            }catch(IOException ioe) { ioe.printStackTrace();
            }catch(ClassNotFoundException c) { c.printStackTrace(); }



        }



        // FILLING OF X AND Ys

        this.plotData.npXnp.addAll(this.plotData.npScoresNPall.keySet());
        Collections.sort(this.plotData.npXnp);

        for (Double i : this.plotData.npXnp) {
            this.plotData.npYnp.add(this.plotData.npScoresNPall.get(i));
        }

        this.plotData.npXsm.addAll(this.plotData.npScoresSM.keySet());
        Collections.sort(this.plotData.npXsm);

        for (Double i : this.plotData.npXsm) {
            this.plotData.npYsm.add(this.plotData.npScoresSM.get(i));
        }

        //FILLING FOR KINGDOMS

        //BACTERIA
        this.plotData.npXbacteria.addAll(this.plotData.npScoresBACTERIA.keySet());
        Collections.sort(this.plotData.npXbacteria);

        for (Double i : this.plotData.npXbacteria) {
            this.plotData.npYbacteria.add(this.plotData.npScoresBACTERIA.get(i));
        }

        //fungi
        this.plotData.npXfungi.addAll(this.plotData.npScoresFUNGI.keySet());
        Collections.sort(this.plotData.npXfungi);

        for (Double i : this.plotData.npXfungi) {
            this.plotData.npYfungi.add(this.plotData.npScoresFUNGI.get(i));
        }

        //PLANTS
        this.plotData.npXplants.addAll(this.plotData.npScoresPLANTS.keySet());
        Collections.sort(this.plotData.npXplants);

        for (Double i : this.plotData.npXplants) {
            this.plotData.npYplants.add(this.plotData.npScoresPLANTS.get(i));
        }

        // X and Y by DB

        //chebi
        this.plotData.npXchebi.addAll(this.plotData.npScoresCHEBI.keySet());
        Collections.sort(this.plotData.npXchebi);

        for (Double i : this.plotData.npXchebi) {
            this.plotData.npYchebi.add(this.plotData.npScoresCHEBI.get(i));
        }

        //TCMDB
        this.plotData.npXtcmdb.addAll(this.plotData.npScoresTCMDB.keySet());
        Collections.sort(this.plotData.npXtcmdb);

        for (Double i : this.plotData.npXtcmdb) {
            this.plotData.npYtcmdb.add(this.plotData.npScoresTCMDB.get(i));
        }

        //ZINC NP
        this.plotData.npXzincnp.addAll(this.plotData.npScoresZINCNP.keySet());
        Collections.sort(this.plotData.npXzincnp);

        for (Double i : this.plotData.npXzincnp) {
            this.plotData.npYzincnp.add(this.plotData.npScoresZINCNP.get(i));
        }

        //PUBCHEM
        this.plotData.npXpubchem.addAll(this.plotData.npScoresPUBCHEM.keySet());
        Collections.sort(this.plotData.npXpubchem);

        for (Double i : this.plotData.npXpubchem) {
            this.plotData.npYpubchem.add(this.plotData.npScoresPUBCHEM.get(i));
        }

        //CHEMBL
        this.plotData.npXchembl.addAll(this.plotData.npScoresCHEMBL.keySet());
        Collections.sort(this.plotData.npXchembl);

        for (Double i : this.plotData.npXchembl) {
            this.plotData.npYchembl.add(this.plotData.npScoresCHEMBL.get(i));
        }

        //NP ATLAS
        this.plotData.npXnpatlas.addAll(this.plotData.npScoresNPATLAS.keySet());
        Collections.sort(this.plotData.npXnpatlas);

        for (Double i : this.plotData.npXnpatlas) {
            this.plotData.npYnpatlas.add(this.plotData.npScoresNPATLAS.get(i));
        }

        //NUBBE
        this.plotData.npXnubbe.addAll(this.plotData.npScoresNUBBE.keySet());
        Collections.sort(this.plotData.npXnubbe);

        for (Double i : this.plotData.npXnubbe) {
            this.plotData.npYnubbe.add(this.plotData.npScoresNUBBE.get(i));
        }

        //SANCDB
        this.plotData.npXsancdb.addAll(this.plotData.npScoresSANCDB.keySet());
        Collections.sort(this.plotData.npXsancdb);

        for (Double i : this.plotData.npXsancdb) {
            this.plotData.npYsancdb.add(this.plotData.npScoresSANCDB.get(i));
        }

        //DRUGBANK
        this.plotData.npXdrugbank.addAll(this.plotData.npScoresDRUGBANK.keySet());
        Collections.sort(this.plotData.npXdrugbank);

        for (Double i : this.plotData.npXdrugbank) {
            this.plotData.npYdrugbank.add(this.plotData.npScoresDRUGBANK.get(i));
        }

        //HMDB
        this.plotData.npXhmdb.addAll(this.plotData.npScoresHMDB.keySet());
        Collections.sort(this.plotData.npXhmdb);

        for (Double i : this.plotData.npXhmdb) {
            this.plotData.npYhmdb.add(this.plotData.npScoresHMDB.get(i));
        }

        //OLD 2012
        this.plotData.npXold2012.addAll(this.plotData.npScoresOLD2012.keySet());
        Collections.sort(this.plotData.npXold2012);

        for (Double i : this.plotData.npXold2012) {
            this.plotData.npYold2012.add(this.plotData.npScoresOLD2012.get(i));
        }

        //SUPERNATURAL
        this.plotData.npXsupernatural.addAll(this.plotData.npScoresSUPERNATURAL.keySet());
        Collections.sort(this.plotData.npXsupernatural);

        for (Double i : this.plotData.npXsupernatural) {
            this.plotData.npYsupernatural.add(this.plotData.npScoresSUPERNATURAL.get(i));
        }

        //AFRODB
        this.plotData.npXafrodb.addAll(this.plotData.npScoresAFRODB.keySet());
        Collections.sort(this.plotData.npXafrodb);

        for (Double i : this.plotData.npXafrodb) {
            this.plotData.npYafrodb.add(this.plotData.npScoresAFRODB.get(i));
        }


        //BY SIZE LESS THAN 100
        this.plotData.npXac100.addAll(this.plotData.npScoresAC100.keySet());
        Collections.sort(this.plotData.npXac100);

        for (Double i : this.plotData.npXac100) {
            this.plotData.npYac100.add(this.plotData.npScoresAC100.get(i));
        }

        //BY SIZE LESS 100-200
        this.plotData.npXac100200.addAll(this.plotData.npScoresAC100200.keySet());
        Collections.sort(this.plotData.npXac100200);

        for (Double i : this.plotData.npXac100200) {
            this.plotData.npYac100200.add(this.plotData.npScoresAC100200.get(i));
        }

        //BY SIZE LESS 200-300
        this.plotData.npXac200300.addAll(this.plotData.npScoresAC200300.keySet());
        Collections.sort(this.plotData.npXac200300);

        for (Double i : this.plotData.npXac200300) {
            this.plotData.npYac200300.add(this.plotData.npScoresAC200300.get(i));
        }

        //BY SIZE LESS THAN 300
        this.plotData.npXac300.addAll(this.plotData.npScoresAC300.keySet());
        Collections.sort(this.plotData.npXac300);

        for (Double i : this.plotData.npXac300) {
            this.plotData.npYac300.add(this.plotData.npScoresAC300.get(i));
        }






    }
















    /**
     *
     * @param filename
     * @return
     *
     * from page / when files have been submitted, serves the files (loads)
     */
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {


        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    /**
     * Handles file not found and file not uploaded situations
     * @param exc
     * @return
     */
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }




    //Cast multipart spring file to conventional file
    public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
    {
        File convFile = new File( multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }







}
