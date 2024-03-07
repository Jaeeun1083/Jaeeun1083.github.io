---
title: Kotlin in action 04. 클래스, 객체, 인터페이스
author: jaeeun
date: 2024-01-07 00:00:00 +0800
categories: [Study, "Kotlin"]
tags: ["Kotlin in action"]
render_with_liquid: false
---

# chapter04. 클래스, 객체, 인터페이스

## 4.1 클래스 계층 정의

### 4.1.1 코틀린 인터페이스

코틀린 인터페이스 안에는 추상 메서드 뿐 아니라 구현이 있는 메서드를 정의할 수 있다. 다만 인터페이스에는 아무런 상태(필드)도 들어갈 수 없다.

만약 인터페이스를 여러개 상속 받을 때 인터페이스에 **디폴트 구현이 들어있는 동일한 이름과 시그니처가 같은 메서드**가 있다면 어느 상위 타입의 멤버 메서드를 호출할지 지정해야한다.

```kotlin
interface Clickable {
    fun click() // 일반 메서드 선언
    fun showOff() = println("I'm clickable") // 디폴트 구현이 있는 메서드
}

interface Focusable {
    fun setFocus(b: Boolean) = println("I ${if (b) "got" else "lost"} focus.")
    fun showOff() = println("I'm focusable!")
}

// interface 구현
class Button : Clickable, Focusable {
    override fun click() = println("I was Clicked")

    // 이름과 시그니처가 같은 멤버 메서드에 대해 둘 이상의 디폴트 구현이 있는 경우 인터페이스를
    // 구현하는 하위 클래스에서 명시적으로 새로운 구현을 제공해야 한다.
    override fun showOff() {
        super<Clickable>.showOff()
        super<Focusable>.showOff() 
    }
}
```

### 4.1.2 open, final, abstract 변경자: 기본적으로 final

코틀린의 클래스와 메서드는 기본적으로 final 이다. 어떤 클래스의 상속을 허용하려면 클래스 앞에 open 변경자를 붙여야한다.

또한 오버라이드를 허용하고 싶은 메서드나 프로퍼티 앞에도 open 변경자를 붙여야 한다.

오버라이드한 메서드는 기본적으로 열려있지만 오버라이드하는 메서드 앞에 final을 명시하여 이후 해당 클래스를 상속했을 때 오버라이드를 막을 수도 있다.

```kotlin
open class RichButton: Clickable { // 이 클래스는 열려있다. 다른 클래스가 이 클래스를 상속할 수 있다.
    fun disable() {} // 이 함수는 파이널이다.
    open fun animate() {} // 이 함수는 열려있다.
    override fun click() {} // 오버라이드한 메서드는 기본적으로 열려있다.
}
```

### 4.1.3 가시성 변경자: 기본적으로 공개

코틀린에서는 패키지 전용 가시성 변경자로 internal이 있다.

이는 모듈 내부에서만 볼 수 있어 모듈 구현에 대한 캡슐화를 제공한다.

```kotlin
internal open class TalkativeButton: Focusable {
    private fun yell () = println("Hey!")
    protected fun whisper() = println("Let's talk!")
}

fun TalkativeButton.giveSpeech() { // public 멤버가 internal 수신 타입인 TalkativeButton을 노출할 수 X
    yell() // yell 은 TalkativeButton의 private 멤버이므로 접근 X
    whisper() // whisper는 TalkativeButton의 protected 멤버이므로 접근 X
}
```

**자바와 다른 코틀린의 protected**
- 코틀린에서 protected 멤버는 오직 어떤 클래스나 그 클래스를 상속한 클래스 안에서만 보인다.

#### 4.1.4 내부 클래스와 중첩된 클래스: 기본적으로 중첩 클래스

코틀린의 중첩 클래스는 명시적으로 요청하지 않는 한 바깥쪽 클래스 인스턴스에 대한 접근 권한이 없다.

