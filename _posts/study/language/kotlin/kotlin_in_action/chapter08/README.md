---
title: Kotlin in action 08. 고차 함수
author: jaeeun
date: 2024-02-04 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---

# chapter08. 고차 함수: 파라미터와 반한 값으로 람다 사용

## 8.1 고차 함수 정의
고차 함수는 다른 함수를 인자로 받거나 함수를 반환하는 함수다. 
코틀린에서는 람다나 함수 참조를 사용해 함수를 값으로 표현할 수 있다.
따라서 고차 함수는 **람다나 함수 참조를 인자로 넘길 수 있거나 람다나 함수 참조를 반환하는 함수**다.


### 8.1.1 함수 타입
함수 타입을 정의하려면 함수 파라미터의 타입을 괄호 안에 넣고, 그 뒤에 화살표(→)를 추가한 다음, 함수의 반환 타입을 지정하면 된다.
```
⌜ 파라미터 타입 ⌝     ⌜ 반환 타입 ⌝
( Int, String ) -> Unit
```

### 8.1.2 인자로 받은 함수 호출
```kotlin
// 고차 함수 정의: 정수 두 개와 함수를 매개변수로 받아 결과를 반환하는 함수
fun operate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

fun main() {
    // 더하기 함수
    val sum: (Int, Int) -> Int = { x, y -> x + y }

    // 빼기 함수
    val subtract: (Int, Int) -> Int = { x, y -> x - y }

    val resultSum = operate(5, 3, sum) // 결과: 8
    val resultSubtract = operate(7, 4, subtract) // 결과: 3   
}
```

### 8.1.4 디폴트 값을 지정한 함수 타입 파라미터나 널이 될 수 있는 함수 타입 파라미터

파라미터를 함수 타입으로 지정할 때도 디폴트 값을 지정할 수 있다.
```kotlin
fun <T> Collection<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    transform: (T) -> String = { it.toString() } // 함수 타입 파라미터를 선언하면서 람다를 디폴트 값으로 지정한다. 
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(transform(element)) // "transform" 파라미터로 받은 함수를 호출한다. 
    }

    result.append(postfix)
    return result.toString()
}

fun main() {
    val letters = listOf("Alpha", "Beta")
    println(letters.joinToString()) // 디폴트 변환 함수를 사용한다. -> Alpha, Beta
    println(letters.joinToString { it.toLowerCase() }) // 람다를 인자로 전달한다. -> alpha, beta
    println(letters.joinToString(separator = "! ", postfix = "! ",
        transform = { it.toUpperCase() })) // 이름 붙은 인자 구문을 사용해 람다를 포함하는 여러 인자를 전달한다. -> ALPHA! BETA!
}
```

### 8.1.5 함수를 함수에서 반환
함수를 다루는 함수로, 다른 함수를 매개변수로 받거나 함수를 반환하는 함수이다.

```kotlin
enum class OperationType { DOUBLE, SQUARE}

// 정수를 받아서 정수를 반환하는 함수를 반환하는 함수
fun createOperation(operationType: OperationType): (Int) -> Int {
    return when (operationType) {
        DOUBLE -> { value -> value * 2 }
        SQUARE -> { value -> value * value }
    }
}

// 사용 예시
val doubleFunction = createOperation(DOUBLE)
val squareFunction = createOperation(SQUARE)

val result1 = doubleFunction(3) // 결과: 6
val result2 = squareFunction(4) // 결과: 16
```

### 8.1.6 람다를 활용한 중복 제거
람다를 사용하면 코드를 간결하게 작성할 수 있고, 반복되는 패턴을 추상화할 수 있다.

웹사이트 방문 기록을 분석하는 예를 살펴보자.

