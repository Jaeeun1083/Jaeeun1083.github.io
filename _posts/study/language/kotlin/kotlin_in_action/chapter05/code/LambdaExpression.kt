package action.jaeeun.chapter05.code

fun maxBy() {
    val people = listOf(Person("name1", 10), Person("name2", 20))
    people.maxBy { p -> p.age }
}

fun modifyOutSideVariableInLambda() {
    var outsideVariable = 10

    val lambda: () -> Unit = {
        println("Outside variable before: $outsideVariable")
        outsideVariable++
        println("Outside variable after: $outsideVariable")
    }

    lambda()

    println("Outside variable outside lambda: $outsideVariable")
}

fun createClosure(): () -> Unit {
    var nonFinalVariable = 10
    val closure: () -> Unit = {
        println("Non-Final Variable: $nonFinalVariable")
        nonFinalVariable += 10
    }
    return closure
}

class Person (
    val name: String,
    val age: Int
)
