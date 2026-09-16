// Define o pacote da camada de apresentação (os menus)
package sistema

// Importa a classe Funcionario
import pessoas.Funcionario

// Importa o CRUD de funcionários
import repositorio.CRUDFuncionario

// Importa o Validador, que lê os dados com segurança
import validacao.Validador


// "object" cria um SINGLETON: existe UMA sessão só.
//
// A Sessão guarda QUEM está usando o sistema neste momento.
// Isso é essencial porque o trabalho exige registrar
// "um responsável pela transação" em toda movimentação.
//
// Sem a sessão, o sistema não saberia quem autorizou o quê.
object Sessao {

    // Funcionário logado no momento.
    //
    // "private" para ninguém trocar o operador por fora.
    // "Funcionario?" é NULLABLE: começa null porque
    // ninguém entrou no sistema ainda.
    private var operador: Funcionario? = null


    // Devolve o funcionário logado (pode ser null)
    fun operador(): Funcionario? = operador


    // Devolve o NOME de quem está logado.
    //
    // "?." só acessa .nome se operador não for null.
    // "?:" devolve "SISTEMA" quando ninguém está logado.
    fun nomeOperador(): String = operador?.nome ?: "SISTEMA"


    // Informa se o operador logado pode autorizar dinheiro.
    //
    // "?." chama a função só se houver operador.
    // "?: false" nega a permissão quando ninguém está logado.
    fun operadorPodeAprovar(): Boolean =
        operador?.podeAutorizarCaixa() ?: false


    // Faz o "login" escolhendo qual funcionário está operando.
    //
    // Devolve true quando alguém entrou de verdade.
    fun entrar(): Boolean {

        // Cria o CRUD para buscar os funcionários no banco
        val crud = CRUDFuncionario()

        // Busca todos os funcionários cadastrados
        val funcionarios = crud.listarTodos()

        // Se não existe nenhum funcionário, não dá para entrar
        if (funcionarios.isEmpty()) {

            // Explica o que o usuário precisa fazer primeiro
            println()
            println("Nenhum funcionário cadastrado ainda.")
            println("Cadastre um funcionário do setor FINANCEIRO")
            println("ou ADMINISTRATIVO para poder mexer no caixa.")
            println()

            // Informa que o login não aconteceu
            return false
        }

        // Cabeçalho da tela de login
        println()
        println("=".repeat(60))
        println("QUEM ESTÁ OPERANDO O SISTEMA?")
        println("=".repeat(60))

        // Mostra a lista numerada de funcionários
        funcionarios.forEachIndexed { indice, funcionario ->

            // Marca com um asterisco quem pode aprovar caixa
            val marca = if (funcionario.podeAutorizarCaixa()) "*" else " "

            // Mostra o número, o nome e o setor
            println("$marca $indice - ${funcionario.nome} " +
                    "(${funcionario.setor})")
        }

        // Legenda explicando o asterisco
        println()
        println("* = pode autorizar movimentações financeiras")

        // Lê a escolha garantindo que o número existe na lista
        val escolha = Validador.lerInteiro(
            "Escolha o operador",
            0,
            funcionarios.size - 1
        )

        // Guarda o funcionário escolhido na sessão
        operador = funcionarios[escolha]

        // Confirma quem entrou
        println()
        println("Bem-vindo(a), ${nomeOperador()}!")

        // Avisa quando o operador NÃO pode mexer no caixa
        if (!operadorPodeAprovar()) {

            // Explica a limitação antes que o usuário se frustre
            println("ATENÇÃO: o setor ${operador?.setor} não autoriza")
            println("movimentações financeiras. Você poderá cadastrar")
            println("e consultar, mas não lançar dinheiro no caixa.")
        }

        // Informa que o login aconteceu
        return true
    }


    // Faz o "logout", limpando o operador da sessão
    fun sair() {

        // Volta a variável para null
        operador = null
    }
}
