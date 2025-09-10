import kotlin.random.Random

class Human(
    var fullName: String,
    var age: Int,
    var currentSpeed: Int
) {
    var x: Int = 0
    var y: Int = 0
    
    fun move() {
        x += Random.nextInt(-currentSpeed, currentSpeed)
        y += Random.nextInt(-currentSpeed, currentSpeed)

        println("${fullName.padEnd(20)} (${age} лет)  | $x | $y |")
    }
}

fun main() {
    var time = 5
 
    val people = listOf(
        Human("Даниил Твердохлеб", 25, 7),
        Human("Рома Пипиленко", 30, 3),
        Human("Илья Чащин", 22, 6),
        Human("Максим Сократов", 28, 4),
        Human("Александр Титякин", 35, 8),
        Human("Михаил Кремер", 26, 2),
        Human("Ульяна Курман", 31, 9),
        Human("Лев Приземин", 24, 7),
        Human("Михаил Пыжов", 29, 5),
        Human("Валерия Шевелькова", 27, 3),
        Human("Павел Шевельков", 27, 2),
        Human("Алексей Чудов", 27, 9),
        Human("Иван Русанов", 27, 2),
        Human("Илья Нестеров", 27, 1)
    )
    
    while (time > 0) {
        println("~".repeat(60))
        println("            [          Время: ${6 - time} секунда           ]")
        println("~".repeat(60))
        
        for (person in people) {
            // println(" - - -")
            person.move()
        }
        
        println("~".repeat(60))
        
        Thread.sleep(1000)
        time--
    }
    
    println("Симуляция завершена!")
}
