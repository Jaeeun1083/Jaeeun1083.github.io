---
title: System Call은 무엇이고 무엇을 위해 쓰일까?
author: jaeeun
date: 2022-06-15 00:00:00 +0800
categories: [Study, "Operating System"]
tags: ["Operating System"]
render_with_liquid: false
---

스터디 Stacked-Book에서 실습과 그림으로 배우는 리눅스 구조 책을 참고하여 학습 후 정리한 글입니다.

# System Call은 무엇이고 무엇을 위해 쓰일까?

우선 System Call을 알아보기 전에 사용자 모드와 커널 모드에 대해 먼저 알아보자

### 사용자 모드와 커널 모드

<img src ="https://user-images.githubusercontent.com/78838791/174520490-cf353949-e0f8-4a33-939f-1786b3955aab.png" width ="700px" alt="사용자_커널모드"/>

- **사용자 모드**에서 실행되는 코드는 할 수 있는 일이 제한된다.
  - 예를 들어 프로세스가 사용자 모드에서 실행 중이면 디스크 입출력 요청을 할 수 없도록 설정한다.
- **커널 모드**는 사용자 모드와 대비되는 모드로서 운영체제의 중요한 코드들이 실행된다.
  - 이 모드에서 실행되는 코드는 모든 특수한 명령어를 포함하여 원하는 모든 작업을 수행할 수 있다.
- 이 때 사용자 프로세스가 디스크를 읽기와 같은 특수 명령어를 실행해야 한다면 이런 **제한 작업의 실행을 허용하기 위해 시스템 콜을 사용**한다.

### System Call 이란?

- System Call은 **OS가 커널에 접근하기위한 인터페이스이며 유저프로그램이 운영체제의 서비스를 받기위해 커널 함수를 호출할때 쓰게되는 기능**을 한다
- 유저 어플리케이션은 유저 모드에서 시스템콜 인터페이스를 호출하면 시스템콜은 **커널모드에서 하드웨어에 접근하여 작업 후 어플리케이션에 반환**한다.

- 예시
  ```
  cp a.txt b.txt
  (a.txt 를 b.txt에 복사한다)
  ```
  - 위의 경우 modebit(사용자모드인지 커널모드인지 알리는 flag)가 커널모드로 설정되고 커널모드로 들어가서 파일을 복사하고 다시 유저모드로 돌아가 로직을 마저 실행하게 된다

#### modebit
-  CPU가 현재 어떤 모드(유저 모드 또는 커널 모드)에서 실행되고 있는지 나타내는 비트이다.
- 모드 비트가 1(커널 모드)이면 CPU는 모든 메모리 영역에 접근 가능하고 모드 비트가 0(유저 모드)이면 CPU는 일부 메모리 영역에만 접근할 수 있다. 유저 모드에서는 커널 메모리에 직접 접근할 수 없으며, 자신의 주소 공간만 접근할 수 있다.
- 이 비트는 CPU의 제어 레지스터에 저장되어 있다.

<img src ="https://user-images.githubusercontent.com/78838791/176114508-efd6c3d6-ce15-4f7b-a6be-9a44fd351a82.png" width ="500px" alt="modebit"/>

- CPU와 메모리 사이에 `MMU (Memory Management Unit)`는 **CPU에서 메모리로 가는 address 정보를 조사**한다.
- 조사에 통과하면 **Instruction Fetch** 즉 instruction을 가져온다. instruction의 구조는 op-code와 operands로 구성된다. op-code를 통해 어떤 연산인지 확인한다.
  - Opcode (명령어 코드): 명령어의 일부로, 어떤 연산이 수행되어야 하는지를 나타내는 부분. Opcode를 통해 CPU는 어떤 동작을 수행해야 하는지 인식한다.
- Opcode 확인 후 만약 privileged op-code(I/O와 같은 중요한 역할을 하는 실행)을 시도하려 할 때 현재 실행 중인 코드가 특권 모드에서 실행 중인지 확인한다.
  - 만약 사용자 모드(유저 모드)에서 특권 명령어를 실행하려고 시도하면, CPU는 그것을 감지하고 해당 명령어 실행을 거부한다.
  - CPU가 운영 체제 커널 모드로 전환되는 경우, 특권 명령어를 실행할 수 있다.

<!-- #### System call table

- 메모리의 특정 주소 범위에는 어떤 동작들이 할당되어 있다 (인터럽트 서비스 루틴). 이것을 가리키는 메모리 주소를 모아놓은 것을 시스템 콜 테이블(System call table)이라고 부르며, 인터럽트 벡터(Interrupt vector)라고도 부른다. -->


### System Call 호출 시 동작 과정

1. 시스템 콜을 실행하기 위해 프로그램은 특정 레지스터에 시스템 콜 번호를 설정하고, `trap` 특수 명령어(예: INT 0x80 또는 SYSENTER)를 실행하고 이 명령어는 커널 안으로 분기하는 동시에 특권 수준을 커널 모드로 상향 조정한다.
2. 커널 모드로 진입하면 운영체제는 인터럽트 처리 루틴 또는 시스템 콜 핸들러로 분기하며, 시스템 콜 번호를 확인하여 어떤 작업을 수행할지 결정한다. 이때, 커널은 특권 수준을 가짐으로써 보안 및 시스템 자원 관리를 수행할 수 있다.
3. 운영체제가 시스템 콜을 처리한 후, 결과를 적절한 레지스터나 메모리 위치에 저장하고, `treturn-from-trap` (또는 특정한 리턴 명령어)를 통해 다시 사용자 모드로 전환한다.  이 과정에서 커널 모드의 특권 수준이 사용자 모드로 다시 하향 조정된다.

### System Call의 분류

- System Call은 다음과 같은 작업을 수행하기 위해 사용된다.

1. 파일 입출력: 파일을 열고 읽고 쓰는 작업과 관련된 시스템 콜은 응용 프로그램이 파일 시스템과 상호작용할 수 있도록 한다. (예: read, write)
2. 프로세스 관리: 프로세스 생성, 종료, 스케줄링, 프로세스 간 통신 등과 관련된 시스템 콜은 응용 프로그램이 프로세스를 관리하고 운영 체제 리소스를 활용할 수 있게 한다. (예: fork, exec)
3. 메모리 관리: 메모리 할당 및 해제와 관련된 시스템 콜은 응용 프로그램이 메모리를 효율적으로 사용할 수 있도록 한다. (예: malloc, free)
4. 네트워크 통신: 네트워크 소켓 생성, 데이터 송수신, 네트워크 연결 관리 등과 관련된 시스템 콜은 네트워크 통신을 가능하게 한다.
5. 하드웨어 접근: 하드웨어와 상호작용하는 작업에 사용되는 시스템 콜도 있다.
