// Define o pacote da camada de apresentação (os menus)
package sistema

// Importa os enums de cor e material
import enumeradores.Cor
import enumeradores.Material

// Importa a classe do produto
import produto.CaixaDaAgua

// Importa os CRUDs usados aqui
import repositorio.CRUDCaixaDAgua
import repositorio.CRUDFornecedor

// Importa o Validador
import validacao.Validador


// Menu que gerencia o PRODUTO da empresa: a caixa d'água.
//
// Atende ao requisito de "controlar o fluxo de um ou mais
// produtos" na parte de cadastro, consulta e estoque.
// (A compra e a venda ficam no menu financeiro, porque
//  mexem em dinheiro.)
fun menuProduto() {

    // do/while repete o menu até o usuário voltar
    do {

        // Cabeçalho do menu
        println()
        println("=".repeat(60))
        println("CAIXAS D'ÁGUA (PRODUTO)")
        println("=".repeat(60))

        // Opções disponíveis
        println("1 - Cadastrar caixa d'água")
        println("2 - Listar caixas d'água")
        println("3 - Editar caixa d'água")
        println("4 - Excluir caixa d'água")
        println("5 - Consultar estoque")
        println("0 - Voltar ao menu principal")

        // Lê a opção de forma segura, aceitando só de 0 a 5
        val opcao = Validador.lerInteiro("Opção", 0, 5)

        // Escolhe o que executar
        when (opcao) {

            // 1: cadastrar produto novo
            1 -> cadastrarCaixa()

            // 2: listar produtos
            2 -> CRUDCaixaDAgua().listar()

            // 3: editar produto
            3 -> editarCaixa()

            // 4: excluir produto
            4 -> excluirCaixa()

            // 5: relatório de estoque
            5 -> consultarEstoque()

            // 0: volta ao menu principal
            0 -> return
        }

    // Repete para sempre; a saída acontece pelo return acima
    } while (true)
}


