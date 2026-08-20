package com.heima.aichat.handler;

import com.heima.aichat.entity.po.AiFiles;
import com.heima.aichat.entity.po.AiVectorIds;
import com.heima.aichat.service.IAiFilesService;
import com.heima.aichat.service.IAiVectorIdsService;
import com.heima.commons.exception.BusinessRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.checkerframework.checker.units.qual.A;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 文件向量化处理器
 * TODO: 任务6.2.2 - 完成消费端消息处理（文件类消息）
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
        List<Integer> idList = parseIds(ids);
        if (idList == null || idList.isEmpty()) {
            log.warn("add vector, idList is empty");
            return;
        }

        //使用aiFilesService，根据id从数据库中查到对应的AiFiles
        for (Integer fileId : idList) {
            AiFiles aiFiles = aiFilesService.getById(fileId);
            if (aiFiles == null) {
                log.warn("FileHandler not found, id: {}", fileId);
                continue;
            }
            String fileUrl = aiFiles.getUrl();
            if (fileUrl == null || fileUrl.isEmpty()) {
                log.warn("add vector, file url is blank, id: {}", fileId);
                continue;
            }
            List<Document> rawDocuments;
            try {
                //判断AiFiles里url的后缀名是什么，目前可以只开发（pdf，md，txt文件）
                rawDocuments = parseFile(aiFiles);
                //spring根据不同的文件类型提供了不同的DocumentReader
                // pdf ==> PagePdfDocumentReader
                // md,txt ==> TextReader
            } catch (Exception e) {
                log.error("add vector, parseFile error, fileId={}", fileId, e);
                continue;
            }

            if (rawDocuments.isEmpty()) {
                log.warn("add vector, no document content, fileId={}", fileId);
                continue;
            }
            //注意！如果返回的Document过长，可能会报错，这里提供了一个切分工具：splitIfNeeded，可能会帮到你
            List<Document> splitDocuments = splitIfNeeded(rawDocuments);
            //调用vectorStore.add，将reader返回的documents写入redis向量库
            vectorStore.add(splitDocuments);
            //调用aiVectorIdsService，把document的id和mysql里aifiles的id写进中间表，后续删除要用到
            for (Document document : splitDocuments) {
                AiVectorIds aiVectorIds = new AiVectorIds();
                aiVectorIds.setType(TYPE);
                aiVectorIds.setSourceId(String.valueOf(fileId));
                aiVectorIds.setDocumentId(document.getId());
                aiVectorIdsService.save(aiVectorIds);
            }
            log.info("文件向量化完成 fileId={},分片数量={}", fileId, splitDocuments.size());
        }
    }

    @Override
    public void update(String ids) {
        delete(ids);
        add(ids);
    }

    @Override
    public void delete(String ids) {
        List<Integer> idList = parseIds(ids);
        if (idList == null || idList.isEmpty()) {
            log.warn("delete向量，解析后的id集合为空");
            return;
        }
        for (Integer id : idList) {
            try {
                List<AiVectorIds> aiVectorIdsList = aiVectorIdsService.listByTypeAndSourceId(TYPE, String.valueOf(id));
                if (aiVectorIdsList == null || aiVectorIdsList.isEmpty()){
                    log.warn("delete向量，id={}对应的aiVectorIds为空", id);
                    continue;
                }
                List<String> documentIdList = aiVectorIdsList.stream()
                        .map(AiVectorIds::getDocumentId)
                        .toList();

                //使用vectorStore.delete删除redis里的向量数据
                vectorStore.delete(documentIdList);
                log.info("Redis向量删除成功，id={}, documentIdList={}", id, documentIdList);

                //使用aiVectorIdsService删除mysql中间表里的数据
                for (String docId : documentIdList) {
                    aiVectorIdsService.removeByDocumentId(docId);
                    log.info("中间映射表删除成功，id={}, documentId={}", id, docId);
                }
            } catch (Exception e){
                log.error("删除向量发生异常，id={}", id, e);
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

    private List<Document> parseFile(AiFiles file) throws Exception {
        String url = file.getUrl();
        String lower = url.toLowerCase();

        if (lower.endsWith(".pdf")) {
            return parsePdf(file.getUrl());
        } else if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".csv")) {
            return parseText(file.getUrl());
        } else {
            log.warn("unsupported file type, name={}, url={}",file.getName(),url);
            return Collections.emptyList();
        }
    }

    private List<Document> parsePdf(String url) throws Exception {
        Resource resource = new UrlResource(url);
        try {
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
            return pdfReader.read();
        } catch (Exception e) {
            log.error("parsePdf error, url={}", url, e);
            return Collections.emptyList();
        }
    }

    private List<Document> parseText(String url) throws Exception{
        Resource resource = new UrlResource(url);
        try {
            TextReader textReader = new TextReader(resource);
            return textReader.read();
        } catch (Exception e) {
            log.error("parseText error, url={}", url, e);
            return Collections.emptyList();
        }
    }

    /**
     * 工具：对大文档做 Token 分块，避免单个 Document 超过 Embedding token 上限
     */
    private List<Document> splitIfNeeded(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return Collections.emptyList();
        }
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withKeepSeparator(true)
                .withChunkSize(1000)
                .withMinChunkLengthToEmbed(10)
                .withPunctuationMarks(List.of('#', '\n'))
                .build();
        return splitter.apply(docs);
    }
}
