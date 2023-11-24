---
title: 클래스 내부에 선언된 Logger 객체를 테스트 클래스에서 검증하기
author: jaeeun
date: 2023-11-24 00:00:00 +0800
categories: ["TIL", "Test"]
tags: ["Logger Test"]
render_with_liquid: false
---

## 클래스 내부에 선언된 Logger 객체를 테스트 클래스에서 검증하려면 어떻게 해야할까?

### 기존 클래스

기존 클래스는 Logger 클래스를 기본 생성자에서 주입받아 사용하는 구조였다.
logger 클래스를 통해 logging 하면 다른 애플리케이션에서 아카이빙해가므로 정상적으로 로깅이 되는지에 대한 검증이 필요했다.

```kotlin
@Component
class PacketRepoImpl(
    private val logger: Logger = LoggerFactory.getLogger("packet"),
) : PacketRepoy {
    // 생략
    override fun record(packet: String) {
      logger.info(
        // 생략
      )
    }
}
```

이에 대한 로깅이 정상적으로 되는지에 대한 테스트 클래스는 Logger를 mock 객체로 만들어 클래스 생성 시 주입하여 검증하면 되었었다.

```kotlin
class PacketRepoImplTest {
    @Test
    fun recordData() {
      val logger = mock(Logger::class.java)
      val repo = PacketRepoImpl(logger)

      repo.record(packet)

      verify(logger).info(
        // 생략
      )
    }
}
```

그러나 다른 기능을 추가하며 수정한 코드는 logger 객체를 생성자 주입 시 함께 받을 수 없었고.. 클래스 내부에 logger 객체를 따로 갖게 되었다.

---

### 내부 클래스에 Logger 객체를 갖도록 수정된 클래스

```kotlin
@Component
class PacketRepoImpl (
    private val anotherConfig: AnotherConfig
) : OnionPacketRepository {
  private val logger: Logger = LoggerFactory.getLogger("packet")
  override fun record(packet: String) {
    val ids = anotherConfig.ids
    if (id in ids) {
      logger.info(
        // 생략
      )
    }
  }
}
```

당연히 기존 테스트 클래스는 통과하지 못했다.

원인은 테스트 클래스에서 생성한 Mock 객체 (Logger)의 info 함수는 호출되지 않았기 때문이다.

```
Wanted but not invoked:
logger.info(
    <any string>,
    // 생략
);
-> at io.PacketRepoImplTest.record(PacketRepoImplTest.kt:40)
Actually, there were zero interactions with this mock.
```

### 테스트를 수정해보자.

삽질의 연속 끝에

ListAppender 클래스를 사용하여 Log 이벤트를 List에 저장하고 검증하도록 바꾸어 이 문제를 해결하였다.

```kotlin
class PacketRepoImplTest {

  private fun getListAppender(): ListAppender<ILoggingEvent> {
    val context: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val logger = context.getLogger("packet")

    return logger.getAppender("LIST") as (ListAppender<ILoggingEvent>)
  }

  @Test
  fun recordData() {
    val listAppender = getListAppender()
    listAppender.start()

    repo.record(packet)

    listAppender.stop()

    val logs = listAppender.list
    val log = listAppender.list.last()
    assertTrue(
      log.argumentArray.any {
        val marker = (it as ObjectAppendingMarker)
        marker.fieldName == "target" && marker.fieldValue == "check"
      },
    )
  }
}
```

위치 : test/resource/logback-test.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <!--  생략  -->
  <logger name="onion-packet" level="DEBUG">
    <appender-ref ref="LIST"/>
  </logger>
</configuration>
```
