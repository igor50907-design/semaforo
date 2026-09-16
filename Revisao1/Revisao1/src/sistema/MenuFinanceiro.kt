// Define o pacote da camada de apresentação (os menus)
package sistema

// Importa o enum que classifica cada movimentação
import enumeradores.TipoMovimentacao

// Importa o Caixa, o único lugar que mexe em dinheiro
import financeiro.Caixa

// Importa os CRUDs usados nas operações
import repositorio.CRUDCaixaDAgua
import repositorio.CRUDCliente
import repositorio.CRUDFornecedor
import repositorio.CRUDFuncionario
import repositorio.CRUDMovimentacao

// Importa o Validador
import validacao.Validador


// Menu que controla TODO o fluxo de caixa da empresa.
//
// Atende aos requisitos de:
//   * controlar compra, venda, estoque e pagamentos
//   * fazer isso de forma SEGURA (encapsulamento)
//   * gerar movimentações financeiras gravadas no banco
fun menuFinanceiro() {

    // do/while repete o menu até o usuário voltar
    do {

        // Cabeçalho mostrando o saldo atual e quem opera
        println()
        println("=".repeat(60))
        println("FINANCEIRO | Saldo: R$ ${Caixa.saldo()} | " +
                "Operador: ${Sessao.nomeOperador()}")
        println("=".repeat(60))

        // Opções de saída de dinheiro
        println("--- SAÍDAS ---")
        println("1 - Comprar caixas do fornecedor")
        println("2 - Pagar salário de funcionário")
        println("3 - Pagar despesa/boleto")

        // Opções de entrada de dinheiro
        println("--- ENTRADAS ---")
        println("4 - Vender caixa d'água ao cliente")
        println("5 - Receber parcela de cliente")

        // Opções de consulta
        println("--- CONSULTAS ---")
        println("6 - Ver extrato completo")
        println("7 - Relatório por tipo de movimentação")
        println("8 - Ver clientes devedores")

        // Opção de voltar
        println("0 - Voltar ao menu principal")

        // Lê a opção de forma segura, aceitando só de 0 a 8
        val opcao = Validador.lerInteiro("Opção", 0, 8)

        // Escolhe o que executar
        when (opcao) {

            // 1: compra de mercadoria (sai dinheiro, entra estoque)
            1 -> comprarDoFornecedor()

            // 2: folha de pagamento (sai dinheiro)
            2 -> pagarSalario()

            // 3: conta a pagar (sai dinheiro)
            3 -> pagarDespesa()

            // 4: venda (entra dinheiro, sai estoque)
            4 -> venderCaixa()

            // 5: cobrança de parcela (entra dinheiro)
            5 -> receberParcela()

            // 6: extrato completo do caixa
            6 -> Caixa.extrato()

            // 7: totais agrupados por tipo
            7 -> CRUDMovimentacao().relatorioPorTipo()

            // 8: lista de quem ainda deve
            8 -> listarDevedores()

            // 0: volta ao menu principal
            0 -> return
        }

    // Repete para sempre; a saída acontece pelo return acima
    } while (true)
}


// Função auxiliar que verifica se o operador logado
// tem permissão para lançar dinheiro.
//
// Ela é chamada no começo de TODA operação financeira,
// evitando repetir esse teste seis vezes.
private fun operadorAutorizado(): Boolean {

    // Pergunta à Sessão se o setor do operador aprova
    if (!Sessao.operadorPodeAprovar()) {

        // Explica o bloqueio
        println()
        println("[BLOQUEADO] ${Sessao.nomeOperador()} não pertence a um")
        println("            setor autorizado a mexer no caixa.")
        println("            Somente FINANCEIRO e ADMINISTRATIVO podem.")

        // Informa que não pode seguir
        return false
    }

    // Está tudo certo, pode seguir
    return true
}


// =============================================================
// 1) COMPRA DO FORNECEDOR (saída de dinheiro + entrada de estoque)
// =============================================================

