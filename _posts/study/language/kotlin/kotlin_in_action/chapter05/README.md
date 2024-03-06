---
title: Kotlin in action 05. 람다로 프로그래밍
author: jaeeun
date: 2024-01-14 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---

# chapter05. 람다로 프로그래밍

## 5.1 람다 식과 멤버 참조

### 5.1.1 람다 소개: 코드 블록을 함수 인자로 넘기기

코틀린은 함수형 프로그래밍을 지원하며, 함수형 프로그래밍에서는 함수를 값처럼 다룰 수 있어 **함수를 직접 다른 함수에 전달**할 수 있다.

이 때 `람다 식`을 사용하면 함수를 선언할 필요가 없고 **코드 블록을 직접 함수의 인자로 전달**할 수 있다.

### 5.1.3 람다 식의 문법

람다식의 기본 문법은 다음과 같다.

```
{ 파라미터 -> 함수 본문 }
```

예를 들어, 정수를 두 개 받아 더한 결과를 반환하는 간단한 람다식은 다음과 같다.

```kotlin
val sum: (Int, Int) -> Int = { x, y -> x + y }
```
- sum은 정수를 두 개 받아 정수를 반환하는 함수 타입을 가지고 있다.
- x와 y는 람다식의 매개변수이며, x + y는 함수 본문이다.

### 5.1.4 현재 영역에 있는 변수에 접근

- 람다를 함수 안에서 정의하면 함수의 파라미터 뿐 아니라 람다 정의의 앞에 선언된 로컬 변수까지 람다에서 사용할 수 있다.

```kotlin
fun main() {
    val outsideVariable = "I'm from outside!"

    val lambda: () -> Unit = {
        println(outsideVariable)
    }

    lambda()
}
```

- 코틀린 람다 안에서는 파이널 변수가 아닌 변수에 접근할 수 있으며 람다 안에서 바깥의 변수를 변경할 수도 있다.
```kotlin
fun main() {
    var outsideVariable = 10

    val lambda: () -> Unit = {
        println("Outside variable before: $outsideVariable")
        outsideVariable++
        println("Outside variable after: $outsideVariable")
    }

    lambda()

    println("Outside variable outside lambda: $outsideVariable")
}
```

이 때 람다 안에서 사용하는 외부 변수를 `람다가 포획한 변수(captured variable)`라고 한다.

기본적으로 함수 안에 정의된 로컬 변수는 함수의 생명주기와 동일하지만 포획한 변수가 있는 람다를 저장해서 함수가 끝난 뒤에 실행해도 람다의 본문 코드는 여전히 포획한 변수를 읽거나 쓸 수 있다.

그 동작 방식에 대해서 알아보자.

### 변수를 포획한 람다의 동작 방식

- 파이널 변수(`val`)를 포획한 경우에는 람다 코드를 변수 값과 함께 저장한다.
- 파이널이 아닌 변수(`var`)를 포획한 경우에는 변수를 특별한 래퍼(`Ref 클래스 인스턴스`)로 감싸서 나중에 변경하거나 읽을 수 있게 한 다음, 래퍼에 대한 참조를 람다 코드와 함께 저장한다.

#### `val`을 포획한 경우

val의 경우, 자바와 마찬가지로 final로 선언되어 해당 값이 포획된다.

```kotlin
fun main() {
  val immutable = 0
  exampleFunction(lambda = { immutable })
}

fun exampleFunction(
  lambda: () -> Int
) {
  lambda()
}
```

**디컴파일**

```
public final class MainKt {
   public static final void main() {
      final int immutable = 0;
      exampleFunction((Function0)(new Function0() {
         public Object invoke() {
            return this.invoke();
         }

         public final int invoke() {
            return immutable;
         }
      }));
   }

   public static void main(String[] var0) {
      main();
   }

   public static final void exampleFunction(@NotNull Function0 lambda) {
      Intrinsics.checkNotNullParameter(lambda, "lambda");
      lambda.invoke();
   }
}
```

<br/>

#### `var`을 포획한 경우

- IntRef라는 final 클래스에 의해 래핑되고, 내부적으로 변경 가능한 변수로 포획된다.
- IntRef로 생성된 클래스는 JVM 힙에 할당 되어 람다 블록에서 변수를 변경하고 생명주기가 끝나도 영향을 받지 않을 수 있다.

> Ref class 설명
> 
> Abstract base class for reference objects. This class defines the operations common to all reference objects. Because reference objects are implemented in close cooperation with the garbage collector, this class may not be subclassed directly.

