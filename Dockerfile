# ==============================================================================
# Stage 1: Build Stage (Gradle + Java 21 JDK)
# ==============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# 1. Tận dụng Docker Layer Cache: Copy các file cấu hình Gradle trước
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Cấp quyền thực thi cho Gradle wrapper script
RUN chmod +x gradlew

# Tải trước các dependencies vào layer cache
RUN ./gradlew dependencies --no-daemon || true

# 2. Copy toàn bộ mã nguồn
COPY src/ src/

# 3. Build executable JAR (bỏ qua tests vì môi trường build chưa có database MySQL)
RUN ./gradlew bootJar --no-daemon -x test

# Chuẩn bị file JAR thực thi duy nhất (loại trừ file -plain.jar nếu có)
RUN find build/libs/ -name "*.jar" ! -name "*plain.jar" -exec cp {} build/libs/app.jar \;

# ==============================================================================
# Stage 2: Runtime Stage (Minimal Java 21 JRE)
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Tạo user/group riêng biệt không có quyền root để tăng tính bảo mật
RUN groupadd -r spring && useradd -r -g spring spring

# Tạo thư mục lưu file upload và phân quyền cho user spring
RUN mkdir -p /app/uploads/foods && chown -R spring:spring /app

# Copy file jar từ Stage 1
COPY --from=builder /build/build/libs/app.jar app.jar

# Copy sẵn các ảnh món ăn mẫu từ thư mục uploads hiện tại (loại trừ file rác qua .dockerignore)
COPY --chown=spring:spring uploads/ /app/uploads/

# Chuyển sang user non-root
USER spring:spring

# Cổng mặc định (Render sẽ tự động inject biến môi trường $PORT khi chạy)
ENV PORT=8080
EXPOSE 8080

# Cấu hình tối ưu bộ nhớ JVM cho Render (Free tier 512MB RAM):
# -XX:+UseContainerSupport: Nhận biết cgroup / giới hạn RAM của container
# -XX:MaxRAMPercentage=75.0: Giới hạn heap size tối đa 75% RAM container (~384MB trên gói 512MB)
# -Dserver.port=${PORT}: Đảm bảo Spring Boot lắng nghe đúng cổng của Render
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]

