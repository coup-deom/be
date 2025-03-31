# Spring Boot 애플리케이션을 위한 Dockerfile
FROM amazoncorretto:17-alpine AS build

# 작업 디렉토리 설정
WORKDIR /workspace/app

# Gradle 빌드에 필요한 파일 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
COPY src src

# Gradle을 사용하여 애플리케이션 빌드
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# 런타임 이미지 생성
FROM amazoncorretto:17-alpine
VOLUME /tmp

# 타임존 설정
RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

# Spring Boot 보안 설정
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]