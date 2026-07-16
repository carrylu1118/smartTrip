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

        //使用aiFilesService，根据id从数据库中查到对应的AiFiles


        //判断AiFiles里url的后缀名是什么，目前可以只开发（pdf，md，txt文件）


        //spring根据不同的文件类型提供了不同的DocumentReader
        // pdf ==> PagePdfDocumentReader
        // md,txt ==> TextReader
        //研究一下这些Reader的使用


        //注意！如果返回的Document过长，可能会报错，这里提供了一个切分工具：splitIfNeeded，可能会帮到你


        //调用vectorStore.add，将reader返回的documents写入redis向量库

        //调用aiVectorIdsService，把document的id和mysql里aifiles的id写进中间表，后续删除要用到

    }

    @Override
    public void update(String ids) {
        delete(ids);
        add(ids);
    }

    @Override
    public void delete(String ids) {
        //参考任务5.2.2里的删除思路
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

        return splitIfNeeded(null);
    }

    private List<Document> parseText(String url) {

        return splitIfNeeded(null);
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
                .withPunctuationMarks(List.of('#','\n'))
                .build();
        return splitter.apply(docs);
    }
}