// Registra a compra de caixas d'água junto ao fornecedor
fun comprarDoFornecedor() {

    // Se o operador não pode aprovar, sai imediatamente
    if (!operadorAutorizado()) return

    // Título da operação
    println()
    println("--- COMPRA DE PRODUTO ---")

    // Cria o CRUD de produtos
    val crudCaixa = CRUDCaixaDAgua()

    // Busca os produtos cadastrados
    val caixas = crudCaixa.listarTodos()

    // Não dá para comprar o que não está cadastrado
    if (caixas.isEmpty()) {

        // Explica o que fazer antes
        println("Cadastre a caixa d'água no menu PRODUTO primeiro.")
        return
    }

    // Mostra a lista numerada de produtos
    caixas.forEachIndexed { indice, caixa ->

        // Mostra número, produto, custo e estoque atual
        println("  $indice - ${caixa.marca} ${caixa.modelo} " +
                "| custo R$ ${caixa.precoCusto} " +
                "| estoque ${caixa.quantidadeEstoque}")
    }

    // Lê qual produto está sendo comprado
    val escolha = Validador.lerInteiro("Produto", 0, caixas.size - 1)

    // Guarda o produto escolhido
    val caixa = caixas[escolha]

    // Lê quantas unidades estão entrando (de 1 a 1000)
    val quantidade = Validador.lerInteiro("Quantidade comprada", 1, 1000)

    // Lê o valor unitário realmente pago nesta compra.
    //
    // Ele pode ser diferente do custo cadastrado,
    // porque o fornecedor pode ter dado desconto.
    val valorUnitario = Validador.lerDinheiro("Valor unitário pago")

    // Calcula o total da compra: unitário x quantidade
    val total = valorUnitario.multiply(quantidade.toBigDecimal())

    // Descobre quem é o fornecedor para gravar o nome
    // no campo "recebedor" da movimentação.
    val nomeFornecedor =

        // Se a caixa não tem fornecedor vinculado, usa um texto genérico
        if (caixa.fornecedorId == null) {

            // Texto padrão quando não há fornecedor cadastrado
            "Fornecedor não informado"

        } else {

            // Busca o fornecedor no banco.
            //
            // "?." só acessa .nome se o fornecedor existir,
            // e "?:" usa um texto padrão se vier null.
            CRUDFornecedor().buscarPorId(caixa.fornecedorId)?.nome
                ?: "Fornecedor #${caixa.fornecedorId}"
        }

    // Mostra o resumo antes de confirmar
    println()
    println("Resumo: $quantidade un. x R$ $valorUnitario = R$ $total")
    println("Pagando para: $nomeFornecedor")

    // Pede confirmação final
    if (!Validador.confirmar("Confirmar a compra")) {

        // Desistiu: avisa e sai
        println("Compra cancelada.")
        return
    }

    // Lança a DESPESA no caixa.
    //
    // Repare que TODOS os seis dados exigidos são informados:
    // valor, pagador, recebedor, motivo, responsável
    // (e a data/hora é preenchida sozinha pelo sistema).
    val movimentacao = Caixa.despesa(

        // Classificação da operação
        tipo = TipoMovimentacao.COMPRA_PRODUTO,

        // QUANTO dinheiro foi usado
        valor = total,

        // QUEM PAGOU: a nossa empresa
        pagador = "EMPRESA",

        // QUEM RECEBEU: o fornecedor
        recebedor = nomeFornecedor,

        // O MOTIVO da operação
        motivo = "Compra de $quantidade un. de " +
                 "${caixa.marca} ${caixa.modelo}",

        // O RESPONSÁVEL: quem está logado no sistema
        responsavel = Sessao.nomeOperador(),

        // A permissão do responsável
        podeAprovar = Sessao.operadorPodeAprovar()
    )

    // Se o Caixa recusou (saldo insuficiente, erro no banco...),
    // o retorno é null e o estoque NÃO pode ser alterado.
    //
    // Essa checagem é o que impede o sistema de aumentar
    // o estoque de um produto que não foi pago.
    if (movimentacao == null) {

        // Avisa que nada aconteceu
        println("[FALHOU] A compra não foi registrada. Estoque inalterado.")
        return
    }

    // O dinheiro saiu, então agora a mercadoria entra.
    //
    // entrarEstoque soma as unidades no objeto em memória.
    caixa.entrarEstoque(quantidade)

    // Grava o novo estoque no banco de dados
    crudCaixa.atualizarEstoque(caixa.id, caixa.quantidadeEstoque)

    // Confirma o resultado final
    println("[OK] Estoque de ${caixa.marca} ${caixa.modelo} " +
            "agora é ${caixa.quantidadeEstoque} un.")
}


