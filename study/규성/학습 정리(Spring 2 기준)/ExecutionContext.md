# ExecutionContext

## 기본 개념

- 프레임워크에서 유지 및 관리하는 키/값으로 된 컬렉션으로 StepExecution 또는 JobExecution 객체의 상태를 저장하는 공유 객체
- DB 에 직렬화 한 값으로 저장됨 (JSON 형태 / {"key" : "value"})
- 공유 범위
  - Step 범위 : 각 Step 의 StepExecution 에 저장되며 Step 간에 서로 공유 안됨
  - Job 범위 : 각 Job 의 JobExecution 에 저장되며 Job 간 서로 공유 안되지만, Job 의 Step 간에는 서로 공유됨
- Job 재 시작시 이미 처리한 row 데이터는 건너뛰고 이후로 수행하도록 할 떄, 이런 상태 정보를 활용하기도 한다.
- Map 의 구조를 지니며 필요할 때, 공유 범위에 한해서 꺼내어 쓸 수 있다.

## 