// Define o pacote onde a enumeração TipoMovimentacao está localizada
package enumeradores

// Cria uma enumeração chamada TipoMovimentacao.
//
// Ela classifica TODO dinheiro que entra ou sai da empresa.
// É o coração do controle de fluxo de caixa.
enum class TipoMovimentacao(

    // Texto amigável mostrado nos menus e relatórios
    val descricao: String,

    // Sinal da operação no caixa:
    //  +1 -> o dinheiro ENTRA  (receita)
    //  -1 -> o dinheiro SAI    (despesa)
    val sinal: Int
) {

    // Cliente pagou pela caixa d'água -> entra dinheiro
    VENDA_PRODUTO("Venda de produto", +1),

    // Cliente pagou pela instalação -> entra dinheiro
    VENDA_SERVICO("Venda de serviço", +1),

    // Cliente pagou uma parcela em aberto -> entra dinheiro
    RECEBIMENTO_PAGAMENTO("Recebimento de pagamento", +1),

    // Empresa comprou caixas do fornecedor -> sai dinheiro
    COMPRA_PRODUTO("Compra de produto", -1),

    // Empresa pagou o salário do funcionário -> sai dinheiro
    PAGAMENTO_SALARIO("Pagamento de salário", -1),

    // Empresa pagou uma conta (luz, água, boleto) -> sai dinheiro
    PAGAMENTO_DESPESA("Pagamento de despesa", -1);

    // Função auxiliar que responde: essa movimentação é entrada?
    //
    // Retorna true quando o sinal for positivo.
    fun ehEntrada(): Boolean = sinal > 0

    // Sobrescreve o toString para mostrar o texto amigável
    override fun toString(): String = descricao
}
