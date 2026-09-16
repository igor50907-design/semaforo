// Define o pacote dos produtos e serviços vendidos
package produto

// Importa o enum com as cores disponíveis
import enumeradores.Cor

// Importa o enum com os materiais disponíveis
import enumeradores.Material

// Importa BigDecimal, usado no preço
import java.math.BigDecimal


// Classe que representa a Caixa d'Água, o produto principal da empresa.
//
// A classe guarda as informações:
//   Marca, Modelo, Dimensão, Cor, Material, Formato,
//   Preço de custo, Preço de venda, Estoque e Fornecedor.
class CaixaDaAgua(

    // Marca da caixa d'água (ex: Fortlev)
    val marca: String,

    // Modelo da caixa d'água (ex: 1000L)
    val modelo: String,

    // Lista com as três dimensões da caixa,
    // na ordem: largura, altura e profundidade.
    //
    // No PostgreSQL isso é gravado como um array float8[].
    val dimensao: MutableList<Double>,

    // Cor da caixa, usando o enum Cor.
    //
    // Usar enum em vez de String impede o usuário
    // de cadastrar uma cor que não existe.
    val cor: Cor,

    // Material de fabricação, usando o enum Material
    val material: Material,

    // Formato da caixa (redonda, quadrada, cilíndrica...)
    val formato: String,

    // Quanto a empresa PAGOU no fornecedor por essa caixa.
    //
    // É "private set": qualquer um pode LER o preço de custo,
    // mas só a própria classe pode ALTERAR. Encapsulamento.
    var precoCusto: BigDecimal,

    // Por quanto a empresa VENDE essa caixa para o cliente
    var precoVenda: BigDecimal,

    // Quantidade de unidades disponíveis no estoque.
    //
    // "private set" protege o estoque: ninguém consegue
    // escrever caixa.quantidadeEstoque = 500 de fora da classe.
    // A única forma de mexer é pelas funções entrarEstoque
    // e sairEstoque, que validam a operação.
    quantidadeInicial: Int = 0,

    // ID do fornecedor que vendeu essa caixa para a empresa.
    //
    // "Int?" é NULLABLE: pode ser null quando ainda
    // não sabemos de qual fornecedor a caixa veio.
    val fornecedorId: Int? = null,

    // ID do registro no banco de dados
    var id: Int = 0
) {

    // Propriedade de estoque com escrita PRIVADA.
    //
    // O valor inicial vem do parâmetro quantidadeInicial.
    var quantidadeEstoque: Int = quantidadeInicial
        private set


    // Função que dá ENTRADA de unidades no estoque (compra).
    //
    // Devolve Boolean para dizer se a operação deu certo.
    fun entrarEstoque(quantidade: Int): Boolean {

        // Não faz sentido dar entrada de zero ou de valor negativo
        if (quantidade <= 0) return false

        // Soma a quantidade no estoque atual
        quantidadeEstoque += quantidade

        // Informa que deu certo
        return true
    }


    // Função que dá SAÍDA de unidades no estoque (venda).
    //
    // Essa função é a que impede vender o que não existe.
    fun sairEstoque(quantidade: Int): Boolean {

        // Quantidade inválida: recusa
        if (quantidade <= 0) return false

        // Estoque insuficiente: recusa a venda.
        //
        // Sem essa linha o estoque ficaria negativo,
        // que é justamente a falha humana que queremos evitar.
        if (quantidade > quantidadeEstoque) return false

        // Desconta a quantidade do estoque
        quantidadeEstoque -= quantidade

        // Informa que deu certo
        return true
    }


    // Propriedade CALCULADA que informa o lucro por unidade.
    //
    // subtract() faz a subtração entre dois BigDecimal.
    val lucroUnitario: BigDecimal
        get() = precoVenda.subtract(precoCusto)


    // Sobrescreve o toString para imprimir a caixa
    // de forma organizada no console.
    //
    // joinToString(" x ") transforma a lista [1.0, 2.0, 0.5]
    // no texto "1.0 x 2.0 x 0.5".
    override fun toString(): String =
        "[$id] $marca $modelo | ${dimensao.joinToString(" x ")} m | " +
        "$cor | $material | $formato | " +
        "Custo: R$ $precoCusto | Venda: R$ $precoVenda | " +
        "Estoque: $quantidadeEstoque un."
}
