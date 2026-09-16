// Define o pacote onde a enumeração Setor está localizada
package enumeradores

// Cria uma enumeração chamada Setor.
//
// O requisito do trabalho pede que a empresa tenha
// NO MÍNIMO 2 setores. Aqui temos 4.
//
// Cada constante do enum recebe dois valores no construtor:
//   descricao   -> texto amigável mostrado no menu
//   podeAprovar -> indica se esse setor tem permissão
//                  para autorizar movimentações financeiras
enum class Setor(

    // Texto amigável do setor (aparece na tela)
    val descricao: String,

    // Regra de negócio: apenas alguns setores podem
    // aprovar/registrar dinheiro entrando ou saindo
    val podeAprovar: Boolean
) {

    // Setor responsável pelo dinheiro da empresa.
    // Pode aprovar movimentações -> true
    FINANCEIRO("Financeiro", true),

    // Setor responsável pela burocracia e contratos.
    // Também pode aprovar movimentações -> true
    ADMINISTRATIVO("Administrativo", true),

    // Setor responsável por transporte e entrega.
    // NÃO pode aprovar movimentações -> false
    LOGISTICA("Logística", false),

    // Setor responsável por instalar as caixas d'água.
    // NÃO pode aprovar movimentações -> false
    INSTALACAO("Instalação", false);

    // Sobrescreve o toString() do enum.
    //
    // "override" = estamos substituindo o comportamento padrão.
    // Sem isso, ao imprimir o setor apareceria "LOGISTICA".
    // Com isso, aparece "Logística".
    override fun toString(): String = descricao
}
