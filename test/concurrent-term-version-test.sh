#!/bin/bash

# 약관 ID 9(TERM_TEST)에 대해 10개의 동시 POST 요청을 보냄
URL="http://localhost:8081/v1/admin/terms/9/versions"
DATA='{"content":"이 약관은 테스트 약관으로 생성되었으며, 실제 서비스에서는 사용되지 않습니다.","isCurrent":false}'

# 동시에 10개 요청 실행
for i in {1..10}; do
  curl -X POST \
    -H "Content-Type: application/json" \
    -d "$DATA" \
    "$URL" &
done

# 모든 백그라운드 프로세스가 완료될 때까지 대기
wait
echo "모든 요청 완료"
