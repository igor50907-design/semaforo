import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

var semaforoAberto = true
var filaCarros = ConcurrentLinkedDeque(listOf("golf", "camaro", "corvette", "jetta"))
var carrosChegada = listOf(
    "civic",
    "gol",
    "onix",
    "porsche 911",
    "hb20",
    "lancer",
    "azera",
    "corolla",
    "creta",
    "hilux",
    "mc laren"
)

fun main() {
    println("=====SEMÁFORO=====")
    controleSemaforo()
    chegadaCarros()
    saidaCarros()
}

fun controleSemaforo() {
    thread {
        while (true) {
            println("o semáforo está aberto")
            Thread.sleep(5000)

            semaforoAberto = false
            println("o semáforo está fechado")
            Thread.sleep(10000)
            semaforoAberto = true
        }
    }
}

fun chegadaCarros() {
    thread {
        while (true) {
            val carroAleatorio = carrosChegada.random()

            filaCarros.addLast(carroAleatorio)
            Thread.sleep(3000)
            println("🚗 chegou ${carroAleatorio} | fila  (${filaCarros.size}) | ${filaCarros}")
        }
    }
}

fun saidaCarros() {
    thread {
        while (true) {
            if (semaforoAberto && filaCarros.isNotEmpty()) {
                val carro = filaCarros.removeFirst()
                println("🚗 passou o carro ${carro} | fila (${filaCarros.size}): $filaCarros")
            }
            Thread.sleep(2000)
        }
    }
}