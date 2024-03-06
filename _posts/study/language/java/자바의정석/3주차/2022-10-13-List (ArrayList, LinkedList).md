---
title: List (ArrayList, LinkedList)
author: jaeeun
date: 2022-10-13 00:00:00 +0800
categories: [Study, Java]
tags: [java]
render_with_liquid: false
---

# List (ArrayList, LinkedList)

- 저장된 요소들의 순서가 있고 데이터에 중복이 가능하다.
- 자바에서는 리스트를 ArrayList와 LinkedList 두가지 기본 형태로 나타낸다.

## List 구현체

### 1. ArrayList

- Resizable한 List 인터페이스의 구현체이다.
- 배열의 최대 크기만큼 원소를 추가할 수 있고 이 배열이 차면 더 큰 배열을 새로 할당한 다음 기존 값을 복사한다.
- ArrayList는 처음에 빈 배열로 시작하고 처음 원소가 추가될 때 용량 10 기반 배열을 할당한다.
  - 초기 용량값을 생성자에 전달하면 하지않아도 된다.

#### 1-1 ArrayList 주요 필드

```
transient Object[] elementData;
    ...
private int size;
```

- elementData
  - 값을 담고있는 Object 배열이다
- size
  - ArrayList의 크기이다.

#### 1-2 ArrayList 생성자

```
public ArrayList() {
  this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA; // 0
}

public ArrayList(int initialCapacity) {
  ...
  this.elementData = new Object[initialCapacity];
}
```
- 파라미터가 없는 생성자로 ArrayList를 선언하게 되면 빈 Object 배열 객체가 elementData에 할당이 된다. 
- 인스턴스를 생성할 때 생성자에 int형 파라미터를 넘기게 되면 값에 따라 초기화된다.

#### 1-3 ArrayList add 메소드

- 초기화된 ArrayList에 add 메소드를 호출하면 내부에서 어떤 동작이 발생하는지 살펴보자.

```
public boolean add(E e) {
  modCount++;
  add(e, elementData, size);
  return true;
}

private void add(E e, Object[] elementData, int s) {
  if (s == elementData.length)
      elementData = grow();
  elementData[s] = e;
  size = s + 1;
}

private Object[] grow() {
  return grow(size + 1);
}

private Object[] grow(int minCapacity) {
  int oldCapacity = elementData.length;
  if (oldCapacity > 0 || elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
    int newCapacity = ArraysSupport.newLength(oldCapacity,
                        minCapacity - oldCapacity, /* minimum growth */
                        oldCapacity >> 1           /* preferred growth */);
    return elementData = Arrays.copyOf(elementData, newCapacity);
  } else {
    return elementData = new Object[Math.max(DEFAULT_CAPACITY,minCapacity)];
  }
}
```
- add가 호출되면 private으로 오버로딩 된 add를 호출한다.
- 넣을 element와 가지고 있는 element 그리고 size를 넘겨준다.
- 현재 size가 배열의 길이와 같다면 grow 메소드를 호출하고 그렇지않다면 해당 인덱스에 값을 저장 후 사이즈를 늘린다.
- grow 메소드가 호출되면 현재 size + 1을 파라미터로 넘겨준다.

<!-- - 파라미터로 받은 값을 minCapacity로 받아 이전 elementData의 길이에 3/2 길이를 리턴해주고 이 값이 minCapacity보다 작을 경우 minCapacity를 리턴받아 newCapacity에 저장한다. -->

- 이후 Arrays.copyOf 메소드에 현재 배열과 새로운 길이를 넘겨 길이가 늘어난 복사된 배열을 리턴한다.

ensureCapacity() 메서드를 이용해 ArrayList 용량을 늘려도 크기 조정 작업을 건너 뛸 수 있다.

<!-- ```
@Benchmark
public List<String> properlySizedArrayList() {
    List<String> list = new ArrayList<>(1_000_000);
    for (int i=0; i < 1_000_000; i++) {
        list.add(item;
    }
    return list;
}

@Benchmark
public List<String> resizingArrayList() {
    List<String> list = new ArrayList<>();
    for (int i=0; i < 1_000_000; i++) {
        list.add(item;
    }
    return list;
}
```

```
Benchmark      mode   Cnt   Score   Error   Units
ResizingList.properly...   thrpt   10   287.388   +-7.135   ops/s
ResizingList.resizeing...   thrpt   10   189.519   +-4.530   ops/s
```

- properlySized... 테스트가 원소 추가 작업을 초당 약 100회 더 처리했다. -->

---
#### 2. LinkedList

- 노드로 이루어진 선형의 자료구조이다.

#### 2-1 LinkedList 주요 필드

```
transient int size = 0;
    ...
transient Node<E> first;
transient Node<E> last;

private static class Node<E> {
  E item;
  Node<E> next;
  Node<E> prev;
  Node(Node<E> prev, E element, Node<E> next) {
    this.item = element;
    this.next = next;
    this.prev = prev;
  }
}
```

- Node
  - Node가 데이터를 담고 있으며, 이전 Node와 다음 Node에 대한 정보도 가지고 있다.
  - Node를 변경함으로써 삽입과 삭제를 하며 재정렬이 필요없기 때문에 ArrayList에 비해 상대적으로 삽입과 삭제가 빠르다

#### 2-3 LinkedList add 메소드

- 초기화된 LinkedList에 add 메소드를 호출하면 내부에서 어떤 동작이 발생하는지 살펴보자.

- **첫번째 노드에 삽입 시**
  ```
  public void addFirst(E e) {
    linkFirst(e);
  }
  
  private void linkFirst(E e) {
    final Node<E> f = first;
    final Node<E> newNode = new Node<>(null, e, f);
    first = newNode;
    if (f == null)
      last = newNode;
    else
      f.prev = newNode;
    size++;
    modCount++;
  }
  ```
  - 새로운 노드를 생성하면서 next에 기존의 첫번째 노드를, 기존 첫번째 노드는 prev는 생성된 노드를 바라보게 한다.

- **마지막 노드에 삽입 시**

  ```
  public boolean add(E e) {
    linkLast(e);
    return true;
  }
  
  void linkLast(E e) {
    final Node<E> l = last;
    final Node<E> newNode = new Node<>(l, e, null);
    last = newNode;
    if (l == null)
      first = newNode;
    else
      l.next = newNode;
    size++;
    modCount++;
  }
  ```
  - 새로운 노드를 생성하면서 prev는 last Node, next는 null로 생성한다.
  - 기존의 last Node의 next는 새로 생성한 node를 바라보게 한다. 그리고 last를 새로만든 Node로 만든다.

#### ArrayList vs LinkedList

- 둘중 어느 것을 쓸지 아니면 비표준 List 구현체를 쓸지는 데이터 접근/수정 패턴에 따라 다르다.
- ArrayList의 특정 인덱스에 원소를 추가하려면 다른 원소들을 모두 한 칸씩 우측으로 이동시켜야한다.
- LinkedList는 삽입 지접을 찾기 위해 노드 레퍼런스를 죽 따라가는 수고는 있지만 삽입 작업은 노드를 하나 생성한 다음 두 레퍼런스를 세팅하면 끝난다.
