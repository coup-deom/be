# Nginx를 위한 Dockerfile
FROM nginx:alpine

# 타임존 설정
RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

# Nginx 사용자 설정 (기본 root 대신 nginx 사용자 사용)
RUN sed -i 's/user  nginx/user  nginx/g' /etc/nginx/nginx.conf

# 기본 서버 설정 제거
RUN rm /etc/nginx/conf.d/default.conf

# 커스텀 설정은 볼륨 마운트로 주입됨
COPY ./conf.d/default.conf /etc/nginx/conf.d/

# 80, 443 포트 오픈
EXPOSE 80 443

# CMD는 기본 nginx 이미지가 제공하는 것을 사용
CMD ["nginx", "-g", "daemon off;"]