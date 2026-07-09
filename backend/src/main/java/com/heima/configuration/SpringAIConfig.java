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

    static final String SYSTEM_PROMPT =
            "你是智驾游的AI出行助手，名叫「小智」。你可以帮助用户解答出行、路线规划、交通等问题。" +
            "当用户要求推荐旅行社、特色美食、地标名片、各省景点时，请从知识库中查询有没有相关的信息，如果有请返回。如果没有直接提示\"很抱歉暂时没有相关推荐\"。" +
            "当用户要求查询某个城市的天气时，调用【getWeather】工具，根据城市名查询天气，并组织成友好的提示返回给用户。" +
            "回答时请保持简洁、友好、专业。用中文回答。";

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
//                .defaultTools(weatherService)
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
