// Define o pacote financeiro
package financeiro

// Importa o enum que classifica a movimentação
import enumeradores.TipoMovimentacao

// Importa a classe que faz o CRUD de movimentações no banco
import repositorio.CRUDMovimentacao

// Importa BigDecimal, usado no saldo
import java.math.BigDecimal


// "object" cria um SINGLETON: existe UM ÚNICO Caixa
// em todo o sistema, igual na vida real (a empresa
// tem um caixa só).
//
// Este objeto é o PORTÃO DE ENTRADA de todo o dinheiro.
// Nenhuma outra parte do código pode somar ou subtrair
// dinheiro por conta própria: é obrigatório passar por aqui.
//
// Isso é ENCAPSULAMENTO aplicado ao fluxo de caixa,
// exatamente como o trabalho pede.
object Caixa {

    // Saldo atual do caixa.
    //
    // "private" = ninguém fora deste objeto enxerga a variável.
    // "var" = só ESTE objeto pode alterar o valor.
    //
    // Se o saldo fosse público, qualquer linha do sistema
    // poderia escrever Caixa.saldo = 1000000 e fraudar a empresa.
    private var saldoInterno: BigDecimal = BigDecimal.ZERO

    // Objeto responsável por gravar as movimentações no banco.
    //
    // Também é privado: o Caixa é o único que grava movimentação.
    private val repositorio = CRUDMovimentacao()


    // Função pública que apenas MOSTRA o saldo.
    //
    // Como ela devolve uma cópia do BigDecimal (BigDecimal
    // é imutável em Java/Kotlin), quem lê não consegue
    // alterar o saldo por meio dela.
    fun saldo(): BigDecimal = saldoInterno


    // Função que carrega o saldo real a partir do banco de dados.
    //
    // Chamada quando o programa inicia, para que o saldo
    // em memória fique igual ao saldo já gravado.
    fun sincronizarComBanco() {

        // Pergunta ao banco a soma de todas as movimentações
        val somaDoBanco = repositorio.somarSaldo()

        // Atualiza o saldo interno com o resultado
        saldoInterno = somaDoBanco
    }


    // =========================================================
    // FUNÇÃO CENTRAL DO FLUXO DE CAIXA
    // =========================================================