// =============================================================
// 2) PAGAMENTO DE SALÁRIO (saída de dinheiro)
// =============================================================

// Registra o pagamento do salário de um funcionário
fun pagarSalario() {

    // Se o operador não pode aprovar, sai imediatamente
    if (!operadorAutorizado()) return

    // Título da operação
    println()
    println("--- PAGAMENTO DE SALÁRIO ---")

    // Busca os funcionários cadastrados
    val funcionarios = CRUDFuncionario().listarTodos()

    // Se não tem ninguém, avisa e sai
    if (funcionarios.isEmpty()) {

        // Mensagem de cadastro vazio
        println("Nenhum funcionário cadastrado.")
        return
    }

    // Mostra a lista numerada de funcionários
    funcionarios.forEachIndexed { indice, funcionario ->

        // Mostra número, nome, setor e salário
        println("  $indice - ${funcionario.nome} " +
                "(${funcionario.setor}) | R$ ${funcionario.salario}")
    }

    // Lê qual funcionário será pago
    val escolha = Validador.lerInteiro("Funcionário", 0, funcionarios.size - 1)

    // Guarda o funcionário escolhido
    val funcionario = funcionarios[escolha]

    // Lê o mês de referência do pagamento
    val referencia = Validador.lerTexto("Mês de referência (ex: 09/2025)", 4)

    // Demonstra o POLIMORFISMO em ação.
    //
    // receberConta() é a MESMA chamada para qualquer Pessoa,
    // mas o Funcionario devolve o valor NEGATIVO, porque
    // para a empresa pagar salário é saída de dinheiro.
    val efeitoNoCaixa = funcionario.receberConta(funcionario.salario)

    // Mostra o resumo antes de confirmar
    println()
    println("Pagando R$ ${funcionario.salario} para ${funcionario.nome}")
    println("Efeito no caixa (polimorfismo): R$ $efeitoNoCaixa")

    // Pede confirmação final
    if (!Validador.confirmar("Confirmar o pagamento")) {

        // Desistiu: avisa e sai
        println("Pagamento cancelado.")
        return
    }

    // Lança a DESPESA no caixa com os seis dados exigidos
    Caixa.despesa(

        // Classificação da operação
        tipo = TipoMovimentacao.PAGAMENTO_SALARIO,

        // QUANTO: o salário, sempre positivo aqui.
        //
        // Quem aplica o sinal negativo é o Caixa, não nós.
        valor = funcionario.salario,

        // QUEM PAGOU: a empresa
        pagador = "EMPRESA",

        // QUEM RECEBEU: o funcionário
        recebedor = funcionario.nome,

        // O MOTIVO: salário do mês informado
        motivo = "Salário ${funcionario.setor} - referência $referencia",

        // O RESPONSÁVEL: quem está logado
        responsavel = Sessao.nomeOperador(),

        // A permissão do responsável
        podeAprovar = Sessao.operadorPodeAprovar()
    )
}


// =============================================================
// 3) PAGAMENTO DE DESPESA (saída de dinheiro)
// =============================================================

// Registra o pagamento de uma conta ou boleto qualquer
fun pagarDespesa() {

    // Se o operador não pode aprovar, sai imediatamente
    if (!operadorAutorizado()) return

    // Título da operação
    println()
    println("--- PAGAMENTO DE DESPESA ---")

    // Lê para quem o dinheiro está indo
    val recebedor = Validador.lerTexto("Para quem está pagando", 2)

    // Lê o motivo da despesa
    val motivo = Validador.lerTexto("Motivo (luz, água, aluguel...)", 3)

    // Lê o valor da conta
    val valor = Validador.lerDinheiro("Valor da despesa")

    // Pede confirmação final
    if (!Validador.confirmar("Confirmar o pagamento de R$ $valor")) {

        // Desistiu: avisa e sai
        println("Pagamento cancelado.")
        return
    }

    // Lança a DESPESA no caixa com os seis dados exigidos
    Caixa.despesa(

        // Classificação da operação
        tipo = TipoMovimentacao.PAGAMENTO_DESPESA,

        // QUANTO dinheiro foi usado
        valor = valor,

        // QUEM PAGOU: a empresa
        pagador = "EMPRESA",

        // QUEM RECEBEU: informado pelo usuário
        recebedor = recebedor,

        // O MOTIVO: informado pelo usuário
        motivo = motivo,

        // O RESPONSÁVEL: quem está logado
        responsavel = Sessao.nomeOperador(),

        // A permissão do responsável
        podeAprovar = Sessao.operadorPodeAprovar()
    )
}