```kotlin
fun main() {
    var mutable = 0
    exampleFunction(lambda = { mutable++ })
}

fun exampleFunction(
    lambda: () -> Int
) {
    lambda()
}
```

**디컴파일**
```
public final class MainKt {
   public static final void main() {
      final Ref.IntRef mutable = new Ref.IntRef();
      mutable.element = 0;
      exampleFunction((Function0)(new Function0() {
         public Object invoke() {
            return this.invoke();
         }

         public final int invoke() {
            Ref.IntRef var10000 = mutable;
            int var1;
            var10000.element = (var1 = var10000.element) + 1;
            return var1;
         }
      }));
   }

   public static void main(String[] var0) {
      main();
   }

   public static final void exampleFunction(@NotNull Function0 lambda) {
      Intrinsics.checkNotNullParameter(lambda, "lambda");
      lambda.invoke();
   }
}
```

### 5.1.5 멤버 참조 (::)

코틀린에서 멤버 참조는 프로퍼티나 메서드를 단 하나만 호출하는 함수 값을 만들어 준다.

**멤버 참조 문법 및 예시**

```
{클래스}::{멤버}

people.maxBy (Person::age)
// = people.maxBy { p -> p.age }
// = people.maxBy { it.age }
```

**멤버 참조**

람다가 인자가 여럿인 다른 함수한테 작업을 위임하는 경우 람다를 정의하지 않고 직접 위임 함수에 대한 참조를 제공면 편리하다.
```kotlin
val action = { person: Person, message: String -> sendEmail(person, message) }

// 람다 대신 멤버 참조 사용
val nextAction = ::sendEmail
```

**생성자 참조(Constructor Reference)**

생성자 참조를 사용하면 클래스 생성 작업을 연기하거나 저장해둘 수 있다.
:: 뒤에 클래스 이름을 넣어 생성자 참조를 만들 수 있다.

```kotlin
val createPerson = ::Person
val p = createPerson("Alice", 29)
```

## 5.2 컬렉션 함수형 API

### 5.2.1 filter와 map

#### filter
컬렉션을 이터레이션 하면서 주어진 람다에 각 원소를 넘겨 람다가 true 를 반환하는 원소를 모은다.

이 때 원소를 변환할 수는 없다.

```kotlin
val list = listOf(1, 2, 3, 4)
println(list.filter { it % 2 == 0 }) // [2, 4]
```

#### map
주어진 람다를 컬렉션의 각 원소에 적용한 결과를 모아서 새 컬렉션을 만든다.

```kotlin
val list = listOf(1, 2, 3, 4)
println(list.map { it * it }) // [1, 4, 9, 16]
```

### 5.2.2 all, any, count, find: 컬렉션에 술어 사용

#### all
모든 요소가 주어진 조건을 만족하는지 여부를 반환한다.

**예시**
```kotlin
val numbers = listOf(1, 2, 3, 4, 5)
val allEven = numbers.all { it % 2 == 0 } // false
```

#### any
모든 요소가 주어진 조건을 만족하는지 여부를 반환한다.

하나 이상의 요소가 조건을 만족하면 true를, 그렇지 않으면 false를 반환한다.

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)
val anyEven = numbers.any { it % 2 == 0 } // true
```

#### count
주어진 조건을 만족하는 요소의 개수를 반환한다.

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)
val countEven = numbers.count { it % 2 == 0 } // 2
```

#### find
주어진 조건을 만족하는 첫 번째 요소를 반환한다. 만족하는 요소가 없으면 null을 반환한다.

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)
val firstEven = numbers.find { it % 2 == 0 } // 2
```

### 5.2.3 groupBy: 리스트를 여러 그룹으로 이뤄진 맵으로 변경

#### groupBy

주어진 함수를 기준으로 컬렉션의 요소들을 그룹화한다.

그룹화된 결과는 맵 형태로 반환되며, 각 그룹의 키는 주어진 함수의 결과이다.

```kotlin
data class Person(val name: String, val age: Int)

val people = listOf(
    Person("Alice", 25),
    Person("Bob", 30),
    Person("Charlie", 25),
    Person("David", 30)
)

val groupedByAge: Map<Int, List<Person>> = people.groupBy { it.age }

