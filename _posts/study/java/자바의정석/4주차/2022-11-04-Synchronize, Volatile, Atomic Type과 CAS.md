---
title: Synchronize, Volatile, Atomic Type과 CAS
author: jaeeun
date: 2022-11-04 00:00:00 +0800
categories: [Study, Java]
tags: [java]
render_with_liquid: false
---

# 제네릭

다양한 타입의 객체들을 다루는 메서드나 클래스에 컴파일 시 타입 체크를 해주는 기능이다. 객체의 타입을 컴파일 시에 체크하기 때문에 객체의 타입 안정성을 높이고 타입 캐스팅을 제거할 수 있다.

제네릭 예시를 통해 살펴보자.

```
#### 제네릭 사용 전 ####
List myIntList = new LinkedList(); // 1
myIntList.add(new Integer(0)); // 2
Integer x = (Integer) myIntList.iterator().next(); // 3

#### 제네릭 사용 후 ####
List<Integer> myIntList = new LinkedList<Integer>(); // 1'
myIntList.add(new Integer(0)); // 2'
Integer x = myIntList.iterator().next(); // 3'
```

- 제네릭은 매개변수로 타입을 전달할 수 있다. 이는 타입 파라미터라고 부르며 타입 파라미터는 `<>` 꺽쇠 안에 선언한다.
- 사용 전후의 세번째 줄을 보면 제네릭을 사용했을 때 타입 캐스팅을 하지 않아도 되는 것을 확인할 수 있다.
- 즉 제네릭을 사용하므로써 **명시적으로 타입을 지정**하게 되고 이는 컴파일 시 잘못된 타입으로 치환되는 것을 막을 수 있다. 따라서 **런타임 시 다른 타입으로 잘못 형변환하여 예외가 발생하는 일이 없다**.


## 제네릭 선언 방법

### 클래스에서 제네릭 사용하기
```
public class ClassName <T> { ... }
public Interface InterfaceName <T> { ... }
```
- 여기서 T는 클래스 내부에서 타입 파라미터를 대표하는 값으로 사용된다.
- 이 경우 T가 타입 파라미터로 넘겨준 실제 타입으로 대체되게 된다.

### 메소드에서 제네릭 사용하기

- 메소드에 한정한 제네릭 즉 클래스와는 별도로 제네릭을 사용할 수 있다.

```
public <T> T genericMethod(T o) {	// 제네릭 메소드
  ...
}
 
[접근 제어자] <제네릭타입> [반환타입] [메소드명]([제네릭타입] [파라미터]) {
  ...
}
```
- 클래스와는 다르게 반환타입 이전에 <> 제네릭 타입을 선언한다.

제네릭을 선언할 때 `<>`꺽쇠 안에는 어떤 단어가 들어가도 상관이 없지만 자바에서 정의한 기본 규칙은 있다.

#### 제네릭 타입의 이름 규칙

- E : 요소 (Element, 자바 컬렉션에서 주로 사용됨)
- K : 키
- N : 숫자
- T : 타입
- V : 값
- S, U, V : 두번째, 세번째, 네번째에서 선언된 타입


## 제네릭 주요 개념

### 와일드 카드

```
void printCollection (Collection<Object> c)  {
  for (Object e : c ) {
    ...
  }
}

void printCollectionWithWildCard (Collection<?> c) {
 
  for (Object e : c) {
    ...
 
  }
}
```
- 이 예시를 보면 `<Object>`라는 타입을 지정하는데 제네릭은 지정한 타입만 받을 수 있기 때문에 최상위 클래스 Object를 사용해 `Collection<Object>`로 선언하더라도 모든 Collection 타입을 허용할 수 있는 것이 아니다.
- 그래서 collection of unknown이라고 하는 모든 element 타입에 매치될 수 있는 **와일드 카드**를 사용하는 이유이다.

그러나 Collection<?>로 와일드카드를 사용하면 Collection 타입은 안전하게 사용할 수 있지만 임의의 객체를 다 받을 수는 없다.

```
Collection<?> c = new ArrayList<String>();
 
c.add(new Object()); // 컴파일 에러
```

- 위의 예제를 보면 c의 element type이 알수 없는 유형을 나타내므로 c에는 nulll 외에는 아무 것도 전달할 수 없다.

### 바운디드 와일드카드

제네릭으로 넘어오는 타입에 대해서 제한하고 싶을 경우 사용할 수 있는 방법이다.

이때 나오는 것이 `extends`, `super`, `? (와일드 카드)` 이이며 다음과 같이 상한(upper bound)과 하한(lower bound)을 제한할 수 있다.

```
<K extends T>	// T와 T의 자손 타입만 가능 (K는 들어오는 타입으로 지정 됨)
<K super T>	    // T와 T의 부모(조상) 타입만 가능 (K는 들어오는 타입으로 지정 됨)
 
<? extends T>	// T와 T의 자손 타입만 가능
<? super T>	    // T와 T의 부모(조상) 타입만 가능
<?>		        // 모든 타입 가능.
```