이는 내부 클래스(inner class)와 달리 중첩 클래스가 기본적으로 정적(static)으로 간주되기 때문이다.

만약 중첩 클래스에서 바깥쪽 클래스 인스턴스에 접근하고 싶다면 인스턴스를 static 으로 선언하여 접근할 수 있다.

```kotlin
class OuterClass {
    private val outerProperty = "Outer Property"

    companion object {
        const val staticOuterProperty = "companionObjectProperty"
    }
    
    class NestedClass {
        // 중첩 클래스는 기본적으로 바깥쪽 클래스의 인스턴스에 접근할 수 없다.
        // outerProperty에 직접 접근할 수 없다.
        // static 인스턴스인 staticOuterProperty에는 접근할 수 있다.
        fun printMessage() {
            println("Nested class message")
            println("Nested class message: $staticOuterProperty")
        }
    }

    inner class InnerClass {
        // 내부 클래스는 바깥쪽 클래스의 인스턴스에 접근할 수 있다.
        // outerProperty에 접근 가능하다.
        fun printMessage() {
            println("Inner class message: $outerProperty")
            println("Inner class message: $this@OuterClass.outerProperty")

        }
    }
}
```

### 4.1.5 봉인된 클래스: 클래스 계층 정의 시 계층 확장 제한

코틀린 컴파일러는 when을 사용할 때 디폴트 분기인 else 분기를 덧붙이게 강제한다.

이 때 디폴트 분기가 있으면 클래스 계층에 새로운 하위 클래스를 추가하더라도 컴파일러가 when이 모든 경우를 처리하는지 제대로 검사할 수 없다. 
```kotlin
interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr
// 추가된 하위 클래스
class Minus(val left: Expr, val right: Expr) : Expr

fun eval(e: Expr): Int =
  when(e) {
    is Num -> e.value
    is Sum -> eval(e.right) + eval(e.left)
    // 새로운 클래스 처리가 없어도 컴파일 에러가 발생하지 않음
    else -> throw IllegalArgumentException()
  }
```

코틀린은 이런 문제를 해결하기 위해 `sealed 클래스`가 있다. 

상위 클래스에 sealed 변경자를 붙이면 그 **상위 클래스를 상속한 하위 클래스 정의를 제한**할 수 있다.

when 식에서 seald 클래스의 모든 하위 클래스를 처리한다면 디폴트 분기가 필요 없다.

```kotlin
sealed class Expr { // sealed로 표시된 클래스는 자동으로 open 이다.
    class Num(val value:Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()
    class Minus(val left: Expr, val right: Expr) : Expr()
}

fun eval(e: Expr): Int =
    when(e) {
        is Expr.Num -> e.value
        is Expr.Sum -> eval(e.right) + eval(e.left)

        // is Expr.Minus -> eval(e.left) - eval(e.right)
        // else -> throw IllegalArgumentException()
    }
```

- 하위 클래스 추가 후 when 에서 해당 클래스를 처리하는 분기문이 없다면 
`'when' expression must be exhaustive, add necessary 'is Minus' branch or 'else' branch instead` 라는 컴파일 에러가 발생한다.

## 4.2 뻔하지 않은 생성자와 프로퍼티를 갖는 클래스 선언

### 4.2.1 클래스 초기화: 주 생성자와 초기화 블록

**주 생성자**
- 클래스 이름 뒤에 위치하며, 생성자 파라미터를 지정 및 그 생성자 파라미터에 의해 초기화 되는 클래스의 속성(property)을 정의할 수 있다.
- 이 때 `val` 은 이 파라미터에 상응하는 프로퍼티가 생성된다는 뜻이다.
```kotlin
class User(val firstName: Int, val lastName: String)
```

