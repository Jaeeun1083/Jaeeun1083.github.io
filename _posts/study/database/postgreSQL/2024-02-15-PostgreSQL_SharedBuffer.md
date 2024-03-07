---
title: PostgreSQL Shared Buffer Mechanism
author: jaeeun
date: 2024-02-14 00:00:00 +0800
categories: [Study, "PostgreSQL"]
tags: ["PostgreSQL 9.6 Performance Story"]
render_with_liquid: false
---

사내 스터디에서 PostgreSQL9.6 성능이야기 책을 참고하여 학습 후 정리한 글입니다.

# Shared Buffer 구조

## Shared Buffer의 구성 요소
Shared Buffer 를 구성하는 요소는 크게 4가지 (해시 테이블, 해시 테이블에 딸린 해시 엘리먼트, 버퍼 상태를 관리하는 버퍼 디스크립터, 실제 블록을 저장하는 버퍼 풀) 이다.

### 해시 테이블

PostgreSQL은 메모리 내의 버퍼를 관리 (검색, 입력)하기 위해 해시 테이블을 사용하며, 해시 충돌 문제를 완화하기 위해 `Segmented 해시 테이블`을 사용한다.

<img src="https://www.highgo.ca/wp-content/uploads/2021/01/dynamic-hash-table-1024x568.png" width="700PX" alt="Segmented 해시 테이블">

- Segment : 해시 테이블을 논리적으로 나누는 단위이다.
- Directory : N개로 나눈 각 세그먼트의 시작 위치를 가리키는 배열이다. 
- Bucket : 각 세그먼트 내에서 데이터를 저장하는 작은 단위이다. 해시 세그먼트 1개는 256개의 버킷으로 구성된다.

#### 버퍼 파티션이란?

앞서 언급한 디렉토리, 해시 세그먼트, 해시 테이블은 Shared Buffer 내에 존재하는 **공유 리소스**이다.
공유 리소스는 `LW (Light Weight) 락`을 이용해 보호한다. LW 락은 차후에 설명한다.

PostgreSQL은 LW 락 경합으로 인해 발생할 수 있는 성능 저하 문제를 해결하기 위해 **해시 테이블을 N개의 버퍼 파티션으로 나누고 버퍼 파티션마다 1개의 LW 락을 할당**한다.

이 때 버퍼 파티션의 개수는 `NUM_BUFFER_PARTITION` 변수로 설정할 수 있다. (9.4 버전까지 버퍼 파티션 개수는 16개 였으나 9.5 버전부터는 128로 상향 조정되었다,)

### 해시 엘리먼트
해시 엘리먼트는 `엘리먼트`와 `엘리먼트 키`로 구성된다.

해시 엘리먼트 메모리 구조는 DB 시작 시점에 일정 개수 ( 버퍼 수 + NUM_BUFFER_PARTITIONS 개의 해시 엘리먼트 배열 ) 만큼 미리 할당한다.

해당 해시 엘리먼트 배열을 관리하는 것은 `freeList`이다.

#### 엘리먼트 키
- 엘리먼트 키는 **BufferTag 구조체**와 **버퍼 디스크립터 배열 인덱스**로 구성된다.
- BufferTag 구조체는 **RelFileNode 구조체**, **forkNum**, **blockNum**으로 구성된다.
  - BufferTag란 클러스터 데이터베이스 내에서 각 블록의 고유 식별자이다.
  - RelFileNode은 유일한 오브젝트 번호를 저장한다.
  - forkNum은 오브젝트 유형 (테이블 또는 인덱스 -> 0 , FSM -> 1 , VM -> 2)
  - blockNum은 블록 번호

#### 엘리먼트
엘리먼트는 Next 엘리먼트를 가리키는 `엘리먼트 포인터`와 `hashvalue`(BufferTag를 이용해서 계산한 값)로 구성된다.

#### freeList
해시 엘리먼트 배열을 관리하는 구조체이다.

