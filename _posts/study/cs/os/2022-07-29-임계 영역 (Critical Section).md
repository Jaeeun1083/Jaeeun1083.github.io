---
title: 임계 영역 (Critical Section)
author: jaeeun
date: 2022-07-28 00:00:00 +0800
categories: [Study, "Operating System"]
tags: ["Operating System"]
render_with_liquid: false
---

스터디 Stacked-Book에서 실습과 그림으로 배우는 리눅스 구조 책을 참고하여 학습 후 정리한 글입니다.

## 임계 영역 (Critical Section)

- 다중 스레드 또는 다중 프로세스 환경에서 공유 자원에 접근하는 코드 영역
- 이 영역에서 여러 프로세스 또는 스레드가 동시에 접근하면 데이터 일관성 문제가 발생할 수 있으므로, 이를 효과적으로 관리하기 위한 메커니즘이 필요하다

### 임계 영역을 관리하기 위한 개념

- Entry Section, Critical Section, Exit Section은 상호배제(Mutual Exclusion) 문제를 해결하기 위한 임계 영역(critical section)의 구성요소 중 일부이다.

<img src="https://user-images.githubusercontent.com/78838791/181688886-4d5c6447-3454-40e4-b29e-9ae2e4ff3f90.png" width="300px"  alt="Critical Section의 구성 요소"/>

- entry-section : 프로세스가 critical-section에 접근하기 전에 **접근 허용 여부를 확인**하는 영역
- critical-section : 실제 작업이 수행되는 영역
- exit-section : critical-section에서 작업이 끝난 후 프로세스가 나왔다는 것을 다른 프로세스에 알려주는 영역

### 임계 영역 문제를 해결하기 위해 필요한 세가지 조건

#### 1. 상호 배타 (Mutual Exclusion)
- 한 프로세스가 임계 영역에 들어갔을 때 다른 프로세스는 임계 영역에서 실행될 수 없다.
-  임계 영역에 들어가려고 시도하는 프로세스는 임계영역에 진입한 프로세스가 나올 때까지 보류되다가 권한을 부여받을 때, 임계영역에 들어가게 된다.

#### 2. 진행 (Progress) _ deadlock을 피하기 위한
- 임계 영역에 아무도 없는 상태에서 임계 영역에 들어가고자 하는 프로세스는 들어갈 수 있어야 한다.

#### 3. 한정된 대기 (Bounded Waiting) _ starvation을 피하기 위한
- 프로세스가 임계 영역에 접근하려고 시도한 후에는 유한한 시간 내에 접근을 허용해야 하며, 무한정 대기하지 않아야 한다.

###  임계 영역을 해결하기 위한 방법

#### 1. 뮤텍스
- 뮤텍스는 임계 영역에 들어가기 전에 뮤텍스 객체를 획득해야 하며, 다른 프로세스나 스레드가 해당 뮤텍스를 획득하려고 시도하면 대기 상태에 들어간다.
- 사용 예제
  ```java
  import java.util.concurrent.locks.Lock;
  import java.util.concurrent.locks.ReentrantLock;
  
  public class MutexExample {
      private static Lock mutex = new ReentrantLock();
  
      public static void main(String[] args) {
          Thread thread1 = new Thread(() -> {
              mutex.lock();
              try {
                  // 임계 영역에 접근하는 코드
              } finally {
                  mutex.unlock();
              }
          });
  
          Thread thread2 = new Thread(() -> {
              mutex.lock();
              try {
                  // 다른 스레드도 임계 영역에 접근하지 못함
              } finally {
                  mutex.unlock();
              }
          });
  
          thread1.start();
          thread2.start();
      }
  }
  ```

#### 2. 세마포어
- 정수 값을 사용하여 프로세스가 리소스에 대한 접근을 조절하는 데 사용된다.
- `P (wait)`와 `V (signal)` 연산을 통해 리소스의 사용과 해제를 조절하여 다중 프로세스 간의 상호 배제와 진행 조건을 만족시킬 수 있다.
  - P (wait) 연산은 리소스를 기다리는 동작을 나타내며, 리소스를 얻을 때까지 기다린다.
  - V (signal) 연산은 리소스를 해제하는 동작을 나타내며, 다른 프로세스에게 리소스를 넘겨준다.
- 사용 예제
  ```java
  import java.util.concurrent.Semaphore;
  
  public class SemaphoreExample {
      private static Semaphore semaphore = new Semaphore(1);
  
      public static void main(String[] args) {
          Thread thread1 = new Thread(() -> {
              try {
                  semaphore.acquire();
                  // 임계 영역에 접근하는 코드
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } finally {
                  semaphore.release();
              }
          });
  
          Thread thread2 = new Thread(() -> {
              try {
                  semaphore.acquire();
                  // 다른 스레드도 임계 영역에 접근하지 못함
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } finally {
                  semaphore.release();
              }
          });
  
          thread1.start();
          thread2.start();
      }
  }
  ```

#### 3. 모니터
- 둘 이상의 스레드나 프로세스가 공유 자원에 안전하게 접근할 수 있도록 공유 자원을 숨기고 해당 접근에 대해 인터페이스만 제공하는 방법이다.
- 모니터 큐를 통해 공유 자원에 대한 작업들을 순차적으로 처리한다.
- 사용 예제
  ```java
  public class MonitorExample {
      private static Object monitor = new Object();
  
      public static void main(String[] args) {
          Thread thread1 = new Thread(() -> {
              synchronized (monitor) {
                  // 임계 영역에 접근하는 코드
              }
          });
  
          Thread thread2 = new Thread(() -> {
              synchronized (monitor) {
                  // 다른 스레드도 임계 영역에 접근하지 못함
              }
          });
  
          thread1.start();
          thread2.start();
      }
  }
  ```
---
스터디에서 한 분이 잘 설명된 글을 공유해 주셨다 참고하면서 공부하니 이해가 더 쉬웠다.

<a href="https://worthpreading.tistory.com/90"> 뮤텍스와 세마포어의 차이</a>
