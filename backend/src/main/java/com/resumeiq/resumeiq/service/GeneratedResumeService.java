package com.resumeiq.resumeiq.service;

import com.resumeiq.resumeiq.dto.GeneratedResumeDtos;
import com.resumeiq.resumeiq.ai.AiAnalysisProvider;
import com.resumeiq.resumeiq.entity.GeneratedResume;
import com.resumeiq.resumeiq.entity.Resume;
import com.resumeiq.resumeiq.entity.ResumeAnalysis;
import com.resumeiq.resumeiq.entity.User;
import com.resumeiq.resumeiq.exception.BadRequestException;
import com.resumeiq.resumeiq.exception.ResourceNotFoundException;
import com.resumeiq.resumeiq.repository.GeneratedResumeRepository;
import com.resumeiq.resumeiq.repository.ResumeAnalysisRepository;
import com.resumeiq.resumeiq.repository.ResumeRepository;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class GeneratedResumeService {
    private final GeneratedResumeRepository generatedResumeRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final LocalStorageService localStorageService;
    private final AiAnalysisProvider aiAnalysisProvider;

    public GeneratedResumeService(GeneratedResumeRepository generatedResumeRepository,
                                 ResumeRepository resumeRepository,
                                 ResumeAnalysisRepository analysisRepository,
                                 LocalStorageService localStorageService,
                                 AiAnalysisProvider aiAnalysisProvider) {
        this.generatedResumeRepository = generatedResumeRepository;
        this.resumeRepository = resumeRepository;
        this.analysisRepository = analysisRepository;
        this.localStorageService = localStorageService;
        this.aiAnalysisProvider = aiAnalysisProvider;
    }

    public GeneratedResume generateResume(Long resumeId, Long analysisId, User user) {
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        ResumeAnalysis analysis = analysisRepository.findByIdAndUser(analysisId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
        if (!analysis.getResume().getId().equals(resume.getId())) {
            throw new BadRequestException("The selected analysis does not belong to this resume");
        }

        String sourceText = resume.getExtractedText() == null ? "" : resume.getExtractedText().trim();
        if (sourceText.isBlank()) throw new BadRequestException("The source resume has no readable text to improve");
        String improvedContent = aiAnalysisProvider.improveResume(sourceText, analysis.getJobDescription(), analysis.getRecommendations());
        if (improvedContent == null || improvedContent.isBlank()) {
            throw new BadRequestException("Resume generation returned empty content. Please retry.");
        }
        improvedContent = normalizeResumeText(improvedContent);
        String storageKey = UUID.randomUUID() + ".docx";
        try {
            byte[] bytes = renderDocx(improvedContent);
            localStorageService.storeBytes(storageKey, bytes);
        } catch (IOException e) {
            throw new BadRequestException("Unable to generate improved resume");
        }

        GeneratedResume generatedResume = new GeneratedResume();
        generatedResume.setUser(user);
        generatedResume.setSourceResume(resume);
        generatedResume.setAnalysis(analysis);
        generatedResume.setFilename("improved-resume.docx");
        generatedResume.setFileType("docx");
        generatedResume.setStorageKey(storageKey);
        generatedResume.setContent(improvedContent);
        return generatedResumeRepository.save(generatedResume);
    }

    public List<GeneratedResume> getGeneratedResumes(User user) {
        return generatedResumeRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public GeneratedResume getGeneratedResume(Long id, User user) {
        return generatedResumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Generated resume not found"));
    }

    public GeneratedResumeDtos.GeneratedResumeResponse toDto(GeneratedResume generatedResume) {
        return new GeneratedResumeDtos.GeneratedResumeResponse(
                generatedResume.getId(),
                generatedResume.getSourceResume().getId(),
                generatedResume.getFilename(),
                generatedResume.getFileType(),
                generatedResume.getContent()
        );
    }

    public GeneratedResume patchGeneratedResume(Long id, User user, String content) {
        if (content == null || content.isBlank()) throw new BadRequestException("Resume content is required");
        if (content.length() > 50000) throw new BadRequestException("Resume content exceeds the 50,000 character limit");
        GeneratedResume generatedResume = getGeneratedResume(id, user);
        generatedResume.setContent(content);
        try {
            localStorageService.storeBytes(generatedResume.getStorageKey(), renderDocx(content));
        } catch (IOException e) {
            throw new BadRequestException("Unable to save the updated resume document");
        }
        return generatedResumeRepository.save(generatedResume);
    }

    public byte[] downloadGeneratedResume(Long id, User user, String format) {
        GeneratedResume generatedResume = getGeneratedResume(id, user);
        if ("pdf".equalsIgnoreCase(format)) {
            return renderPdf(generatedResume.getContent());
        }
        if (!"docx".equalsIgnoreCase(format)) {
            throw new BadRequestException("Unsupported download format. Choose DOCX or PDF.");
        }
        try {
            return renderDocx(generatedResume.getContent());
        } catch (IOException e) {
            throw new BadRequestException("Unable to download generated resume");
        }
    }

    private String normalizeResumeText(String source) {
        String normalized = source.replace("\r\n", "\n").replace('\r', '\n');
        StringBuilder result = new StringBuilder();
        for (String rawLine : normalized.split("\n")) {
            String line = rawLine.replaceAll("\\s+", " ").trim();
            if (line.isEmpty()) {
                if (result.length() > 0 && result.charAt(result.length() - 1) != '\n') result.append('\n');
                continue;
            }
            if (line.length() > 180 && !line.startsWith("- ") && !line.startsWith("• ")) {
                for (String sentence : line.split("(?<=[.!?])\\s+")) {
                    if (!sentence.isBlank()) result.append("• ").append(sentence.trim()).append('\n');
                }
            } else {
                result.append(line).append('\n');
            }
        }
        return result.toString().trim();
    }

    private byte[] renderDocx(String content) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            String[] lines = content.split("\\n");
            for (String line : lines) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setFontFamily("Arial");
                run.setFontSize(line.length() < 45 && line.equals(line.toUpperCase()) ? 13 : 10);
                run.setBold(line.length() < 45 && line.equals(line.toUpperCase()));
                run.setText(line);
            }
            document.write(output);
            return output.toByteArray();
        }
    }

    private byte[] renderPdf(String content) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            for (String line : content.split("\\n")) {
                document.add(new Paragraph(line));
            }
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BadRequestException("Unable to render PDF export");
        }
    }
}
