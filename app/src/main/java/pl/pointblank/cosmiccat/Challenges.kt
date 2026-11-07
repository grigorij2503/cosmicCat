package pl.pointblank.cosmiccat

internal sealed interface Challenge {
    val title: String
    val instruction: String
    val hints: List<String>
    fun accepts(answer: List<Int>): Boolean
}

internal data class Batteries(val target: Int, val charges: List<Int>, override val hints: List<String>,
    override val title: String = "Zasilanie podnośnika") : Challenge {
    override val instruction = "Wybierz dwie baterie, które razem dadzą $target iskier."

    override fun accepts(answer: List<Int>) =
        answer.size == 2 && answer.distinct().size == 2 &&
            answer.all { it in charges.indices } && answer.sumOf { charges[it] } == target
}

internal enum class ShapeToken(val glyph: String, val label: String, val color: PaintColor) {
    TRIANGLE("▲", "Trójkąt", PaintColor.PINK),
    CIRCLE("●", "Koło", PaintColor.BLUE),
    DIAMOND("◆", "Romb", PaintColor.MINT),
}

internal data class CrystalPattern(val shown: List<ShapeToken>, val missing: ShapeToken, override val hints: List<String>,
    override val title: String = "Kod lodowej śluzy") : Challenge {
    override val instruction = "Jaki kryształ pasuje na końcu wzoru?"
    override fun accepts(answer: List<Int>) = answer.singleOrNull() == missing.ordinal
}

internal enum class Pigment(val label: String, val rgb: Int) {
    RED("Czerwona", 0xFF6369), YELLOW("Żółta", 0xFFE266), BLUE("Niebieska", 0x619FFA),
    ORANGE("Pomarańczowa", 0xFF9F51), GREEN("Zielona", 0x66CF80), PURPLE("Fioletowa", 0xB58AF8),
}

internal data class PaintMix(val first: Pigment, val second: Pigment, val result: Pigment) : Challenge {
    override val title = "Mieszanie farb"
    override val instruction = "Jaką farbę otrzymasz po zmieszaniu tych dwóch kolorów?"
    override val hints = listOf(
        "Popatrz na obie farby i nazwij ich kolory.",
        "Szukaj nowego koloru wśród trzech próbek.",
        "Wynik to farba: ${result.label.lowercase()}.",
    )
    val choices = listOf(Pigment.ORANGE, Pigment.GREEN, Pigment.PURPLE)

    override fun accepts(answer: List<Int>) = answer.singleOrNull()?.let(choices::getOrNull) == result
}

internal data class Cell(val x: Int, val y: Int) {
    operator fun plus(other: Cell) = Cell(x + other.x, y + other.y)
    fun inside(width: Int, height: Int) = x in 0 until width && y in 0 until height
}

internal data class Mirrors(val receiverAtTop: Boolean, override val hints: List<String>) : Challenge {
    override val title = "Światło dla teleskopu"
    override val instruction = "Obróć lusterka. Promień ma trafić do żółtego czujnika."
    val emitter = Cell(0, 3)
    val receiver = Cell(2, if (receiverAtTop) 0 else 3)
    val mirrors = listOf(Cell(0, 1), Cell(2, 1))
    val initial = arrayListOf(1, if (receiverAtTop) 1 else 0)

    fun trace(answer: List<Int>): List<Cell> {
        if (answer.size != mirrors.size || answer.any { it !in 0..1 }) return listOf(emitter)
        val path = mutableListOf(emitter)
        var step = Cell(0, -1)
        repeat(16) {
            val next = path.last() + step
            if (!next.inside(4, 4)) return path
            path += next
            if (next == receiver) return path
            val index = mirrors.indexOf(next)
            if (index >= 0) {
                step = if (answer[index] == 0) Cell(-step.y, -step.x) else Cell(step.y, step.x)
            }
        }
        return path
    }

    override fun accepts(answer: List<Int>) = trace(answer).last() == receiver
}

internal enum class Move(val glyph: String, val label: String, val delta: Cell) {
    UP("↑", "W górę", Cell(0, -1)), RIGHT("→", "W prawo", Cell(1, 0)),
    DOWN("↓", "W dół", Cell(0, 1)), LEFT("←", "W lewo", Cell(-1, 0)),
}

internal data class DroneRoute(val reverse: Boolean, override val hints: List<String>) : Challenge {
    override val title = "Trasa dla drona"
    override val instruction = "Ułóż strzałki. Dron ma dolecieć do skrzynki, omijając przeszkody."
    val width = 4
    val height = 3
    val limit = 7
    private fun mirror(x: Int, y: Int) = Cell(if (reverse) width - 1 - x else x, y)
    val start = mirror(0, 2)
    val target = mirror(3, 0)
    val barriers = setOf(mirror(0, 1), mirror(2, 2), mirror(1, 0))

