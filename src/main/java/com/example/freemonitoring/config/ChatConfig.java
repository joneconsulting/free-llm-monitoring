package com.example.freemonitoring.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    /**
     * 자동 구성된 ChatClient.Builder에는 ObservationRegistry가 이미 연결되어 있다.
     * → 별도 설정 없이 spring.ai.chat.client 관측이 기록된다.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        당신은 'WeLab 쇼핑몰' 고객센터 상담원입니다.
                        한국어로 간결하게 답하고, 모르는 내용은 모른다고 답하세요.
                        """)
                .build();
    }

    /**
     * 주의: VectorStore를 직접 Bean으로 만들 때는 ObservationRegistry를 넘겨야 한다.
     * 넘기지 않으면 NOOP 레지스트리가 사용되어 db.vector.client.operation 메트릭이 생기지 않는다.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel,
                                   ObservationRegistry observationRegistry) {
        return SimpleVectorStore.builder(embeddingModel)
                .observationRegistry(observationRegistry)
                .build();
    }
}
