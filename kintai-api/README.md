# kintai-api

Spring Boot製の勤怠API。休憩は**自動算出**。FlywayでDDLを管理。

## ローカル起動
```bash
# Postgres起動は kintai-infra/local/docker-compose.yml を参照
mvn spring-boot:run
