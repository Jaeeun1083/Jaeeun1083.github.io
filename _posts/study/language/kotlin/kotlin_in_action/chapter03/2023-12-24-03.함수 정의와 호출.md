---
title: Kotlin in action 03. 함수 정의와 호출
author: jaeeun
date: 2023-12-24 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---

## chapter03. 함수 정의와 호출

### 3.2 함수를 호출하기 쉽게 만들기

자바 컬렉션의 `toString`을 커스텀하게 처리할 수 있는 함수 `joinToString` 를 코틀린 답게 구현해보자.

#### joinToString() 함수 초기 구현

```kotlin
fun <T> joinToString(
    collection: Collection<T>,
    separator: String,
    prefix: String,
    postfix: String
): String {
    val result = StringBuilder(prefix)
    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}

// 호출
fun main() {
    joinSoTring(collection, " ", " ", ".")
}
```

코틀린에서는 함수에 전달하는 인자 중 일부(또는 전부)의 이름을 명시할 수 있다.
```kotlin
// 전달 인자를 명시하여 오류 방지 및 가독성 증진이 가능하다.
joinSoTring(collection, seperator = " ", prefix = " ", postfix = ".")
```


#### 3.2.2 디폴드 파라미터 값

- 코틀린에서는 함수 선언에서 파라미터의 디폴트 값을 지정할 수 있으므로 오버로드를 줄일 수 있다.
```kotlin
fun <T> joinToString(
        collection: Collection<T>,
        separator: String = ", ",
        prefix: String = "",
        postfix: String = ""
): String {
    // ... 생략
}

// 호출
fun main() {
    val list = listOf(1, 2, 3)
    joinSoTring(list, ", ", "", "") // 1, 2, 3
    joinSoTring(list) // 1, 2, 3
    joinSoTring(list, "; ") // 1; 2; 3
}
```

> 디폴트 값과 자바
> 
> 자바에는 디폴트 파라미터가 없어 코틀린 함수가 디폴트 파라미터를 제공하더라도 모든 인자를 명시해야 한다.
> 
> @JvmOverloads 애너테이션을 추가하면 코틀린 컴파일러가 자동으로 맨 마지막 파라미터로부터 파라미터를 하나씩 생략한 오버로딩한 자바 메서드를 추가해준다.

#### 3.2.3 정적인 유틸리티 클래스 없애기: 최상위 함수와 프로퍼티

#### 최상위 함수

코틀린에서는 함수를 소스 파일의 최상위 수준, 클래스 밖에 위치 시킬 수 있다.
```kotlin
package strings

fun joinToString(/* 생략 */) : String { /* 생략 */ }
```

JVM이 클래스 안에 들어있는 코드만 실행할 수 있기 때문에 컴파일러는 이 파일을 컴파일할 때 새로운 클래스를 정의해준다.
```java
package strings;
public class JoinKt { //join.kt -> 코틀린 소스 파일 이름과 자바 클래스 이름이 대응된다.
    public static String joinToString(/* 생략 */) { /* 생략 */ }
}
```

#### 최상위 프로퍼티
프로퍼티 또한 최상위 수준에 둘 수 있다. 이 때 값은 정적 필드에 저장된다.

```kotlin
var opCount = 0
fun performOperation() {
    opCount++
    // ...
}
fun reportOperationCount() {
    println("Operation performed $opCount times")
}
```
- 최상위 프로퍼티도 다른 프로퍼티처럼 접근자 메서드를 통해 자바 코드에 노출된다.
- `const` 변경자를 추가하면 프로퍼티를 `public static final` 필드로 컴파일하게 만들 수 있다.
  - **primitive 타입**과 **String 타입**만 const로 지정할 수 있다.

### 3.3 메서드를 다른 클래스에 추가: 확장 함수와 확장 프로퍼티

#### 확장 함수
- 어떤 클래스의 멤버 메서드인 것처럼 호출할 수 있지만 그 클래스의 밖에 선언된 함수다.
- 확장 함수를 만들려면 추가하려는 함수 이름 앞에 그 함수가 확장할 클래스 이름을 붙이면 된다.
  - 수신 객체 타입(receiver type): 클래스 이름
  - 수신 객체(receiver object): 확장 함수가 호출되는 대상이 되는 값(객체)

```kotlin
fun <T> Collection<T>.joinToString( // Collection<T>에 대한 확장 함수를 선언
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)
    for ((index, element) in this.withIndex()) { // "this" 는 수신 객체를 기리킨다. 여기는 T 타입의 원소 컬렉션
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}

// 호출
fun main() {
    val list = listOf(1, 2, 3)
    println(list.joinToString(separator = "; ", prefix = "(", postfix = ")"))
}
```

#### 확장 함수는 오버라이드 할 수 없다.

- 확장 함수는 클래스의 일부가 아니다. 확장 함수는 클래스 밖에 선언된다.
- 확장 함수가 정적 메서드와 같은 특성을 가지므로 확장 함수를 하위 클래스에서 오버라이드할 수 없다.

#### 확장 프로퍼티

```kotlin
val String.lastChar: Char get() = get(length - 1)
```
- 확장 프로퍼티를 사용하면 기존 클래스 객체에 대한 프로퍼티 형식의 구문으로 사용할 수 있는 API를 추가할 수 있다.
- 프로퍼티라는 이름으로 불리기는 하지만 상태를 저장할 적절한 방법이 없기 때문에 실제로 확장 프로퍼티는 아무 상태도 가질 수 없다.
- 하지만 프로퍼티 문법으로 더 짧게 코드를 작성할 수 있어서 편한 경우가 있다.

### 3.4 컬렉션 처리: 가변 길이 인자, 중위 함수 호출, 라이브러리 지원

코틀린은 다음과 같은 특성이 있다.
- **varag 키워드**를 사용하면 호출시 인자 개수가 달라질 수 있는 함수를 정의할 수 있다. 
- **중위 함수** 호출 구문을 사용하면 인자가 하나뿐인 메소드를 간편하게 호출할 수 있다. 
- **구조 분해 선언**을 사용하면 복합적인 분해해서 여러 변수에 나눠 담을 수 있다

#### 가변 인자 함수
가변 길이 인자는 메소드를 호출할 때 원하는 개수만큼 값을 인자로 넘기면 자바 컴파일러가 배열에 그 값들을 넣어주는 기능이다.

코틀린은 파라미터 앞에 `vararg 변경자`를 붙인다

이미 배열에 들어있는 원소를 가변 길이 인자로 넘기려면 배열을 명시적으로 풀어서 배열의 각 원소가 인자로 전달되게 해야 한다. (스프레드 연산자)
```kotlin
fun main(args: Array<String>) {
    val list = listOf("args: ", *args)
}
```

#### 중위 호출
코틀린에는 함수 호출의 가독성을 향상시킬 수 있는 방법 중 하나로 중위 호출 방식이 있다.

```kotlin
1.to("one") // "to"  메서드를 일반적인 방식으로 호출

1 to "one" // "to" 메서드를 중위 호출 방식으로 호출
```
- 중위 호출 시에는 수신 객체와 유일한 메서드 인자 사이에 메서드 이름을 넣는다.

```kotlin
infix fun Any.to(other: Any) = Pair(this, other)
```
- 함수를 중위 호출이 가능하게 하려면 `infix 변경자`를 함수 선언 앞에 추가 하면 된다.

#### 구조 분해 선언
구조 분해를 사용해 여러 변수를 초기화할 수 있다.

```kotlin
val (number, name) = 1 to "one" //두 변수를 즉시 초기화할 수 있다.
```
