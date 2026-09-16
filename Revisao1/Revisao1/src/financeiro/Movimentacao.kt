// Define o pacote financeiro
package financeiro

// Importa o enum que classifica a movimentação
import enumeradores.TipoMovimentacao

// Importa BigDecimal, usado no valor da movimentação
import java.math.BigDecimal

// Importa LocalDateTime, que guarda DATA e HORA juntas.
//
// Usamos LocalDateTime (e não LocalDate) porque o trabalho
// exige registrar "a data e a hora" da movimentação.
import java.time.LocalDateTime

// Importa o formatador para mostrar a data/hora bonitinha
import java.time.format.DateTimeFormatter


// Classe que representa UMA movimentação financeira.
//
// Ela é o registro imutável de tudo que aconteceu com o dinheiro.
// O trabalho pede que cada movimentação guarde:
//   * Quanto dinheiro foi usado   -> valor
//   * Quem foi o pagador          -> pagador
//   * Quem recebeu                -> recebedor
//   * A data e a hora             -> dataHora
//   * O motivo/descrição          -> motivo
//   * Um responsável              -> responsavel
//
// TODOS os campos são "val" (imutáveis) de propósito:
// um lançamento financeiro NUNCA pode ser alterado depois
// de criado. Se estiver errado, faz-se um estorno.
class Movimentacao(

    // 1) QUANTO dinheiro foi usado.
    //
    // Guardado sempre com o sinal já aplicado:
    // positivo = entrou dinheiro, negativo = saiu dinheiro.
    val valor: BigDecimal,

    // 2) QUEM PAGOU (nome de quem tirou dinheiro do bolso)
    val pagador: String,

    // 3) QUEM RECEBEU (nome de quem ficou com o dinheiro)
    val recebedor: String,

    // 4) A DATA E A HORA exata do lançamento.
    //
    // O valor padrão é LocalDateTime.now(), ou seja,
    // o momento em que o objeto é criado. Assim o
    // usuário não consegue mentir sobre quando aconteceu.
    val dataHora: LocalDateTime = LocalDateTime.now(),

    // 5) O MOTIVO/DESCRIÇÃO da movimentação
    val motivo: String = "",

    // 6) O RESPONSÁVEL pela transação
    //    (o funcionário que autorizou o lançamento)
    val responsavel: String = "",

    // Classificação da movimentação (venda, compra, salário...)
    val tipo: TipoMovimentacao = TipoMovimentacao.PAGAMENTO_DESPESA,

    // ID do registro no banco de dados
    var id: Int = 0
) {

    // "companion object" é o equivalente ao "static" do Java.
    //
    // O que está aqui dentro pertence à CLASSE, não ao objeto.
    // Serve para guardar constantes compartilhadas.
    companion object {

        // Formato usado para mostrar data e hora no padrão brasileiro
        val FORMATO: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    }


    // Propriedade calculada: essa movimentação é uma entrada?
    //
    // signum() > 0 significa que o valor é positivo.
    val ehEntrada: Boolean
        get() = valor.signum() > 0


    // Sobrescreve o toString para imprimir o extrato
    // de forma legível no console.
    override fun toString(): String {

        // Escolhe a seta conforme o dinheiro entra ou sai
        val seta = if (ehEntrada) "ENTRADA" else "SAIDA  "

        // Monta o texto final juntando todos os campos exigidos
        return "[$id] ${dataHora.format(FORMATO)} | $seta | " +
               "R$ $valor | $tipo | " +
               "Pagador: $pagador -> Recebedor: $recebedor | " +
               "Motivo: $motivo | Responsável: $responsavel"
    }
}
