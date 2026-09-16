// Define o pacote dos produtos e serviços vendidos
package produto

// Importa BigDecimal, usado no preço do serviço
import java.math.BigDecimal

// Importa LocalDate, usado na data de instalação
import java.time.LocalDate


// Classe que representa o SERVIÇO de instalação da caixa d'água.
//
// Ela liga três coisas: o cliente que contratou,
// o instalador que vai executar e a caixa que será instalada.
class Servico(

    // ID do cliente que contratou o serviço
    val clienteId: Int,

    // ID do funcionário (instalador) responsável pela execução
    val instaladorId: Int,

    // ID da caixa d'água que será instalada
    val caixaId: Int,

    // Data marcada para a instalação
    val dataInstalacao: LocalDate,

    // Quanto a empresa vai cobrar pela mão de obra
    val preco: BigDecimal,

    // Situação atual do serviço.
    //
    // "private set" protege o status: ele só muda
    // pelas funções concluir() e cancelar().
    statusInicial: String = "AGENDADO",

    // ID do registro no banco de dados
    var id: Int = 0
) {

    // Situação do serviço, com escrita privada
    var status: String = statusInicial
        private set


    // Função que marca o serviço como concluído.
    //
    // Devolve false quando o serviço já foi finalizado antes,
    // impedindo que o mesmo serviço seja cobrado duas vezes.
    fun concluir(): Boolean {

        // Só é possível concluir um serviço que está AGENDADO
        if (status != "AGENDADO") return false

        // Muda a situação para concluído
        status = "CONCLUIDO"

        // Informa que deu certo
        return true
    }


    // Função que cancela o serviço
    fun cancelar(): Boolean {

        // Um serviço já concluído não pode ser cancelado
        if (status == "CONCLUIDO") return false

        // Muda a situação para cancelado
        status = "CANCELADO"

        // Informa que deu certo
        return true
    }


    // Sobrescreve o toString para imprimir o serviço no console
    override fun toString(): String =
        "[$id] Cliente #$clienteId | Instalador #$instaladorId | " +
        "Caixa #$caixaId | ${dataInstalacao} | R$ $preco | $status"
}
