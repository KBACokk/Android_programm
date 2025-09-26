import kotlin.random.Random

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