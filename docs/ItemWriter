# ItermWriter
## 1. 개요
- 스프링 배치 출력(output) 메커니즘 담당
- 여러가지 포맷 지원(파일, DB, Nosql,)

```java
// 
public interface ItemWriter<T>{
	void write(List<? extends T> items) throws Exception;
}

```

## 	2. 종류
### 2.1. 파일 기반 ItermWriter
#### 2.1.1. FlatFileItemWriter
FlatFileItemWriter 트랜잭션 안에서 의도적 쓰기 지연 메커니즘(롤백)

```java
@Bean
@StepScope
public FlatFileItemWriter<Customer> customerItemWriter( @Value("#{jobParameters['outputFile']}") Resource outputFile) {
	return new FlatFileItemWriterBuilder<Customer>()
		.name("customerItemWriter")
		.resource(outputFile)
		.delimited()
		.delmiter(";")
		.names(new String[] {"zip", "state", "city", "address", "lastName", "firstName"})	
		.build();

}

```

#### 2.1.2. StaxEventItemWriter

### 2.2. 데이터베이스 기반 ItermWriter 
#### 2.2.1. JdbBatchItemWriter
#### 2.2.2. HibernateItemWriter 
#### 2.2.3. JpaItemWriter
