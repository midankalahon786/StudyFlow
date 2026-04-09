package com.r786.studyflow.core.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    public FileStorageService(){
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try{
            Files.createDirectories(this.fileStorageLocation);
        }catch (IOException ex){
            throw new RuntimeException("Could not create the directory where uploaded files will be stored.",ex);
        }
    }

    public String storeFile(MultipartFile file, String subFolder){
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        String fileExtension = "";
        if(originalFileName.contains(".")){
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + fileExtension;

        try{
            Path targetLocation = this.fileStorageLocation.resolve(subFolder).resolve(fileName).normalize();
            if(!targetLocation.startsWith(this.fileStorageLocation)){
                throw new RuntimeException("Cannot store file outside of current directory.");
            }
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        }catch (IOException ex){
            throw new RuntimeException("Could not store file" + fileName + ". Please try again!",ex);
        }
    }
}
