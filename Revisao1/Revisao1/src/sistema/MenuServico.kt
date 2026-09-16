// Define o pacote da camada de apresentação (os menus)
package sistema

// Importa o enum de setores, usado para filtrar instaladores
import enumeradores.Setor

// Importa o enum que classifica a movimentação
import enumeradores.TipoMovimentacao

// Importa o Caixa, o único lugar que mexe em dinheiro
import financeiro.Caixa

// Importa a classe Servico
import produto.Servico

// Importa os CRUDs usados aqui
import repositorio.CRUDCaixaDAgua
import repositorio.CRUDCliente
import repositorio.CRUDFuncionario
import repositorio.CRUDServico

// Importa o Validador
import validacao.Validador


// Menu que controla o SERVIÇO de instalação.
//
// Atende à parte de "manutenção/montagem" do requisito
// de controlar o fluxo de um ou mais produto/serviço.
fun menuServico() {

    // do/while repete o menu até o usuário voltar
    do {

        // Cabeçalho do menu
        println()
        println("=".repeat(60))
        println("SERVIÇOS DE INSTALAÇÃO")
        println("=".repeat(60))

        // Opções disponíveis
        println("1 - Agendar instalação")
        println("2 - Listar serviços")
        println("3 - Concluir serviço (gera receita)")
        println("4 - Cancelar serviço")
        println("0 - Voltar ao menu principal")

        // Lê a opção de forma segura, aceitando só de 0 a 4
        val opcao = Validador.lerInteiro("Opção", 0, 4)

        // Escolhe o que executar
        when (opcao) {

            // 1: agendar uma instalação nova
            1 -> agendarServico()

            // 2: listar os serviços já agendados
            2 -> CRUDServico().listar()

            // 3: concluir e cobrar o serviço
            3 -> concluirServico()

            // 4: cancelar um serviço agendado
            4 -> cancelarServico()

            // 0: volta ao menu principal
            0 -> return
        }

    // Repete para sempre; a saída acontece pelo return acima
    } while (true)
}


// Agenda um novo serviço de instalação
fun agendarServico() {

    // Título da operação
    println()
    println("--- AGENDAR INSTALAÇÃO ---")

    // Busca os clientes cadastrados
    val clientes = CRUDCliente().listarTodos()

    // Sem cliente não existe serviço
    if (clientes.isEmpty()) {

        // Explica o que fazer antes
        println("Cadastre um cliente primeiro.")
        return
    }

    // Busca APENAS os funcionários do setor de instalação.
    //
    // filter garante que o financeiro não seja escalado
    // para subir no telhado instalar caixa d'água.
    val instaladores = CRUDFuncionario().listarTodos()
        .filter { funcionario -> funcionario.setor == Setor.INSTALACAO }

    // Se não há instalador, avisa e sai
    if (instaladores.isEmpty()) {

        // Explica o que falta
        println("Nenhum funcionário do setor INSTALAÇÃO cadastrado.")
        return
    }

    // Busca os produtos cadastrados
    val caixas = CRUDCaixaDAgua().listarTodos()

    // Sem produto não existe instalação
    if (caixas.isEmpty()) {

        // Explica o que falta
        println("Cadastre uma caixa d'água primeiro.")
        return
    }

    // Mostra a lista numerada de clientes
    println("CLIENTES:")
    clientes.forEachIndexed { indice, cliente ->

        // Mostra número e nome
        println("  $indice - ${cliente.nome}")
    }

    // Lê qual cliente contratou
    val escolhaCliente = Validador.lerInteiro("Cliente", 0, clientes.size - 1)

    // Mostra a lista numerada de instaladores
    println("INSTALADORES:")
    instaladores.forEachIndexed { indice, funcionario ->

        // Mostra número, nome e turno
        println("  $indice - ${funcionario.nome} " +
                "| turno ${funcionario.turno}")
    }

    // Lê qual instalador vai executar
    val escolhaInstalador = Validador.lerInteiro(
        "Instalador",
        0,
        instaladores.size - 1
    )

    // Mostra a lista numerada de produtos
    println("CAIXAS:")
    caixas.forEachIndexed { indice, caixa ->

        // Mostra número, marca e modelo
        println("  $indice - ${caixa.marca} ${caixa.modelo}")
    }

    // Lê qual caixa será instalada
    val escolhaCaixa = Validador.lerInteiro("Caixa", 0, caixas.size - 1)

    // Lê a data da instalação, validada pelo formato dd/MM/aaaa
    val data = Validador.lerData("Data da instalação (dd/MM/aaaa)")

    // Regra de negócio: não se agenda instalação no passado.
    //
    // isBefore compara duas datas e devolve true
    // quando a primeira vem antes da segunda.
    if (data.isBefore(java.time.LocalDate.now())) {

        // Avisa o problema
        println("[ERRO] Não é possível agendar uma data que já passou.")
        return
    }

    // Lê o preço da mão de obra
    val preco = Validador.lerDinheiro("Preço da instalação")

    // Monta o objeto Servico com a situação inicial AGENDADO
    val servico = Servico(
        clienteId = clientes[escolhaCliente].id,
        instaladorId = instaladores[escolhaInstalador].id,
        caixaId = caixas[escolhaCaixa].id,
        dataInstalacao = data,
        preco = preco
    )

    // Manda o CRUD gravar no banco
    CRUDServico().salvar(servico)

    // Explica o próximo passo.
    //
    // Nenhum dinheiro entra agora: o serviço só é cobrado
    // depois de executado. Isso é regra contábil correta.
    println("Nenhum valor foi lançado no caixa ainda.")
    println("A receita entra quando o serviço for CONCLUÍDO.")
}


