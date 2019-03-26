package de.unijena.cheminf.nplsweb.nplsweb.controllers;

import de.unijena.cheminf.nplsweb.nplsweb.misc.PlotData;
import de.unijena.cheminf.nplsweb.nplsweb.misc.SessionCleaner;
import de.unijena.cheminf.nplsweb.nplsweb.model.MoleculeRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class NPlsWebController {

    private final StorageService storageService;

    @Autowired
    MoleculeRepository moleculeRepository;

    @Autowired
    HttpServletRequest request;

    @Autowired
    ReaderService readerService;

    @Autowired
    NpScorerService npScorerService;

    @Autowired
    UserInputMoleculeReaderService userInputMoleculeReaderService;

    @Autowired
    SessionCleaner sessionCleaner;


    private boolean newPlot = false;
    private PlotData plotData = new PlotData();

    @Autowired
    public NPlsWebController(StorageService storageService) {
        this.storageService = storageService;
    }



    //*******************************************
    //******** CLEANING THE SESSION
    @GetMapping(value="/clear")
    public String clearSession(RedirectAttributes redirectAttributes){


        // sending all user molecules with all scores
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate localDate = LocalDate.now();
        String userDate = dtf.format(localDate);

        String thisSessionId =  RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getHeader("User-Agent")+"--"+ipAddress+"--"+userDate;


        //deleting entries with current sessionId from the database
        sessionCleaner.clearSession(thisSessionId);

        return("redirect:/");
    }

    //  ******************************************
    // ******* READING MOLECULE FROM STRING upload
    @PostMapping(value="/smiles", consumes = {MediaType.TEXT_PLAIN_VALUE} )
    public String readMoleculeFromSMILES(@RequestBody String smiles, RedirectAttributes redirectAttributes){

        try {

            smiles = smiles.replace("\n", "").replace("\r", "");


            smiles = smiles.split("smiles=")[1];



            boolean acceptMolecule = userInputMoleculeReaderService.verifySMILES(smiles);

            //System.out.println("molecule accepted "+acceptMolecule);

            //need to include a validation step from the reader service
            if (acceptMolecule) {
                System.out.println("here smiles : " + smiles);

                String smifile = userInputMoleculeReaderService.transformToSMI(smiles);

                readerService.startService(smifile);
                readerService.doWorkWithFile();
                npScorerService.setMolecules(readerService.getMolecules());


                String ipAddress = request.getHeader("X-FORWARDED-FOR");
                if (ipAddress == null) {
                    ipAddress = request.getRemoteAddr();
                }

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate localDate = LocalDate.now();
                String userDate = dtf.format(localDate);

                String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getHeader("User-Agent") + "--" + ipAddress + "--" + userDate;
                npScorerService.setSesstionId(sessionId);
                npScorerService.doWork();
                redirectAttributes.addFlashAttribute("numberOfProcessedMolecules", "1");

                return "redirect:/results";
            } else {

                redirectAttributes.addFlashAttribute("notSmiles", "");
                return "redirect:/";
            }
        }catch (ArrayIndexOutOfBoundsException exception){

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



            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate localDate = LocalDate.now();
            String userDate = dtf.format(localDate);

            String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getHeader("User-Agent")+"--"+ipAddress+"--"+userDate;

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

                String ipAddress = request.getHeader("X-FORWARDED-FOR");
                if (ipAddress == null) {
                    ipAddress = request.getRemoteAddr();
                }

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate localDate = LocalDate.now();
                String userDate = dtf.format(localDate);

                String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getHeader("User-Agent")+"--"+ipAddress+"--"+userDate;

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

        //model.addAttribute("numberOfProcessedMolecules", ""+readerService.getMolecules().keySet().size()+"");



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
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate localDate = LocalDate.now();
            String userDate = dtf.format(localDate);

            String thisSessionId =  RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getHeader("User-Agent")+"--"+ipAddress+"--"+userDate;

            model.addAttribute("scores", npScorerService.returnResultsAsUserUploadedMolecules( thisSessionId ));
            System.out.println("in results");


            //FINDING data for the plot
            if(!this.plotData.isInitialised){
                fillPlotData();
            }


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






            model.addAttribute( "npXuefs" ,  this.plotData.npXuefs );
            model.addAttribute( "npYuefs" ,  this.plotData.npYuefs );

            model.addAttribute( "npXhit" ,  this.plotData.npXhit );
            model.addAttribute( "npYhit" ,  this.plotData.npYhit );

            model.addAttribute( "npXsancdb" ,  this.plotData.npXsancdb );
            model.addAttribute( "npYsancdb" ,  this.plotData.npYsancdb );

            model.addAttribute( "npXafrodb" ,  this.plotData.npXafrodb );
            model.addAttribute( "npYafrodb" ,  this.plotData.npYafrodb );

            model.addAttribute( "npXnpact" ,  this.plotData.npXnpact );
            model.addAttribute( "npYnpact" ,  this.plotData.npYnpact );

            model.addAttribute( "npXselleckchem" ,  this.plotData.npXsellecchem );
            model.addAttribute( "npYselleckchem" ,  this.plotData.npYsellecchem );

            model.addAttribute( "npXchembl" ,  this.plotData.npXchembl );
            model.addAttribute( "npYchembl" ,  this.plotData.npYchembl );

            model.addAttribute( "npXnubbe" ,  this.plotData.npXnubbe );
            model.addAttribute( "npYnubbe" ,  this.plotData.npYnubbe );

            model.addAttribute( "npXstreptomedb" ,  this.plotData.npXstreptomedb );
            model.addAttribute( "npYstreptomedb" ,  this.plotData.npYstreptomedb );

            model.addAttribute( "npXpubchem" ,  this.plotData.npXpubchem );
            model.addAttribute( "npYpubchem" ,  this.plotData.npYpubchem );

            model.addAttribute( "npXnanpdb" ,  this.plotData.npXnanpdb );
            model.addAttribute( "npYnanpdb" ,  this.plotData.npYnanpdb );

            model.addAttribute( "npXchebi" ,  this.plotData.npXchebi );
            model.addAttribute( "npYchebi" ,  this.plotData.npYchebi );

            model.addAttribute( "npXnpatlas" ,  this.plotData.npXnpatlas );
            model.addAttribute( "npYnpatlas" ,  this.plotData.npYnpatlas );

            model.addAttribute( "npXtcmdb" ,  this.plotData.npXtcmdb );
            model.addAttribute( "npYtcmdb" ,  this.plotData.npYtcmdb );

            model.addAttribute( "npXibs" ,  this.plotData.npXibs );
            model.addAttribute( "npYibs" ,  this.plotData.npYibs );

            model.addAttribute( "npXold2012" ,  this.plotData.npXold2012 );
            model.addAttribute( "npYold2012" ,  this.plotData.npYold2012 );

            model.addAttribute( "npXzincnp" ,  this.plotData.npXzincnp );
            model.addAttribute( "npYzincnp" ,  this.plotData.npYzincnp );

            model.addAttribute( "npXunpd" ,  this.plotData.npXunpd );
            model.addAttribute( "npYunpd" ,  this.plotData.npYunpd );

            model.addAttribute( "npXsupernatural" ,  this.plotData.npXsupernatural );
            model.addAttribute( "npYsupernatural" ,  this.plotData.npYsupernatural );






            model.addAttribute( "npXdrugbank" ,  this.plotData.npXdrugbank );
            model.addAttribute( "npYdrugbank" ,  this.plotData.npYdrugbank );

            model.addAttribute( "npXhmdb" ,  this.plotData.npXhmdb );
            model.addAttribute( "npYhmdb" ,  this.plotData.npYhmdb );





            model.addAttribute("npXacTranche1", this.plotData.npXacTranche1);
            model.addAttribute("npYacTranche1", this.plotData.npYacTranche1);

            model.addAttribute("npXacTranche2", this.plotData.npXacTranche2);
            model.addAttribute("npYacTranche2", this.plotData.npYacTranche2);

            model.addAttribute("npXacTranche3", this.plotData.npXacTranche3);
            model.addAttribute("npYacTranche3", this.plotData.npYacTranche3);

            model.addAttribute("npXacTranche4", this.plotData.npXacTranche4);
            model.addAttribute("npYacTranche4", this.plotData.npYacTranche4);


            model.addAttribute("globalCounts", this.plotData.globalCounts);



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
     */
    @GetMapping("/")
    public String indexPageMethod(Model model) throws IOException {

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId() + "--" + request.getHeader("User-Agent");

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
        if(!this.plotData.isInitialised){
            fillPlotData();
        }


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



        model.addAttribute( "npXuefs" ,  this.plotData.npXuefs );
        model.addAttribute( "npYuefs" ,  this.plotData.npYuefs );

        model.addAttribute( "npXhit" ,  this.plotData.npXhit );
        model.addAttribute( "npYhit" ,  this.plotData.npYhit );

        model.addAttribute( "npXsancdb" ,  this.plotData.npXsancdb );
        model.addAttribute( "npYsancdb" ,  this.plotData.npYsancdb );

        model.addAttribute( "npXafrodb" ,  this.plotData.npXafrodb );
        model.addAttribute( "npYafrodb" ,  this.plotData.npYafrodb );

        model.addAttribute( "npXnpact" ,  this.plotData.npXnpact );
        model.addAttribute( "npYnpact" ,  this.plotData.npYnpact );

        model.addAttribute( "npXselleckchem" ,  this.plotData.npXsellecchem );
        model.addAttribute( "npYselleckchem" ,  this.plotData.npYsellecchem );

        model.addAttribute( "npXchembl" ,  this.plotData.npXchembl );
        model.addAttribute( "npYchembl" ,  this.plotData.npYchembl );

        model.addAttribute( "npXnubbe" ,  this.plotData.npXnubbe );
        model.addAttribute( "npYnubbe" ,  this.plotData.npYnubbe );

        model.addAttribute( "npXstreptomedb" ,  this.plotData.npXstreptomedb );
        model.addAttribute( "npYstreptomedb" ,  this.plotData.npYstreptomedb );

        model.addAttribute( "npXpubchem" ,  this.plotData.npXpubchem );
        model.addAttribute( "npYpubchem" ,  this.plotData.npYpubchem );

        model.addAttribute( "npXnanpdb" ,  this.plotData.npXnanpdb );
        model.addAttribute( "npYnanpdb" ,  this.plotData.npYnanpdb );

        model.addAttribute( "npXchebi" ,  this.plotData.npXchebi );
        model.addAttribute( "npYchebi" ,  this.plotData.npYchebi );

        model.addAttribute( "npXnpatlas" ,  this.plotData.npXnpatlas );
        model.addAttribute( "npYnpatlas" ,  this.plotData.npYnpatlas );

        model.addAttribute( "npXtcmdb" ,  this.plotData.npXtcmdb );
        model.addAttribute( "npYtcmdb" ,  this.plotData.npYtcmdb );

        model.addAttribute( "npXibs" ,  this.plotData.npXibs );
        model.addAttribute( "npYibs" ,  this.plotData.npYibs );

        model.addAttribute( "npXold2012" ,  this.plotData.npXold2012 );
        model.addAttribute( "npYold2012" ,  this.plotData.npYold2012 );

        model.addAttribute( "npXzincnp" ,  this.plotData.npXzincnp );
        model.addAttribute( "npYzincnp" ,  this.plotData.npYzincnp );

        model.addAttribute( "npXunpd" ,  this.plotData.npXunpd );
        model.addAttribute( "npYunpd" ,  this.plotData.npYunpd );

        model.addAttribute( "npXsupernatural" ,  this.plotData.npXsupernatural );
        model.addAttribute( "npYsupernatural" ,  this.plotData.npYsupernatural );






        model.addAttribute( "npXdrugbank" ,  this.plotData.npXdrugbank );
        model.addAttribute( "npYdrugbank" ,  this.plotData.npYdrugbank );

        model.addAttribute( "npXhmdb" ,  this.plotData.npXhmdb );
        model.addAttribute( "npYhmdb" ,  this.plotData.npYhmdb );






        model.addAttribute("npXacTranche1", this.plotData.npXacTranche1);
        model.addAttribute("npYacTranche1", this.plotData.npYacTranche1);

        model.addAttribute("npXacTranche2", this.plotData.npXacTranche2);
        model.addAttribute("npYacTranche2", this.plotData.npYacTranche2);

        model.addAttribute("npXacTranche3", this.plotData.npXacTranche3);
        model.addAttribute("npYacTranche3", this.plotData.npYacTranche3);

        model.addAttribute("npXacTranche4", this.plotData.npXacTranche4);
        model.addAttribute("npYacTranche4", this.plotData.npYacTranche4);


        model.addAttribute("globalCounts", this.plotData.globalCounts);




        model.addAttribute("files", filesToServe);



        return "index";
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










    public void fillPlotData(){

        if(this.newPlot==true){
            //new plot - query all data and serialize

            this.plotData.npScoresNPall = npScorerService.returnAllNPLScoresNP();

            //Get general plot for all SM
            this.plotData.npScoresSM = npScorerService.returnAllNPLScoresSM();

            //Get subplots by DB
            this.plotData.npScoresUEFS = npScorerService.returnAllNPLScoresUEFS();
            this.plotData.npScoresHIT = npScorerService.returnAllNPLScoresHIT();
            this.plotData.npScoresSANCDB = npScorerService.returnAllNPLScoresSANCDB();
            this.plotData.npScoresAFRODB = npScorerService.returnAllNPLScoresAFRODB();
            this.plotData.npScoresNPACT = npScorerService.returnAllNPLScoresNPACT();
            this.plotData.npScoresSELLECCHEM = npScorerService.returnAllNPLScoresSELLECKCHEM();
            this.plotData.npScoresCHEMBL = npScorerService.returnAllNPLScoresCHEMBL();
            this.plotData.npScoresNUBBE = npScorerService.returnAllNPLScoresNUBBE();
            this.plotData.npScoresSTREPTOMEDB = npScorerService.returnAllNPLScoresSTREPTOMEDB();
            this.plotData.npScoresPUBCHEM = npScorerService.returnAllNPLScoresPUBCHEM();
            this.plotData.npScoresNANPDB = npScorerService.returnAllNPLScoresNANPDB();
            this.plotData.npScoresCHEBI = npScorerService.returnAllNPLScoresCHEBI();
            this.plotData.npScoresNPATLAS = npScorerService.returnAllNPLScoresNPATLAS();
            this.plotData.npScoresTCMDB = npScorerService.returnAllNPLScoresTCMDB();
            this.plotData.npScoresIBS = npScorerService.returnAllNPLScoresIBS();
            this.plotData.npScoresOLD2012 = npScorerService.returnAllNPLScoresOLD2012();
            this.plotData.npScoresZINCNP = npScorerService.returnAllNPLScoresZINCNP();
            this.plotData.npScoresUNPD = npScorerService.returnAllNPLScoresUNPD();
            this.plotData.npScoresSUPERNATURAL = npScorerService.returnAllNPLScoresSUPENATURAL();


            this.plotData.npScoresDRUGBANK = npScorerService.returnAllNPLScoresDRUGBANK();
            this.plotData.npScoresHMDB = npScorerService.returnAllNPLScoresHMDB();



            //Get subplots by organism

            this.plotData.npScoresBACTERIA = npScorerService.returnAllNPLScoresBACTERIA();
            this.plotData.npScoresFUNGI = npScorerService.returnAllNPLScoresFUNGI();
            this.plotData.npScoresPLANTS = npScorerService.returnAllNPLScoresPLANTS();


            //Get subplots by size
            this.plotData.npScoresACTranche1 = npScorerService.returnAllNPLScoresACTranche1();
            this.plotData.npScoresACTranche2 = npScorerService.returnAllNPLScoresACTranche2();
            this.plotData.npScoresACTranche3 = npScorerService.returnAllNPLScoresACTranche3();
            this.plotData.npScoresACTranche4 = npScorerService.returnAllNPLScoresACTranche4();



            // Get counts

            this.plotData.globalCounts = new Hashtable<>();
            this.plotData.globalCounts.put("countAllNP", moleculeRepository.countAllNP());
            this.plotData.globalCounts.put("countAllSM", moleculeRepository.countAllSM());

            this.plotData.globalCounts.put("countUEFS", moleculeRepository.countAllNPBySource("UEFS"));
            this.plotData.globalCounts.put("countHIT", moleculeRepository.countAllNPBySource("HIT"));
            this.plotData.globalCounts.put("countSANCDB", moleculeRepository.countAllNPBySource("SANCDB"));
            this.plotData.globalCounts.put("countAFRODB", moleculeRepository.countAllNPBySource("AFRODB"));
            this.plotData.globalCounts.put("countNPACT", moleculeRepository.countAllNPBySource("NPACT"));
            this.plotData.globalCounts.put("countSELLECKCHEM", moleculeRepository.countAllNPBySource("SELLECKCHEM"));
            this.plotData.globalCounts.put("countCHEMBL", moleculeRepository.countAllNPBySource("CHEMBL"));
            this.plotData.globalCounts.put("countNUBBE", moleculeRepository.countAllNPBySource("NUBBE"));
            this.plotData.globalCounts.put("countSTREPTOMEDB", moleculeRepository.countAllNPBySource("STREPTOMEDB"));
            this.plotData.globalCounts.put("countPUBCHEM", moleculeRepository.countAllNPBySource("PUBCHEM"));
            this.plotData.globalCounts.put("countNANPDB", moleculeRepository.countAllNPBySource("NANPDB"));
            this.plotData.globalCounts.put("countCHEBI", moleculeRepository.countAllNPBySource("CHEBI"));
            this.plotData.globalCounts.put("countNPATLAS", moleculeRepository.countAllNPBySource("NPATLAS"));
            this.plotData.globalCounts.put("countTCMDB", moleculeRepository.countAllNPBySource("TCMDB"));
            this.plotData.globalCounts.put("countIBS", moleculeRepository.countAllNPBySource("IBS"));
            this.plotData.globalCounts.put("countOld2102", moleculeRepository.countAllNPBySource("OLD2012"));
            this.plotData.globalCounts.put("countZINCNP", moleculeRepository.countAllNPBySource("ZINC"));
            this.plotData.globalCounts.put("countUNPD", moleculeRepository.countAllNPBySource("UNPD"));
            this.plotData.globalCounts.put("countSUPERNATURAL", moleculeRepository.countAllNPBySource("SUPERNATURAL"));



            this.plotData.globalCounts.put("countDRUGBANK", moleculeRepository.countAllDrugbank());


            this.plotData.globalCounts.put("countBacteria", moleculeRepository.countAllNPInBacteria());
            this.plotData.globalCounts.put("countPlants", moleculeRepository.countAllNPInPlants());
            this.plotData.globalCounts.put("countFungi", moleculeRepository.countAllNPInFungi());

            this.plotData.globalCounts.put("countTranche1", moleculeRepository.countAllTranche1());
            this.plotData.globalCounts.put("countTranche2", moleculeRepository.countAllTranche2());
            this.plotData.globalCounts.put("countTranche3", moleculeRepository.countAllTranche3());
            this.plotData.globalCounts.put("countTranche4", moleculeRepository.countAllTranche4());





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


                fos = new FileOutputStream("archive/npScoresUEFS.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresUEFS);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresHIT.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresHIT);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresSANCDB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresSANCDB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresAFRODB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresAFRODB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresNPACT.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresNPACT);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresSELLECKCHEM.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresSELLECCHEM);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresCHEMBL.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresCHEMBL);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresNUBBE.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresNUBBE);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresSTREPTOMEDB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresSTREPTOMEDB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresPUBCHEM.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresPUBCHEM);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresNANPDB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresNANPDB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresCHEBI.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresCHEBI);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresNPATLAS.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresNPATLAS);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresTCMDB.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresTCMDB);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresIBS.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresIBS);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresOLD2012.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresOLD2012);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresZINCNP.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresZINCNP);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresUNPD.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresUNPD);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresSUPERNATURAL.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresSUPERNATURAL);
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

                fos = new FileOutputStream("archive/npScoresACTranche1.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresACTranche1);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresACTranche2.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresACTranche2);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresACTranche3.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresACTranche3);
                oos.close();
                fos.close();

                fos = new FileOutputStream("archive/npScoresACTranche4.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.npScoresACTranche4);
                oos.close();
                fos.close();


                fos = new FileOutputStream("archive/globalCounts.ser");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this.plotData.globalCounts);
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





                fis = new FileInputStream("archive/npScoresUEFS.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresUEFS = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresHIT.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresHIT = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresSANCDB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresSANCDB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresAFRODB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresAFRODB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresNPACT.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresNPACT = (Hashtable) ois.readObject();
                ois.close();
                fis.close();


                fis = new FileInputStream("archive/npScoresSELLECKCHEM.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresSELLECCHEM = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresCHEMBL.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresCHEMBL = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresNUBBE.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresNUBBE = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresSTREPTOMEDB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresSTREPTOMEDB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresPUBCHEM.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresPUBCHEM = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresNANPDB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresNANPDB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresCHEBI.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresCHEBI = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresNPATLAS.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresNPATLAS = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresTCMDB.ser");
                ois = new ObjectInputStream(fis);
                this.plotData. npScoresTCMDB = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresIBS.ser");
                ois = new ObjectInputStream(fis);
                this.plotData. npScoresIBS = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresOLD2012.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresOLD2012 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresZINCNP.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresZINCNP = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresUNPD.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresUNPD = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresSUPERNATURAL.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresSUPERNATURAL = (Hashtable) ois.readObject();
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


                fis = new FileInputStream("archive/npScoresACTranche1.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresACTranche1 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresACTranche2.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresACTranche2 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresACTranche3.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresACTranche3 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/npScoresACTranche4.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.npScoresACTranche4 = (Hashtable) ois.readObject();
                ois.close();
                fis.close();

                fis = new FileInputStream("archive/globalCounts.ser");
                ois = new ObjectInputStream(fis);
                this.plotData.globalCounts = (Hashtable) ois.readObject();
                ois.close();
                fis.close();



            }catch(IOException ioe) { ioe.printStackTrace();
            }catch(ClassNotFoundException c) { c.printStackTrace(); }


            this.plotData.isInitialised=true;
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

        //UEFS
        this.plotData.npXchebi.addAll(this.plotData.npScoresUEFS.keySet());
        Collections.sort(this.plotData.npXuefs);

        for (Double i : this.plotData.npXuefs) {
            this.plotData.npYuefs.add(this.plotData.npScoresUEFS.get(i));
        }

        //HIT
        this.plotData.npXhit.addAll(this.plotData.npScoresHIT.keySet());
        Collections.sort(this.plotData.npXhit);

        for (Double i : this.plotData.npXhit) {
            this.plotData.npYhit.add(this.plotData.npScoresHIT.get(i));
        }

        //SANCDB
        this.plotData.npXsancdb.addAll(this.plotData.npScoresSANCDB.keySet());
        Collections.sort(this.plotData.npXsancdb);

        for (Double i : this.plotData.npXsancdb) {
            this.plotData.npYsancdb.add(this.plotData.npScoresSANCDB.get(i));
        }

        //AFRODB
        this.plotData.npXafrodb.addAll(this.plotData.npScoresAFRODB.keySet());
        Collections.sort(this.plotData.npXafrodb);

        for (Double i : this.plotData.npXafrodb) {
            this.plotData.npYafrodb.add(this.plotData.npScoresAFRODB.get(i));
        }

        //NPACT
        this.plotData.npXnpact.addAll(this.plotData.npScoresNPACT.keySet());
        Collections.sort(this.plotData.npXnpact);

        for (Double i : this.plotData.npXnpact) {
            this.plotData.npYnpact.add(this.plotData.npScoresNPACT.get(i));
        }

        //SELLECKCHEM
        this.plotData.npXsellecchem.addAll(this.plotData.npScoresSELLECCHEM.keySet());
        Collections.sort(this.plotData.npXsellecchem);

        for (Double i : this.plotData.npXsellecchem) {
            this.plotData.npYsellecchem.add(this.plotData.npScoresSELLECCHEM.get(i));
        }

        //CHEMBL
        this.plotData.npXchembl.addAll(this.plotData.npScoresCHEMBL.keySet());
        Collections.sort(this.plotData.npXchembl);

        for (Double i : this.plotData.npXchembl) {
            this.plotData.npYchembl.add(this.plotData.npScoresCHEMBL.get(i));
        }

        //NUBBE
        this.plotData.npXnubbe.addAll(this.plotData.npScoresNUBBE.keySet());
        Collections.sort(this.plotData.npXnubbe);

        for (Double i : this.plotData.npXnubbe) {
            this.plotData.npYnubbe.add(this.plotData.npScoresNUBBE.get(i));
        }

        //StreptomeDB
        this.plotData.npXstreptomedb.addAll(this.plotData.npScoresSTREPTOMEDB.keySet());
        Collections.sort(this.plotData.npXstreptomedb);

        for (Double i : this.plotData.npXstreptomedb) {
            this.plotData.npYstreptomedb.add(this.plotData.npScoresSTREPTOMEDB.get(i));
        }

        //PUBCHEM
        this.plotData.npXpubchem.addAll(this.plotData.npScoresPUBCHEM.keySet());
        Collections.sort(this.plotData.npXpubchem);

        for (Double i : this.plotData.npXpubchem) {
            this.plotData.npYpubchem.add(this.plotData.npScoresPUBCHEM.get(i));
        }

        //NANPDB
        this.plotData.npXnanpdb.addAll(this.plotData.npScoresNANPDB.keySet());
        Collections.sort(this.plotData.npXnanpdb);

        for (Double i : this.plotData.npXnanpdb) {
            this.plotData.npYnanpdb.add(this.plotData.npScoresNANPDB.get(i));
        }

        //chebi
        this.plotData.npXchebi.addAll(this.plotData.npScoresCHEBI.keySet());
        Collections.sort(this.plotData.npXchebi);

        for (Double i : this.plotData.npXchebi) {
            this.plotData.npYchebi.add(this.plotData.npScoresCHEBI.get(i));
        }

        //NP ATLAS
        this.plotData.npXnpatlas.addAll(this.plotData.npScoresNPATLAS.keySet());
        Collections.sort(this.plotData.npXnpatlas);

        for (Double i : this.plotData.npXnpatlas) {
            this.plotData.npYnpatlas.add(this.plotData.npScoresNPATLAS.get(i));
        }

        //TCMDB
        this.plotData.npXtcmdb.addAll(this.plotData.npScoresTCMDB.keySet());
        Collections.sort(this.plotData.npXtcmdb);

        for (Double i : this.plotData.npXtcmdb) {
            this.plotData.npYtcmdb.add(this.plotData.npScoresTCMDB.get(i));
        }

        //IBS
        this.plotData.npXibs.addAll(this.plotData.npScoresIBS.keySet());
        Collections.sort(this.plotData.npXibs);

        for (Double i : this.plotData.npXibs) {
            this.plotData.npYibs.add(this.plotData.npScoresIBS.get(i));
        }

        //OLD 2012
        this.plotData.npXold2012.addAll(this.plotData.npScoresOLD2012.keySet());
        Collections.sort(this.plotData.npXold2012);

        for (Double i : this.plotData.npXold2012) {
            this.plotData.npYold2012.add(this.plotData.npScoresOLD2012.get(i));
        }

        //ZINC NP
        this.plotData.npXzincnp.addAll(this.plotData.npScoresZINCNP.keySet());
        Collections.sort(this.plotData.npXzincnp);

        for (Double i : this.plotData.npXzincnp) {
            this.plotData.npYzincnp.add(this.plotData.npScoresZINCNP.get(i));
        }

        //UNPD
        this.plotData.npXunpd.addAll(this.plotData.npScoresUNPD.keySet());
        Collections.sort(this.plotData.npXunpd);

        for (Double i : this.plotData.npXunpd) {
            this.plotData.npYunpd.add(this.plotData.npScoresUNPD.get(i));
        }

        //SUPERNATURAL
        this.plotData.npXsupernatural.addAll(this.plotData.npScoresSUPERNATURAL.keySet());
        Collections.sort(this.plotData.npXsupernatural);

        for (Double i : this.plotData.npXsupernatural) {
            this.plotData.npYsupernatural.add(this.plotData.npScoresSUPERNATURAL.get(i));
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





        //BY SIZE IN TRANCHE 1
        this.plotData.npXacTranche1.addAll(this.plotData.npScoresACTranche1.keySet());
        Collections.sort(this.plotData.npXacTranche1);

        for (Double i : this.plotData.npXacTranche1) {
            this.plotData.npYacTranche1.add(this.plotData.npScoresACTranche1.get(i));
        }

        //BY SIZE IN TRANCHE 2
        this.plotData.npXacTranche2.addAll(this.plotData.npScoresACTranche2.keySet());
        Collections.sort(this.plotData.npXacTranche2);

        for (Double i : this.plotData.npXacTranche2) {
            this.plotData.npYacTranche2.add(this.plotData.npScoresACTranche2.get(i));
        }

        //BY SIZE IN TRANCHE 3
        this.plotData.npXacTranche3.addAll(this.plotData.npScoresACTranche3.keySet());
        Collections.sort(this.plotData.npXacTranche3);

        for (Double i : this.plotData.npXacTranche3) {
            this.plotData.npYacTranche3.add(this.plotData.npScoresACTranche3.get(i));
        }

        //BY SIZE IN TRANCHE 4
        this.plotData.npXacTranche4.addAll(this.plotData.npScoresACTranche4.keySet());
        Collections.sort(this.plotData.npXacTranche4);

        for (Double i : this.plotData.npXacTranche4) {
            this.plotData.npYacTranche4.add(this.plotData.npScoresACTranche4.get(i));
        }






    }







}
