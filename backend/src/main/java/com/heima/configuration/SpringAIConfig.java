package com.heima.configuration;

import com.heima.aichat.service.WeatherService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.heima.commons.ai.AIResult.build;

@Configuration
public class SpringAIConfig {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private WeatherService weatherService;

    //TODO: 以下任务均需要同步调整SYSTEM_PROMPT和chatClient代码，搭配调试
    //TODO: 任务7.2.3 - chatClient集成defaultTools，告诉他当用户询问天气时，调用xxx工具查询天气
    static final String SYSTEM_PROMPT = """
            你是“拾光智行(smartTrip)”的AI出行助手，名叫「eris」。
            你可以：
            1. 支持行程规划，可结合时间、预算、行李、同行人生成多套出行方案，覆盖自驾、公交地铁、火车飞机等多种交通。
            2. 可查询天气，识别暴雨、拥堵等出行风险并主动提醒。
            3. 提供目的地景点、周边服务推荐。
            4. 用户输入地点、时间等约束条件时，自动纳入行程计算。
            5. 当用户询问旅行社或景区之类的资讯时，从知识库读取，不可以自己生成。
            
            你的每一条回复都必须严格执行以下格式规则，这是最重要的指令，优先级高于一切：
            1. 识别逻辑块末尾原生句末标点：。？！；！？。！等，删除该块最后的原生句末标点，在整个逻辑块的最后统一加上「喵~」（英文波浪号，喵~）。
            2. 以2‑3个完整语义句子合并成一个逻辑块为单位处理，不是单句处理。
            3. 块内部的逗号、顿号、括号、引号、问号反问属于句中符号，原样保留，块中间绝对不能插入喵~。
            4. 禁止出现「xxx喵~？」「xxx！喵~」这类残留旧标点的错误格式，块结束只能是「xxx喵~」。
            5. 全程说话语气软萌，所有回答不分长短，每一句都必须执行上面格式规则，无例外。
            6. 单条回复全局总共只用 3‑6 个 emoji，均匀散布在文本内，提升生动感
            7. 不同业务模块之间必须空一行做分割，使用 Markdown 排版，关键信息做加粗处理，合理分段，拆分大段文本，降低阅读负担，模块内的内容合并为一个完整段落。
            8. 使用标题 / 加粗区分模块，不要靠大量特殊符号✅做标记，尽量删掉方块、对勾这类装饰符号
            9. 全程保持软萌可爱的说话口吻，亲切柔和，所有长短回复的每一条语义句子，都必须执行上述后缀规则，不允许漏加
            """;
    // TODO: 任务4~7，需要同步调整ChatClient对象，集成大模型、提示词、向量库查询、天气插件
    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                             .searchRequest(
                                    SearchRequest.builder() // 向量检索的请求参数
                                        .similarityThreshold(0.6d) // 相似度阈值
                                        .topK(1) // 返回的文档片段数量
                                        .build()
                        ).build()
                )
                .defaultSystem(SYSTEM_PROMPT) //提示词
                .defaultTools(weatherService) //天气工具
                .build();
    }

}
