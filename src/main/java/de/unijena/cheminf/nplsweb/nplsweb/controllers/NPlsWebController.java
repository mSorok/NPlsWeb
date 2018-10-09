package de.unijena.cheminf.nplsweb.nplsweb.controllers;

import de.unijena.cheminf.nplsweb.nplsweb.reader.ReaderService;
import de.unijena.cheminf.nplsweb.nplsweb.scorer.NpScorerService;
import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageFileNotFoundException;
import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class NPlsWebController {

    private boolean newPlot = false;



    private final StorageService storageService;

    @Autowired
    ReaderService readerService;

    @Autowired
    NpScorerService npScorerService;

    @Autowired
    public NPlsWebController(StorageService storageService) {
        this.storageService = storageService;
    }


    //  ******************************************
    // ******* READING MOLECULES

    @PostMapping("/")
    public String readMoleculesFromFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes){



        if(!file.isEmpty()) {


                storageService.store(file);
                String loadedFile = "upload-dir/" + file.getOriginalFilename();

                if (readerService.startService(loadedFile)) {

                    readerService.doWorkWithFile();

                    npScorerService.setMolecules(readerService.getMolecules());

                    String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

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

    @GetMapping("results")
    public String showResults(Model model) throws IOException{

        model.addAttribute("numberOfProcessedMolecules", ""+readerService.getMolecules().keySet().size()+"");



        if(!npScorerService.processFinished()){
            System.out.println("process not finished");
            model.addAttribute("numberOfProcessedMolecules", ""+readerService.getMolecules().keySet().size()+"");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return("redirect:/results");
        }
        else {

            // sending all user molecules with all scores
            model.addAttribute("scores", npScorerService.returnResultsAsUserUploadedMolecules());
            System.out.println("in results");



            // GENERATING GENERAL PLOTS


            ArrayList<Double> npXnp = new ArrayList<>();
            ArrayList<Double> npYnp = new ArrayList<>();


            ArrayList<Double> npXsm = new ArrayList<>();
            ArrayList<Double> npYsm = new ArrayList<>();

            Hashtable<Double, Double> npScoresNP = null;
            Hashtable<Double, Double> npScoresSM = null;


            if(this.newPlot) {
                //Get general plot for all NP
                npScoresNP = npScorerService.returnAllNPLScoresNP();

                //Get general plot for all SM
                npScoresSM = npScorerService.returnAllNPLScoresSM();

                // and serialize them
                try
                {
                    FileOutputStream fos = new FileOutputStream("archive/npScoresNP.ser");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(npScoresNP);
                    oos.close();
                    fos.close();

                    FileOutputStream fos2 = new FileOutputStream("archive/npScoresSM.ser");
                    ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
                    oos2.writeObject(npScoresSM);
                    oos2.close();
                    fos2.close();

                }catch(IOException ioe) { ioe.printStackTrace(); }
            }
            else{
                // FROM SERIALIZATION

                try
                {
                    FileInputStream fis = new FileInputStream("archive/npScoresNP.ser");
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    npScoresNP = (Hashtable) ois.readObject();
                    ois.close();
                    fis.close();

                    FileInputStream fis2 = new FileInputStream("archive/npScoresSM.ser");
                    ObjectInputStream ois2 = new ObjectInputStream(fis2);
                    npScoresSM = (Hashtable) ois2.readObject();
                    ois2.close();
                    fis2.close();



                }catch(IOException ioe) { ioe.printStackTrace();
                }catch(ClassNotFoundException c) { c.printStackTrace(); }


            }

            npXnp.addAll(npScoresNP.keySet());
            Collections.sort(npXnp);

            for (Double i : npXnp) {
                npYnp.add(npScoresNP.get(i));
            }

            npXsm.addAll(npScoresSM.keySet());
            Collections.sort(npXsm);

            for (Double i : npXsm) {
                npYsm.add(npScoresSM.get(i));
            }



            model.addAttribute( "npXdataNP" ,  npXnp );
            model.addAttribute( "npYdataNP" ,  npYnp );

            model.addAttribute( "npXdataSM" ,  npXsm );
            model.addAttribute( "npYdataSM" ,  npYsm );



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

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(NPlsWebController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));



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

}