// =============================================================
// 4) VENDA AO CLIENTE (entrada de dinheiro + saída de estoque)
// =============================================================

// Registra a venda de caixas d'água para um cliente.
//
// Esta é a operação mais completa do sistema: ela mexe
// em estoque, em caixa e na dívida do cliente ao mesmo tempo.
fun venderCaixa() {

    // Se o operador não pode aprovar, sai imediatamente
    if (!operadorAutorizado()) return

    // Título da operação
    println()
    println("--- VENDA DE PRODUTO ---")

    // Cria os CRUDs necessários
    val crudCaixa = CRUDCaixaDAgua()
    val crudCliente = CRUDCliente()

    // Busca os clientes cadastrados
    val clientes = crudCliente.listarTodos()

    // Sem cliente não existe venda
    if (clientes.isEmpty()) {

        // Explica o que fazer antes
        println("Cadastre um cliente no menu PESSOAS primeiro.")
        return
    }

    // Busca apenas os produtos que TÊM estoque.
    //
    // filter percorre a lista e mantém só o que passa no teste.
    // Assim é impossível escolher um produto esgotado.
    val caixas = crudCaixa.listarTodos()
        .filter { caixa -> caixa.quantidadeEstoque > 0 }

    // Se nada tem estoque, avisa e sai
    if (caixas.isEmpty()) {

        // Explica o problema
        println("Nenhum produto com estoque disponível.")
        println("Compre do fornecedor primeiro.")
        return
    }

    // Mostra a lista numerada de clientes
    println("CLIENTES:")
    clientes.forEachIndexed { indice, cliente ->

        // Mostra número e nome
        println("  $indice - ${cliente.nome}")
    }

    // Lê qual cliente está comprando
    val escolhaCliente = Validador.lerInteiro("Cliente", 0, clientes.size - 1)

    // Guarda o cliente escolhido
    val cliente = clientes[escolhaCliente]

    // Mostra a lista numerada de produtos disponíveis
    println("PRODUTOS DISPONÍVEIS:")
    caixas.forEachIndexed { indice, caixa ->

        // Mostra número, produto, preço e estoque
        println("  $indice - ${caixa.marca} ${caixa.modelo} " +
                "| R$ ${caixa.precoVenda} " +
                "| ${caixa.quantidadeEstoque} un. disponível(is)")
    }

    // Lê qual produto está sendo vendido
    val escolhaCaixa = Validador.lerInteiro("Produto", 0, caixas.size - 1)

    // Guarda o produto escolhido
    val caixa = caixas[escolhaCaixa]

    // Lê a quantidade, limitando ao estoque existente.
    //
    // O próprio Validador já impede pedir mais do que existe,
    // porque o máximo é a quantidade em estoque.
    val quantidade = Validador.lerInteiro(
        "Quantidade a vender",
        1,
        caixa.quantidadeEstoque
    )

    // Calcula o total da venda
    val total = caixa.precoVenda.multiply(quantidade.toBigDecimal())

    // Mostra o resumo
    println()
    println("Resumo: $quantidade un. x R$ ${caixa.precoVenda} = R$ $total")

    // Pergunta se o cliente vai pagar agora ou parcelar
    val pagouAgora = Validador.confirmar("O cliente vai pagar à vista")

    // -----------------------------------------------------
    // CAMINHO 1: PAGAMENTO À VISTA
    // -----------------------------------------------------
    if (pagouAgora) {

        // Lança a RECEITA no caixa com os seis dados exigidos
        val movimentacao = Caixa.receita(

            // Classificação da operação
            tipo = TipoMovimentacao.VENDA_PRODUTO,

            // QUANTO dinheiro entrou
            valor = total,

            // QUEM PAGOU: o cliente
            pagador = cliente.nome,

            // QUEM RECEBEU: a empresa
            recebedor = "EMPRESA",

            // O MOTIVO da entrada
            motivo = "Venda à vista de $quantidade un. de " +
                     "${caixa.marca} ${caixa.modelo}",

            // O RESPONSÁVEL pela transação
            responsavel = Sessao.nomeOperador(),

            // A permissão do responsável
            podeAprovar = Sessao.operadorPodeAprovar()
        )

        // Se o caixa recusou, a venda inteira é abortada.
        //
        // O estoque NÃO é baixado, senão o produto sumiria
        // do sistema sem que o dinheiro tivesse entrado.
        if (movimentacao == null) {

            // Avisa que nada aconteceu
            println("[FALHOU] A venda não foi registrada. Estoque inalterado.")
            return
        }

    // -----------------------------------------------------
    // CAMINHO 2: VENDA PARCELADA
    // -----------------------------------------------------
    } else {

        // Lê em quantas vezes o cliente vai pagar
        val parcelas = Validador.lerInteiro("Em quantas parcelas", 1, 24)

        // Divide o total pelo número de parcelas.
        //
        // O "2" são as casas decimais e HALF_UP arredonda
        // meio centavo para cima, que é a regra comercial.
        val valorParcela = total.divide(
            parcelas.toBigDecimal(),
            2,
            java.math.RoundingMode.HALF_UP
        )

        // Adiciona cada parcela na dívida do cliente.
        //
        // repeat(n) executa o bloco n vezes.
        repeat(parcelas) {

            // Registra mais uma parcela em aberto
            cliente.adicionarParcela(valorParcela)
        }

        // Grava a dívida atualizada no banco
        crudCliente.editar(cliente, cliente.id)

        // Explica o que aconteceu.
        //
        // IMPORTANTE: nenhum dinheiro entrou no caixa agora.
        // Só entra quando o cliente pagar cada parcela.
        println("[OK] $parcelas parcela(s) de R$ $valorParcela " +
                "lançada(s) para ${cliente.nome}.")
        println("     Nenhum valor entrou no caixa ainda.")
        println("     Use 'Receber parcela de cliente' quando ele pagar.")
    }

    // -----------------------------------------------------
    // BAIXA NO ESTOQUE (acontece nos dois caminhos)
    // -----------------------------------------------------

    // A mercadoria saiu fisicamente, então o estoque cai
    // tanto na venda à vista quanto na venda parcelada.
    //
    // sairEstoque devolve false se algo estiver errado.
    if (caixa.sairEstoque(quantidade)) {

        // Grava o novo estoque no banco
        crudCaixa.atualizarEstoque(caixa.id, caixa.quantidadeEstoque)

        // Confirma o estoque restante
        println("[OK] Estoque restante: ${caixa.quantidadeEstoque} un.")

    } else {

        // Situação rara, mas o aviso existe para não passar em branco
        println("[ERRO] Não foi possível baixar o estoque.")
    }
}


