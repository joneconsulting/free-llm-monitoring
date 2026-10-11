# Windows PowerShell 용 부하 스크립트 (load-test.sh 와 동일한 동작)
#
#   .\scripts\load-test.ps1                    # 기본: 30회, topK=4
#   .\scripts\load-test.ps1 -Count 60
#   .\scripts\load-test.ps1 -TopK 20           # F-4 비용 폭탄 재현
#
# 실행이 막히면(스크립트 실행 정책): PowerShell 에서 한 번만
#   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
#
# 주의: OpenAI 를 사용 중이면 실제 비용이 발생한다

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Count = 30,
    [int]$TopK = 4
)

$Questions = @(
    "배송비는 얼마인가요?",
    "반품은 언제까지 할 수 있나요?",
    "포인트는 언제 소멸되나요?",
    "VIP 등급 기준이 뭐예요?",
    "제주도 배송비 알려주세요",
    "쿠폰이랑 포인트 같이 써도 돼요?",
    "고객센터 운영 시간은요?"
)
$AgentMessages = @(
    "주문 A1001 어디쯤 왔어?",
    "A1002 주문 상태 알려줘",
    "2만원어치 사면 제주도 배송비 얼마야?"
)

Write-Host "▶ $BaseUrl 에 ${Count}회 요청 (topK=$TopK)"
for ($i = 1; $i -le $Count; $i++) {
    switch ($i % 3) {
        0 { $q = $AgentMessages | Get-Random
            $url = "$BaseUrl/api/agent?message=" + [uri]::EscapeDataString($q) }
        1 { $q = $Questions | Get-Random
            $url = "$BaseUrl/api/rag?topK=$TopK&question=" + [uri]::EscapeDataString($q) }
        2 { $q = $Questions | Get-Random
            $url = "$BaseUrl/api/chat?message=" + [uri]::EscapeDataString($q) }
    }
    try {
        $code = (Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 120).StatusCode
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
    }
    "[{0:D2}/{1:D2}] {2}  {3}" -f $i, $Count, $code, $q | Write-Host
    Start-Sleep -Milliseconds 500
}
Write-Host "✔ 완료 — Grafana: http://localhost:3000/d/free-llm-monitoring"
