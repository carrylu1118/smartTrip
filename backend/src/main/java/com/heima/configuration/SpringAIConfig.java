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

@Configuration
public class SpringAIConfig {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private WeatherService weatherService;

    //TODO: 任务4.2.2 - 提示词工程，优化以下提示词，给他个名字角色，让它知道自己可以做什么
    //TODO: 任务5.2.3 - 优化提示词，告诉他当用户询问旅行社之类的资讯时，从知识库读取
    //TODO: 任务6.2.3 - 优化提示词，告诉他当用户询问景区之类的百科时，从知识库读取
    //TODO: 任务7.2.3 - 优化提示词，告诉他当用户询问天气时，调用xxx工具查询天气
    static final String SYSTEM_PROMPT =
            "你是智驾游的AI出行助手，名叫「xxx」。你可以xxx。";

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(weatherService)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore).searchRequest(
                                SearchRequest.builder()
                                        .similarityThreshold(0.5)
                                        .topK(3)
                                        .build()
                        ).build()
                ).build();
    }

}
