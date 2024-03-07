---
title: Kotlin in action 07. 연산자 오버로딩과 기타 관례
author: jaeeun
date: 2024-01-28 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---

# chapter07. 연산자 오버로딩과 기타 관례

## 7.1 산술 연산자 오버로딩

### 7.1.1 이항 산술 연산 오버로딩

코틀린에서 이항 산술 연산자를 오버로딩하려면, 클래스에 해당 연산자에 대한 멤버 함수를 정의해야한다.

예를 들어 `+` 연산자를 오버로딩하려면 `plus`라는 멤버 함수를 정의해야한다.

x 좌표와 y 좌표를 갖는 필드를 갖는 Point라는 클래스를 만들고 이 클래스에서 `+` 연산자를 오버로딩 하는 예제를 살펴보자.

```kotlin
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

fun main() {
    val p1 = Point(10, 20)
    val p1 = Point(30, 40)

    println(p1 + p2) // Point(x=40. y=60)
}
```

- 이 예제에서 Point 클래스는 + 연산자를 오버로딩하기 위해 `plus` 멤버 함수를 `operator 키워드`를 붙여 정의했다.
- 이렇게 하면 `point1 + point2`와 같이 + 연산자를 사용하여 두 값을 더할 수 있게 된다.

여기서 중요한 점은 연산자를 오버로딩할 때 사용되는 **함수 이름이 특별한 규칙을 따라야 한다**는 것이고 그 **연산자를 오버로딩 하는 함수 앞에는 꼭 operator가 있어야 한다**.

예를 들어, `+` 연산자는 `plus` 함수에 의해 오버로딩된다. 이와 같이 각각의 이항 산술 연산자에는 특정한 함수 이름이 정해져있다.

#### 오버로딩 가능한 이항 산술 연산자

| 이항 산술 연산자 | 함수 이름     |
|-----------|-----------|
| `+`       | `plus`    |
| `-`       | `minus`   |
| `*`       | `times`   |
| `/`       | `div`     |
| `%`       | `rem`     |
| `..`      | `rangeTo` |

### 7.1.2 복합 대입 연산자 오버로딩

복합 대입 연산자(+=, -=, *=, /+ ...)는 변수의 현재 값과 다른 값을 결합하여 새로운 값을 변수에 할당하는 연산자이다.

경우에 따라 복합 대입 연산자의 연산이 객체에 대한 **다른 참조를 바꾸기보다 원래 객체의 내부 상태를 변경하게 만들고 싶을 수 있다**.

이때 **반환 타입이 Unit인 복합 대입 연산자 함수** (`plusAssign`, `minusAssign`, `timesAssign` ...)를 정의하면 코틀린은 그 함수를 사용한다.

```kotlin
// 복합 대입 연산자의 일반적인 형식
operator fun <operator> (other: T): T {
    // 연산 수행
    // 결과를 반환하거나 해당 객체를 수정하여 반환
}

// 예제
data class Point(var x: Int, var y: Int) {
    // 복합 대입 연산자 += 오버로딩
    operator fun plusAssign(other: Point) {
        // 현재 객체(this)를 수정하여 더한 결과를 저장
        this.x += other.x
        this.y += other.y
    }
}
```

### 7.1.3 단항 연산자 오버로딩
단항 연산자를 오버로딩하여 사용자가 정의한 클래스에 대해 해당 연산을 수행할 수 있다. 

단항 연산자에는 +, -, ++, --, ! 등이 포함된다.

#### 오버로딩 가능한 단항 산술 연산자

| 단항 산술 연산자 | 함수 이름        |
|-----------|--------------|
| `+`       | `unaryPlus`  |
| `-`       | `unaryMinus` |
| `++`      | `inc`        |
| `--`      | `dec`        |
| `!`       | `not`        |

## 7.2 비교 연산자 오버로딩

### 7.2.1 동등성 연산자: equals

data class의 경우 컴파일러가 자동으로 equals를 생성해주지만 직접 equals를 구현한다면 다음과 같다.

