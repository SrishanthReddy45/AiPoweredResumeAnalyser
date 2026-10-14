package com.resumeiq.resumeiq.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalStorageService {
    private final Path storageDirectory;

    public LocalStorageService(@Value("${app.storage.directory:./storage}") String storageDirectory) throws IOException {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
        Files.createDirectories(this.storageDirectory);
    }

    public String storeFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1) : "bin";
        String storageKey = UUID.randomUUID() + "." + ext;
        return storeBytes(storageKey, file.getBytes());
    }

    public String storeBytes(String storageKey, byte[] bytes) throws IOException {
        Path target = storageDirectory.resolve(storageKey).normalize();
        if (!target.startsWith(storageDirectory.toAbsolutePath().normalize()) && storageDirectory.isAbsolute()) {
            throw new IOException("Invalid storage key");
        }
        Files.createDirectories(storageDirectory);
        Files.write(target, bytes);
        return storageKey;
    }

    public byte[] readFile(String storageKey) throws IOException {
        Path file = storageDirectory.resolve(storageKey).normalize();
        if (!file.startsWith(storageDirectory)) throw new IOException("Invalid storage key");
        return Files.readAllBytes(file);
    }

    public void deleteFile(String storageKey) throws IOException {
        Path file = storageDirectory.resolve(storageKey).normalize();
        if (!file.startsWith(storageDirectory)) throw new IOException("Invalid storage key");
        Files.deleteIfExists(file);
    }
}
