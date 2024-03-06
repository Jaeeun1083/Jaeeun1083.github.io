---
title: Kotlin in action 02. 코틀린 기초
author: jaeeun
date: 2023-12-16 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---

## chapter02. 코틀린 기초

### 2.1 기본 요소: 함수와 변수

#### 2.1.2 함수

기본적인 함수 선언 방법은 다음과 같다.
- **함수 선언**은 fun 키워드로 시작한다.
- **함수 이름**은 fun 키워드 다음에 온다.
- **파라미터 목록** 은 함수 이름 뒤 괄호 안에 들어 간다.
- **함수 반환 타입**은 파라미터 목록의 닫는 괄호 다음에 오는데, 괄호와 반환 타입 사이를 콜론으로 구분 한다.

```kotlin
fun max(a: Int, b: Int) : Int {
    return if (a>b) a else b
}
```

**함수 본문이 식 하나인 경우** 중괄호를 없애고 return을 제거하면서 등호를 식 앞에 붙여 더 간결하게 표현할 수도 있다. 또한 `타입 추론` 기능이 있어 **반환 타입 생략이 가능**하다.

```kotlin
fun max(a: Int, b: Int) : Int = if (a>b) a else b

// 반환 타입 추론 기능
fun max(a: Int, b: Int)  = if (a>b) a else b
```

#### 2.1.3 변수

변수 선언 시 사용하는 키워드
- **val**
  - 변경 불가능한 참조를 저장하는 변수
  - 참조 자체는 불변일지라도 그 참조가 가리키는 객체의 내부 값은 변경될 수 있다.
       ```kotlin
       val language = arrayListOf("Java")
       language.add("Kotlin")
       ```
- **var**
  - 변경 가능한 참조 (변수의 값을 바꿀 수 있음)
  - 변수의 타입은 고정돼 바뀌지 않는다.

#### 2.1.4 더 쉽게 문자열 형식 지정: 문자열 템플릿

- 변수를 문자열 안에 사용할 수 있는데 이 때 변수 앖에 `$`를 추가해야 한다.
- $ 문자를 문자열에 넣고 싶으면 `\`를 사용해 $를 이스케이프 시켜야 한다.
- 식을 넣고 싶을 경후 중괄호 ({})를 사용할 수 있다.

```kotlin
fun main(args: Array<String>) {
  val name = "Kotlin"
  println("Hello $name!") // 결과 : Hello Kotlin

  println("\$x") // 결과 : $x

  println("Hello, ${if (args.isNotEmpty()) args[0] else "someone"}!")
}
```

### 2.2 클래스와 프로퍼티

클래스의 목적은 데이터를 캡슐화하고 캡슐화한 데이터를 다루는 코드를 한 주체에 가두는 것이다.

자바에서는 데이터를 필드에 저장하며 이러한 멤버 필드에 클라이언트가 접근할 수 있는 접근자 메서드(ex. getter, setter) 를 제공한다.
이러한 **필드와 접근자를 묶어 프로퍼티**라고 한다.

#### 2.2.1 프로퍼티

코틀린은 프로퍼티를 언어 기본 기능으로 제공한다.
- 읽기 전용 프로퍼티 (val) : 비공개 필드와 필드를 읽는 공개 게터를 만든다.
- 쓸 수 있는 프로퍼티 (var) : 비공개 필드, 공개 게터, 공개 세터를 만든다.
```kotlin
class Person (
  val name: String, // 쓸 수 있는 프로퍼티
  var isMarried: Boolean // 읽기 전용 프로퍼티
)

// Person 클래스 사용하기
val person = Person("Bob", true)
println(person.name)

person.isMarried = false
println(person.isMarried)
```

#### 2.2.2 커스텀 접근자

접근자를 직접 작성하는 방법을 알아보자.

```kotlin
class Rectangle(val height: Int, val width: Int){
  val isSquare: Boolean
    get() { // 프로퍼티 게터 선언
      return height == width
    }
}
// getter를 재설정하여, rectangle.isSquare이 호출되면 위 get함수가 실행된다.
```

#### 2.2.3 코틀린 소스코드 구조: 디렉터리와 패키지

코틀린에서는 자바와 달리 여러클래스를 한 파일에 넣을 수 있고, 파일의 이름도 마음대로 정할 수 있다.

### 2.3 선택 표현과 처리: enum과 when

#### 2.3.1 enum 클래스 정의

```kotlin
enum class Color {
    RED, ORANGE, YELLOW
}