**초기화 블록**
- 클래스 내에서 초기화 코드를 실행해야 할 경우 사용할 수 있다. `init`키워드로 표시된다.
```kotlin
class User(val firstName: String, val lastName: String) {
    // 주 생성자에서 선언한 속성 초기화
    // 초기화 블록은 객체가 생성될 때 실행됨
    val fullName: String
    init {
        fullName = "$lastName $firstName"
    }
}

// 프로퍼티 초기화 코드를 초기화 블록이 아닌 선언부에 포함시킬 수 있다.
class User(val firstName: String, val lastName: String) {
    val fullName: String = "$lastName $firstName"
}
```

### 4.2.2 부 생성자: 상위 클래스를 다른 방식으로 초기화

부 생성자는 constructor 키워드를 사용하여 선언하며, 필요한 매개변수와 함께 정의된다.

```kotlin
class MyClass {
    // 주 생성자와 함께 사용할 수 있는 부 생성자
    constructor() {
        // 부 생성자의 초기화 코드
    }

    // 다른 부 생성자
    constructor(param: Int) {
        // 부 생성자의 초기화 코드
    }
}
```

### 4.2.4 커스텀 접근자

프로퍼티의 접근자는 기본적으로 자동으로 생성되지만, 특정한 동작이나 로직을 수행하도록 하려면 커스텀 접근자를 사용할 수 있다.

혹은 기본 가시성을 가진 게터를 컴파일러가 생성하게 두는 대신 세터의 가시성을 private으로 지정할 수도 있다.

```kotlin
class MyClass {
    // 프로퍼티 선언
    var myProperty: Int = 0
        get() {
            // 커스텀한 get 접근자
            println("Getting the value of myProperty")
            return field
        }
        set(value) {
            // 커스텀한 set 접근자
            println("Setting the value of myProperty to $value")
            field = value
        }
    var counter: Int = 0
        private set
}
```

## 4.3 컴파일러가 생성한 메서드: 데이터 클래스와 클래스 위임

### 4.3.1 데이터 클래스: 모든 클래스가 정의해야 하는 메서드 자동 생성

어떤 클래스가 데이터를 저장하는 역할만 수행해야 한다면 toString, equals, hashCode를 오버라이드 해야한다.

코틀린에서는 `data 변경자`를 붙여 필요 메서드를 컴파일러가 자동으로 만들어주게 할 수 있다.
- toString : 각 필드를 선언 순서대로 표시하는 문자열 표현을 만들어 준다.
- equals : 모든 프로퍼티 값의 동등성을 확인한다.
- hashCode: 모든 프로퍼티의 해시 값을 바탕으로 계산한 해시 값을 반환한다.
- copy: 객체를 복사하면서 일부 프로퍼티를 바꿀 수 있게 해준다.

#### 4.3.3 클래스 위임: by 키워드

상속을 허용하지 않는 클래스에 새로운 동작을 추가해야할 때 `데코레이터 패턴`을 활용할 수 있다.
- 데코레이터 패턴이란 상속을 허용하지 않는 클래스 대신 사용할 수 있는 새로운 클래스를 만들되 **기존 클래스와 같은 인터페이스를 데코레이터가 제공**하게 만들고,
  **기존 클래스를 데코레이터 내부에 필드로 유지**하는 것이다.
- 이 때 **새로 정의해야 하는 기능은 데코레이터의 메서드에 새로 정의**하고 **기존 기능이 그대로 필요한 부분은 데코레이터 메서드가 기존 클래스의 메서드에 요청을 전달**한다.
코틀린에서는 `by 키워드`를 통해 **인터페이스에 대한 구현을 다른 객체에 위임 중이라는 사실을 명시**할 수 있다.

by 키워드를 사용하여 데코레이터를 만드는 예시를 보자

```kotlin
class DelegationCollection<T>: Collection<T> {
    private val innerList = arrayListOf<T>()
    /* 구현해야 하는 메서드
    override val size: Int
        get() = TODO("Not yet implemented")

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<T> {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun contains(element: T): Boolean {
        TODO("Not yet implemented")
   } */
}
```

