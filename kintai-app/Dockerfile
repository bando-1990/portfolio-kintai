# ─────────────────────────────────────
# 開発ステージ（docker compose up で使用）
# ─────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS development
WORKDIR /app

# 依存関係だけ先にキャッシュ（pom.xml が変わらない限りこの層は使い回される）
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# ソースコードをコピー（開発時はvolumeマウントで上書きされる）
COPY src ./src

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]

# ─────────────────────────────────────
# 本番ビルドステージ
# ─────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─────────────────────────────────────
# 本番実行ステージ（軽量JREのみ）
# ─────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS production
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