// Cadastra uma caixa d'água nova
fun cadastrarCaixa() {

    // Título da operação
    println()
    println("--- NOVA CAIXA D'ÁGUA ---")

    // Lê a marca (mínimo 2 caracteres)
    val marca = Validador.lerTexto("Marca", 2)

    // Lê o modelo (mínimo 1 caractere)
    val modelo = Validador.lerTexto("Modelo", 1)

    // Lê a largura em metros, aceitando só número positivo
    val largura = Validador.lerDouble("Largura (m)")

    // Lê a altura em metros
    val altura = Validador.lerDouble("Altura (m)")

    // Lê a profundidade em metros
    val profundidade = Validador.lerDouble("Profundidade (m)")

    // Monta a lista com as três dimensões, sempre nesta ordem:
    // largura, altura e profundidade.
    val dimensao = mutableListOf(largura, altura, profundidade)

    // Mostra o menu de cores montado a partir do enum
    val cor = Validador.lerEnum("Cor", Cor.entries.toTypedArray())

    // Mostra o menu de materiais montado a partir do enum
    val material = Validador.lerEnum("Material", Material.entries.toTypedArray())

    // Lê o formato da caixa
    val formato = Validador.lerTexto("Formato (redonda, quadrada...)", 3)

    // Lê quanto a empresa pagou por unidade
    val precoCusto = Validador.lerDinheiro("Preço de CUSTO (por unidade)")

    // Lê por quanto a empresa vai vender
    val precoVenda = Validador.lerDinheiro("Preço de VENDA (por unidade)")

    // Regra de negócio: vender abaixo do custo dá prejuízo.
    //
    // O sistema não proíbe (às vezes é uma promoção proposital),
    // mas AVISA e pede confirmação. Isso evita erro de digitação.
    if (precoVenda < precoCusto) {

        // Mostra o alerta
        println()
        println("[ATENÇÃO] O preço de venda está ABAIXO do custo.")
        println("          Isso gera prejuízo em cada unidade vendida.")

        // Se o usuário não confirmar, cancela o cadastro
        if (!Validador.confirmar("Continuar mesmo assim")) {

            // Avisa que nada foi salvo
            println("Cadastro cancelado.")
            return
        }
    }

    // Cria o CRUD de fornecedores para oferecer a vinculação
    val crudFornecedor = CRUDFornecedor()

    // Busca os fornecedores cadastrados
    val fornecedores = crudFornecedor.listarTodos()

    // Variável que guardará o ID do fornecedor escolhido.
    //
    // "Int?" é NULLABLE: pode ficar null se não houver
    // fornecedor cadastrado ou se o usuário não quiser vincular.
    var fornecedorId: Int? = null

    // Só oferece a escolha se existir algum fornecedor
    if (fornecedores.isNotEmpty()) {

        // Pergunta se o usuário quer vincular
        if (Validador.confirmar("Vincular a um fornecedor")) {

            // Mostra a lista numerada de fornecedores
            fornecedores.forEachIndexed { indice, fornecedor ->

                // Mostra o número e o nome da empresa
                println("  $indice - ${fornecedor.nome}")
            }

            // Lê a escolha dentro da faixa válida
            val escolha = Validador.lerInteiro(
                "Fornecedor",
                0,
                fornecedores.size - 1
            )

            // Guarda o ID do fornecedor escolhido
            fornecedorId = fornecedores[escolha].id
        }
    }

    // Monta o objeto CaixaDaAgua com todos os dados validados.
    //
    // O estoque inicial é 0 de propósito: unidade só entra
    // no estoque através de uma COMPRA registrada no caixa.
    // Assim é impossível ter produto que apareceu do nada.
    val caixa = CaixaDaAgua(
        marca = marca,
        modelo = modelo,
        dimensao = dimensao,
        cor = cor,
        material = material,
        formato = formato,
        precoCusto = precoCusto,
        precoVenda = precoVenda,
        quantidadeInicial = 0,
        fornecedorId = fornecedorId
    )

    // Manda o CRUD gravar no banco
    CRUDCaixaDAgua().salvar(caixa)

    // Explica o próximo passo para o usuário
    println("Use o menu FINANCEIRO > Comprar do fornecedor")
    println("para dar entrada de unidades no estoque.")
}


// Edita uma caixa d'água já cadastrada
fun editarCaixa() {

    // Cria o CRUD
    val crud = CRUDCaixaDAgua()

    // Mostra a lista para o usuário escolher o ID
    crud.listar()

    // Lê o ID da caixa
    val id = Validador.lerInteiro("ID da caixa a editar", 1)

    // Busca a caixa no banco
    val atual = crud.buscarPorId(id)

    // Se não encontrou, avisa e sai
    if (atual == null) {

        // Mensagem de registro inexistente
        println("[AVISO] Nenhuma caixa com o ID $id.")
        return
    }

    // Mostra o que está sendo editado
    println("Editando: $atual")

    // Lê os novos dados
    val marca = Validador.lerTexto("Nova marca", 2)
    val modelo = Validador.lerTexto("Novo modelo", 1)
    val largura = Validador.lerDouble("Nova largura (m)")
    val altura = Validador.lerDouble("Nova altura (m)")
    val profundidade = Validador.lerDouble("Nova profundidade (m)")
    val cor = Validador.lerEnum("Nova cor", Cor.entries.toTypedArray())
    val material = Validador.lerEnum("Novo material", Material.entries.toTypedArray())
    val formato = Validador.lerTexto("Novo formato", 3)
    val precoCusto = Validador.lerDinheiro("Novo preço de custo")
    val precoVenda = Validador.lerDinheiro("Novo preço de venda")

    // Monta o objeto novo.
    //
    // O ESTOQUE é preservado (atual.quantidadeEstoque):
    // ele não pode ser alterado por digitação, só por
    // compra ou venda registrada. Isso protege o inventário.
    val nova = CaixaDaAgua(
        marca = marca,
        modelo = modelo,
        dimensao = mutableListOf(largura, altura, profundidade),
        cor = cor,
        material = material,
        formato = formato,
        precoCusto = precoCusto,
        precoVenda = precoVenda,
        quantidadeInicial = atual.quantidadeEstoque,
        fornecedorId = atual.fornecedorId,
        id = id
    )

    // Manda o CRUD atualizar no banco
    crud.editar(nova, id)
}