```kotlin
class Point(val x: Int, val y: Int) {
    override fun equals(other: Any?): Boolean { // Any에 정의된 메서드 오버라이딩
        if (this === other) return true // 최적화. 파라미터가 this와 같은 객체인지.
        if (other !is Point) return false // 파라미터 타입 검사 및 스마트 캐스트.
        return x == other.x && y == other.y // 프로퍼티 비교
    }
}
```

### 7.2.2 순서 연산자: compareTo
`compareTo` 함수는 코틀린에서 비교 연산을 정의하는 데 사용되는 함수이다. 이 함수는 `Comparable` 인터페이스를 구현하는 클래스에서 사용된다.

예를 들어, `Comparable` 인터페이스를 구현하는 Person 클래스는 다음과 같다.

```kotlin
class Person(
    val firstName: String, val lastName: String
) : Comparable<Person> {
    override fun compareTo(other: Person): Int {
        return compareValuesBy(this, other, Person::lastName, Person::firstName)
    }
}
```

## 7.3 컬렉션과 범위에 대해 쓸 수 있는 관례

### 7.3.1 인덱스로 원소에 접근: get과 set

코틀린에서는 컬렉션과 범위에 대해 인덱스를 사용하여 원소에 접근할 수 있다. 이러한 동작은 get 및 set 관례에 의해 정의된다.

이를 통해 마치 배열처럼 대괄호([])를 사용하여 인덱스로 원소에 접근하거나 수정할 수 있다.

#### get 관례

```kotlin
operator fun Point.get(index: Int): Int {
    return when(index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("Invalid coordinate $index")
    }
}

fun main() {
    val point = Point(10, 20)
    println(p[1]) // 출력 : 20
}
```

#### set 관례

```kotlin
operator fun MutablePoint.set(index: Int, value: Int) {
    return when(index) {
        0 -> x = value
        1 -> y = value
        else -> throw IndexOutOfBoundsException("Invalid coordinate $index")
    }
}
fun main() {
    val point = MutablePoint(10, 20)
    p[1] = 42
    println(p) // 출력 : MutablePoint(x=10, y=42)
}
```

### 7.3.2 in 관례

`in` 연산자는 객체가 컬렉션에 들어있는지를 검사하는 데 사용된다. 이런 경우 in 연산자와 대응하는 경우는 `contains`다.

in 관례를 사용하여 Rectangle 클래스에서 점이 직사각형 안에 있는지를 확인하는 방법을 보자.

```kotlin
data class Point(val x: Int, val y: Int)

data class Rectangle(val upperLeft: Point, val lowerRight: Point) {
    operator fun contains(p: Point): Boolean {
        return p.x in upperLeft.x until lowerRight.x &&
                p.y in upperLeft.y until lowerRight.y
    }
}

fun main() {
    val rectangle = Rectangle(Point(10, 20), Point(50, 40))
    val pointInside = Point(30, 30)
    val pointOutside = Point(5, 5)

    println("Point $pointInside is inside the rectangle: ${pointInside in rectangle}")
    println("Point $pointOutside is inside the rectangle: ${pointOutside in rectangle}")
}
```

### 7.3.3 rangeTo 관례
`rangeTo` 함수는 범위를 반환한다. **start..end 표기법**을 사용하여 범위를 생성할 때 **내부적으로 start.rangeTo(end)로 해석** 된다.

코틀린 표준 라이브러리에는 모든 `Comparable` 객체에 대해 적용 가능한 rangeTo 함수가 들어있어 만약 **Comparable 인터페이스를 구현한 객체라면 rangeTo를 정의할 필요가 없다**.

```kotlin
operator fun <T: Comparable<T>> T.rangeTo(that: T): ClosedRange<T>
```

### 7.3.4 for 루프를 위한 iterator 관례
iterator 관례는 객체를 반복할 수 있는 iterator를 생성하는 데 사용된다. 이를 통해 객체를 for 루프와 같은 반복문에서 사용할 수 있다.

iterator 관례를 구현하려면 해당 클래스에 iterator() 함수를 정의해야 한다.
```kotlin
interface Iterator<out T> {
    fun hasNext(): Boolean
    fun next(): T
}
```


아래는 LocalDate를 사용하여 날짜에 대해 이터레이션하는 예제 코드이다.

