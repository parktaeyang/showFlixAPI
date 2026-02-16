# DDD 아키텍처 적용 가이드

> **작성일**: 2025-10-14  
> **목적**: 프로젝트에 DDD(Domain-Driven Design) 4계층 아키텍처를 적용하여 유지보수성과 확장성을 향상시킨다.

---

## 📌 목차

1. [왜 DDD를 도입했는가?](#1-왜-ddd를-도입했는가)
2. [4계층 아키텍처 개요](#2-4계층-아키텍처-개요)
3. [각 계층의 역할과 책임](#3-각-계층의-역할과-책임)
4. [실제 코드 예시](#4-실제-코드-예시)
5. [데이터 흐름](#5-데이터-흐름)
6. [Command 패턴 사용법](#6-command-패턴-사용법)
7. [새로운 기능 개발 시 가이드](#7-새로운-기능-개발-시-가이드)
8. [주의사항 및 Best Practice](#8-주의사항-및-best-practice)
9. [기존 코드와의 차이점](#9-기존-코드와의-차이점)
10. [FAQ](#10-faq)

---

## 1. 왜 DDD를 도입했는가?

### 기존 코드의 문제점

```java
// ❌ 기존 방식
@Controller
public class SampleController {
    @Autowired
    private SapRepository sapRepository;  // Controller가 Infrastructure 직접 의존
    
    @RequestMapping("/test.do")
    public String test(HttpServletRequest request) {
        // Controller에 비즈니스 로직 + 외부 시스템 호출 혼재
        String contrNo = request.getParameter("contrNo");
        ZFSDL_DETAIL_CONT_RES response = sapRepository.getDetailContract(...);
        // 변환 로직도 Controller에...
        return "success";
    }
}
```

**문제점**:
- Controller가 너무 많은 책임을 가짐
- 비즈니스 로직이 여기저기 흩어짐
- 외부 시스템(SAP) 변경 시 Controller 수정 필요
- 테스트 어려움
- 코드 재사용 불가

### DDD 도입 후

```java
// ✅ DDD 방식
@Controller
public class DDDSampleController {
    @Autowired
    private DDDSampleService service;  // Service만 의존
    
    @RequestMapping("/dddTest.json")
    @ResponseBody
    public DDDSampleResponse test(@RequestBody DDDSampleRequest request) {
        // 단순 위임만
        ContractDomain domain = service.getContractDetail(
            DDDSampleAssembler.toCommand(request)
        );
        return DDDSampleAssembler.toResponse(domain);
    }
}
```

**개선점**:
- 각 계층의 책임이 명확
- 비즈니스 로직이 Service에 집중
- 외부 시스템 변경해도 Controller는 수정 불필요
- 테스트 용이
- 코드 재사용 가능

---

## 2. 4계층 아키텍처 개요

```
┌─────────────────────────────────────────────────────────┐
│                  Interfaces Layer                        │
│  (사용자 인터페이스: Controller, DTO, Assembler)          │
└─────────────────────────────────────────────────────────┘
                          ↓ Command
┌─────────────────────────────────────────────────────────┐
│                  Application Layer                       │
│  (유스케이스 조율: Service, Command)                      │
└─────────────────────────────────────────────────────────┘
                          ↓ Domain
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
│  (비즈니스 로직: Domain Model, Value Object)              │
└─────────────────────────────────────────────────────────┘
                          ↑
┌─────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                     │
│  (외부 시스템 연동: Adapter, Mapper, Repository)          │
└─────────────────────────────────────────────────────────┘
```

### 패키지 구조

```
com.vms.jpa/
├── interfaces/              # Interfaces Layer
│   ├── DDDSampleController.java
│   ├── assembler/
│   │   └── DDDSampleAssembler.java
│   └── dto/
│       ├── DDDSampleRequest.java
│       └── DDDSampleResponse.java
│
├── application/             # Application Layer
│   ├── DDDSampleService.java
│   └── command/
│       └── DDDSampleCommand.java
│
├── domain/                  # Domain Layer
│   └── ContractDomain.java
│
└── infrastructure/          # Infrastructure Layer
    └── sap/
        ├── adapter/
        │   └── DDDSampleRfcAdapter.java
        └── mapper/
            └── DDDSampleRfcMapper.java
```

---

## 3. 각 계층의 역할과 책임

### 3.1 Interfaces Layer (인터페이스 계층)

**역할**: 외부 세계와의 접점

**구성 요소**:
- **Controller**: HTTP 요청/응답 처리
- **Request/Response DTO**: 클라이언트와 주고받는 데이터
- **Assembler**: DTO ↔ Command/Domain 변환

**책임**:
```java
✅ HTTP 요청 수신 및 검증 (Spring Validation)
✅ Request DTO → Command 변환
✅ Service 호출
✅ Domain → Response DTO 변환
✅ HTTP 응답 반환

❌ 비즈니스 로직 포함 금지
❌ Infrastructure 세부사항 알면 안 됨
❌ 직접 Repository나 외부 시스템 호출 금지
```

**예시**:
```java
@Controller
@RequestMapping("/ddd/sample")
public class DDDSampleController {
    
    private final DDDSampleService service;
    
    @RequestMapping(value = "/dddTest.json", method = POST)
    @ResponseBody
    public DDDSampleResponse dddTest(@RequestBody DDDSampleRequest request) throws Exception {
        // 1. Request → Command 변환
        ContractDomain domain = service.getContractDetail(
            DDDSampleAssembler.toCommand(request)
        );
        
        // 2. Domain → Response 변환
        return DDDSampleAssembler.toResponse(domain);
    }
}
```

---

### 3.2 Application Layer (애플리케이션 계층)

**역할**: 유스케이스 조율자 (Orchestrator)

**구성 요소**:
- **Service**: 유스케이스 실행 로직
- **Command**: 유스케이스 실행에 필요한 입력 파라미터

**책임**:
```java
✅ 입력 데이터 검증
✅ 트랜잭션 관리
✅ 여러 Adapter/Repository 조율
✅ 비즈니스 규칙 적용
✅ Infrastructure DTO 생성 (RFC DTO 등)

❌ HTTP 관련 코드 금지 (HttpServletRequest 등)
❌ 복잡한 비즈니스 로직은 Domain Layer로
```

**예시**:
```java
@Service
@Transactional(readOnly = true)
public class DDDSampleService {
    
    private final DDDSampleRfcAdapter rfcAdapter;
    
    public ContractDomain getContractDetail(DDDSampleCommand command) throws Exception {
        // 1. 입력 검증
        validateCommand(command);
        
        // 2. RFC DTO 생성 (Infrastructure 세부사항)
        ZFSDL_DETAIL_CONT_REQ.GetContractDetail rfcRequest = buildRfcRequest(command);
        
        // 3. Adapter 호출
        ContractDomain domain = rfcAdapter.getContractList(rfcRequest);
        
        // 4. 비즈니스 규칙 적용
        applyBusinessRules(domain);
        
        return domain;
    }
    
    private void validateCommand(DDDSampleCommand command) {
        if (command.getContrNo().length() != 10) {
            throw new IllegalArgumentException("계약번호를 확인해주세요.");
        }
    }
}
```

---

### 3.3 Domain Layer (도메인 계층)

**역할**: 비즈니스 로직의 핵심

**구성 요소**:
- **Domain Model**: 비즈니스 개념을 표현하는 객체
- **Value Object**: 불변 값 객체

**책임**:
```java
✅ 핵심 비즈니스 로직
✅ 도메인 규칙 검증
✅ 상태 변경 로직
✅ 계산 로직

❌ Infrastructure 의존 금지
❌ Framework 의존 금지 (가능한 한)
❌ DB, HTTP, 외부 시스템 알면 안 됨
```

**예시**:
```java
@Getter
public class ContractDomain {
    
    private final String contrNo;
    private final String productGroup;
    private final BigDecimal supplyPrice;
    private final BigDecimal taxAmount;
    
    public ContractDomain(String contrNo, String productGroup, 
                          BigDecimal supplyPrice, BigDecimal taxAmount) {
        this.contrNo = contrNo;
        this.productGroup = productGroup;
        this.supplyPrice = supplyPrice;
        this.taxAmount = taxAmount;
    }
    
    // 비즈니스 로직: 총 렌탈료 계산
    public BigDecimal rentalFee() {
        return supplyPrice.add(taxAmount);
    }
    
    // 비즈니스 로직: 할인 적용
    public ContractDomain applyDiscount(BigDecimal discountRate) {
        BigDecimal discountedPrice = supplyPrice.multiply(
            BigDecimal.ONE.subtract(discountRate)
        );
        return new ContractDomain(contrNo, productGroup, discountedPrice, taxAmount);
    }
}
```

**불변성 (Immutability) 중요!**:
- 모든 필드는 `final`
- Setter 없음
- 상태 변경 시 새 객체 반환

---

### 3.4 Infrastructure Layer (인프라 계층)

**역할**: 외부 시스템과의 연동

**구성 요소**:
- **Adapter**: 외부 시스템 호출 로직
- **Mapper**: 외부 시스템 응답 → Domain 변환
- **Repository**: DB 접근 (JPA, MyBatis)

**책임**:
```java
✅ 외부 시스템 호출 (SAP RFC, REST API 등)
✅ DB 접근
✅ 외부 응답 → Domain 변환
✅ 기술적 세부사항 처리

❌ 비즈니스 로직 포함 금지
```

**예시**:
```java
// Adapter
@Component
public class DDDSampleRfcAdapter {
    
    private final SapRepository sapRepository;
    
    public ContractDomain getContractList(
        ZFSDL_DETAIL_CONT_REQ.GetContractDetail rfcReq
    ) throws Exception {
        // 1. SAP RFC 호출
        ZFSDL_DETAIL_CONT_RES.GetContractDetail response = 
            sapRepository.getDetailContract(rfcReq);
        
        // 2. RFC 응답 → Domain 변환
        return DDDSampleRfcMapper.toDomain(response);
    }
}

// Mapper
public class DDDSampleRfcMapper {
    
    public static ContractDomain toDomain(
        ZFSDL_DETAIL_CONT_RES.GetContractDetail response
    ) {
        if (response == null || response.getET_CONT1().isEmpty()) {
            throw new IllegalStateException("계약 정보가 없습니다.");
        }
        
        var contract = response.getET_CONT1().get(0);
        
        return new ContractDomain(
            contract.getZUONR(),
            contract.getSTMAT_TX(),
            new BigDecimal(contract.getZKWBTR()),
            new BigDecimal(contract.getZMWST())
        );
    }
}
```

---

## 4. 실제 코드 예시

### 전체 흐름 코드

#### 4.1 Request DTO

```java
// interfaces/dto/DDDSampleRequest.java
@Getter
@Setter
@NoArgsConstructor
public class DDDSampleRequest {
    private String contrNo;
    
    @Builder
    public DDDSampleRequest(String contrNo) {
        this.contrNo = contrNo;
    }
}
```

#### 4.2 Response DTO

```java
// interfaces/dto/DDDSampleResponse.java
@Getter
public class DDDSampleResponse {
    private String contrNo;
    private String productGroup;
    private BigDecimal supplyPrice;
    private BigDecimal taxAmount;
    private BigDecimal rentalFee;
    
    @Builder
    public DDDSampleResponse(String contrNo, String productGroup,
                            BigDecimal supplyPrice, BigDecimal taxAmount,
                            BigDecimal rentalFee) {
        this.contrNo = contrNo;
        this.productGroup = productGroup;
        this.supplyPrice = supplyPrice;
        this.taxAmount = taxAmount;
        this.rentalFee = rentalFee;
    }
}
```

#### 4.3 Command

```java
// application/command/DDDSampleCommand.java
@Getter
@Builder
public class DDDSampleCommand {
    private final String contrNo;
    // 필드 추가 시 여기에만 추가
}
```

#### 4.4 Assembler

```java
// interfaces/assembler/DDDSampleAssembler.java
public class DDDSampleAssembler {
    
    // Request → Command
    public static DDDSampleCommand toCommand(DDDSampleRequest request) {
        return DDDSampleCommand.builder()
                .contrNo(request.getContrNo())
                .build();
    }
    
    // Domain → Response
    public static DDDSampleResponse toResponse(ContractDomain domain) {
        return DDDSampleResponse.builder()
                .contrNo(domain.getContrNo())
                .productGroup(domain.getProductGroup())
                .supplyPrice(domain.getSupplyPrice())
                .taxAmount(domain.getTaxAmount())
                .rentalFee(domain.rentalFee())
                .build();
    }
}
```

#### 4.5 Controller

```java
// interfaces/DDDSampleController.java
@Slf4j
@Controller
@RequestMapping("/ddd/sample")
public class DDDSampleController {
    
    private final DDDSampleService service;
    
    @Autowired
    public DDDSampleController(DDDSampleService service) {
        this.service = service;
    }
    
    @RequestMapping(value = "/dddTest.json", method = POST)
    @ResponseBody
    public DDDSampleResponse dddTest(@RequestBody DDDSampleRequest request) 
        throws Exception {
        
        ContractDomain domain = service.getContractDetail(
            DDDSampleAssembler.toCommand(request)
        );
        
        return DDDSampleAssembler.toResponse(domain);
    }
}
```

#### 4.6 Service

```java
// application/DDDSampleService.java
@Service
@Transactional(readOnly = true)
public class DDDSampleService {
    
    private final DDDSampleRfcAdapter rfcAdapter;
    
    public ContractDomain getContractDetail(DDDSampleCommand command) 
        throws Exception {
        
        validateCommand(command);
        
        ZFSDL_DETAIL_CONT_REQ.GetContractDetail rfcRequest = 
            buildRfcRequest(command);
        
        ContractDomain domain = rfcAdapter.getContractList(rfcRequest);
        
        applyBusinessRules(domain);
        
        return domain;
    }
    
    private void validateCommand(DDDSampleCommand command) {
        if (command.getContrNo().length() != 10) {
            throw new IllegalArgumentException("계약번호를 확인해주세요.");
        }
    }
    
    private ZFSDL_DETAIL_CONT_REQ.GetContractDetail buildRfcRequest(
        DDDSampleCommand command) {
        return ZFSDL_DETAIL_CONT_REQ.GetContractDetail.builder()
                .I_ZUONR(command.getContrNo())
                .build();
    }
    
    private void applyBusinessRules(ContractDomain domain) {
        if (domain == null) {
            throw new IllegalStateException("계약 정보를 찾을 수 없습니다.");
        }
    }
}
```

#### 4.7 Domain

```java
// domain/ContractDomain.java
@Getter
public class ContractDomain {
    
    private final String contrNo;
    private final String productGroup;
    private final BigDecimal supplyPrice;
    private final BigDecimal taxAmount;
    
    public ContractDomain(String contrNo, String productGroup,
                         BigDecimal supplyPrice, BigDecimal taxAmount) {
        this.contrNo = contrNo;
        this.productGroup = productGroup;
        this.supplyPrice = supplyPrice;
        this.taxAmount = taxAmount;
    }
    
    // 비즈니스 로직
    public BigDecimal rentalFee() {
        return supplyPrice.add(taxAmount);
    }
}
```

#### 4.8 Adapter & Mapper

```java
// infrastructure/sap/adapter/DDDSampleRfcAdapter.java
@Component
public class DDDSampleRfcAdapter {
    
    private final SapRepository sapRepository;
    
    public ContractDomain getContractList(
        ZFSDL_DETAIL_CONT_REQ.GetContractDetail rfcReq) throws Exception {
        
        ZFSDL_DETAIL_CONT_RES.GetContractDetail response = 
            sapRepository.getDetailContract(rfcReq);
        
        return DDDSampleRfcMapper.toDomain(response);
    }
}

// infrastructure/sap/mapper/DDDSampleRfcMapper.java
public class DDDSampleRfcMapper {
    
    public static ContractDomain toDomain(
        ZFSDL_DETAIL_CONT_RES.GetContractDetail response) {
        
        var contract = response.getET_CONT1().get(0);
        
        return new ContractDomain(
            contract.getZUONR(),
            contract.getSTMAT_TX(),
            new BigDecimal(contract.getZKWBTR()),
            new BigDecimal(contract.getZMWST())
        );
    }
}
```

---

## 5. 데이터 흐름

```
[Client]
   ↓ HTTP POST /ddd/sample/dddTest.json
   ↓ { "contrNo": "1234567890" }
   
┌──────────────────────────────────────┐
│ 1. Controller (Interfaces)           │
│    - DDDSampleRequest 수신            │
└──────────────────────────────────────┘
   ↓ Assembler.toCommand(request)
   ↓ DDDSampleCommand
   
┌──────────────────────────────────────┐
│ 2. Service (Application)             │
│    - validateCommand()               │
│    - buildRfcRequest() → RFC DTO     │
└──────────────────────────────────────┘
   ↓ ZFSDL_DETAIL_CONT_REQ
   
┌──────────────────────────────────────┐
│ 3. Adapter (Infrastructure)          │
│    - sapRepository.getDetailContract()│
└──────────────────────────────────────┘
   ↓ ZFSDL_DETAIL_CONT_RES
   
┌──────────────────────────────────────┐
│ 4. Mapper (Infrastructure)           │
│    - toDomain() 변환                  │
└──────────────────────────────────────┘
   ↓ ContractDomain
   
┌──────────────────────────────────────┐
│ 5. Service (Application)             │
│    - applyBusinessRules()            │
│    - return domain                   │
└──────────────────────────────────────┘
   ↓ ContractDomain
   
┌──────────────────────────────────────┐
│ 6. Controller (Interfaces)           │
│    - Assembler.toResponse(domain)    │
└──────────────────────────────────────┘
   ↓ DDDSampleResponse
   ↓ { "contrNo": "1234567890", ... }
   
[Client]
```

---

## 6. Command 패턴 사용법

### 왜 Command 패턴인가?

**문제 상황**:
```java
// ❌ 파라미터가 많아지면?
public ContractDomain getContract(
    String contrNo,
    String startDate,
    String endDate,
    String productGroup,
    Integer pageNumber,
    Integer pageSize
) {
    // ...
}
```

**해결책: Command 객체**:
```java
// ✅ Command로 캡슐화
public ContractDomain getContract(DDDSampleCommand command) {
    // command.getContrNo()
    // command.getStartDate()
    // ...
}
```

### Command 작성 가이드

```java
@Getter
@Builder
public class DDDSampleCommand {
    
    // 필수 필드
    private final String contrNo;
    
    // 선택 필드 (nullable)
    private final String startDate;
    private final String endDate;
    private final String productGroup;
    
    // 기본값이 있는 필드
    @Builder.Default
    private final Integer pageNumber = 1;
    
    @Builder.Default
    private final Integer pageSize = 10;
}
```

### 언제 Command를 사용하나?

| 파라미터 개수 | 권장 방식 | 이유 |
|-------------|----------|------|
| 1~2개 | 직접 전달 | 간단명료 |
| 3개 이상 | Command 객체 | 확장성, 유지보수성 |

---

## 7. 새로운 기능 개발 시 가이드

### Step 1: 요구사항 분석

**예시**: "고객 정보 조회 API 개발"

### Step 2: 패키지 구조 생성

```
com.vms.customer/
├── interfaces/
│   ├── CustomerController.java
│   ├── assembler/
│   │   └── CustomerAssembler.java
│   └── dto/
│       ├── CustomerRequest.java
│       └── CustomerResponse.java
├── application/
│   ├── CustomerService.java
│   └── command/
│       └── GetCustomerCommand.java
├── domain/
│   └── CustomerDomain.java
└── infrastructure/
    └── db/
        ├── repository/
        │   └── CustomerRepository.java
        └── entity/
            └── CustomerEntity.java
```

### Step 3: 구현 순서

#### 1️⃣ Domain 모델 작성 (가장 먼저!)

```java
@Getter
public class CustomerDomain {
    private final String customerId;
    private final String customerName;
    private final String phoneNumber;
    
    public CustomerDomain(String customerId, String customerName, String phoneNumber) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
    }
    
    // 비즈니스 로직
    public String getFormattedPhoneNumber() {
        return phoneNumber.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }
}
```

#### 2️⃣ Command 작성

```java
@Getter
@Builder
public class GetCustomerCommand {
    private final String customerId;
}
```

#### 3️⃣ Service 작성

```java
@Service
@Transactional(readOnly = true)
public class CustomerService {
    
    private final CustomerRepository repository;
    
    public CustomerDomain getCustomer(GetCustomerCommand command) {
        validateCommand(command);
        return repository.findById(command.getCustomerId());
    }
    
    private void validateCommand(GetCustomerCommand command) {
        if (command.getCustomerId() == null) {
            throw new IllegalArgumentException("고객ID는 필수입니다.");
        }
    }
}
```

#### 4️⃣ Repository/Adapter 작성

```java
@Repository
public class CustomerRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    public CustomerDomain findById(String customerId) {
        CustomerEntity entity = em.find(CustomerEntity.class, customerId);
        return CustomerMapper.toDomain(entity);
    }
}
```

#### 5️⃣ DTO 작성

```java
// Request
@Getter
@Setter
public class CustomerRequest {
    private String customerId;
}

// Response
@Getter
@Builder
public class CustomerResponse {
    private String customerId;
    private String customerName;
    private String phoneNumber;
}
```

#### 6️⃣ Assembler 작성

```java
public class CustomerAssembler {
    
    public static GetCustomerCommand toCommand(CustomerRequest request) {
        return GetCustomerCommand.builder()
                .customerId(request.getCustomerId())
                .build();
    }
    
    public static CustomerResponse toResponse(CustomerDomain domain) {
        return CustomerResponse.builder()
                .customerId(domain.getCustomerId())
                .customerName(domain.getCustomerName())
                .phoneNumber(domain.getFormattedPhoneNumber())
                .build();
    }
}
```

#### 7️⃣ Controller 작성 (마지막!)

```java
@Controller
@RequestMapping("/customer")
public class CustomerController {
    
    private final CustomerService service;
    
    @RequestMapping(value = "/get.json", method = POST)
    @ResponseBody
    public CustomerResponse getCustomer(@RequestBody CustomerRequest request) {
        CustomerDomain domain = service.getCustomer(
            CustomerAssembler.toCommand(request)
        );
        return CustomerAssembler.toResponse(domain);
    }
}
```

---

## 8. 주의사항 

1. **Controller에서 Infrastructure 직접 호출**
   ```java
   // ❌ Bad
   @Controller
   public class BadController {
       @Autowired
       private SapRepository sapRepository;  // 직접 의존!
   }
   ```

2. **Domain에 Framework 의존**
   ```java
   // ❌ Bad
   @Entity  // JPA 어노테이션을 Domain에!
   public class BadDomain {
       @Column
       private String name;
   }
   ```

3. **Service에 HttpServletRequest 사용**
   ```java
   // ❌ Bad
   public void process(HttpServletRequest request) {
       String param = request.getParameter("name");
   }
   ```

4. **Assembler/Mapper에 비즈니스 로직**
   ```java
   // ❌ Bad
   public static CustomerResponse toResponse(CustomerDomain domain) {
       // 비즈니스 로직이 변환 로직에!
       if (domain.getAge() < 20) {
           throw new BusinessException("미성년자입니다.");
       }
       return ...;
   }
   ```

---

## 9. 기존 코드와의 차이점

### 비교표

| 항목 | 기존 방식 | DDD 방식 |
|-----|---------|---------|
| **Controller 역할** | 비즈니스 로직 + 외부 시스템 호출 | HTTP 요청/응답만 처리 |
| **Service 역할** | 없거나 단순 위임 | 유스케이스 조율 |
| **Domain 모델** | DTO와 혼재 | 순수 비즈니스 객체 |
| **외부 시스템 호출** | Controller에서 직접 | Adapter로 격리 |
| **테스트** | 어려움 | 각 계층 독립 테스트 |
| **확장성** | 낮음 | 높음 |

### 기존 코드 예시

```java
// ❌ 기존 방식(극단적 예시)
@Controller
public class OldController {
    @Autowired
    private SapRepository sapRepository;
    
    @RequestMapping("/old.do")
    public String old(HttpServletRequest request, ModelMap model) {
        // 1. 파라미터 추출
        String contrNo = request.getParameter("contrNo");
        
        // 2. 검증
        if (contrNo == null || contrNo.isEmpty()) {
            return error("계약번호 필수");
        }
        
        // 3. RFC DTO 생성
        ZFSDL_DETAIL_CONT_REQ.GetContractDetail rfcReq = 
            ZFSDL_DETAIL_CONT_REQ.GetContractDetail.builder()
                .I_ZUONR(contrNo)
                .build();
        
        // 4. SAP 호출
        ZFSDL_DETAIL_CONT_RES.GetContractDetail res = 
            sapRepository.getDetailContract(rfcReq);
        
        // 5. 데이터 변환
        Map<String, Object> result = new HashMap<>();
        result.put("contrNo", res.getET_CONT1().get(0).getZUONR());
        result.put("productGroup", res.getET_CONT1().get(0).getSTMAT_TX());
        
        // 6. ModelMap 반환
        model.addAttribute("data", result);
        return "view";
    }
}
```

**문제점**:
- Controller가 모든 것을 다 함
- 비즈니스 로직 재사용 불가
- 테스트 어려움
- SAP 변경 시 Controller 수정 필요

### DDD 방식

```java
// ✅ DDD 방식
@Controller
public class DDDSampleController {
    private final DDDSampleService service;
    
    @RequestMapping("/dddTest.json")
    @ResponseBody
    public DDDSampleResponse test(@RequestBody DDDSampleRequest request) {
        ContractDomain domain = service.getContractDetail(
            DDDSampleAssembler.toCommand(request)
        );
        return DDDSampleAssembler.toResponse(domain);
    }
}
```

**장점**:
- Controller는 단순 위임만
- 비즈니스 로직은 Service에 
- SAP 변경해도 Controller 수정 불필요
- 등 layer별로 독립적으로 구현되어 유지보수 용이

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|-----|------|---------|-------|
| 2025-10-14 | 1.0 | 최초 작성 | 박태양 |

---

##나중에 정리할 것
Domain은 "무엇이 유효한가?"를 정의 / Service는 "누가, 언제, 어떤 조건에서 할 수 있는가?"를 판단
ClaimReceiptInfoServiceImpl의 getBillingList의 경우 어댑터를 통해 필요한 도메인을 생성 후 서비스단에서 독립적계산 함수를 구현

**끝.**

