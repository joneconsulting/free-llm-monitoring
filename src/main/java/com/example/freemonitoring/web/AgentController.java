package com.example.freemonitoring.web;

import com.example.freemonitoring.tools.OrderTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tool 호출 요청: 모델이 툴을 고르고(1차) → 툴 실행 → 결과로 답변(2차)
 *   → 요청 1번에 모델 호출이 2번 기록된다 (대시보드 "모델 호출 수")
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ChatClient chatClient;
    private final OrderTools orderTools;

    public AgentController(ChatClient chatClient, OrderTools orderTools) {
        this.chatClient = chatClient;
        this.orderTools = orderTools;
    }

    @GetMapping
    public String ask(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .tools(orderTools)
                .call()
                .content();
    }
}