```kotlin
enum class OS { WINDOWS, LINUX, MAC, IOS, ANDROID }

data class SiteVisit(
    val path: String,
    val duration: Double,
    val os: OS
)

val log = listOf(
    SiteVisit("/", 34.0, OS.WINDOWS),
    SiteVisit("/", 22.0, OS.MAC),
    SiteVisit("/login", 12.0, OS.WINDOWS),
    SiteVisit("/signup", 8.0, OS.IOS),
    SiteVisit("/", 16.3, OS.ANDROID)
)

// 윈도우 사용자의 평균 방문 시간 구하기
val averageWindowsDuration = log
    .filter { it.os == OS.WINDOWS }
    .map(SiteVisit::duration)
    .average()

// 맥 사용자의 평균 방문 시간 구하기
val averageWindowsDuration = log
    .filter { it.os == OS.MAC }
    .map(SiteVisit::duration)
    .average()
```

이 경우 중복을 피하기 위해 일반 함수를 사용하여 중복을 제거할 수 있다.
```kotlin
fun List<SiteVisit>.averageDurationFor(os: OS) =
        filter { it.os == os }.map(SiteVisit::duration).average()

fun main() {
    log.averageDurationFor(OS.WINDOWS)
    log.averageDurationFor(OS.MAC)
    // 만약 모바일 디바이스 (IOS, ANDROID)의 평균 방문 시간을 구하고 싶다면
    // 해당 일반 함수를 사용할 수 없다.
    val averageWindowsDuration = log
        .filter { it.os in setOf(OS.IOS, OS.ANDROID) }
        .map(SiteVisit::duration)
        .average()
    
    // 마찬가지로 IOS 사용자의 /signup 페이지 평균 방문 시간을 구하고 싶다면
    // 해당 일반 함수를 사용할 수 없다.
    val averageWindowsDuration = log
        .filter { it.os == OS.IOS && it.path == "/signup" }
        .map(SiteVisit::duration)
        .average()
}
```

이러한 일반 함수를 사용하게 되면 복잡한 질의를 사용해 방문 기록을 분석할 수 없다. 즉 중복이 다시 발생한다.

이러한 경우 그 **질의 코드를 람다로 만들면 중복을 제거할 수 있다.**
```kotlin
fun List<SiteVisit>.averageDurationFor(predicate: (SiteVisit) -> Boolean) =
    filter(predicate).map(SiteVisit::duration).average()

fun main() {
    println(log.averageDurationFor { it.os in setOf(OS.ANDROID, OS.IOS) })
    println(log.averageDurationFor { it.os == OS.IOS && it.path == "/signup" })
}
```

## 8.2 인라인 함수: 람다의 부가 비용 없애기

### 8.2.1 인라이닝이 작동하는 방식
코틀린의 `inline` 키워드는 **함수나 람다를 호출하는 곳에 해당 함수 또는 람다의 본문을 인라인으로 삽입**하는 기능을 제공한다.
즉 함수를 호출하는 코드를 함수를 호출하는 바이트코드 대신에 함수 본문을 번역한 바이트 코드로 컴파일 된다.

이는 함수 호출로 인해 발생하는 오버헤드를 최소화하고, 성능을 향상시킬 수 있게 도와준다.

### 8.2.2 인라인 함수의 한계
함수 본문에서 파라미터로 받은 람다를 호출한다면 그 호출을 쉽게 람다 본문으로 바꿀 수 있다.

하지만 파라미터로 받은 람다를 다른 변수에 저장하고 나중에 그 변수를 사용한다면 람다를 표현하는 객체가 어딘가는 존재해야 하기 때문에 람다를 인라이닝할 수 없다.
```kotlin
// 람다를 직접 호출
inline fun inlineExample(lambda: () -> Unit) {
    lambda()
}

// 람다를 변수에 저장하고 나중에 사용하는 경우 (인라인 불가능)
fun nonInlineExample(lambda: () -> Unit) {
    val storedLambda = lambda
    // 나중에 storedLambda를 사용
    storedLambda()
}
```

### 8.2.3 컬렉션 연산 인라이닝

filter와 map은 인라인 함수다. 따라서 그 두 함수의 본문은 인라이닝되며, 추가 객체나 클래스 생성은 없다.

