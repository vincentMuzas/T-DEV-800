package com.pictManager.fichier;

import java.util.List;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pictManager.config.JwtTokenUtil;
import com.pictManager.model.User;
import com.pictManager.service.UserService;

@RestController
@RequestMapping("/api")
public class FichierRessource {
    private final Logger log = LoggerFactory.getLogger(FichierRessource.class);
    // private static final String ENTITY_NAME = "fichier";
    @Value("${app.name}")
    private String appName;

    private final FichierService fichierService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public FichierRessource(FichierService fichierService, UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.fichierService = fichierService;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * {@code POST /fichiers} : upload a file
     *
     * @param file the file to upload
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/fichiers", consumes = "multipart/form-data")
    public ResponseEntity<FichierDTO> saveFile(@RequestParam("file") MultipartFile file, @RequestHeader(name="Authorization") String token) throws Exception {
        log.debug("file : {}, size : {}", file.getName(), file.getSize());
        final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
        final FichierDTO fichierDTO = this.fichierService.saveFile(file, user);
        return ResponseEntity.ok().body(fichierDTO);
    }

    /**
     * {@code GET /fichiers} : get all the fichiers
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fichiers
     */
    @GetMapping("/fichiers")
    public List<FichierDTO> getAllFichier() {
        log.debug("REST request to get all fichiers");
        return this.fichierService.findAll();
    }

    /**
     * {@code GET /fichiers/:id} : get the coresponding Fichier
     *
     * @param id the id of the FIchier to retrieve
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the Fichier
     */
    @GetMapping("/fichier/{id}")
    public ResponseEntity<FichierDTO> getFichier(@PathVariable Long id) {
        log.debug("REST request to get fichier: {}", id);
        Optional<FichierDTO> fichier = this.fichierService.findOne(id);
        if (fichier.isPresent())
            return ResponseEntity.ok().body(fichier.get());
        else
            return ResponseEntity.badRequest().build();
    }

    /**
     * {@code DELETE /fichiers/:id} : delete fichier
     *
     * @param id the id of {@link Fichier} to delete
     * @return the {@link RensponseEntity} with status {@code 204 (NO_CONTENT)}
     */
    @DeleteMapping("/fichiers/{id}")
    public ResponseEntity<Void> deleteFichier(@PathVariable Long id) {
        log.debug("REST request to delete fichier : {}", id);
        this.fichierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fichiers/public-url/{id}")
    public ResponseEntity<String> getPublicUrl(@PathVariable Long id) {
        log.debug("REST request to get url for fichier: {}", id);
        String publicUrl = this.fichierService.findPublicUrlById(id);
        return ResponseEntity.ok(publicUrl);
    }

    @GetMapping("/fichiers/file/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) throws Exception {
        log.debug("RES request to get file {} in byte array", id);
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());;
        byte[] media = this.fichierService.getFileAsByteArray(id);
        return new ResponseEntity<>(media, headers, HttpStatus.SC_OK);
    }

    /*
     * Download a Fichier
     *
     * @param id fichier id
     * @return Fichier
     */
    @GetMapping("/fichiers/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) {
        log.debug("REST request to download file {}", id);
        return this.fichierService.downloadFile(id);
    }
}
