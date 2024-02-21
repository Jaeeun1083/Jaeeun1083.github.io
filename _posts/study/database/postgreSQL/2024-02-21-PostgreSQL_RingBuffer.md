---
title: PostgreSQL Bulk IO 처리를 위한 Ring Buffer
author: jaeeun
date: 2024-02-21 00:00:00 +0800
categories: [Study, "PostgreSQL"]
tags: ["PostgreSQL 9.6 Performance Story"]
render_with_liquid: false
---

사내 스터디에서 PostgreSQL9.6 성능이야기 책을 참고하여 학습 후 정리한 글입니다.

### Q1. IO 전략 4가지 중 BULK READ는 언제 발생할까?
- Shared Buffer 크기의 1/4 이상인 테이블에 대해 Seq Scan 시 발생한다.
  ```c
  // PostgreSQL의 IO 유형
  typedef enum BufferAccessStrategyType {
  	BAS_NORMAL, // 랜덤 액세스 용
    BAS_BULKREAD, // Large Seq Scan 용
    BAS_BULKWRITE, // 대량 Write 용
    BAS_VACCUM // VACCUM
  }
  ```

### Q2. SeqScan 으로 발생할 수 있는 위험성은 무엇이며 어떻게 해결할까?
- 발생할 수 있는 위험성
  - 큰 테이블을 읽는 경우, Ring Buffer를 사용하지 않으면 백엔드 프로세스가 버퍼 풀에 저장된 모든 페이지를 삭제(evict)할 수 있다.
  - 페이지가 삭제되면 버퍼 풀에 저장된 데이터가 줄어들게 되어, 캐시 히트 비율이 감소한다. 왜냐하면 필요한 데이터를 디스크에서 다시 읽어와야 하기 때문이다.
- 해결 방법인 Ring Buffer
  - Ring Buffer를 사용하면 큰 테이블을 처리할 때 발생할 수 있는 성능 문제를 완화하고 캐시 히트 비율을 유지할 수 있다.
  - 페이지를 일시적으로 보관하여 다시 읽지 않아도 되게 하므로, 디스크 I/O를 줄이고 쿼리 성능을 향상시키는데 도움이 된다.
   
> [참고 링크](https://www.interdb.jp/pg/pgsql08/05.html)
> - **Ring Buffer의 목적**: Ring Buffer는 큰 테이블을 읽을 때 발생할 수 있는 성능 문제를 해결하기 위한 목적으로 사용된다.
> - **버퍼 풀에서의 페이지 삭제 문제**: 큰 테이블을 읽는 경우, Ring Buffer를 사용하지 않으면 백엔드 프로세스가 버퍼 풀에 저장된 모든 페이지를 삭제(evict)할 수 있다.
> - **캐시 히트 비율 감소**: 페이지가 삭제되면 버퍼 풀에 저장된 데이터가 줄어들게 되어, 캐시 히트 비율이 감소한다. 왜냐하면 필요한 데이터를 디스크에서 다시 읽어와야 하기 때문이다.
> - **Ring Buffer의 역할**: Ring Buffer는 이러한 문제를 피하기 위해 큰 테이블을 위한 임시 버퍼 영역을 제공한다. Ring Buffer를 사용하면 버퍼 풀에서 페이지를 삭제하지 않고, 필요한 부분만을 임시로 저장함으로써 캐시 히트 비율을 높일 수 있다.

###  Q3. pg_prewarm()이란?
- 해당 테이블을 강제로 buffer로 올린다.

###  postgreSQL의 5가지 스캔 방식

```sql
CREATE TABLE post (
    id serial PRIMARY KEY,
    title varchar(255),
    author varchar(255),
    created_at timestamp
);
```

#### 1. Sequential Scan
- 테이블의 모든 데이터를 하나씩 확인하는 방법. 주로 인덱스가 없는 column을 조건으로 검색할 경우에 사용
  ```sql
  SELECT * FROM post WHERE title = 'title1';
  ```

#### 2. Index Scan
- 인덱스 탐색 방식. 인덱스가 만들어진 컬럼을 조건문으로 조회할 때 사용
  ```sql
  SELECT * FROM post WHERE id = 1;
  ```

#### 3. Index Only Scan
- 인덱스에 필요한 데이터가 있는 경우 사용. 인덱스에 필요한 값이 있으므로 실제 데이터를 Fetch하지 않아도 됨.
  ```sql
  SELECT id FROM post WHERE id = 1;
  ```

#### 4. Bitmap Scan
- index scan 과 sequential scan 이 조합된 방식.
  - 인덱스 스캔을 사용하여 검색 조건에 해당하는 행을 찾고 각 행에 대한 위치를 Bitmap 형태로 기록한다.
  - 그 다음 Bitmap 에 기록된 위치 정보를 사용하여 테이블을 순차적으로 스캔하면서 조건에 맞는 행을 선택.
  - 이 과정에서 인덱스 스캔으로 얻은 위치 정보를 활용하여 특정 행만을 순차적으로 접근함

  ```sql
  CREATE INDEX idx_author ON post(author);
  SELECT id FROM post WHERE id < 600000 AND author = 'author1';
  ```

#### 5. TID Scan
- 실제 테이블의 데이터를 식별하기 위해 사용하는  `TID`라는 것을 이용한 쿼리

  ```sql
  SELECT id FROM post WHERE ctid = '(1, 1)';
  ```