enum class Color(
  val r : Int, val g: Int, val b: Int //상수의 프로퍼티 지정
) {
  RED(255, 0, 0), ORANGE(255, 165, 0), // 상수를 생성할 때 프로퍼티 값을 지정한다.
  YELLOW(255, 255,0); //끝에 반드시 세미콜론을 붙여준다.(유일)
    
  fun rgb() = (r*256 +g)*256 +b //메소드
}

println(Color.RED.rgb())
```

#### 2.3.2 when으로 enum 클래스 다루기

**자바에서 switch와 코틀린에서는 when**은 같은 역할을 한다. 이 때 when 도 if 와 마찬가지로 값을 만들어내는 식이므로 **식이 본문인 함수에 when을 바로 사용**할 수 있다.

```kotlin
fun getMnemonic(color: Color) = // 함수의 반환 값으로 when 식을 직접 사용한다.
  when (color) {
    Color.RED -> "Richard"
    Color.ORANGE -> "Of"
    // 생략
  }
    
println(getMnemonic(Color.RED)) //Richard
```

한 when 분기 안에 여러 값을 사용할 수도 있다.
```kotlin
fun getMnemonic(color: Color) = 
  when (color) {
    Color.RED, Color.ORANGE -> "warm"
    Color.BLUE, Color.VIOLOET -> "cold"
    // 생략
  }
    
println(getMnemonic(Color.RED)) //warm
```

#### 2.3.3 when과 임의의 객체를 함께 사용

코틀린 when의 분기 조건은 임의의 객체를 허용한다.
```kotlin
fun mix(c1: Color, c2: Color) = 
  when (setOf(c1, c2)) {
    setOf(RED, YELLOW) -> ORANGE
    setOf(YELLOW, BLUE) -> GREEN
    else -> throw Exception("Dirty Color")
  }
println(mix(BLUE,YELLOW)) //GREEN
```

#### 2.3.4 인자 없는 when 사용
위의 예제는 mix 함수 호출 시 여러 Set 인스턴스를 생선한다.
이 함수가 아주 자주 호출된다면 불필요한 가비지 객체가 늘어나는 것을 방지하기 위해 인자가 없는 when 식을 활용하여 가독성은 떨어지지만 성능을 향상시킬수 있다.
```kotlin
fun mixOptimized(c1: Color, c2: Color) = 
  when  {  //when의 식의 인자로 받은 객체가 같은지 테스트한다.
    (c1 == RED && c2 ==YELLOW) ||
    (c1 == YELLOW && c2 == RED) -> 
    ORANGE
    // 생략
  }
```

#### 2.3.5 스마트 캐스트 : 타입 검사와 타입 캐스트를 조합

자바에서는 어떤 변수의 타입을 `instanceof`로 확인 후 그 타입에 속한 멤버에 접근하기 위해 명시적으로 변수 타입을 캐스팅해야 하지만 

코틀린에서는 `is`로 검사하고 나면 변수를 캐스팅하지 않아도 해당 타입으로 선언된 것처럼 사용할 수 있다.
이를 **스마트 캐스트**라고 한다.

원하는 타입으로 명시적으로 타입캐스팅을 하려면 `as`를 사용하면 된다.

```kotlin
interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr

fun eval(e: Expr) : Int {
  if (e is Num) {
    // is로 검사 후 as 로 타입 캐스팅은 코틀린에서 불필요한 중복이다.
    // val n = e as Num 
    return e.value
  }
  if (e is Sum) { //e가 Sum인지 체크되어서, e는 스마트캐스팅되어 e.left를 이용할 수 있다.
    return eval(e.right) + eval(e.left)
  }
}
```
스마트 캐스트의 동작 방식을 디컴파일하여 알아보면 다음과 같다.

```java
public final class Test {
  public static final int eval(@NotNull Expr e) {
    if (e instanceof Num) {
      return ((Num)e).getValue();
    }
  }
}
```

#### 2.3.6 리팩토링: if를 when으로 변경
코틀린의 if는 값을 만들어 내기 때문에, 자바와 달리 3항 연산자가 따로 없다.
이 특성을 이용해 위 예제의 return 문과 중괄호를 없앨 수 있다.
```kotlin
fun eval(e: Expr) : Int = 
  if (e is Num) {
    e.value
  } else if (e is Sum) { 
    eval(e.right) + eval(e.left)
  }else {
    throw IllegalArgumentException()
  }