// =============================================================
// 5) RECEBIMENTO DE PARCELA (entrada de dinheiro)
// =============================================================

// Registra o pagamento de uma parcela em aberto
fun receberParcela() {

    // Se o operador não pode aprovar, sai imediatamente
    if (!operadorAutorizado()) return

    // Título da operação
    println()
    println("--- RECEBIMENTO DE PARCELA ---")

    // Cria o CRUD de clientes
    val crudCliente = CRUDCliente()

    // Busca apenas os clientes que TÊM dívida.
    //
    // filter usa a propriedade calculada dividasAbertas.
    val devedores = crudCliente.listarTodos()
        .filter { cliente -> cliente.dividasAbertas }

    // Se ninguém deve nada, avisa e sai
    if (devedores.isEmpty()) {

        // Boa notícia para a empresa
        println("Nenhum cliente com parcelas em aberto.")
        return
    }

    // Mostra a lista numerada de devedores
    devedores.forEachIndexed { indice, cliente ->

        // Mostra número, nome e total devido
        println("  $indice - ${cliente.nome} " +
                "| deve R$ ${cliente.totalDevido} " +
                "em ${cliente.listarParcelas().size} parcela(s)")
    }

    // Lê qual cliente está pagando
    val escolha = Validador.lerInteiro("Cliente", 0, devedores.size - 1)

    // Guarda o cliente escolhido
    val cliente = devedores[escolha]

    // Pega a lista de parcelas desse cliente
    val parcelas = cliente.listarParcelas()

    // Mostra as parcelas numeradas
    println("PARCELAS DE ${cliente.nome}:")
    parcelas.forEachIndexed { indice, valor ->

        // Mostra o número da parcela e o valor
        println("  $indice - R$ $valor")
    }

    // Lê qual parcela está sendo paga
    val indiceParcela = Validador.lerInteiro(
        "Qual parcela está sendo paga",
        0,
        parcelas.size - 1
    )

    // Quita a parcela no objeto em memória.
    //
    // O retorno é NULLABLE: devolve null se o índice
    // não existir, o que aqui já foi evitado pelo Validador.
    val valorPago = cliente.quitarParcela(indiceParcela)

    // Verificação extra de segurança
    if (valorPago == null) {

        // Avisa que a parcela não foi encontrada
        println("[ERRO] Parcela não encontrada.")
        return
    }

    // Lança a RECEITA no caixa com os seis dados exigidos
    val movimentacao = Caixa.receita(

        // Classificação da operação
        tipo = TipoMovimentacao.RECEBIMENTO_PAGAMENTO,

        // QUANTO dinheiro entrou
        valor = valorPago,

        // QUEM PAGOU: o cliente
        pagador = cliente.nome,

        // QUEM RECEBEU: a empresa
        recebedor = "EMPRESA",

        // O MOTIVO da entrada
        motivo = "Recebimento de parcela",

        // O RESPONSÁVEL pela transação
        responsavel = Sessao.nomeOperador(),

        // A permissão do responsável
        podeAprovar = Sessao.operadorPodeAprovar()
    )

    // Se o caixa recusou, devolve a parcela para a dívida.
    //
    // Sem isso, a dívida sumiria do sistema sem o dinheiro
    // ter entrado, e a empresa perderia o valor.
    if (movimentacao == null) {

        // Recoloca a parcela na lista
        cliente.adicionarParcela(valorPago)

        // Avisa que a dívida continua
        println("[FALHOU] A parcela foi devolvida para a dívida.")
        return
    }

    // Deu certo: grava a dívida atualizada no banco
    crudCliente.editar(cliente, cliente.id)

    // Informa quanto o cliente ainda deve
    println("[OK] ${cliente.nome} agora deve R$ ${cliente.totalDevido}.")
}


