---
title: Kotlin in action 09. 제네릭스
author: jaeeun
date: 2024-02-04 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---
# chapter09. 제네릭스

## 9.1 제네릭 타입 파라미터

제네릭스를 사용하면 타입 파라미터를 받는 타입을 정의할 수 있다.
제네릭 타입의 인스턴스를 만들려면 타입 파라미터를 구체적인 타입인자로 치환해야 한다.

제네릭 클래스 `Box`를 정의하는 예시를 통해 알아보자.

```kotlin
class Box<T>(val value: T)
```

여기서 `Box` 클래스는 어떤 타입 `T`에 대해 상자를 만드는 일반적인 클래스이다. 이를 사용해 구체적인 타입을 가진 인스턴스를 만들려면 `Box` 클래스의 타입 파라미터 `T`를 대체하는 구체적인 타입 인자를 넘겨야 한다.
```kotlin
val intBox: Box<Int> = Box(42)
val stringBox: Box<String> = Box("Hello, Kotlin!")
```

### 9.1.1 제네릭 함수와 프로퍼티

제네릭 함수를 호출할 때는 반드시 구체적 타입으로 타입 인자를 넘겨야 한다.
예를 들어 slice 함수 정의를 보자
```kotlin
fun <T> List<T>.slice(indicies: IntRange): List<T>

fun main() {
    val letters = ('a'..'z').toList()
    println(letters.slice<Char>(0..2)) // [a, b, c]

    // 컴파일러가 타입 인자를 추론할 수도 있다.
    println(letters.slice(10..13)) // [k, l, m, n]
}
```
- 함수의 타입 파라미터 T가 수신 객체와 반환 타입에 쓰인다.
-  수신 객체와 반환 타입 모두 `List<T>` 이다.

### 9.1.2 제네릭 클래스 선언
타입 파라미터를 넣은 꺾쇠 기호 (<>)를 클래스(또는 인터페이스) 뒤에 붙이면 클래스(인터페이스)를 제네릭하게 만들 수 있다.

```kotlin
interface List<T> { // List 인터페이스에 T라는 타입 파라미터 정의
    operator fun get(index: Int): T // 인터페이스 안에서 T를 일반 타입처럼 사용
}

// 사용
class StringList: List<String> { // 구체적 타입 인자로 String 지정
    override fun get(index: Int): String = ...
}

class ArrayList<T>: List<T> { // 제네릭 타입 파라미터 T를 List 타입 인자로 넘김
    override fun get(index: Int): T = ...
}
```


### 9.1.3 타입 파라미터 제약
클래스나 함수에 사용할 수 있는 타입 인자를 제한하는 기능이다.

어떤 타입을 제네릭 타입의 타입 파라미터에 대한 상한 (`upper bound`)으로 지정하면 그 제네릭 타입을 인스턴스화할 때 사용하는 타입 인자는 반드시 **그 상한 타입이거나 그 상한 타입의 하위 타입**이어야한다.

제약을 가하려면 타입 파라미터 이름 뒤에 콜론(:)을 표시하고 그 뒤에 상환 타입을 적으면 된다.
```kotlin
// Number를 타입 파라미터 상한으로 지정
fun <T : Number> oneHalf(value: T): Double {
    // Number 클래스에 정의된 메서드 호출
    return value.toDouble() / 2.0
}

// Comparable<T>를 확장한 타입만 인자로 사용 가능
fun <T: Comparable<T>> max(first: T, second: T): T {
    return if (first > second) first else second
}
```


코틀린에서는 제네릭 타입 파라미터에 대해 둘 이상의 제약을 가하는 경우, 여러 제약을 `where` 절을 사용하여 명시할 수 있다.

아래는 `where` 절을 사용하여 두 개 이상의 제약을 가하는 예시이다.
이 예시에서는 타입 파라미터 `T`가 `Comparable` 인터페이스를 구현하고, `Serializable` 인터페이스를 상속하는 것을 요구한다.

```kotlin
fun <T> exampleFunction(value: T)
        where T : Comparable<T>, T : Serializable {
    // ...
}
```

### 9.1.4 타입 파라미터를 널이 될 수 없는 타입으로 한정
아무런 상한을 정하지 않은 타입 파라미터는 결과적으로 Any?를 상한으로 정한 파라미터와 같다.
널 가능성을 제외한 아무런 제약도 필요 없다면 Any를 상한으로 사용하면 된다.

## 9.2 실행 시 제네릭스의 동작
JVM의 제네릭스는 보통 타입 소거를 사용해 구현된다. 즉 실행 시점에 제네릭 클래스의 인스턴스에 타입 인자 정보가 들어있지 않다.

이 때 함수를 `inline`으로 만들면 타입 인자가 지워지지 않게 할 수 있다.

### 9.2.1 실행 시점의 제네릭: 타입 검사와 캐스트
제네릭 타입 인자 정보는 런타임에 지워진다. (타입 소거)

이러한 타입 소거로 인해 생기는 한계는 **타입 인자를 따로 저장하지 않기 때문에 실행 시점에 타입 인자를 검사할 수 없다**는 점이다.

```kotlin
if (value is List<String>) {...}
// ERROR: Cannot check for instance of erased type
```

이 때 스타 프로젝션 (star projection)을 사용하면 어떤 값이 Set 이나 Map이 아닌 List라는 사실을 확인할 수 있다. 즉 인자를 알 수 없는 제네릭 타입을 표현할 때 사용할 수 있다.
```kotlin
if (value is List<*>) {...}
```

