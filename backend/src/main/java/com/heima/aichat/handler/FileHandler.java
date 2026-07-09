package com.heima.aichat.handler;

import com.heima.aichat.entity.po.AiFiles;
import com.heima.aichat.entity.po.AiVectorIds;
import com.heima.aichat.service.IAiFilesService;
import com.heima.aichat.service.IAiVectorIdsService;
import com.heima.commons.exception.BusinessRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 文件向量化处理器
 *
 * <p>根据文件 URL 后缀判断类型，使用对应的 DocumentReader 解析文件内容，
 * 将解析后的文本片段写入 Redis 向量库。支持 PDF、DOCX、TXT。
 */
@Component("HITCH_AI_FILE")
public class FileHandler implements MqHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);
    private static final String TYPE = "AI_FILE";

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private IAiFilesService aiFilesService;

    @Autowired
    private IAiVectorIdsService aiVectorIdsService;

    @Override
    public void add(String ids) {
        for (Integer id : parseIds(ids)) {
            AiFiles file = aiFilesService.getById(id);
            if (file == null) {
                log.warn("AiFiles not found: id={}", id);
                continue;
            }
            if (file.getUrl() == null || file.getUrl().isEmpty()) {
                log.warn("AiFiles url is empty: id={}", id);
                continue;
            }

            try {
                List<Document> docs = parseFile(file);
                if (docs.isEmpty()) {
                    log.warn("No content extracted from file: id={}", id);
                    continue;
                }

                vectorStore.add(docs);
                log.info("Vector docs added for AiFiles id={}, count={}", id, docs.size());

                // 每个文档多条映射：同一 sourceId，不同 documentId
                for (Document doc : docs) {
                    AiVectorIds mapping = new AiVectorIds();
                    mapping.setType(TYPE);
                    mapping.setSourceId(String.valueOf(file.getId()));
                    mapping.setDocumentId(doc.getId());
                    aiVectorIdsService.save(mapping);
                }
            } catch (Exception e) {
                log.error("Failed to process file id={}, url={}: {}", id, file.getUrl(), e.getMessage());
            }
        }
    }

    @Override
    public void update(String ids) {
        delete(ids);
        add(ids);
    }

    @Override
    public void delete(String ids) {
        for (Integer id : parseIds(ids)) {
            List<AiVectorIds> mappings = aiVectorIdsService.listByTypeAndSourceId(TYPE, String.valueOf(id));
            if (mappings.isEmpty()) {
                continue;
            }
            List<String> docIds = mappings.stream()
                    .map(AiVectorIds::getDocumentId)
                    .toList();
            vectorStore.delete(docIds);
            log.info("Vector docs deleted: sourceId={}, count={}", id, docIds.size());
            for (AiVectorIds m : mappings) {
                aiVectorIdsService.removeByDocumentId(m.getDocumentId());
            }
        }
    }

    // ---- 内部 ----

    private List<Integer> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }

    private List<Document> parseFile(AiFiles file){
        String url = file.getUrl();
        String lower = url.toLowerCase();

        if (lower.endsWith(".pdf")) {
            return parsePdf(file.getUrl());
        }else if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".csv")) {
            return parseText(file.getUrl());
        } else {
            // 未知类型
            throw new RuntimeException("Unknown file type, trying as text: "+"name="+ file.getName() + ",url="+ url);
        }
    }

    private List<Document> parsePdf(String url) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                url,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(
                                ExtractedTextFormatter.builder()
                                        .withNumberOfTopTextLinesToDelete(1)
                                        .withNumberOfBottomTextLinesToDelete(1)
                                        .overrideLineSeparator("\n")
                                        .build()
                        )
                        .withPagesPerDocument(1)
                        .build()
        );
        List<Document> documents = reader.read();
        log.info("Pdf documents read: {}", documents.size());
        return splitIfNeeded(documents);
    }

    private List<Document> parseText(String url) {
        TextReader reader = new TextReader(url);
        List<Document> documents = reader.read();
        log.info("Text documents read: {}", documents.size());
        return splitIfNeeded(documents);
    }

    /**
     * 对大文档做 Token 分块，避免单个 Document 超过 Embedding token 上限
     */
    private List<Document> splitIfNeeded(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return Collections.emptyList();
        }
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withKeepSeparator(true)
                .withChunkSize(1000)
                .withMinChunkLengthToEmbed(10)
                .withPunctuationMarks(List.of('#','\n'))
                .build();
        return splitter.apply(docs);
    }
}
