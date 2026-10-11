package com.example.freemonitoring.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 실습용 FAQ 20건을 시작 시점에 SimpleVectorStore에 적재한다.
 * F-4 "비용 폭탄" 재현 실습에서 topK를 4 → 20으로 올리면
 * 프롬프트에 들어가는 문서가 5배가 되어 입력 토큰과 비용이 함께 뛴다.
 */
@Component
public class FaqDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FaqDataLoader.class);

    private final VectorStore vectorStore;

    public FaqDataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Document> documents = FAQ.entrySet().stream()
                .map(e -> new Document(e.getValue(), Map.of("category", e.getKey().split("-")[0])))
                .toList();
        try {
            vectorStore.add(documents);
            log.info("FAQ {}건 적재 완료", documents.size());
        } catch (RuntimeException e) {
            // API 키가 잘못되어도 앱은 기동되도록 한다 (메트릭·오류 확인용).
            // 적재에 실패해도 앱은 계속 뜨고, RAG 응답만 근거 없이 나간다.
            log.warn("FAQ 적재 실패 — 임베딩 모델 연결을 확인하세요: {}", e.getMessage());
        }
    }

    private static final Map<String, String> FAQ = Map.ofEntries(
            Map.entry("배송-1", "일반 배송은 결제 완료 후 영업일 기준 2~3일 내 도착합니다. 도서산간 지역은 1~2일이 추가될 수 있으며, 주문 상세 화면에서 배송 단계를 확인할 수 있습니다."),
            Map.entry("배송-2", "오늘출발 상품은 평일 오후 2시 이전 결제 시 당일 출고됩니다. 토요일, 일요일, 공휴일 주문은 다음 영업일에 출고됩니다."),
            Map.entry("배송-3", "배송비는 3만 원 이상 구매 시 무료이며, 3만 원 미만은 3,000원입니다. 제주도는 3,000원, 도서산간 지역은 5,000원의 추가 배송비가 부과됩니다."),
            Map.entry("배송-4", "배송지 변경은 '상품 준비 중' 단계까지만 가능합니다. '배송 중' 단계부터는 택배사 고객센터를 통해 변경을 요청해야 합니다."),
            Map.entry("배송-5", "해외 배송은 현재 일본, 미국, 싱가포르로만 가능합니다. 관부가세는 수령인이 부담하며 배송 기간은 7~14일입니다."),
            Map.entry("반품-1", "단순 변심 반품은 상품 수령 후 7일 이내에 신청할 수 있으며, 왕복 배송비 6,000원은 고객 부담입니다."),
            Map.entry("반품-2", "상품 불량이나 오배송의 경우 수령 후 30일 이내 반품할 수 있고, 배송비는 전액 회사가 부담합니다. 사진을 첨부하면 처리 속도가 빨라집니다."),
            Map.entry("반품-3", "포장을 개봉하여 상품 가치가 훼손된 경우, 화장품·식품처럼 위생 관련 상품은 반품이 제한될 수 있습니다."),
            Map.entry("반품-4", "환불은 반품 상품 회수 및 검수 완료 후 영업일 기준 3일 이내 처리됩니다. 카드 결제는 카드사 사정에 따라 취소 반영까지 최대 7일이 걸릴 수 있습니다."),
            Map.entry("교환-1", "사이즈·색상 교환은 수령 후 7일 이내 1회에 한해 가능합니다. 교환 상품의 재고가 없으면 반품 후 재주문으로 안내됩니다."),
            Map.entry("포인트-1", "구매 확정 시 결제 금액의 1%가 포인트로 적립됩니다. 멤버십 골드 등급은 2%, VIP 등급은 3%가 적립됩니다."),
            Map.entry("포인트-2", "포인트는 1,000점 이상부터 1점 단위로 사용할 수 있으며, 적립일로부터 1년이 지나면 자동 소멸됩니다."),
            Map.entry("포인트-3", "리뷰 작성 시 텍스트 리뷰는 100점, 사진 리뷰는 500점이 적립됩니다. 리뷰 포인트는 구매 확정 후 30일 이내 작성분에만 지급됩니다."),
            Map.entry("회원-1", "멤버십 등급은 최근 6개월 구매 금액 기준으로 매월 1일 갱신됩니다. 30만 원 이상은 골드, 100만 원 이상은 VIP입니다."),
            Map.entry("회원-2", "회원 탈퇴 시 보유 포인트와 쿠폰은 모두 소멸되며 복구할 수 없습니다. 탈퇴 후 30일간은 같은 이메일로 재가입할 수 없습니다."),
            Map.entry("결제-1", "결제 수단은 신용카드, 체크카드, 계좌이체, 휴대폰 결제, 간편결제를 지원합니다. 무이자 할부는 카드사별 프로모션에 따라 달라집니다."),
            Map.entry("결제-2", "주문 취소는 '결제 완료' 단계에서 즉시 가능하며, '상품 준비 중' 단계부터는 판매자 승인 후 취소됩니다."),
            Map.entry("쿠폰-1", "쿠폰은 주문 1건당 1장만 사용할 수 있으며 포인트와는 중복 사용이 가능합니다. 일부 특가 상품에는 쿠폰이 적용되지 않습니다."),
            Map.entry("쿠폰-2", "신규 가입 시 10% 할인 쿠폰(최대 5,000원)이 지급되며, 발급일로부터 14일간 사용할 수 있습니다."),
            Map.entry("고객센터-1", "고객센터 운영 시간은 평일 오전 9시부터 오후 6시까지이며, 점심시간은 낮 12시부터 1시까지입니다. 주말과 공휴일은 챗봇 상담만 가능합니다.")
    );
}