하지만 이 코드는 리스트를 걸러낸 결과를 저장하는 중간 리스트를 만든다. 처리할 원소가 많아지면 중간 리스트를 사용하는 부가 비용도 커진다. 

asSequence를 통해 리스트 대신 시퀀스를 사용하면 중간 리스트로 인한 부가 비용은 줄어든다. 이때 각 중간 시퀀스는 람다를 필드에 저장하는 객체로 표현되며, 최종 연산은 중간 시퀀스에 있는 여러 람다를 연쇄 호출한다.

### 8.2.5 자원 관리를 위해 인라인된 람다 사용

코틀린에서는 자바 try-with-resource와 같은 기능을 제공하는 `use`라는 함수가 코틀린 표준 라이브러리 안에 들어있다.

use 함수는 닫을 수 있는(closeable) 자원에 대한 확장 함수이며, 람다를 인자로 받는다.

**자바 try-with-resource**
```java9
static String readFirstLineFromFile(String path) throws IOException {
    try(BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();    
    }
}
```

**코틀린 use**

```kotlin
fun readFirstLineFromFile(path: String): String {
    BufferedReader(FileReader(path)).use { br -> 
        return br.readLine()
    }
}
```
- use 함수 내에서 람다가 실행되고 나서 자동으로 close가 호출되어 자원이 해제된다.

## 8.3 고차 함수 안에서 흐름 제어

### 8.3.1 람다 안의 return문: 람다를 둘러싼 함수로부터 반환

람다 안에서 return 을 사용하면 람다로부터만 반환되는 게 아니라 **그 람다를 호출하는 함수가 실행을 끝내고 반환된다.**

자신을 둘러싸고 있는 블록보다 더 바깥에 있는 다른 블록을 반환되게 만드는 return 문을 `넌로컬(non-local) return` 이라 부른다.

```kotlin
fun lookForAlice(people: List<Person>) {
    for (person in people) {
        if (person.name == "Alice") {
            println("Found!")
            return // lookForAlice 함수에서 반환된다.
        }
    }
    println("Alice is not found")
}

fun lookForAlice(people: List<Person>) {
    people.forEach {
        if (it.name == "Alice") {
            println("Found!")
            return // lookForAlice 함수에서 반환된다.
        }
    }
    println("Alice is not found")
}
```

### 8.3.2 람다로부터 반환: 레이블을 사용한 return

람다 식에서도 `로컬(local) return`을 사용할 수 있다. 안에서 로컬 return은 for루프의 break와 비슷한 역할을 한다. 

로컬 return은 **람다의 실행을 끝내고 람다를 호출했던 코드의 실행을 계속 이어간다.**

로컬 return과 넌로컬 return을 구분하기 위해 레이블(label)을 사용해야 한다.

```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach label@{ // 람다 식 앞에 레이블을 붙인다.
        if (it.name == "Alice") return@label // return@label은 앞에서 정의한 레이블을 참조한다.
    }
    println("Alice might be somewhere") // 항상 이 줄이 출력된다.
}
```

람다에 레이블을 붙여서 사용하는 대신 람다를 인자로 받는 인라인 함수의 이름을 return 뒤에 레이블로 사용해도 된다.

```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach { 
        if (it.name == "Alice") return@forEach 
    }
    println("Alice might be somewhere")
}
```

### 8.3.3 무명 함수: 기본적으로 로컬 return

무명 함수는 코드 블록을 함수에 넘길 때 사용할 수 있는 다른 방법이다.

무명 함수 안에서 레이블이 붙지 않은 return 식은 무명 함수 자체를 반환시킬 뿐 무명 함수를 둘러싼 다른 함수를 반환시키지 않는다.

```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach(fun (person) {
        if (person.name == "Alice") return
        println("${person.name} is not Alice")
    })
}

// filter에 무명 함수 넘기기
fun lookForUnder30Age(people: List<Person>) {
    people.filter(fun(person): Boolean {
        return person.age < 30
    })
}
```
