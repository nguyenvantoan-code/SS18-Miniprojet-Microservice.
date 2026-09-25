# 🏦 RikkeiBank API – Digital Banking Microservice Platform

> **Môn:** IT214 – Java Microservice | **Session:** 18 – Mini Project  
> **Sinh viên:** Đào Trọng Trí | **Mã SV:** PTIT062

---

## 🏗️ Kiến trúc hệ thống

```
Client (Postman / Mobile App)
        │
        ▼
┌──────────────┐      ┌───────────────┐
│ Config-Server│      │ Eureka-Server │
│  :8888       │      │  :8761        │
└──────────────┘      └───────────────┘
        │                     │
        ▼                     ▼
┌────────────────────────────────────────┐
│          API Gateway :8080             │
│  JWT Validation (GlobalFilter)         │
│  Load Balancing (lb://)                │
└──┬─────────┬──────────┬──────┬────────┘
   │         │          │      │
   ▼         ▼          ▼      ▼
identity  customer  account  transaction  notification
:8081     :8082     :8083    :8084        :8085
   │                  │         │              │
   │ JWT              │ Kafka (Saga)            │
   ▼                  ▼         ▼              ▼
MySQL/Redis       MySQL     MySQL         Kafka Consumer
                  Redis     (Pessimistic  (Reactive WebFlux)
                            Lock)
```

## 📦 Danh sách Services

| Service | Port | Mô tả |
|---|---|---|
| `config-server` | 8888 | Tập trung cấu hình (Native profile) |
| `eureka-server` | 8761 | Service Registry & Discovery |
| `api-gateway` | 8080 | Gateway + JWT Filter + Load Balancing |
| `identity-service` | 8081 | Đăng ký/Đăng nhập, JWT, Phân quyền (ADMIN/TELLER/CUSTOMER) |
| `customer-service` | 8082 | Quản lý khách hàng & loại tài khoản |
| `account-service` | 8083 | Tài khoản ngân hàng + **Saga Choreography Transfer** |
| `transaction-service` | 8084 | Ghi nhận giao dịch từ Kafka events |
| `notification-service` | 8085 | **Reactive WebFlux** Kafka Consumer – Thông báo |

## 🔐 Phân quyền

| Role | Quyền |
|---|---|
| **ADMIN** | Toàn quyền: CRUD user, customer, account type; Thu hồi token bất kỳ user |
| **TELLER** | Xem & xử lý giao dịch trong ngày; KHÔNG sửa tài khoản |
| **CUSTOMER** | Xem tài khoản + lịch sử giao dịch của chính mình; Thực hiện chuyển khoản |

## 🚀 Cách chạy

```bash
# 1. Khởi động hạ tầng
docker compose up -d

# 2. Build toàn bộ (từ thư mục root)
mvn clean install -DskipTests

# 3. Khởi động theo thứ tự:
#    a. Config Server
mvn -pl config-server spring-boot:run
#    b. Eureka Server
mvn -pl eureka-server spring-boot:run
#    c. API Gateway + Services (có thể chạy song song)
mvn -pl api-gateway spring-boot:run
mvn -pl identity-service spring-boot:run
mvn -pl customer-service spring-boot:run
mvn -pl account-service spring-boot:run
mvn -pl transaction-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

## 💡 Luồng Nghiệp vụ Chính: Saga Chuyển Khoản

```
POST /api/transfers (qua Gateway)
      │
      ▼ account-service
1. Kiểm tra số dư tài khoản nguồn
2. DEBIT: trừ tiền tài khoản nguồn (Pessimistic Lock)
3. Publish → DEBIT_COMPLETED lên Kafka [banking-transfer]
      │
      ▼ account-service (consumer: account-credit-group)
4. CREDIT: cộng tiền tài khoản đích
5a. Thành công → Publish CREDIT_COMPLETED
5b. Thất bại   → Publish CREDIT_FAILED
      │
      ├── [CREDIT_FAILED] → account-service consumer
      │   └── Hoàn tiền về nguồn (compensateDebit)
      │       → Publish COMPENSATE_CREDIT
      │
      ▼ transaction-service (consumer: transaction-recorder-group)
6. CREDIT_COMPLETED → Ghi Transaction(COMPLETED)
6. COMPENSATE_CREDIT → Ghi Transaction(ROLLBACK)
7. Publish notification event
      │
      ▼ notification-service (Reactive Kafka Consumer)
8. Nhận sự kiện → Gửi thông báo Push/SMS/Email (log)
```

## 🔍 Chứng minh các tính năng

### Circuit Breaker (Resilience4j)
- account-service gọi customer-service qua FeignClient
- Khi customer-service ngưng: Circuit chuyển CLOSED → OPEN → HALF-OPEN
- Fallback method trả về lỗi có ý nghĩa

### Redis Cache (Cache-Aside)
- `identity-service`: `@Cacheable("users")`
- `customer-service`: `@Cacheable("customers")`, `@CachePut`, `@CacheEvict`
- `account-service`: `@Cacheable("accounts")`

### Kafka Saga Rollback
Để chứng minh rollback: Tắt account-service sau khi DEBIT xong, hoặc chạy scenario tài khoản đích bị khóa (FROZEN/CLOSED).

### Reactive WebFlux Consumer
`notification-service` dùng `reactor-kafka` (KafkaReceiver) thay vì `@KafkaListener` thông thường, minh chứng tư duy Async/Event-driven.