    // Registra uma movimentação financeira de forma SEGURA.
    //
    // Ela recebe todos os dados exigidos pelo trabalho e faz,
    // nesta ordem:
    //   1. valida os dados
    //   2. aplica o sinal (entrada ou saída)
    //   3. verifica se há saldo suficiente
    //   4. grava a movimentação no banco
    //   5. atualiza o saldo em memória
    //
    // Devolve a Movimentacao criada, ou null (NULLABLE)
    // quando a operação for recusada.
    fun registrar(

        // Classificação da operação (venda, compra, salário...)
        tipo: TipoMovimentacao,

        // Valor SEM sinal, sempre positivo.
        // Quem decide o sinal é o próprio Caixa, não quem chama.
        valor: BigDecimal,

        // Quem pagou
        pagador: String,

        // Quem recebeu
        recebedor: String,

        // Motivo/descrição da operação
        motivo: String,

        // Funcionário responsável por autorizar
        responsavel: String,

        // Indica se o responsável pertence a um setor
        // com permissão para aprovar movimentações
        responsavelPodeAprovar: Boolean

    ): Movimentacao? {

        // -----------------------------------------------------
        // VALIDAÇÃO 1: valor precisa ser positivo
        // -----------------------------------------------------

        // signum() <= 0 quer dizer zero ou negativo
        if (valor.signum() <= 0) {

            // Avisa e cancela a operação devolvendo null
            println("[CAIXA] Recusado: o valor precisa ser maior que zero.")
            return null
        }


        // -----------------------------------------------------
        // VALIDAÇÃO 2: pagador e recebedor não podem ser vazios
        // -----------------------------------------------------

        // isBlank() é true quando o texto está vazio
        // ou só tem espaços em branco.
        if (pagador.isBlank() || recebedor.isBlank()) {

            // Avisa e cancela
            println("[CAIXA] Recusado: informe o pagador e o recebedor.")
            return null
        }


        // -----------------------------------------------------
        // VALIDAÇÃO 3: o responsável precisa ter permissão
        // -----------------------------------------------------

        // Apenas os setores Financeiro e Administrativo
        // podem autorizar dinheiro entrando ou saindo.
        if (!responsavelPodeAprovar) {

            // Avisa e cancela
            println("[CAIXA] Recusado: '$responsavel' não tem permissão " +
                    "para autorizar movimentações financeiras.")
            return null
        }


        // -----------------------------------------------------
        // APLICA O SINAL DA OPERAÇÃO
        // -----------------------------------------------------

        // Se o tipo for entrada, o valor fica positivo.
        // Se for saída, o valor é multiplicado por -1.
        //
        // Repare que quem chamou a função NÃO controla o sinal:
        // essa regra mora dentro do enum e do Caixa.
        val valorComSinal =
            if (tipo.ehEntrada()) valor else valor.negate()


        // -----------------------------------------------------
        // VALIDAÇÃO 4: não deixar o caixa ficar negativo
        // -----------------------------------------------------

        // Calcula como ficaria o saldo depois da operação
        val saldoFuturo = saldoInterno.add(valorComSinal)

        // Se o saldo futuro for negativo, a saída é recusada
        if (saldoFuturo.signum() < 0) {

            // Explica exatamente o motivo da recusa
            println("[CAIXA] Recusado: saldo insuficiente. " +
                    "Saldo atual R$ $saldoInterno, " +
                    "operação de R$ $valorComSinal.")

            // Cancela devolvendo null
            return null
        }


        // -----------------------------------------------------
        // CRIA E GRAVA A MOVIMENTAÇÃO
        // -----------------------------------------------------

        // Monta o objeto com TODOS os dados exigidos pelo trabalho
        val movimentacao = Movimentacao(

            // Quanto dinheiro foi usado (já com o sinal)
            valor = valorComSinal,

            // Quem pagou
            pagador = pagador,

            // Quem recebeu
            recebedor = recebedor,

            // Motivo da operação
            motivo = motivo,

            // Quem autorizou
            responsavel = responsavel,

            // Classificação da operação
            tipo = tipo

            // A data e a hora não são passadas de propósito:
            // o valor padrão LocalDateTime.now() é usado,
            // garantindo que o horário seja o real.
        )

        // Manda o CRUD gravar a movimentação no PostgreSQL.
        //
        // Devolve o ID gerado pelo banco, ou null se falhou.
        val idGerado: Int? = repositorio.salvarRetornandoId(movimentacao)

        // Se o banco não gravou, a operação inteira é abortada.
        //
        // Muito importante: o saldo em memória NÃO é alterado,
        // senão o programa mostraria um dinheiro que não existe
        // no banco de dados.
        if (idGerado == null) {

            // Avisa e cancela
            println("[CAIXA] Recusado: falha ao gravar no banco de dados.")
            return null
        }

        // Guarda no objeto o ID que o banco gerou
        movimentacao.id = idGerado


        // -----------------------------------------------------
        // ATUALIZA O SALDO EM MEMÓRIA
        // -----------------------------------------------------

        // Agora que o banco confirmou, o saldo pode ser atualizado
        saldoInterno = saldoFuturo

        // Mostra o comprovante da operação
        println("[CAIXA] Lançado com sucesso: $movimentacao")

        // Mostra o novo saldo
        println("[CAIXA] Novo saldo: R$ $saldoInterno")

        // Devolve a movimentação criada
        return movimentacao
    }


    // Atalho para lançar uma RECEITA (entrada de dinheiro).
    //
    // Não repete a lógica: apenas chama registrar().
    fun receita(
        tipo: TipoMovimentacao,
        valor: BigDecimal,
        pagador: String,
        recebedor: String,
        motivo: String,
        responsavel: String,
        podeAprovar: Boolean
    ): Movimentacao? {

        // Trava de segurança: recusa se o tipo não for de entrada
        if (!tipo.ehEntrada()) {

            // Avisa e cancela
            println("[CAIXA] Recusado: '$tipo' não é um tipo de receita.")
            return null
        }

        // Repassa para a função central
        return registrar(
            tipo, valor, pagador, recebedor,
            motivo, responsavel, podeAprovar
        )
    }


    // Atalho para lançar uma DESPESA (saída de dinheiro)
    fun despesa(
        tipo: TipoMovimentacao,
        valor: BigDecimal,
        pagador: String,
        recebedor: String,
        motivo: String,
        responsavel: String,
        podeAprovar: Boolean
    ): Movimentacao? {

        // Trava de segurança: recusa se o tipo for de entrada
        if (tipo.ehEntrada()) {

            // Avisa e cancela
            println("[CAIXA] Recusado: '$tipo' não é um tipo de despesa.")
            return null
        }

        // Repassa para a função central
        return registrar(
            tipo, valor, pagador, recebedor,
            motivo, responsavel, podeAprovar
        )
    }


    // Mostra o extrato completo lendo direto do banco de dados
    fun extrato() {

        // Cabeçalho do relatório
        println("=".repeat(70))
        println("EXTRATO DO CAIXA")
        println("=".repeat(70))

        // Pede ao CRUD para listar todas as movimentações
        repositorio.listar()

        // Mostra o saldo no rodapé
        println("=".repeat(70))
        println("SALDO ATUAL: R$ $saldoInterno")
        println("=".repeat(70))
    }
}
