package action.jaeeun.chapter06.code

fun main() {
    // 공유되는 컬렉션
    val sharedList: MutableList<Int> = mutableListOf(1, 2, 3)

    // 읽기 전용 참조
    val readOnlyList: List<Int> = sharedList

    // 변경 가능한 참조
    val mutableList: MutableList<Int> = sharedList

    // 읽기 전용 리스트를 순회하며 출력
    Thread {
        readOnlyList.forEach {
            println("Read-only thread: $it")
            Thread.sleep(100) // 시뮬레이션을 위한 딜레이
        }
    }.start()

    // 변경 가능한 리스트에 요소 추가
    Thread {
        Thread.sleep(200) // 두 번째 스레드가 조금 늦게 시작하도록 딜레이
        mutableList[2] = 4
        println("Mutable thread added an element.")
    }.start()
}