- 다음과 같이 `DelegationCollection`이라고 하는 Collection을 상속하는 클래스를 만들게 되면 `Class 'DelegationCollection' is not abstract and does not implement abstract member` 라고 하며 
  abstract member를 implements하기를 요구한다. 아무 동작을 변경하지 않는 데코레이터를 만들 때도 해당 member들을 implement해야한다.

그러나 인터페이스를 구현할 때 `by 키워드`를 통해 그 인터페이스에 대한 구현을 다른 객체에 위임 중이라는 사실을 명시할 수 있다.
만약 위임하지 않고 새로운 구현을 제공하고 싶다면 override 해서 구현할 수 있다. 
```kotlin
class DelegatingWithByKeyword<T> (
    innerList: Collection<T> = ArrayList<T>()
) : Collection<T> by innerList { // Collection의 구현을 innserList에게 위임한다.
    override fun contains(element: T): Boolean {
        TODO("Not yet implemented")
    }
}
```

## 4.4 object 키워드: 클래스 선언과 인스턴스 생성


### 4.4.1 객체 선언: 싱글턴을 쉽게 만들기

코틀린은 객체 선언 (object declaration) 기능을 통해 싱글턴을 지원한다.

클래스와 마찬가지로 객체 선언 안에도 프로퍼티, 메서드, 초기화 블록이 들어갈 수 있지만 생성자(주 생성자, 부 생성자)는 객체 선언에 쓸 수 없다.
- 싱글턴 객체는 객체 선언문이 있는 위치에서 생성자 호출 없이 즉시 만들어지기 때문이다. (= 클래스 정의와 동시에 해당 클래스의 인스턴스 생성 )

> 코틀린 객체를 자바에서 사용하기
> 
> 코틀린 객체 선언은 유일한 인스턴스에 대한 (이름이 INSTANCE인) 정적인 필드가 있는 자바 클래스로 컴파일 된다.
> 자바 코드에서 코틀린 싱글턴 객체를 사용하려면 정적인 INSTANCE 필드를 사용하면 된다.

### 4.4.2 동반 객체: 팩토리 메서드와 정적 멤버가 들어갈 장소

동반 객체(Companion Object)는 코틀린에서 클래스 내에 정의된 객체로, 해당 클래스의 인스턴스 없이도 호출할 수 있는 멤버를 가질 수 있다.

일종의 클래스 수준의 멤버를 정의하기 위한 객체이다.

```kotlin
class MyClass {
    // 일반 속성과 메서드
    val property1: Int = 10

    fun method1() {
        println("Method 1")
    }

    // 동반 객체 정의
    companion object {
        val property2: String = "Companion Property"
        
        fun method2() {
            println("Companion Method 2")
        }
    }
}

// 동반 객체의 멤버는 해당 클래스의 인스턴스가 없어도 사용할 수 있다.
fun main() {
    println(MyClass.property2) // Companion Property
    MyClass.method2()           // Companion Method 2
}
```

### 4.4.4 객체 식: 무명 내부 클래스를 다른 방식으로 작성

무명 객체 (anonymous object)를 정의할 때도 object 키워드를 사용할 수 있다.

이벤트 리스너 구현 예시를 통해 알아보자

```kotlin
// 이벤트 리스너를 정의하는 인터페이스
interface OnClickListener {
    fun onClick()
}

// 예시에서 사용할 클래스
class Button {
    // 클릭 이벤트 리스너를 저장할 프로퍼티
    var clickListener: OnClickListener? = null

    // 클릭 이벤트가 발생했을 때 호출되는 메서드
    fun click() {
        // 클릭 이벤트 리스너가 설정되어 있다면 해당 리스너의 onClick 메서드 호출
        clickListener?.onClick()
    }
}

fun main() {
    // Button 객체 생성
    val button = Button()

    // 무명 객체를 이용한 이벤트 리스너 설정
    button.clickListener = object : OnClickListener {
        override fun onClick() {
            println("Button Clicked!")
        }
    }

    // 버튼 클릭 시 이벤트 리스너의 onClick 메서드가 호출됨
    button.click()
}
```