freeList 개수는 NUM_FREELISTS로 설정할 수 있으며 기본 설정 값은 32이다.
만약 Shared Buffer 가 1GiB 라면 해시 엘리먼트 배열은 132,000개가 되고 1개의 freeList 마다 4100개의 해시 엘리먼트를 관리하게 된다.

### 버퍼 디스크립터
버퍼 디스크립터는 버퍼 메타데이터를 관리하기 위한 구조체이다.
- tag : BufferTag 를 저장한다
- buf_id : 실제 버퍼가 저장된 버퍼 풀 배열 내의 인덱스 번호이다.
- wait_backend_pid : 버퍼를 액세스하기 위해서는 버퍼 PIN이 필요한데 해당 칼럼은 버퍼 PIN을 대기하는 프로세스 ID를 제공한다.
- context_lock : 버퍼를 액세스할 때 필요한 LW 락이다.

### Spin 락과 LW 락
Shared Buffer를 액세스할 때는 Spin 락과 LW 락을 획득해야 한다.

#### 스핀락
- 락을 얻을 때까지 계속 무한 루프(스핀)를 돌면서 대기한다.
- 다른 스레드나 프로세스를 대기시키지 않고 빠르게 락을 얻을 수 있고 Sleep 상태로 빠지지 않으므로 Context Switching 이 발생하지 않는다.

#### LW 락
- [**LW 락의 종류**](https://www.percona.com/blog/postgresql-locking-part-3-lightweight-locks/)
  - 공유 락 (Shared Lock): 여러 프로세스나 스레드가 동시에 **읽기 액세스**를 수행할 수 있도록 하는 락
  - 배타 락 (Exclusive Lock): 한 번에 하나의 프로세스나 스레드만 **쓰기 액세스**를 수행할 수 있도록 하는 락

- **LW 락 경합이란?**
  - 여러 스레드나 프로세스가 동일한 리소스에 접근하려고 할 때 발생하는 현상을 뜻한다.
  - LW 락 경합이 발생하면, 각 스레드나 프로세스는 해당 리소스에 대한 LW 락을 획득하기 위해 경쟁하게 된다.
  - 하나의 스레드나 프로세스가 이미 LW 락을 소유하고 있는 경우, 다른 스레드나 프로세스는 그 LW 락을 획득하기 위해 대기 상태에 들어가게 되는데 이러한 상태를 경합 상태라고 한다.

### Shared Buffer 내에 있는 블록을 읽는 경우

1. **BufferTag 생성:**
   1. 해당 BufferTag를 이용하여 hashvalue를 계산
   2. 계산된 hashvalue를 이용하여 버퍼 파티션 번호를 계산
2. **LW 락 획득:**
   1. 계산된 버퍼 파티션에 대한 LW 락을 Shared 모드로 획득
3. **해시 테이블에서 Tag 찾기:**
   1. hashvalue를 이용하여 해시 테이블의 버킷 번호를 계산
   2. 계산된 버킷 번호를 해시 세그먼트 번호와 인덱스 번호로 치환
   3. 해시 체인을 따라가면서 BufferTag를 찾음
4. 버퍼 디스크립터 배열 인덱스 설정:
   1. 찾은 BufferTag의 버퍼 ID를 확인하여 버퍼 디스크립터 배열에서 해당 인덱스에 PIN을 설정
5. **LW 락 해제:**
   1. 버퍼 파티션에 대한 LW 락을 해제
6. **버퍼 내용 읽기:**
   1. 버퍼 풀 **배열 인덱스 내용을 읽음**.
7. **PIN 해제:**
   1. 버퍼 디스크립터 배열에서 인덱스에 대한 PIN 해제

### Disk Read가 발생하는 경우
Shared Buffer 내에 존재하지 않는 블록을 읽기 위해서는 Disk Read 를 통해 해당 블록을 Shared Buffer로 로딩한 후 해당 버퍼를 읽는다.

Shared Buffer 내에 있는 블록을 읽는 경우와 동일하게 진행되지만 3-3 에서 BufferTag를 찾지 못해 검색에 실패하고 LW 락을 해제한다.