// =============================================================
// 8) RELATÓRIO DE DEVEDORES
// =============================================================

// Mostra todos os clientes que ainda têm parcelas em aberto
fun listarDevedores() {

    // Busca só os clientes com dívida
    val devedores = CRUDCliente().listarTodos()
        .filter { cliente -> cliente.dividasAbertas }

    // Se ninguém deve, avisa e sai
    if (devedores.isEmpty()) {

        // Boa notícia
        println("Nenhum cliente com parcelas em aberto.")
        return
    }

    // Cabeçalho do relatório
    println()
    println("=".repeat(60))
    println("CLIENTES DEVEDORES")
    println("=".repeat(60))

    // Acumulador do total a receber
    var totalGeral = java.math.BigDecimal.ZERO

    // Percorre cada devedor
    devedores.forEach { cliente ->

        // Soma a dívida dele no total geral
        totalGeral = totalGeral.add(cliente.totalDevido)

        // Mostra o nome, o telefone e o quanto deve
        println("${cliente.nome} | tel: ${cliente.telefone} | " +
                "R$ ${cliente.totalDevido} " +
                "(${cliente.listarParcelas().size} parcela(s))")
    }

    // Rodapé com o total a receber
    println("-".repeat(60))
    println("TOTAL A RECEBER: R$ $totalGeral")
    println("=".repeat(60))
}
