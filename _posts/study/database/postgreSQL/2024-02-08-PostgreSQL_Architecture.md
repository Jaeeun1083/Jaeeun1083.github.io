---
title: PostgreSQL Architecture
author: jaeeun
date: 2024-02-08 00:00:00 +0800
categories: [Study, "PostgreSQL"]
tags: ["PostgreSQL 9.6 Performance Story"]
render_with_liquid: false
---

사내 스터디에서 PostgreSQL9.6 성능이야기 책을 참고하여 학습 후 정리한 글입니다.

# 01. 아키텍처 개요

postgreSQL 의 물리적 구조는 다음과 같다.

<img src = "https://severalnines.com/sites/default/files/blog/node_5122/image17.jpg" width=500>

참고 : https://severalnines.com/blog/understanding-postgresql-architecture/

## Shared Memory

Shared Memory란 Data Block 및 트랜잭션 로그와 같은 정보를 캐싱하는 공간으로 PostgreSQL Server의 모든 프로세스들에 의해 공유되는 영역이기도 하다.

Shared Memory의 구성 요소 중 대표적인 4가지 영역에 대해 간단히 알아보자.

### 1. Shared Buffer
Data와 Data의 변경 사항을 Block 단위로 캐싱하여 I/O를 빠르게 처리하기 위한 영역이다.
### 2. WAL Buffer (Write Ahead Log Buffer)
Session들이 수행하는 트랜잭션에 대한 변경 로그를 캐싱하는 공간으로 WAL 버퍼 내에 저장된 내용은 정해진 시점에 WAL 파일로 기록된다. 복구 작업 시 Data를 재구송할 수 있도록 하는 영역이다.
### 3. Clog Buffer (Commit Log Buffer)
각 트랜잭션의 상태(in_progress, committed, aborted) 정보를 캐싱하는 공간으로 모든 트랜잭션의 상태가 있다.
### 4. Lock Space
Lock과 관련된 내용을 보관하는 영역으로 Lock 정보는 모든 백그라운드 프로세스 및 사용자 프로세스에 의해 공유된다.

## Local Memory (Process Memory)
개별 Backend 프로세스가 할당 받아 사용하는 공간이다. 로컬 메모리와 관련된 주요 파라미터는 다음과 같다.

### 1. work memory
정렬 작업 (order by, distinct, merge join ...), bitmap 작업, 해시 조인과 merge 조인 작업 시 사용되는 공간이다.

### 2. maintenance work memory
vaccum 작업, 인덱스 생성, 테이블 변경, foreign key 추가 등의 작업에 사용되는 공간이다.

### 3. temp buffer
temporary 테이블을 저장하기 위한 공간이다. session 단위로 할당되는 비 공유 메모리 영역이다.

## PostgreSQL 프로세스 유형

PostgreSQL에는 네 가지 프로세스 유형 (PostMaster, Background, Backend, Client) 이 있다.

### 1. PostMaster 프로세스
PostgreSQL을 기동할 때 가장 먼저 시작되는 프로세스이다.

<img src = "https://severalnines.com/sites/default/files/blog/node_5122/image2.jpg" width="300"/>

역할
- Shared Memory 영역을 할당하며 다양한 **백그라운드 프로세스를 시작**한다.
- **Client 접속 요청이 있을 때 Backend 프로세스를 생성**한다.
- 가장 상위 프로세스로서 **하위 프로세스들의 비정상 작동 유무를 체크**하며, 문제 발생 시 재기동 역할을 수행한다.

### 2. Background 프로세스

PostgreSQL 작업에 필요한 백그라운드 프로세스 목록은 다음과 같다.

- **logger**: 오류메시지를 로그 파일에 기록한다. 프로세스 정보는 $PGDATA/pg_log 아래에 저장된다.
- **checkpointer**: PostgreSQL Server 다운 또는 충돌 문제가 발생하면 마지막 CheckPoint 레코드를 확인하여 복구 작업을 시작한다.
- **writer**: Shared Buffer의 Dirty Block을 디스크에 기록한다.
- **wal writer**: WAL 버퍼를 주기적으로 확인하여 기록되지 않은 모든 트랜잭션 레코드를 WAL 파일에 기록한다.
- **autovaccum launcher**: Vaccum이 필요한 시점에 autovacuum worker를 fork 한다.
- **archiver**: Archiving을 수행하는 프로세스이다. archiving 이란 WAL 세그먼트가 전환될 때 WAL 파일을 Archive 영역으로 복사하는 기능이며, 복사된 WAL 파일을 Archive 이라고한다.
- **stats collector**: 세션 실행 정보( pg_stat_activity ), 테이블 사용 통계 정보(pg_stat_all_tables) 등 DBMS 사용 통계를 수집한다.

### 3. Backend 프로세스
사용자 프로세스의 쿼리 요청을 수행한 후, 결과를 전송하는 역할을 수행한다. 

### 4. Client 프로세스
모든 백엔드 사용자 연결에 할당되는 백그라운드 프로세스를 나타낸다.

## 데이터베이스 구조

#### 데이터베이스 관련 사항
- initdb()가 실행되면 template0, template1, postgres 데이터베이스가 생성된다.

### Base Directory

postgreSQL 초기 설치 시 (initDB) 생성되는 Default Tablespace 디렉토리 중 하나로 pg_default Tablespace라고도 부른다.

```
$ ls /var/lib/postgresql/data/base
> 1  12406  12407


$ oid2name -U postgre
> All databases:
    Oid  Database Name  Tablespace
----------------------------------
  12407       postgres  pg_default
  12406      template0  pg_default
      1      template1  pg_default
```