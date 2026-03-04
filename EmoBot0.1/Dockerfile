# 传统 Dockerfile 备选方案：当 build-image 无法拉取 Docker Hub 时使用
# 使用微软 MCR 镜像，国内网络通常比 Docker Hub 更稳定
# 构建: docker build -t emobot:1.0.0 .
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY target/emobot-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
