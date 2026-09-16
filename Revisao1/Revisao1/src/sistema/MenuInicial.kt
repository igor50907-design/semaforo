// Define o pacote da camada de apresentação (os menus)
package sistema

// Importa o Caixa para mostrar o saldo no cabeçalho
import financeiro.Caixa

// Importa o Validador, que lê a opção com segurança
import validacao.Validador


// Função do MENU PRINCIPAL do sistema.
//
// É ela que amarra tudo: a partir daqui o usuário
// chega em pessoas, produtos, serviços e financeiro.
fun menuInicial() {

    // Mostra a tela de boas-vindas uma única vez
    println()
    println("*".repeat(60))
    println("*  SISTEMA DE GESTÃO - CAIXAS D'ÁGUA")
    println("*  Kotlin + PostgreSQL")
    println("*".repeat(60))

    // Sincroniza o saldo em memória com o que está no banco.
    //
    // Sem isso, o sistema começaria sempre com saldo zero,
    // mesmo tendo movimentações já gravadas.
    Caixa.sincronizarComBanco()

    // Faz o login do operador.
    //
    // Se não houver nenhum funcionário cadastrado ainda,
    // entrar() devolve false e o sistema continua sem operador,
    // permitindo cadastrar o primeiro funcionário.
    Sessao.entrar()

    // do/while repete o menu até o usuário escolher sair
    do {

        // Cabeçalho com as informações do momento
        println()
        println("=".repeat(60))
        println("MENU PRINCIPAL")
        println("Operador: ${Sessao.nomeOperador()} | " +
                "Saldo em caixa: R$ ${Caixa.saldo()}")
        println("=".repeat(60))

        // Opções do sistema
        println("1 - Gestão de pessoas (funcionários, clientes, fornecedores)")
        println("2 - Caixas d'água (produto e estoque)")
        println("3 - Serviços de instalação")
        println("4 - Financeiro (compras, vendas, pagamentos e extrato)")
        println("5 - Trocar de operador")
        println("0 - Sair do sistema")

        // Lê a opção de forma segura.
        //
        // O Validador só devolve um número entre 0 e 5,
        // então nenhuma letra ou número fora da faixa passa daqui.
        val opcao = Validador.lerInteiro("Opção", 0, 5)

        // Escolhe o que executar
        when (opcao) {

            // 1: abre o submenu de pessoas
            1 -> menuPessoas()

            // 2: abre o submenu de produto e estoque
            2 -> menuProduto()

            // 3: abre o submenu de serviços
            3 -> menuServico()

            // 4: abre o submenu financeiro
            4 -> menuFinanceiro()

            // 5: faz logout e pede um novo login
            5 -> {

                // Limpa o operador atual
                Sessao.sair()

                // Pede o novo login
                Sessao.entrar()
            }

            // 0: encerra o programa
            0 -> {

                // Mensagem de despedida
                println()
                println("Encerrando o sistema. Até logo!")

                // "return" sai da função menuInicial,
                // o que faz o programa terminar.
                return
            }
        }

    // Repete para sempre; a saída acontece pelo return acima
    } while (true)
}
