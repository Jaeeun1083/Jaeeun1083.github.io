---
title: Kotlin in action 부록. 코루틴
author: jaeeun
date: 2024-02-04 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---

# 코루틴
코루틴이란 실행의 지연과 재개를 허용함으로써,
**비선점적 멀티태스킹**을 위한 **서브 루틴**을 일반화한 컴퓨터 프로그램 구성요소를 뜻한다.

#### 비선점적 멀티태스킹이란?
- 비선점형 : 하나의 프로세스가 CPU를 할당받으면 종료되기 전까지 다른 프로세스가 CPU를 강제로 차지할 수 없다.
- 선점형 : 하나의 프로세스가 다른 프로세스 대신에 프로세서(CPU)를 강제로 차지할 수 있다.

#### 서브 루틴이란?
- 메인 루틴 : 프로그램 전체의 개괄적인 동작으로 main 함수에 의해 수행되는 흐름
- 서브 루틴 : 반복적인 기능을 모은 동작으로 main 함수 내에서 실행되는 개별 함수의 흐름

#### 코루틴 컨텍스트
CoroutineContext는 원소나 원소들의 집합을 나타내는 인터페이스이다.

`Job`, `CoroutineName`, `CoroutineDispatcher`와 같은 Element 객체들이 인덱싱된 집합이며 각 Element 또한 CoroutineContext 이다.

#### 코루틴 빌더

코루틴을 생성하는 메서드를 뜻한다. 종류로는 launch, async, withContext, runBlocking 등이 있다.

## 2.1 코루틴 빌더

### launch
코루틴을 잡 (Job)으로 반환하며 만들어진 코루틴은 기본적으로 즉시 실행된다.

launch가 작동하려면 CoroutineScope 객체가 블록의 this로 지정되어야 하는데, 다른 suspend 함수 내부라면 해당 함수가 사용 중인 CoroutineScope가 있겠지만, 그렇지 않은 경우에는 `GlobalScope`를 사용하면 된다
- **내부에서 CoroutineScope를 가진 경우:**
```kotlin
suspend fun mySuspendFunction() {
    // 현재 함수의 CoroutineScope를 사용하여 launch 실행
    launch {
        // 비동기 작업 수행
    }
}
```
- **GlobalScope 사용:**
```kotlin
fun main() {
    // GlobalScope를 사용하여 launch 실행
    GlobalScope.launch {
        // 비동기 작업 수행
    }

    // 메인 스레드는 계속 진행
    println("Main thread is not blocked!")

    // 메인 스레드가 종료되면 코루틴도 함께 종료될 수 있으므로, 안전하게 대기
    runBlocking {
        delay(2000) // 예시로 2초 대기
    }
}
```

이 때 GlobalScope는 메인 스레드가 실행 중인 동안만 코루틴의 동작을 보장해준다.

이를 방지하기 위해서는 비동기적으로 launch를 실행하거나, launch의 실행이 끝날 때까지 현재 스레드를 블록시키는 방법 (`runBolocing()`)이 있다.

`yield`는 주로 Sequence 빌더와 함께 사용되며, **호출한 시점에서 현재의 값을 반환하고 일시 중단된 코루틴을 재개**한다.

### runBlocking
스레드를 블라킹해야 하는 경우 runBlocking을 사용할 수 있다.
코루틴이 중단되었을 경우 runBlocking 빌더는 suspend 메인 함수와 마찬가지로 시작한 스레드를 중단시킨다.

따라서 runBlocking 내부에서 `delay`를 호출하면 `Thread.sleep()`과 비슷하게 동작한다.
```kotlin
fun main() {
    runBlocking {
        delay(1000L)
        println("World")
    }
}
```

### async
async 코루틴 빌더는 launch와 비슷하지만 값을 생성한다는 차이가 있다. async 함수는 Deferred<T>타입의 객체를 리턴한다.

Deferred에는 작업이 끝나면 값을 반환하는 중단 메서드인 await가 있다.
```kotlin
fun main() = runBlocking {
    val resultDeferred:Deferred<Int> = GlobalScope.async {
        // launch는 반환 값이 없다.
        delay(1000)
        42
    }
    // await을 통해 async의 결과를 얻을 수 있다.
    val result:Int = resultDeferred.await()
    println("After await: $result")

    job.join() // launch의 작업이 완료될 때까지 기다림
}

```