```

혹은 if 중첩 대신 when을 사용할 수도 있다.
```kotlin
fun eval(e: Expr) : Int = 
  when (e) {
  is Num ->  //스마트캐스트가 사용됨
    e.value
  is Sum ->
    eval(e.right) + eval(e.left) //블록의 마지막이 결과값이다
  else ->
    throw IllegalArgumentException()
  }
```

### 2.4 대상을 이터레이션: while과 for 루프

#### 2.4.1 while 루프
- while과 do-while 루프가 있고 문법은 자바와 동일하다.

#### 2.4.2 수에 대한 이터레이션: 범위와 수열
루프의 가장 흔한 용례인 초깃값, 증가 값, 최종 값을 사용한 루프를 대신하기 위해 코틀린에서는 범위를 사용한다.
```kotlin
for (i in 1..100) { //1 ~ 100
  print(i) // 1 to 100
}

for (i in 100 downTo 1 step 2) { // 감소 수열
  print(i) // 100, 98, 96 ..
}

for (i in 0 until 100){ //i in 0..99 와 같다.
  print(i) // 1 to 99
}
```

#### 2.4.3 맵에 대한 이터레이션

.. 연산자는 숫자 타입의 값뿐 아니라 문자 타입의 값에도 적용할 수 있다.

또한 for 루프를 사용해 이터레이션하는 컬렉션의 원소를 풀 수도 있다.

```kotlin
val binaryReps = TreeMap<Char, String>()

for (c in 'A' .. 'F') { // A~F
  val binary = Integer.toBinaryString(c.toInt())
  binaryReps[c] = binary
}
for ((letter,binary) in binaryReps){ // 맵의 키와 값을 두 변수에 각각 대입하여 이터레이션 한다.
  println("$letter = $binary") // A = 1000001  B = 1000010 ...
}
```

컬렉션에 대해서도 인덱스를 사용할 수 있다.

```kotlin
val list = arrayListOf("10", "11", "1001")

for ((index, element) in list.withIndex()){
  println("$index = $element")
}
```

#### 2.4.4 in으로 컬렉션이나 범위의 원소 검사

- in을 사용해 값이 범위에 속하는지 검사하기
  ```kotlin
  fun isLetter(c: Char) = c in 'a'..'z' || c in 'A'..'Z' // 'a' <= c && c <= 'z'
  fun isNotDigit(c: Char) = c !in '0'..'9'
  ```
- when에서 in 사용하기
  ```kotlin
  fun recognize (c: Char) = when(c){
    in '0'..'9' -> "It's a digit"
    else -> "I don't know"
  }
  ```
- 비교가능한 클래스라면(java.lang.Comparable) 범위를 만들수있다.
  ```kotlin
  println("Kotlin" in "Java".."Scala") // "Java" <= "Kotlin" && "Kotlin" <= "Scala"
  ```
- 컬렉션에서도 사용이 가능하다.
  ```kotlin
  println("Kotlin" in setOf("Java", "Scala")) // setOf(new String[]{"Java", "Scala"}).contains("Kotlin");
  ```
  
### 2.5 코틀린의 예외 처리
코틀린의 throw는 식이므로 다른 식에 포함될 수 있다.

```kotlin
val percentage = 
  if (number in 1..100)
  	number
  else
   	throw IllegalArgumentException("A percentage value must be between 0 and 100: $number")
```

#### 2.5.1 try, catch, finally

코틀린은 체크 예외와 언체크 예외를 구별하지 않으며 함수가 던질 수 있는 예외를 명시할 필요가 없다.

```kotlin
fun readNumber(reader: BufferedReader) : Int? {
    try {
        // 생략
    } catch (e: NumberFormatException) { // 예외 타입을 :의 오른쪽에 쓴다.
        return null
    } finally {
        reader.close
    }
}
```

`throw`는 코틀린의 표현식이므로 Elvis 표현식의 일부로 사용할 수 있다.
```kotlin
val s = person.name ?: throw IllegalArgumentException("Name required")
```

throw 표현식의 유형은 `Nothing`이다. 
이 유형에는 값이 없으며 자신의 코드에서 Nothing를 사용하여 반환하지 않는 함수를 표시할 수 있다.
```kotlin
fun fail(message: String): Nothing {
    throw IllegalArgumentException(message)
}
```

#### 2.5.2 try를 식으로 사용

```kotlin
fun readNumber(reader: BufferedReader){
  val number = try{
   	Integer.parseInt(reader.readLine())
  } catch(e: NumberFormatException) {
  	return
  }
  println(number)
}
```
