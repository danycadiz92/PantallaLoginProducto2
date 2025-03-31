import kotlin.random.Random

class GameEngine {

    fun girarRuleta(): Int {
        // Simula la ruleta del bingo (por ejemplo, números del 0 al 36)
        return Random.nextInt(0, 37)
    }

    fun calcularGanancia(numero: Int): Int {
        return when (numero) {
            0 -> 50  // premio mayor
            in 1..10 -> 20
            in 11..20 -> 10
            in 21..30 -> -10  // pierde monedas
            else -> -20       // pierde más
        }
    }
}
