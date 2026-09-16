// Define o pacote das classes que representam gente
package pessoas

// Importa BigDecimal, usado nas parcelas em aberto
import java.math.BigDecimal


// Classe FILHA de Pessoa que representa o cliente da empresa.
//
// O cliente é quem COMPRA a caixa d'água e contrata a instalação.
class Cliente(

    // Dados repassados para a classe-mãe
    nome: String,
    documento: String,
    idade: Int,
    email: String,
    telefone: String,

    // Lista das parcelas que o cliente ainda deve.
    //
    // "private" faz com que NINGUÉM fora desta classe
    // consiga mexer na lista diretamente. Isso é ENCAPSULAMENTO:
    // o dado fica protegido e só muda pelas funções que criamos.
    private val parcelasAPagar: MutableList<BigDecimal> = mutableListOf(),

    // ID do banco de dados
    idCliente: Int = 0

// Chama o construtor de Pessoa
) : Pessoa(nome, documento, idade, email, telefone, idCliente) {


    // Sobrescreve receberConta.
    //
    // Do ponto de vista da EMPRESA, quando o cliente paga,
    // o dinheiro ENTRA. Por isso o valor volta positivo.
    override fun receberConta(dinheiro: BigDecimal): BigDecimal {

        // abs() garante que o valor seja sempre positivo,
        // mesmo que alguém passe um número negativo por engano.
        return dinheiro.abs()
    }


    // Sobrescreve o papel do cliente
    override fun papel(): String = "Cliente"


    // Função que devolve uma CÓPIA somente-leitura das parcelas.
    //
    // toList() cria uma lista IMUTÁVEL. Assim quem chamar
    // essa função consegue LER as parcelas, mas não consegue
    // adicionar nem remover nada. Encapsulamento na prática.
    fun listarParcelas(): List<BigDecimal> = parcelasAPagar.toList()


    // Função que adiciona uma nova parcela em aberto
    fun adicionarParcela(valor: BigDecimal) {

        // Só aceita valores positivos.
        // signum() > 0 significa "é maior que zero".
        if (valor.signum() > 0) {

            // Adiciona a parcela na lista protegida
            parcelasAPagar.add(valor)
        }
    }


    // Função que quita (paga) a parcela de uma posição da lista.
    //
    // Devolve BigDecimal? (NULLABLE): pode devolver null
    // quando o índice informado não existir.
    fun quitarParcela(indice: Int): BigDecimal? {

        // indices é a faixa de posições válidas da lista.
        // "!in" significa "NÃO está dentro".
        if (indice !in parcelasAPagar.indices) {

            // Índice inválido: devolve null em vez de quebrar
            return null
        }

        // removeAt() apaga a parcela e devolve o valor removido
        return parcelasAPagar.removeAt(indice)
    }


    // Propriedade CALCULADA (não ocupa memória).
    //
    // O "get()" faz o valor ser recalculado toda vez
    // que alguém lê cliente.dividasAbertas.
    //
    // isNotEmpty() devolve true quando ainda há parcelas.
    val dividasAbertas: Boolean
        get() = parcelasAPagar.isNotEmpty()


    // Propriedade calculada com o total devido pelo cliente.
    val totalDevido: BigDecimal

        // fold() percorre a lista somando tudo.
        //
        // Ele começa em BigDecimal.ZERO (o acumulador) e,
        // a cada parcela, soma o valor no acumulador.
        get() = parcelasAPagar.fold(BigDecimal.ZERO) { soma, parcela ->
            soma.add(parcela)
        }
}
