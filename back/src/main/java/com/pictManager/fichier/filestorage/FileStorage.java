package com.pictManager.fichier.filestorage;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import com.pictManager.fichier.Fichier;

public interface FileStorage {
    void store(Fichier fichier, File file) throws Exception;
    void store(Fichier fichier, InputStream inputStream) throws Exception;
    File retriever(Fichier fichier) throws Exception;
    void delete(Fichier fichier);
    boolean exists(Fichier fichier);
    String getPublicUrl(Fichier fichier, Date expiration);
}
