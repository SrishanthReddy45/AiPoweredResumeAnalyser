package com.resumeiq.resumeiq.service;

import com.resumeiq.resumeiq.dto.ResumeDtos;
import com.resumeiq.resumeiq.entity.ProcessingStatus;
import com.resumeiq.resumeiq.entity.Resume;
import com.resumeiq.resumeiq.entity.User;
import com.resumeiq.resumeiq.exception.BadRequestException;
import com.resumeiq.resumeiq.exception.ResourceNotFoundException;
import com.resumeiq.resumeiq.repository.ResumeRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final LocalStorageService localStorageService;

    public ResumeService(ResumeRepository resumeRepository, LocalStorageService localStorageService) {
        this.resumeRepository = resumeRepository;
        this.localStorageService = localStorageService;
    }

    public Resume uploadResume(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            throw new BadRequestException("File exceeds the 10MB upload limit");
        }

        String originalFilename = file.getOriginalFilename();
        String lowerName = originalFilename == null ? "" : originalFilename.toLowerCase();
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".docx")) {
            throw new BadRequestException("Unsupported file type. Please upload a PDF or DOCX resume");
        }

        try {
            validateFileSignature(file, lowerName);
            String extractedText = extractText(file, originalFilename);
            String storageKey = localStorageService.storeFile(file);
            Resume resume = new Resume();
            resume.setUser(user);
            resume.setOriginalFilename(originalFilename);
            resume.setFileType(lowerName.endsWith(".pdf") ? "pdf" : "docx");
            resume.setFileSize(file.getSize());
            resume.setStorageKey(storageKey);
            resume.setExtractedText(extractedText);
            resume.setParsedResumeData("{\"source\":\"uploaded\",\"status\":\"parsed\"}");
            resume.setProcessingStatus(ProcessingStatus.PARSED);
            return resumeRepository.save(resume);
        } catch (IOException e) {
            throw new BadRequestException("Unable to process the uploaded document");
        }
    }

    public List<Resume> getUserResumes(User user) {
        return resumeRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Resume getResumeById(Long id, User user) {
        return resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    public byte[] downloadResume(Long id, User user) {
        Resume resume = getResumeById(id, user);
        try {
            return localStorageService.readFile(resume.getStorageKey());
        } catch (IOException e) {
            throw new BadRequestException("Unable to download resume");
        }
    }

    public void deleteResume(Long id, User user) {
        Resume resume = getResumeById(id, user);
        try {
            localStorageService.deleteFile(resume.getStorageKey());
        } catch (IOException ignored) {
        }
        resumeRepository.delete(resume);
    }

    public ResumeDtos.ResumeResponse toDto(Resume resume) {
        return new ResumeDtos.ResumeResponse(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getFileType(),
                resume.getFileSize(),
                resume.getProcessingStatus().name(),
                resume.getExtractedText(),
                resume.getParsedResumeData(),
                resume.getCreatedAt()
        );
    }

    private void validateFileSignature(MultipartFile file, String lowerName) throws IOException {
        byte[] bytes = file.getBytes();
        if (lowerName.endsWith(".pdf")) {
            if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F' || bytes[4] != '-') {
                throw new BadRequestException("The uploaded file is not a valid PDF document");
            }
        } else if (lowerName.endsWith(".docx")) {
            if (bytes.length < 4 || bytes[0] != 'P' || bytes[1] != 'K') {
                throw new BadRequestException("The uploaded file is not a valid DOCX document");
            }
        }
    }

    private String extractText(MultipartFile file, String originalFilename) throws IOException {
        byte[] bytes = file.getBytes();
        String lowerName = originalFilename == null ? "" : originalFilename.toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                if (text == null || text.isBlank()) {
                    throw new BadRequestException("No readable text could be extracted from the PDF");
                }
                return text;
            } catch (Exception e) {
                throw new BadRequestException("The uploaded PDF could not be parsed");
            }
        }

        if (lowerName.endsWith(".docx")) {
            try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                String text = extractor.getText();
                if (text == null || text.isBlank()) {
                    throw new BadRequestException("No readable text could be extracted from the DOCX file");
                }
                return text;
            } catch (Exception e) {
                throw new BadRequestException("The uploaded DOCX could not be parsed");
            }
        }

        throw new BadRequestException("Unsupported resume format");
    }
}
