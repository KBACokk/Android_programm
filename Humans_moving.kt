import kotlin.random.Random

open class Human(
    var Name: String,
    var age: Int,                                   // Создаём класс Human со свойствами Имя, востраст и текущая скорость.
    var Speed: Int
) {
    var x: Int = 0                                 // Задаём начальные координаты для всех симуляций людей ( 0 ; 0 ) соответственно
    var y: Int = 0

    open fun move() {
        x += Random.nextInt(-Speed, Speed + 1)         // случайно "добавляем" или "удаляем" значение равное +/- текущей скорости к значению координат
        y += Random.nextInt(-Speed, Speed + 1)

        println("${Name.padEnd(20)} (${age} лет)  | ${x.toString().padStart(3)} | ${y.toString().padStart(3)} |")    // просто выводим информацию об участникахсимуляции
    }
}

class Driver(
    Name: String,
    age: Int,
    Speed: Int
) : Human(Name, age, Speed) {

    override fun move() {
        // Движение только по горизонтали или вертикали (без диагоналей)
        when (Random.nextInt(0, 2)) {
            0 -> { // Движение по горизонтали
                x += if (Random.nextBoolean()) Random.nextInt(-Speed, Speed + 1) else -Random.nextInt(-Speed, Speed + 1)
            }
            1 -> { // Движение по вертикали
                y += if (Random.nextBoolean()) Random.nextInt(-Speed, Speed + 1) else -Random.nextInt(-Speed, Speed + 1)
            }
        }

        println("${Name.padEnd(20)} (${age} лет)  | ${x.toString().padStart(3)} | ${y.toString().padStart(3)} | [Водитель]")
    }
}

fun main() {
    var time = 5                                                  // Время продолжительности симуляции

    val people = listOf(                                          // Лист участников
        Human("Даниил Твердохлеб", 25, 7),
        Human("Рома Пипиленко", 30, 3),
        Human("Илья Чащин", 22, 6),
        Human("Максим Сократов", 28, 4),
        Human("Александр Титякин", 35, 8),
        Human("Михаил Кремер", 26, 2),
        Human("Ульяна Курман", 31, 9),
        Driver("Лев Приземин", 24, 7),
        Human("Михаил Пыжов", 29, 5),
        Driver("Валерия Шевелькова", 27, 3),
        Human("Павел Шевельков", 27, 2),
        Human("Алексей Чудов", 27, 9),
        Human("Иван Русанов", 27, 2),
        Human("Илья Нестеров", 27, 1)
    )

    while (time > 0) {
        println("~".repeat(60))                                                       // Визуальное разделение границ вывод по времени
        println("            [          Время: ${6 - time} секунда           ]")      // Оставшееся время
        println("~".repeat(60))

        for (person in people) {                                                      // выводим лист участников с изменёнными параметрами положения
            // println(" - - -")
            person.move()
        }

        println("~".repeat(60))

        Thread.sleep(1000)                                                           // Уводим программу в сон на 1000 мс ~ 1 с
        time--                                                                       // Учитываем прошедшую секунду  ^ , выводим соответствующую информацию и повторям цикл ещё time - 1 раз
    }                                                                                //                               |

    println("Симуляция завершена")
}
