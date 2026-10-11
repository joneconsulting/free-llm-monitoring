#!/usr/bin/env bash
# 대시보드에 그래프를 그리기 위한 간단한 부하 스크립트
#
#   ./scripts/load-test.sh                 # 기본: 30회, topK=4
#   COUNT=60 ./scripts/load-test.sh
#   TOPK=20 ./scripts/load-test.sh         # F-4 비용 폭탄 재현
#
# 주의: OpenAI 를 사용 중이면 실제 비용이 발생한다 (gpt-4o-mini 기준 30회 ≈ 수십 원 수준)

BASE_URL="${BASE_URL:-http://localhost:8080}"
COUNT="${COUNT:-30}"
TOPK="${TOPK:-4}"

QUESTIONS=(
  "배송비는 얼마인가요?"
  "반품은 언제까지 할 수 있나요?"
  "포인트는 언제 소멸되나요?"
  "VIP 등급 기준이 뭐예요?"
  "제주도 배송비 알려주세요"
  "쿠폰이랑 포인트 같이 써도 돼요?"
  "고객센터 운영 시간은요?"
)
AGENT_MESSAGES=(
  "주문 A1001 어디쯤 왔어?"
  "A1002 주문 상태 알려줘"
  "2만원어치 사면 제주도 배송비 얼마야?"
)

urlencode() { python3 -c "import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1]))" "$1"; }

echo "▶ ${BASE_URL} 에 ${COUNT}회 요청 (topK=${TOPK})"
for i in $(seq 1 "$COUNT"); do
  case $((i % 3)) in
    0) q="${AGENT_MESSAGES[$((RANDOM % ${#AGENT_MESSAGES[@]}))]}"
       url="${BASE_URL}/api/agent?message=$(urlencode "$q")" ;;
    1) q="${QUESTIONS[$((RANDOM % ${#QUESTIONS[@]}))]}"
       url="${BASE_URL}/api/rag?topK=${TOPK}&question=$(urlencode "$q")" ;;
    2) q="${QUESTIONS[$((RANDOM % ${#QUESTIONS[@]}))]}"
       url="${BASE_URL}/api/chat?message=$(urlencode "$q")" ;;
  esac
  code=$(curl -s -o /dev/null -w "%{http_code}" "$url")
  printf "[%02d/%02d] %s  %s\n" "$i" "$COUNT" "$code" "$q"
  sleep 0.5
done
echo "✔ 완료 — Grafana: http://localhost:3000/d/free-llm-monitoring"
