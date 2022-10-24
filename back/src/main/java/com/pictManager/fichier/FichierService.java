package com.pictManager.fichier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.util.IOUtils;
import com.pictManager.fichier.filestorage.FileStorageOnAwsS3;
import com.pictManager.model.User;
import com.pictManager.shared.ObjectNotFindException;

@Service
@Transactional
public class FichierService {
    private final Logger log = LoggerFactory.getLogger(FichierService.class);

    private final FichierRepository fichierRepository;

    private final FileStorageOnAwsS3 fileStorageOnAwsS3;

    private final FichierMapper fichierMapper;

    public FichierService(FichierRepository fichierRepository, FileStorageOnAwsS3 fileStorageOnAwsS3, FichierMapper fichierMapper) {
        this.fichierRepository = fichierRepository;
        this.fileStorageOnAwsS3 = fileStorageOnAwsS3;
        this.fichierMapper = fichierMapper;
    }

    /**
     * save a ficher
     *
     * @param ficherDTO the entity to save
     * @return the persisted entity
     */
    public FichierDTO save(FichierDTO fichierDTO) {
        log.debug("Request to save fichier : {}", fichierDTO);
        Fichier fichier = this.fichierMapper.toEntity(fichierDTO);
        fichier = this.fichierRepository.save(fichier);
        return this.fichierMapper.toDTO(fichier);
    }

    /**
     * get all the fichiers
     *
     * @return the lsit of entities
     */
    @Transactional(readOnly = true)
    public List<FichierDTO> findAll() {
        log.debug("Request to get all fichiers");
        return this.fichierRepository.findAll()
            .stream()
            .map(this.fichierMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get one fichier by id
     *
     * @param id the id of the entity.
     * @return the dto
     */
    @Transactional(readOnly = true)
    public Optional<FichierDTO> findOne(Long id) {
        log.debug("Request to get fichier: {}", id);
        return this.fichierRepository.findById(id).map(this.fichierMapper::toDTO);
    }

    /**
     * Get one fichier by id
     *
     * @param id the id of the entity.
     * @return the dto
     */
    @Transactional(readOnly = true)
    public Optional<Fichier> findById(Long id) {
        log.debug("Request to get fichier: {}", id);
        return this.fichierRepository.findById(id);
    }

    /**
     * Delete the fichier by id
     *
     * @param id the id of the entity
     */
    public void delete(Fichier fichier) {
        log.debug("Request to delete Fichier: {}", fichier);
        this.fichierRepository.deleteById(fichier.getId());
        // this.fileStorageOnAwsS3.delete(fichier);
    }

    /**
     * Delete the fichier by id
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Fichier: {}", id);
        Fichier fichier = this.findById(id).get();
        this.fichierRepository.deleteById(fichier.getId());
        // this.fileStorageOnAwsS3.delete(fichier); // localstack can't handle DELETE methode
    }

    public FichierDTO saveFile(MultipartFile file, User user) throws IOException, Exception {
        Fichier fichier = new Fichier().nom(normalizeFileName(file.getOriginalFilename())).user(user);

        fichier = this.fichierRepository.save(fichier);
        this.fileStorageOnAwsS3.store(fichier, file.getInputStream());
        return this.fichierMapper.toDTO(fichier);
    }

    public Fichier save(MultipartFile file, User user) throws IOException, Exception {
        Fichier fichier = new Fichier().nom(normalizeFileName(file.getOriginalFilename())).user(user);

        fichier = this.fichierRepository.save(fichier);
        this.fileStorageOnAwsS3.store(fichier, file.getInputStream());
        return fichier;
    }

    /**
     * Replace spétial characters
     *
     * @param s
     * @return
     */
    private static String normalizeFileName(String s) {
        return Normalizer.normalize(s, Form.NFD).replace("[^\\p{ASCII}]", "");
    }

    /**
     * Get public URL by id
     *
     * @param fichierId id of file
     * @return public URL
     */
    public String findPublicUrlById(Long fichierId) {
        return this.fichierRepository.findById(fichierId)
            .map(this.fileStorageOnAwsS3::getPublicUrl)
            .orElseThrow(() -> ObjectNotFindException.byId(Fichier.class, fichierId));
    }

    public byte[] getFileAsByteArray(Long id) throws Exception {
        File file = this.fileStorageOnAwsS3.retriever(this.fichierRepository.findById(id)
            .orElseThrow(() -> ObjectNotFindException.byId(Fichier.class, id)));
        InputStream is = new FileInputStream(file);
        return IOUtils.toByteArray(is);
    }

    public ResponseEntity<InputStreamResource> downloadFile(Long id) {
        Fichier fichier = this.fichierRepository.findById(id)
            .orElseThrow(() -> ObjectNotFindException.byId(Fichier.class, id));

        try {
            String mimeType = "application/octet-stream";
            MediaType mediaType = MediaType.valueOf(mimeType);
            File file = this.fileStorageOnAwsS3.retriever(fichier);
            InputStreamResource ressource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType)
                .contentLength(file.length())
                .body(ressource);
        } catch (Exception e) {
            log.error("Downloading Error: {}, {}, {}", e.getClass(), e.getMessage(), e.getCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