// Conclui um serviço e lança a receita no caixa
fun concluirServico() {

    // Se o operador não pode aprovar dinheiro, sai.
    //
    // Concluir gera receita, então precisa de permissão.
    if (!Sessao.operadorPodeAprovar()) {

        // Explica o bloqueio
        println("[BLOQUEADO] ${Sessao.nomeOperador()} não pode lançar receita.")
        return
    }

    // Cria os CRUDs necessários
    val crudServico = CRUDServico()
    val crudCliente = CRUDCliente()

    // Busca apenas os serviços que ainda estão agendados.
    //
    // Um serviço já concluído não pode ser cobrado de novo.
    val agendados = crudServico.listarTodos()
        .filter { servico -> servico.status == "AGENDADO" }

    // Se não tem nada agendado, avisa e sai
    if (agendados.isEmpty()) {

        // Mensagem de lista vazia
        println("Nenhum serviço agendado no momento.")
        return
    }

    // Mostra a lista numerada de serviços agendados
    println("SERVIÇOS AGENDADOS:")
    agendados.forEachIndexed { indice, servico ->

        // Mostra número, data e preço
        println("  $indice - ${servico.dataInstalacao} | " +
                "R$ ${servico.preco} | cliente #${servico.clienteId}")
    }

    // Lê qual serviço foi executado
    val escolha = Validador.lerInteiro("Serviço", 0, agendados.size - 1)

    // Guarda o serviço escolhido
    val servico = agendados[escolha]

    // Busca o cliente para gravar o nome dele como pagador.
    //
    // "?." e "?:" tratam o caso do cliente ter sido apagado.
    val nomeCliente = crudCliente.buscarPorId(servico.clienteId)?.nome
        ?: "Cliente #${servico.clienteId}"

    // Pede confirmação antes de cobrar
    if (!Validador.confirmar(
            "Confirmar a conclusão e cobrar R$ ${servico.preco}")) {

        // Desistiu: avisa e sai
        println("Operação cancelada.")
        return
    }

    // Muda a situação do serviço no objeto em memória.
    //
    // concluir() devolve false se o serviço já estava fechado.
    if (!servico.concluir()) {

        // Avisa que a situação impede a conclusão
        println("[ERRO] Esse serviço não pode ser concluído.")
        return
    }

    // Lança a RECEITA no caixa com os seis dados exigidos
    val movimentacao = Caixa.receita(

        // Classificação da operação
        tipo = TipoMovimentacao.VENDA_SERVICO,

        // QUANTO dinheiro entrou
        valor = servico.preco,

        // QUEM PAGOU: o cliente
        pagador = nomeCliente,

        // QUEM RECEBEU: a empresa
        recebedor = "EMPRESA",

        // O MOTIVO da entrada
        motivo = "Instalação concluída (serviço #${servico.id})",

        // O RESPONSÁVEL pela transação
        responsavel = Sessao.nomeOperador(),

        // A permissão do responsável
        podeAprovar = Sessao.operadorPodeAprovar()
    )

    // Se o caixa recusou, o serviço volta a ficar agendado.
    //
    // Sem isso, o serviço apareceria como pago sem ter
    // gerado nenhuma entrada de dinheiro.
    if (movimentacao == null) {

        // Avisa que nada mudou
        println("[FALHOU] O serviço continua AGENDADO.")
        return
    }

    // Grava a nova situação do serviço no banco
    crudServico.editar(servico, servico.id)

    // Confirma o resultado
    println("[OK] Serviço #${servico.id} concluído e cobrado.")
}


// Cancela um serviço agendado
fun cancelarServico() {

    // Cria o CRUD
    val crud = CRUDServico()

    // Mostra a lista de serviços
    crud.listar()

    // Lê o ID do serviço a cancelar
    val id = Validador.lerInteiro("ID do serviço a cancelar", 1)

    // Busca o serviço no banco
    val servico = crud.buscarPorId(id)

    // Se não encontrou, avisa e sai
    if (servico == null) {

        // Mensagem de registro inexistente
        println("[AVISO] Nenhum serviço com o ID $id.")
        return
    }

    // Tenta cancelar no objeto em memória.
    //
    // cancelar() devolve false quando o serviço já foi
    // concluído, porque aí o dinheiro já entrou no caixa.
    if (!servico.cancelar()) {

        // Explica o motivo do bloqueio
        println("[BLOQUEADO] Serviço já concluído não pode ser cancelado.")
        println("            Registre um estorno no financeiro.")
        return
    }

    // Grava a nova situação no banco
    crud.editar(servico, id)

    // Confirma o cancelamento
    println("[OK] Serviço $id cancelado.")
}
