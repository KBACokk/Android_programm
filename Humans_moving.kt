import kotlin.random.Random
import kotlin.concurrent.thread

val carBrands = listOf(
    "Toyota Camry", "Kia Rio", "Lada Granta", "Volkswagen Polo",
    "Hyundai Solaris", "Skoda Octavia", "Renault Logan", "Ford Focus",
)
// Списки марок машин и категорий прав
val licenseCategories = listOf("A", "B", "C", "D", "BE", "CE", "DE")

open class Human(
    var name: String,
    var age: Int,                                         // Создаём класс Human со свойствами Имя, возтраст и текущая скорость.
    var speed: Int
) {
    var x: Int = 0
    var y: Int = 0                                        // Задаём начальные координаты для всех симуляций людей ( 0 ; 0 ) соответственно

    open fun move() {
        x += Random.nextInt(-speed, speed + 1)
        y += Random.nextInt(-speed, speed + 1)          // случайно "добавляем" или "удаляем" значение равное +/- текущей скорости к значению координат

        println("${name.padEnd(20)} (${age} лет)  | ${x.toString().padStart(3)} | ${y.toString().padStart(3)} |")  // просто выводим информацию об участниках симуляции (именно Human)
    }
}

class Driver(
    name: String,
    age: Int,
    speed: Int,
    val carBrand: String = carBrands.random(),                   // Случайная марка машины
    val licenseCategory: String = licenseCategories.random()     // Случайная категория прав
) : Human(name, age, speed) {                                    // Так же берём данные из класса Human

    override fun move() {
        val direction = Random.nextInt(0, 4)

        when (direction) {
            0 -> y += speed
            1 -> x += speed                                       // Система движения в одном направлении
            2 -> y -= speed
            3 -> x -= speed
        }

        println("${name.padEnd(20)} (${age} лет)  | ${x.toString().padStart(3)} | ${y.toString().padStart(3)} | [Водитель: $carBrand, права: $licenseCategory]")  // и ещё один просто выводим информацию об участниках симуляции (Но уже именно класса Driver)
    }
}

fun main() {
    val simulationTime = 5                                                 // Время продолжительности симуляции

    val people = listOf(
        Human("Даниил Твердохлеб", 25, 7),
        Human("Рома Пипиленко", 30, 3),
        Human("Илья Чащин", 22, 6),
        Human("Максим Сократов", 28, 4),
        Human("Александр Титякин", 35, 8),
        Human("Михаил Кремер", 26, 2),                 // Лист участников, как Human, так и Driver
        Human("Ульяна Курман", 31, 9),
        Driver("Лев Приземин", 24, 7),
        Human("Михаил Пыжов", 29, 5),
        Driver("Валерия Шевелькова", 27, 3),
        Human("Павел Шевельков", 27, 2),
        Human("Алексей Чудов", 27, 9),
        Human("Иван Русанов", 27, 2),
        Human("Илья Нестеров", 27, 1)
    )

    println("=".repeat(70))                                     // Визуальное разделение границ вывод по времени
    println("Начало симуляции с параллельным движением")            // Оставшееся время
    println("=".repeat(70))

    for (time in 1..simulationTime) {
        println("~".repeat(70))
        println("            [          Время: $time секунда           ]")
        println("~".repeat(70))

        val threads = mutableListOf<Thread>()                        //Создаём "изменяемый список потоков", позволяющий нам добавлять N колличество потоков

        for (person in people) {
            val thread = thread {                                    //Для каждого человека из списка создаётся отдельный поток
                person.move()                                        // Выполняем действие move, прописанное в каждом из используемых классов
            }
            threads.add(thread)                                      // Добавляем поток в уже созданный спиок для его дальнейшего "контроля", в противном случае поток не сможет прекратить свою работу и будет выполнять фиктивную работу
        }

        threads.forEach { it.join() }                                 // Дожидаемся окончания всех потоков перед завершением/продолжением работы программы

        println("~".repeat(70))

        if (time < simulationTime) {
            Thread.sleep(1000)                                  // Ждём 1000 мс ~ 1с для визуального разделения движения
        }
    }

    println("Симуляция завершена")

}