as 나 as? 캐스팅에도 `Unchecked cast: List<*>` 라는 컴파일러 경고가 발생하지만 컴파일을 진행하므로 사용할 수 있다.
```kotlin
fun printSum(c: Collection<*>) {
    val intList = c as? List<Int>
        ?: throw IllegalArgumentException("List is expected")
}
```

### 9.2.2 실체화한 타입 파라미터를 사용한 함수 선언

제네릭 타입의 타입 인자 정보는 실행 시점에 지워진다.
따라서 제네릭 클래스의 인스턴스가 있어도 그 인스턴스를 만들 때 사용한 타입 인자를 알아낼 수 없다.
제네릭 타입의 타입 인자도 마찬가지로 그 함수 본문에서는 호출 시 쓰인 타입 인자를 알 수 없다.

```kotlin
fun <T> isA(value: Any) = value is T
// Error : Cannot check for instance of erased type : T
```

이 때 함수를 inline 함수로 만들고 타입 파라미터를 reified로 지정하면 value의 타입이 T의 인스턴스인지를 실행 시점에 검사할 수 있다.
```kotlin
inline fun <reified T> isA(value: Any) = value is T

fun main() {
    println(isA<String>("abc"))
}
```

#### 인라인 함수에서만 실체화한 타입 인자를 쓸 수 있는 이유
컴파일러는 인라인 함수의 본문을 구현한 바이트 코드를 그 함수가 호출되는 모든 지점에 삽입한다.
컴파일러는 **실체화한 타입 인자**를 사용해 **인라인 함수를 호출하는 각 부분의 정확한 타입 인자**를 알 수 있다.

### 9.2.4 실체화한 타입 파라미터의 제약

실체화한 타입 파라미터는 몇가지 제약이 있다.
- 타입 파라미터 클래스의 인스턴스 생성하기
- 타입 파라미터 클래스의 동반 객체 메서드 호출하기
- 실체화한 타입 파라미터를 요구하는 함수를 호출하면서 실체화하지 않은 타입 파라미터로 받은 타입을 타입 인자로 넘기기
- 클래스, 프로퍼티, 인라인 함수가 아닌 함수의 타입 파라미터를 reified로 지정하기

## 9.3 변성: 제네릭과 하위 타입
변성 (variance)란 `List<String>`와 `List<Any>`와 같이 기저 타입이 같고 타입 인자가 다른 여러 타입이 서로 어떤 관계가 있는지 설명하는 개념이다.

### 9.3.2 클래스, 타입, 하위 타입

클래스는 객체를 생성하기 위한 템플릿이며, 타입은 값의 종류를 나타낸다.
클래스를 사용하여 타입을 정의하고, 그를 기반으로 객체를 생성한다.

클래스는 하나의 타입을 나타내며, 클래스의 인스턴스는 해당 타입의 값이 된다.
```kotlin
// class
class Person(val name: String, val age: Int)

// Person은 타입으로, person 변수가 가질 수 있는 값의 종류를 뜻함
val person: Person = Person("John", 25)
```

하위 타입은 어떤 타입의 객체가 다른 타입의 객체를 대신하여 사용될 수 있는지를 나타낸다.
즉 하위 타입은 **부모 타입의 객체가 자식 타입으로 대체**될 수 있는 관계를 의미한다.

```
Number (상위 타입) <-- Int (하위 타입)
```

제네릭 타입을 인스턴스화할 때 타입 인자로 서로 다른 타입이 들어가면 **인스턴스 타입 사이의 하위 타입 관계가 성립하지 않으면** 그 제네릭 타입을 `무공변`이라고 한다.

### 9.3.3 공변성: 하위 타입 관계를 유지

**공변성(Covariance):** `out` 키워드를 사용하여 선언된 타입 매개변수를 공변적으로 만들 수 있다. 공변성은 하위 타입 관계가 유지되는 것을 의미한다.

out 키워드는 두가지를 함께 의미한다.
- 공변성 : 하위 타입 관계가 유지 된다.
- 사용 제한 : T를 아웃 위치 (return type)에서만 사용할 수 있다. 즉 클래스가`T`타입의 값을 생산할 수는 있지만`T`타입의 값을 소비할 수는 없다
```kotlin
var listInt: MutableList<out Int> = mutableListOf(1,2,3)
var listNumber: MutableList<out Number> = listInt
```

### 9.3.4 반공변성: 뒤집힌 하위 타입 관계
타입`B`가 타입`A`의 하위 타입인 경우`Consumer<A>`가`Consumer<B>`의 하위 타입인 관계가 성립하면 제네릭 클래스`Consumer<T>`는 타입 인자`T`에 대해 반공변이다.

`in`이라는 키워드는 그 키워드가 붙은 타입이 이 클래스의 메소드 안으로 전달돼 메소드에 의해 소비된다는 뜻이다. 즉, 오직 in 위치에서만 사용할 수 있다

```kotlin
var listNumber: MutableList<in Number> = mutableListOf(1,2,3)
var listInt: MutableList<in Int> = listNumber
```

### 9.3.5 사용 지점 변성: 타입이 언급되는 지점에서 변성 지정

변성의 종류
- 선언 지점 변성(declaration site variance): 클래스 선언에 지정하는 것을 선언 지점 변성이라 부르며, 한번의 선언만으로 변성을 추가 지정할 필요가 없으므로 코드가 간결해진다
- 사용 지점 변성(use site variance): 자바에서처럼 타입 파라미터가 있는 타입을 사용할때 마다 해당 타입 파라미터를 하위 타입이나 상위 타입 중 어떤 타입으로 대치할 수 있는지 명시하는 방법이다.

