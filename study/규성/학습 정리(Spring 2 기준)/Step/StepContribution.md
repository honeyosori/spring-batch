# StepContribution

## 기본 개념

- 청크 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트하는 도메인 객체
- 청크 커밋 직전에 StepExecution 의 apply 메서드를 호출하여 상태를 업데이트함
- ExitStatus 의 기본 종료코드 외 사용자 정의 종료코드를 생성해서 적용할 수 있음

## 실행 흐름

![image](https://github.com/honeyosori/spring-batch/assets/53935439/c00e5189-de8c-40ff-a948-688aae4fee18)

> StepExecution 이 완료되는 시점(DB 커밋 직전)에 apply 메서드를 호출하여 속성들의 상태를 최종 업데이트한다.

## 직접 코드 기반으로 살펴보기

### StepExecution

![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/a2a71367-8607-4557-8fa6-cb324af03b16)

### StepContribution

![image](https://github.com/honeyosori/spring-batch/assets/53935439/4eb75dcf-8c0b-406e-93e3-512c47ed6025)