```kotlin
// 이 객체는 LocalDate 원소에 대한 iterator를 구현한다.
operator fun ClosedRange<LocalDate>.iterator(): Iterator<LocalDate> =
    object : Iterator<LocalDate> {
        
    var current = start
        
    override fun hasNext(): Boolean = current <= endInclusive // compareTo 관례를 사용해 날짜를 비교
    

    override fun next() = current.apply { // 현재 날짜 저장 후 다음 날짜를 변경. 그 후 저장해둔 날짜 반환.
        current = plusDays(1)
    }
    
}

fun main() {
    val newYear = LocalDate.ofYearDay(2017, 1)
    val daysOff = newYear.minusDays(1) ..newYear
    for (dayOff in daysOff) { println(dayOff) }
}
```

## 7.4 구조 분해 선언과 component 함수

내부에서 구조 분해 선언은 관례를 사용하는데 구조 분해 선언의 각 변수를 초기화 하기 위해 `componentN`이라는 함수를 호출한다.

여기서 N은 구조 분해 선언에 있는 변수 위치에 따라 붙는 번호다.

data 클래스의 주 생성자에 들어있는 프로퍼티에 대해서는 컴파일러가 자동으로 componentN 함수를 만들어 준다.
```kotlin
class Point(val x: Int, val y: Int) {
    operator fun component1() = x
    operator fun component2() = y
}
```

## 7.5 프로퍼티 접근자 로직 재활용: 위임 프로퍼티

`위임 프로퍼티`(delegated property)는 프로퍼티의 **get/set 접근자 로직을 다른 객체에 위임**(delegate)함으로써 코드의 재사용성을 높이는 기능이다.

이를 통해 프로퍼티의 동작을 변경하거나 확장할 수 있다.

코틀린에서는 `by` 키워드를 사용하여 위임 프로퍼티를 정의한다.

```kotlin
class ExampleDelegate {
    private var storedValue: String = ""

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        println("Getting value: $storedValue")
        return storedValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("Setting value: $value")
        storedValue = value
    }
}

class Example {
    var customProperty: String by ExampleDelegate()
}

fun main() {
    val example = Example()

    // customProperty에 값을 설정하면 setValue가 호출됨
    example.customProperty = "Hello"

    // customProperty에 접근하면 getValue가 호출되며 저장된 값 반환
    println(example.customProperty)
}
```

### 7.5.2 위임 프로퍼티 사용: by lazy()를 사용한 프로퍼티 초기화 지연

지연 초기화는 객체의 일부분을 초기화하지 않고 남겨뒀다가 그 부분의 값이 필요할 경우 초기화할 때 흔히 쓰이는 패턴이다.

by lazy()를 사용하여 프로퍼티를 지연 초기화하려면 다음과 같은 구문을 사용한다.
```kotlin
val property: Type by lazy { initializationCode }
```
- `Type`: 프로퍼티의 타입.
- `initializationCode`: 프로퍼티가 처음 사용될 때 실행되는 초기화 코드 블록.

### 7.5.4 위임 프로퍼티 컴파일 규칙

위임 프로퍼티가 어떤 방식으로 동작하는지 정리해보자.

다음과 같은 위임 프로퍼티가 있는 클래스를 가정한다.
```kotlin
class C {
    // MyDelegate 인스턴스를 <delegate>에 저장하고,
    // 프로퍼티 prop은 <delegate>를 통해 값을 가져오고 설정함
    var prop: Type by MyDelegate()
}

val c= C()
```

컴파일러는 MyDelegate **클래스의 인스턴스를 감춰진 프로퍼티에 저장**하며 그 감춰진 프로퍼티를 `<delegate>`라는 이름으로 부른다.

또한 컴파일러는 **프로퍼티를 표현하기 위해 KProperty 타입의 객체를 사용**한다. 이 객체를 `<property>`라고 부른다.

```
// 컴파일러가 생성한 코드
class C {
    private val <delegate> = MyDelegate()
    var prop: Type
        get() = <delegate>.getValue(this, <property>)
        set(value: Type) = <delegate>.setValue(this, <property>, value)
}
```