/*
* {
*    25=[Person(name=Alice, age=25), Person(name=Charlie, age=25)],
*    30=[Person(name=Bob, age=30), Person(name=David, age=30)]
* }
* */
```

### 5.2.4 flatMap과 flatten: 중첩된 컬렉션 안의 원소 처리

#### flatMap

인자로 주어진 람다를 컬렉션의 모든 객체에 적용하고 (map), 람다를 적용한 결과 얻어지는 여러 리스트를 한 리스트로 한데 모은다. (flatten)

```kotlin
val originalList = listOf("hello", "world")
println(originalList.flatMap { it.toList() })
```
```
original list:      ["hello", "world"]

map:                [['h', 'e', 'l', 'l', 'o'],
                     ['w', 'o', 'r', 'l', 'd']]

flatten:            ['h', 'e', 'l', 'l', 'o', 'w', 'o', 'r', 'l', 'd']

result:             ['h', 'e', 'l', 'l', 'o', 'w', 'o', 'r', 'l', 'd']
```

### 5.3 지연 계산 (lazy) 컬렉션 연산

컬렉션 함수는 결과 컬렉션을 즉시 생성한다. 즉 컬렉션 함수를 연쇄하면 매 단계마다
계산 중간 결과를 새로운 컬렉션에 임시로 담는다.

이 때 시퀀스를 사용하면 중간 임시 컬렉션을 사용하지 않고 컬렉션 연산을 연쇄할 수 있다.

#### sequece 동작 방식

코틀린 지연 계산 시퀀스는 sequence 인터페이스에서 시작하는데 이 인터페이스는 한 번에 하나씩 열거될 수 있는 원소의 시퀀스를 표현한다.

Sequence 안에는 iterator 라는 메서드가 있고 이 메서드를 통해 시퀀스로부터 원소 값을 얻을 수 있다.

```kotlin
public interface Sequence<out T> {
    /**
     * Returns an [Iterator] that returns the values from the sequence.
     *
     * Throws an exception if the sequence is constrained to be iterated once and `iterator` is invoked the second time.
     */
    public operator fun iterator(): Iterator<T>
}
```

#### sequence 생성
`sequenceOf` 함수나 `컬렉션의 asSequence` 함수 혹은 `generateSequence` 함수를 사용하여 Sequence를 생성할 수 있다.

```kotlin
val sequence = sequenceOf(1, 2, 3, 4, 5)

val list = listOf(1, 2, 3, 4, 5)
val sequence2 = list.asSequence()
```

```kotlin
val naturalNumbers = generateSequence(0) { it + 1 }
val numberTo100 = naturalNumbers.takeWhile { it <= 100 }
println(numberTo100.sum()) // 지연 연산은 sum의 결과를 계산할 때 수행된다.
```

#### 지연 계산

Sequence는 주로 중간 연산과 최종 연산으로 나뉜다.

- **중간 연산** (map, filter..)
  - 중간연산은 다른 시퀀스를 반환하고 이 시퀀스는 최초 시퀀스의 원소 변환 방법을 안다.
- **최종 연산** (toList, toSet, forEach..)
  - 최종 연산이 호출되면 모든 중간 연산이 한 번에 실행되며 결과를 반환한다.

### 5.4 자바 함수형 인터페이스 활용

SAM은 "Single Abstract Method"의 약어로, 한 개의 추상 메서드만을 가진 인터페이스를 가리킨다.

SAM 변환을 통해 자바의 함수형 인터페이스를 더 간결하게 사용할 수 있다.

예를 들어, 자바에서 다음과 같은 함수형 인터페이스가 있을 때
```java
interface OnClickListener {
    void onClick(View view);
}
```

이 인터페이스는 추상 메서드 onClick 하나만을 가지고 있다. 코틀린에서는 SAM 변환을 통해 다음과 같이 사용할 수 있다.

```kotlin
button.setOnClickListener { view ->
    // onClick 구현
}
```

### 5.5 수신 객체 지정 람다: with와 apply

`with`와 `apply` 는 코틀린에서 객체 초기화나 설정을 간편하게 수행하는 데 사용되는 함수이다.

#### with
with 함수는 주어진 객체를 블록 내에서 사용하고, 블록 내에서 해당 객체의 멤버에 직접 접근할 수 있도록 한다.

with 가 반환하는 값은 람다 코드를 실행한 결과이며 그 결과는 람다 식의 본문에 있는 마지막 식의 값이다.

```kotlin
val person = Person()

with(person) {
    name = "John"
    age = 30
}

println(person.name) // "John"
println(person.age)  // 30
```

#### apply
apply 함수는 with 함수와 유사하지만, 차이점은 항상 자기 자신을 반환한다.

```kotlin
val person = Person().apply {
    name = "John"
    age = 30
}
```
