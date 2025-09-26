import kotlin.random.Random

open class Human(
    var name: String,
    var age: Int,                                         // Создаём класс Human со свойствами Имя, возтраст и текущая скорость.
    override val speed: Int
) : Movable {
    override var x: Int = 0
    override var y: Int = 0                                        // Задаём начальные координаты для всех симуляций людей ( 0 ; 0 ) соответственно

    override fun move() {
        x += Random.nextInt(-speed, speed + 1)
        y += Random.nextInt(-speed, speed + 1)          // случайно "добавляем" или "удаляем" значение равное +/- текущей скорости к значению координат

        println("${name.padEnd(20)} (${age} лет)  | ${x.toString().padStart(3)} | ${y.toString().padStart(3)} |")  // просто выводим информацию об участниках симуляции (именно Human)
    }
}