// Exclui uma caixa d'água do cadastro
fun excluirCaixa() {

    // Cria o CRUD
    val crud = CRUDCaixaDAgua()

    // Mostra a lista de caixas
    crud.listar()

    // Lê o ID da caixa a excluir
    val id = Validador.lerInteiro("ID da caixa a excluir", 1)

    // Busca a caixa para conferir a situação dela
    val caixa = crud.buscarPorId(id)

    // Se não encontrou, avisa e sai
    if (caixa == null) {

        // Mensagem de registro inexistente
        println("[AVISO] Nenhuma caixa com o ID $id.")
        return
    }

    // Regra de negócio: não se apaga produto que ainda
    // existe fisicamente no depósito.
    if (caixa.quantidadeEstoque > 0) {

        // Explica o motivo do bloqueio
        println("[BLOQUEADO] Ainda existem ${caixa.quantidadeEstoque} " +
                "unidade(s) em estoque.")
        println("            Venda ou dê baixa antes de excluir.")

        // Sai sem apagar
        return
    }

    // Pede confirmação antes de apagar
    if (Validador.confirmar("Confirma a exclusão da caixa $id")) {

        // Confirmou: apaga
        crud.excluir(id)

    } else {

        // Desistiu: avisa
        println("Operação cancelada.")
    }
}


// Mostra o relatório de estoque com o valor total imobilizado
fun consultarEstoque() {

    // Busca todas as caixas cadastradas
    val lista = CRUDCaixaDAgua().listarTodos()

    // Se não tem produto, avisa e sai
    if (lista.isEmpty()) {

        // Mensagem de cadastro vazio
        println("Nenhum produto cadastrado.")
        return
    }

    // Cabeçalho do relatório
    println()
    println("=".repeat(70))
    println("RELATÓRIO DE ESTOQUE")
    println("=".repeat(70))

    // Acumulador do valor total investido em estoque.
    //
    // Começa em zero e vai somando produto por produto.
    var valorTotal = java.math.BigDecimal.ZERO

    // Contador de unidades no depósito
    var totalUnidades = 0

    // Percorre cada produto
    lista.forEach { caixa ->

        // Calcula quanto vale o estoque desse produto:
        // preço de custo multiplicado pela quantidade.
        //
        // toBigDecimal() converte o Int para BigDecimal
        // porque não se multiplica Int com BigDecimal direto.
        val valorProduto = caixa.precoCusto
            .multiply(caixa.quantidadeEstoque.toBigDecimal())

        // Soma no acumulador geral
        valorTotal = valorTotal.add(valorProduto)

        // Soma as unidades no contador
        totalUnidades += caixa.quantidadeEstoque

        // Define um alerta visual para estoque baixo.
        //
        // when sem argumento funciona como uma sequência
        // de if/else if, testando condição por condição.
        val alerta = when {

            // Sem nenhuma unidade: alerta forte
            caixa.quantidadeEstoque == 0 -> "  <<< SEM ESTOQUE"

            // Até 3 unidades: alerta de reposição
            caixa.quantidadeEstoque <= 3 -> "  <<< ESTOQUE BAIXO"

            // Acima disso: nenhum alerta
            else -> ""
        }

        // Mostra a linha do relatório
        println("[${caixa.id}] ${caixa.marca} ${caixa.modelo} | " +
                "${caixa.quantidadeEstoque} un. | " +
                "Custo unit. R$ ${caixa.precoCusto} | " +
                "Total R$ $valorProduto$alerta")
    }

    // Rodapé com os totais
    println("-".repeat(70))
    println("TOTAL: $totalUnidades unidade(s) | " +
            "Valor imobilizado: R$ $valorTotal")
    println("=".repeat(70))
}
