# Chunk Process 아키텍처

## 전체 구조

![image](https://github.com/honeyosori/spring-batch/assets/53935439/aa99a091-14d4-4c63-b290-64aae53f4d56)

## 세부 구조

![image](https://github.com/honeyosori/spring-batch/assets/53935439/dd688c17-5883-43f3-8b2a-9372a38f02fe)

> ItemReader 와 ItemProcessor 에서 각각의 Item 들을 한 건씩 Chunk 크기 만큼 처리한다. 
> 반면 ItemWriter 의 경우는 Item 단위가 아닌 Chunk 단위로 일괄적으로 출력 작업을 처리하며 따라서 Chunk size 가 Commit 의 단위가 된다.
> 주의할 점은 ItemReader 에서는 Item 을 한 건 읽고 곧바로 ItemProcessor 에 전달하는 것이 아니라 Chunk size 만큼 모두 읽은 다음 전달하며,
> ItemProcessor 는 전달 받은 Chunk 에 담긴 Item 들을 또 다시 Item 개수 만큼 반복해서 처리한다는 것이다.