# free-llm-monitoring

무료 강의 『Spring AI LLM 비용 모니터링 — Actuator 한 줄로 만드는 토큰 대시보드』 실습 프로젝트

- Spring Boot 3.5 · Spring AI 1.0.x GA · Java 21
- Actuator · Micrometer · Prometheus · Grafana

> 원화 비용 메트릭, 분산 추적(Jaeger), LLM 헬스 체크, 알림 규칙과 장애 재현은
> 『Spring 개발자를 위한 실전 LLM 서비스 개발』 섹션 16 에서 이 프로젝트를 확장해 다룹니다.

## 1. 실행

```bash
# ① 관측 스택 기동 (Prometheus, Grafana)
cd observability
docker compose up -d

# ② 애플리케이션 실행 (OpenAI) — 프로젝트 루트에서
cd ..
export OPENAI_API_KEY=sk-...
./gradlew bootRun

# ③ 부하 발생 → 대시보드 확인
./scripts/load-test.sh
```

Windows (PowerShell):
```powershell
cd observability; docker compose up -d; cd ..
$env:OPENAI_API_KEY="sk-..."
.\gradlew.bat bootRun

# 새 PowerShell 창에서
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

.\scripts\load-test.ps1
```

| 도구 | 주소 |
|---|---|
| Grafana 대시보드 | http://localhost:3000/d/free-llm-monitoring (admin / admin) |
| Prometheus | http://localhost:9090 |
| Actuator | http://localhost:8080/actuator |

### Ollama 로 실행 (무료)
```bash
cd observability && docker compose --profile ollama up -d
docker exec fm-ollama ollama pull llama3.2
docker exec fm-ollama ollama pull nomic-embed-text
cd .. && ./gradlew bootRun --args='--spring.ai.model.chat=ollama --spring.ai.model.embedding=ollama'
```

## 2. 강의별 코드 위치

| 강의 | 파일 |
|---|---|
| F-0 실행 확인 | `http/observability.http` 0) |
| F-2 메트릭 | `build.gradle`(actuator), `application.yml`(management), `web/ChatController` |
| F-3 대시보드 | `build.gradle`(micrometer-registry-prometheus), `observability/docker-compose.yml`, `prometheus.yml`, `grafana/dashboards/free-llm-monitoring-dashboard.json` |
| F-4 비용 폭탄 | `web/RagController`(topK), `scripts/load-test.sh` (`TOPK=20`) |

## 3. 엔드포인트

| 엔드포인트 | 설명 |
|---|---|
| `GET /api/chat?message=` | 단순 채팅 |
| `GET /api/chat/stream?message=` | 스트리밍 채팅 |
| `GET /api/rag?question=&topK=4` | FAQ 20건 검색 + 답변 (시작 시 FAQ 임베딩·적재) |
| `GET /api/agent?message=` | 주문 조회 툴 사용 (모델 호출 2회) |

## 4. 주요 메트릭 (Prometheus 이름)

| Micrometer | Prometheus | 비고 |
|---|---|---|
| `gen_ai.client.operation` | `gen_ai_client_operation_seconds_{count,sum,max,bucket}` | ChatModel·EmbeddingModel 호출 시간 |
| `gen_ai.client.token.usage` | `gen_ai_client_token_usage_total` | 라벨 `gen_ai_token_type`=input/output/total |
| `db.vector.client.operation` | `db_vector_client_operation_seconds_*` | 벡터 검색·저장 시간 |

## 5. 정리
```bash
# 앱: Ctrl + C
cd observability
docker compose down        # 컨테이너 정리
docker compose down -v     # Ollama 모델까지 삭제
```

## 6. [Week 0 검증 대상]
- `./gradlew build` 로 컴파일 확인 (제작 환경에서는 의존성 저장소 접근 불가로 미확인)
- `/actuator/prometheus` 실제 출력으로 메트릭·라벨 이름 확인
- Docker 이미지 태그 최신 안정판 확인
