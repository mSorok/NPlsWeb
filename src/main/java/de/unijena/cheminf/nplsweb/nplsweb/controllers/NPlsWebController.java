package de.unijena.cheminf.nplsweb.nplsweb.controllers;

import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageFileNotFoundException;
import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

@Controller
public class NPlsWebController {

    private final StorageService storageService;

    @Autowired
    public NPlsWebController(StorageService storageService) {
        this.storageService = storageService;
    }


    //  ******************************************
    // ******* READING MOLECULES

    @PostMapping("/")
    public String readMolecules(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes){

        if(!file.isEmpty()) {
            storageService.store(file);
            String loadedFile = "upload-dir/" + file.getOriginalFilename();

            // read molecules
            //launch computation


        }
        else{

            redirectAttributes.addFlashAttribute("noFileError", "You need to load a file!");


            return "redirect:/";

        }


        return "/";
    }


    // ******************************************
    // ***** SCORING **********************


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