    fun trace(answer: List<Int>): List<Cell> {
        val path = mutableListOf(start)
        for (index in answer.take(limit)) {
            val move = Move.entries.getOrNull(index) ?: break
            val next = path.last() + move.delta
            if (!next.inside(width, height) || next in barriers) break
            path += next
        }
        return path
    }

    override fun accepts(answer: List<Int>): Boolean {
        if (answer.isEmpty() || answer.size > limit) return false
        val path = trace(answer)
        return path.size == answer.size + 1 && path.last() == target
    }
}

internal data class WindReadings(val readings: List<Int>, val strongest: Boolean, override val hints: List<String>) : Challenge {
    override val title = "Czujniki wiatru"
    override val instruction = if (strongest) "Który czujnik zarejestrował najsilniejszy wiatr?"
        else "Gdzie wiatr jest najsłabszy? Wybierz czujnik."
    override fun accepts(answer: List<Int>): Boolean {
        val value = answer.singleOrNull()?.let(readings::getOrNull)
        return value == if (strongest) readings.max() else readings.min()
    }
}

internal object IceSorting : Challenge {
    override val title = "Sortownia lodu"
    override val instruction = "Ułóż odłamki od najmniejszego do największego."
    override val hints = listOf("Zacznij od najmniejszego odłamka.", "Średni odłamek jest po lewej.",
        "Kolejność to Mały → Średni → Duży.")
    val labels = listOf("Średni", "Duży", "Mały")
    override fun accepts(answer: List<Int>) = answer == listOf(2, 0, 1)
}

private val stations = mapOf(
    Destination.MARS to Batteries(9, listOf(2, 4, 5), listOf(
        "Zacznij od baterii z 5 iskrami. Ile brakuje do 9?", "Po piątce policz 6, 7, 8 i 9.", "Połącz 4 i 5.")),
    Destination.PLUTO to CrystalPattern(
        listOf(ShapeToken.TRIANGLE, ShapeToken.CIRCLE, ShapeToken.CIRCLE, ShapeToken.TRIANGLE, ShapeToken.CIRCLE),
        ShapeToken.CIRCLE, listOf("Popatrz na pierwsze trzy kryształy.", "Wzór to trójkąt, koło, koło.",
            "Na końcu wstaw koło.")),
    Destination.VENUS to Mirrors(false, listOf(
        "Pierwsze lusterko musi odbić światło w prawo.", "Ustaw pierwsze lusterko jako /.", "Ustaw kolejno / i \\.")),
    Destination.JUPITER to DroneRoute(false, listOf(
        "Najpierw skieruj drona w prawo.", "Potem poleć w górę i jeszcze raz w prawo.", "Spróbuj → ↑ → ↑ →.")),
    Destination.NEPTUNE to WindReadings(listOf(5, 2, 8), false, listOf(
        "Krótszy pasek oznacza słabszy wiatr.", "Porównaj 5, 2 i 8.", "Wybierz czujnik B z wynikiem 2.")),
    Destination.SATURN to IceSorting,
)

private val homeChallenges = mapOf(
    Alien.MIKI to Batteries(7, listOf(2, 3, 4), listOf(
        "Dobierz dwie różne baterie.", "Jedna z nich ma 4 iskry.", "Połącz 3 i 4.")),
    Alien.PUSZEK to CrystalPattern(
        listOf(ShapeToken.CIRCLE, ShapeToken.DIAMOND, ShapeToken.CIRCLE, ShapeToken.DIAMOND, ShapeToken.CIRCLE),
        ShapeToken.DIAMOND, listOf("Dwa kształty występują na zmianę.", "Po kole przychodzi romb.", "Wybierz romb.")),
    Alien.ISKRA to Mirrors(true, listOf(
        "Tym razem czujnik jest u góry.", "Pierwsze lusterko ma odbijać promień w prawo.", "Ustaw oba lusterka jako /.")),
    Alien.KROPKA to DroneRoute(true, listOf(
        "Dron startuje po prawej. Najpierw poleć w lewo.", "Potem skieruj się w górę i jeszcze raz w lewo.",
        "Spróbuj ← ↑ ← ↑ ←.")),
    Alien.BABEL to WindReadings(listOf(4, 9, 6), true, listOf(
        "Dłuższy pasek oznacza silniejszy wiatr.", "Porównaj 4, 9 i 6.", "Wybierz czujnik B z wynikiem 9.")),
)

internal fun stationChallenge(destination: Destination) = stations.getValue(destination)
internal fun homeChallenge(alien: Alien) = homeChallenges.getValue(alien)
