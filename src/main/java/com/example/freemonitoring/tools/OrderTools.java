package com.example.freemonitoring.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 주문 조회 툴 (/api/agent 에서 사용).
 * 툴이 호출되면 spring.ai.tool 관측이 기록되고,
 * 메트릭 라벨 spring_ai_tool_definition_name 으로 툴 이름이 구분된다.
 */
@Component
public class OrderTools {

    private static final Map<String, String> ORDERS = Map.of(
            "A1001", "배송 중 (CJ대한통운, 내일 도착 예정)",
            "A1002", "상품 준비 중",
            "A1003", "배송 완료 (10월 8일 수령)"
    );

    @Tool(description = "주문 번호로 주문의 현재 배송 상태를 조회한다")
    public String getOrderStatus(@ToolParam(description = "주문 번호. 예: A1001") String orderId) {
        // 외부 주문 시스템 호출 지연을 흉내낸다
        sleep(300);
        return ORDERS.getOrDefault(orderId.trim().toUpperCase(), "존재하지 않는 주문 번호입니다");
    }

    @Tool(description = "주문 금액과 지역으로 배송비를 계산한다")
    public int calculateShippingFee(
            @ToolParam(description = "주문 금액(원)") int orderAmount,
            @ToolParam(description = "배송 지역. 일반, 제주, 도서산간 중 하나") String region) {
        int base = orderAmount >= 30_000 ? 0 : 3_000;
        int extra = switch (region) {
            case "제주" -> 3_000;
            case "도서산간" -> 5_000;
            default -> 0;
        };
        return base + extra;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
