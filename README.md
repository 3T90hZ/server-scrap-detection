# Scrap Detection Backend

Spring Boot API dùng chung cho RecyClick Web, Mobile và luồng camera.

## Yêu cầu

- Java 21 và Maven 3.9, hoặc Docker với Docker Compose.
- MySQL 8 cho runtime. Test dùng H2 in-memory và không cần MySQL local.

## Kiểm tra trước khi bàn giao

```bash
mvn -B test
mvn -B package
```

Docker build cũng chạy toàn bộ test và sẽ dừng nếu test thất bại:

```bash
docker build -t scrap-smart:latest .
```

## Chạy bằng Docker Compose

```bash
cp .env.example .env
# Điền các giá trị còn trống trong .env
docker compose config --quiet
docker compose up -d
```

API được publish ở cổng `8081`; MySQL chỉ nằm trong Docker network và không mở
cổng database ra host. Không commit file `.env`.

Các biến bắt buộc:

- `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`: dùng hai mật khẩu khác nhau; ứng dụng
  đăng nhập bằng user `MYSQL_USER`, không dùng MySQL root.
- `JWT_SECRET`: chuỗi ngẫu nhiên tối thiểu 64 byte cho HS512. Thay secret sẽ làm
  mọi JWT đang tồn tại hết hiệu lực.
- `APP_FRONTEND_BASE_URL`: origin dùng trong liên kết đặt lại mật khẩu.
- `APP_CORS_ALLOWED_ORIGINS`: danh sách origin Web được phép, phân tách bằng dấu phẩy.

SMTP có thể cấu hình bằng `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME` và
`SMTP_PASSWORD`. Credential từng xuất hiện trong Git history phải được rotate;
xóa nó khỏi file hiện tại không làm secret cũ an toàn trở lại.

Trước lần deploy đầu tiên của cấu hình này, cập nhật
`/opt/scrap-smart/.env` với toàn bộ biến bắt buộc. Nếu volume MySQL đã tồn tại từ
bản cũ chỉ dùng root, tạo `MYSQL_USER` và cấp quyền trên database `scrap` trước khi
đổi credential của ứng dụng. Compose chỉ tự tạo user này khi khởi tạo một volume
MySQL mới.

Ví dụ thao tác trong MySQL client, với password lấy từ `.env` và không ghi trực
tiếp vào Git:

```sql
CREATE USER IF NOT EXISTS 'scrap_app'@'%' IDENTIFIED BY '<MYSQL_PASSWORD>';
GRANT ALL PRIVILEGES ON scrap.* TO 'scrap_app'@'%';
FLUSH PRIVILEGES;
```

## ⚠️ Xử lý lỗi schema khi nâng cấp (Migration)

Hibernate dùng `ddl-auto: update` nên chỉ **thêm** cột/bảng mới, **không bao giờ tự xóa** cột cũ.
Nếu database của bạn được tạo từ phiên bản code cũ, một số cột thừa vẫn tồn tại với ràng buộc `NOT NULL`, gây lỗi khi insert.

### Lỗi: `Field 'customer' doesn't have a default value` khi tạo giao dịch

Bảng `transactions` phiên bản cũ có cột `customer` (lưu trực tiếp customer ID). Phiên bản mới đã chuyển thông tin customer sang bảng `bills`, nên Entity `Transaction.java` không còn map cột này nữa.

**Cách sửa — chạy 1 trong 2 câu SQL sau trên database:**

```sql
-- Cách 1: Cho phép NULL (an toàn, giữ lại dữ liệu cũ)
ALTER TABLE transactions MODIFY COLUMN customer BIGINT NULL DEFAULT NULL;

-- Cách 2: Xóa cột luôn (sạch sẽ hơn, mất dữ liệu cũ của cột này)
ALTER TABLE transactions DROP COLUMN customer;
```

### Lỗi: `Referencing column 'bill_id' and referenced column 'bill_id' are incompatible`

Xảy ra khi cột `bill_id` trong bảng `transactions` có kiểu `VARCHAR(36)` (từ schema cũ dùng UUID), nhưng Entity mới dùng `BIGINT` (auto-increment). Hibernate không thể tạo foreign key vì kiểu dữ liệu không khớp.

**Cách sửa:**

```sql
-- Bước 1: Xóa foreign key cũ (nếu có)
SET FOREIGN_KEY_CHECKS = 0;

-- Bước 2: Đổi kiểu dữ liệu
ALTER TABLE transactions MODIFY COLUMN bill_id BIGINT NULL DEFAULT NULL;

SET FOREIGN_KEY_CHECKS = 1;
```

> **Mẹo chung:** Nếu gặp nhiều lỗi kiểu dữ liệu không tương thích, cách nhanh nhất là **xóa toàn bộ database và tạo lại** (Hibernate sẽ tự sinh schema đúng). Chỉ áp dụng khi dữ liệu hiện tại là dữ liệu test và không cần giữ lại:
> ```sql
> DROP DATABASE pos_db;
> CREATE DATABASE pos_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> ```

---

## Jenkins

Jenkins đọc secret từ `/opt/scrap-smart/.env`, build image (bao gồm test), kiểm
tra cấu hình Compose rồi cập nhật container. Pipeline không dừng MySQL trong mỗi
lần deploy và xóa bản sao `.env` khỏi workspace khi hoàn tất.
