---
title: Thread Safe
author: jaeeun
date: 2022-07-15 00:00:00 +0800
categories: [Study, "Operating System"]
tags: ["Operating System"]
render_with_liquid: false
---

스터디 Stacked-Book에서 실습과 그림으로 배우는 리눅스 구조 책을 참고하여 학습 후 정리한 글입니다.

## Thread Safe
- 멀티 쓰레드 프로그래밍에서 안전하게 동작하는 것을 의미한다. 
- 멀티 쓰레드가 동시에 데이터나 자원을 접근하고 수정할 때 발생할 수 있는 문제를 방지하기 위한 개념이다.

### Thread safe가 고려되지 않은 예제

- sum 변수를 인스턴스 변수로 공유하고 멀티 쓰레드 환경에 놓여 데이터를 수정했을 때를 확인해보자

```java
class ThreadTest {
  private int sum = 0;

  @DisplayName("2개의 쓰레드로 1억 만들기")
  @Test
  void 두개의_쓰레드로_1억_만들기() throws InterruptedException {
    Thread thread1 = new Thread(this::workerThread);
    Thread thread2 = new Thread(this::workerThread);

    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    assertEquals(100_000_000, sum);
  }
  private void workerThread() {
    for (int i=0; i < 25_000_000; i++) sum +=2;
  }
}

```

#### 결과
```
Expected :100000000
Actual   :52532250
```
- 확인 결과 매번 다른 값이 나오게 되는데 이 이유는 두 개의 스레드가 동시에 sum 변수를 수정하려고 하기 때문에 문제가 발생하는 것이다.
- 즉 코드에서 발생한 결과는 **경쟁 조건(`race condition`)** 으로 인해 예상치 못한 값이 나온 것이라고 할 수 있다.

### Thread safe하게 구현하기

Thread safe한 코드를 작성하기 위한 방법들을 알아보자.

#### 1. 뮤텍스(Mutex) 나 세마포어 (Semaphore) 사용

일반적으로 뮤텍스는 상호 배제를 위해 사용되고, 세마포어는 특정 작업 또는 자원의 동시 액세스를 제어하기 위해 사용된다.

**뮤텍스 예제**

```java
class ThreadTest {
    ...
    private final Lock mutex = new ReentrantLock();

    @DisplayName("2개의 쓰레드로 1억 만들기")
    @Test
    void 두개의_쓰레드로_1억_만들기() throws InterruptedException {
      ...
    }
    private void workerThread() {
        for (int i = 0; i < 25_000_000; i++) {
            mutex.lock(); // 뮤텍스 락 획득
            try {
                sum += 2;
            } finally {
                mutex.unlock(); // 뮤텍스 락 해제
            }
        }
    }
}
```

**세마포어 예제**

```java
class ThreadTest {
    ...
    private final Semaphore semaphore = new Semaphore(1);

    @DisplayName("2개의 쓰레드로 1억 만들기")
    @Test
    void 두개의_쓰레드로_1억_만들기() throws InterruptedException {
        ...
    }

    private void workerThread() {
        for (int i = 0; i < 25_000_000; i++) {
            try {
                semaphore.acquire(); // 세마포어 획득
                sum += 2;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release(); // 세마포어 해제
            }
        }
    }
}
```

- 동작 방식
  - 뮤텍스 :  한 번에 하나의 스레드만 임계 영역(Critical Section)에 진입할 수 있도록 하는 동기화 메커니즘이다.  뮤텍스는 락(lock)과 언락(unlock) 두 가지 상태만 가지며, 하나의 스레드만 락을 소유할 수 있다. 다른 스레드가 소유된 락을 얻으려고 하면 대기한다.
  - 세마포어 : 특정 리소스의 개수를 나타내는 카운터와 함께 사용된다. 스레드는 세마포어를 획득할 때마다 카운터가 감소하고, 세마포어를 반환할 때마다 카운터가 증가한다.

  
#### 2. Synchronization (암묵적인락, instrinsic lock)

- 자바에서 단일 연산 특성을 보장하기 위해 락을 제공하는 키워드이다.
- 자바에 내장된 락으로, 암묵적인락 혹은 모니터락이라고 불린다.

```java
class ThreadTest {
    /*
    *  1. 락은 스레드가 synchronized 블록에 들어가기전에 자동으로 확보되며 정상적으로던,
    *      예외가 발생하던 해당 블록을 벗어날때 자동으로 해제된다
    *  2. 자바에서 암묵적인 락은 뮤텍스(또는 상호배제 락)로 동작한다. 한번에 한 스레드만 특정 락을 소유할 수있다. 
    * */
   private synchronized void workerThread() {
       ...
   }
}
```

#### 3. 원자적(Atomic) 연산
 
- java.util.concurrent 패키지에서 제공하는 Atomic 클래스를 사용하여 원자적(atomic) 연산을 수행할 수 있다.

```java
class ThreadTest {
  private AtomicInteger sum = new AtomicInteger(0);
  ...
}

```

#### 4. volatile 키워드
- 위의 예제의 문제점을 해결하기 위한 방법은 아니지만 volatile 키워드를 사용하여 멀티 쓰레드 환경에서 발생할 수 있는 문제 중 하나인 **스레드 간 가시성(visibility) 문제를 해결**할 수 있다.
- **가시성 보장**
  - volatile 변수는 메모리에서 직접 읽고 쓰므로 다른 스레드에서의 변경 사항을 즉시 볼 수 있다.
- **최적화 방지**
  - 컴파일러나 CPU가 변수를 캐싱하거나 재정렬하지 않도록 한다. 따라서 volatile 변수의 값을 항상 메모리와 동기화하게 된다.
- **원자적 연산이 아니다.**
  - 변수의 가시성만 보장하고, 변수가 여러 작업으로 구성된 원자적 연산을 보장하지 않는다. 따라서 volatile 변수를 증가 또는 감소하는 등의 연산을 수행할 때 동기화가 필요할 수 있다.

> 가시성 문제란?
> 
> 멀티 스레드 환경에서 공유된 변수를 여러 스레드가 동시에 읽거나 쓰는 경우, CPU 캐시와 메모리 간의 불일치로 인해 예상치 못한 결과가 발생할 수 있다.
> 이를 가시성 문제라고 한다.
