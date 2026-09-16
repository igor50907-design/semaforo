// Define o pacote da camada de apresentação (os menus)
package sistema

// Importa os enums usados no cadastro de funcionário
import enumeradores.Habilidade
import enumeradores.Setor
import enumeradores.Turno

// Importa as classes de pessoas
import pessoas.Cliente
import pessoas.Fornecedor
import pessoas.Funcionario

// Importa os CRUDs de pessoas
import repositorio.CRUDCliente
import repositorio.CRUDFornecedor
import repositorio.CRUDFuncionario

// Importa o Validador, que garante dados corretos
import validacao.Validador


// Menu que gerencia TODAS as pessoas envolvidas no negócio:
// funcionários (divididos em setores), clientes e fornecedores.
//
// Atende ao requisito "ajudar a gerenciar as pessoas
// envolvidas no negócio".
fun menuPessoas() {

    // do/while repete o menu até o usuário escolher sair
    do {

        // Cabeçalho do menu
        println()
        println("=".repeat(60))
        println("GESTÃO DE PESSOAS")
        println("=".repeat(60))

        // Opções de funcionários
        println("--- FUNCIONÁRIOS ---")
        println(" 1 - Cadastrar funcionário")
        println(" 2 - Listar funcionários")
        println(" 3 - Listar funcionários POR SETOR")
        println(" 4 - Editar funcionário")
        println(" 5 - Excluir funcionário")

        // Opções de clientes
        println("--- CLIENTES ---")
        println(" 6 - Cadastrar cliente")
        println(" 7 - Listar clientes")
        println(" 8 - Editar cliente")
        println(" 9 - Excluir cliente")

        // Opções de fornecedores
        println("--- FORNECEDORES ---")
        println("10 - Cadastrar fornecedor")
        println("11 - Listar fornecedores")
        println("12 - Editar fornecedor")
        println("13 - Excluir fornecedor")

        // Opção de voltar
        println(" 0 - Voltar ao menu principal")

        // Lê a opção de forma segura, aceitando só de 0 a 13
        val opcao = Validador.lerInteiro("Opção", 0, 13)

        // when funciona como um switch, escolhendo o que executar
        when (opcao) {

            // 1: cadastrar um funcionário novo
            1 -> cadastrarFuncionario()

            // 2: listar todos os funcionários
            2 -> CRUDFuncionario().listar()

            // 3: mostrar os funcionários agrupados por setor
            3 -> CRUDFuncionario().listarPorSetor()

            // 4: editar um funcionário existente
            4 -> editarFuncionario()

            // 5: excluir um funcionário
            5 -> excluirFuncionario()

            // 6: cadastrar um cliente novo
            6 -> cadastrarCliente()

            // 7: listar todos os clientes
            7 -> CRUDCliente().listar()

            // 8: editar um cliente existente
            8 -> editarCliente()

            // 9: excluir um cliente
            9 -> excluirCliente()

            // 10: cadastrar um fornecedor novo
            10 -> cadastrarFornecedor()

            // 11: listar todos os fornecedores
            11 -> CRUDFornecedor().listar()

            // 12: editar um fornecedor existente
            12 -> editarFornecedor()

            // 13: excluir um fornecedor
            13 -> excluirFornecedor()

            // 0: sai do laço e volta ao menu principal
            0 -> return
        }

    // Repete para sempre; a saída acontece pelo return acima
    } while (true)
}


// =============================================================
// FUNCIONÁRIOS
// =============================================================

// Cadastra um funcionário novo pedindo os dados ao usuário
fun cadastrarFuncionario() {

    // Título da operação
    println()
    println("--- NOVO FUNCIONÁRIO ---")

    // Lê o nome validado por REGEX (só letras e espaços)
    val nome = Validador.lerNome("Nome do funcionário")

    // Lê o CPF validado por REGEX e por dígito verificador
    val cpf = Validador.lerCpf()

    // Lê a idade limitada entre 16 e 90 anos.
    //
    // 16 é a idade mínima legal para trabalhar no Brasil.
    val idade = Validador.lerInteiro("Idade", 16, 90)

    // Lê o e-mail validado por REGEX
    val email = Validador.lerEmail()

    // Lê o telefone validado por REGEX
    val telefone = Validador.lerTelefone()

    // Lê o salário como BigDecimal, recusando zero e negativo
    val salario = Validador.lerDinheiro("Salário mensal")

    // Mostra o menu de setores montado a partir do enum.
    //
    // Setor.entries devolve todas as constantes do enum,
    // e toTypedArray() converte para o formato que a função espera.
    val setor = Validador.lerEnum("Setor", Setor.entries.toTypedArray())

    // Mostra o menu de turnos montado a partir do enum
    val turno = Validador.lerEnum("Turno", Turno.entries.toTypedArray())

    // Mostra o menu de habilidades montado a partir do enum
    val habilidade = Validador.lerEnum(
        "Habilidade",
        Habilidade.entries.toTypedArray()
    )

    // Monta o objeto Funcionario com os dados validados
    val funcionario = Funcionario(
        nome = nome,
        documento = cpf,
        idade = idade,
        email = email,
        telefone = telefone,
        salario = salario,
        setor = setor,
        turno = turno,
        habilidade = habilidade
    )

    // Manda o CRUD gravar no banco de dados
    CRUDFuncionario().salvar(funcionario)
}


// Edita os dados de um funcionário já cadastrado
fun editarFuncionario() {

    // Cria o CRUD que será usado
    val crud = CRUDFuncionario()

    // Mostra a lista para o usuário saber qual ID escolher
    crud.listar()

    // Lê o ID, aceitando apenas números positivos
    val id = Validador.lerInteiro("ID do funcionário a editar", 1)

    // Busca o funcionário no banco.
    //
    // O retorno é NULLABLE, por isso precisa ser verificado.
    val atual = crud.buscarPorId(id)

    // Se não encontrou, avisa e sai da função
    if (atual == null) {

        // Mensagem de registro inexistente
        println("[AVISO] Nenhum funcionário com o ID $id.")
        return
    }

    // Mostra os dados atuais antes de alterar
    println("Editando: ${atual.nome}")

    // Lê os novos dados
    val nome = Validador.lerNome("Novo nome")
    val email = Validador.lerEmail("Novo e-mail")
    val telefone = Validador.lerTelefone("Novo telefone")
    val salario = Validador.lerDinheiro("Novo salário")
    val setor = Validador.lerEnum("Novo setor", Setor.entries.toTypedArray())
    val turno = Validador.lerEnum("Novo turno", Turno.entries.toTypedArray())
    val habilidade = Validador.lerEnum(
        "Nova habilidade",
        Habilidade.entries.toTypedArray()
    )

    // Monta o objeto com os dados novos.
    //
    // O CPF e a idade são mantidos: são dados que
    // não mudam e servem para identificar a pessoa.
    val novo = Funcionario(
        nome = nome,
        documento = atual.documento,
        idade = atual.idade,
        email = email,
        telefone = telefone,
        salario = salario,
        setor = setor,
        turno = turno,
        habilidade = habilidade,
        idFuncionario = id
    )

    // Manda o CRUD atualizar no banco
    crud.editar(novo, id)
}


// Exclui um funcionário do sistema
fun excluirFuncionario() {

    // Cria o CRUD
    val crud = CRUDFuncionario()

    // Mostra a lista para o usuário escolher
    crud.listar()

    // Lê o ID a excluir
    val id = Validador.lerInteiro("ID do funcionário a excluir", 1)

    // Busca o funcionário para confirmar que ele existe
    val funcionario = crud.buscarPorId(id)

    // Se não encontrou, avisa e sai
    if (funcionario == null) {

        // Mensagem de registro inexistente
        println("[AVISO] Nenhum funcionário com o ID $id.")
        return
    }

    // Pede confirmação antes de apagar.
    //
    // Essa pergunta é uma trava contra a falha humana:
    // impede que o usuário apague alguém por engano.
    if (Validador.confirmar("Excluir '${funcionario.nome}' mesmo")) {

        // Confirmou: apaga
        crud.excluir(id)

    } else {

        // Desistiu: avisa que nada foi feito
        println("Operação cancelada.")
    }
}


// =============================================================
// CLIENTES
// =============================================================

// Cadastra um cliente novo
fun cadastrarCliente() {

    // Título da operação
    println()
    println("--- NOVO CLIENTE ---")

    // Lê o nome validado por REGEX
    val nome = Validador.lerNome("Nome do cliente")

    // Lê o CPF validado por REGEX e dígito verificador
    val cpf = Validador.lerCpf()

    // Lê a idade limitada entre 18 e 120.
    //
    // 18 porque menor de idade não assina contrato.
    val idade = Validador.lerInteiro("Idade", 18, 120)

    // Lê o e-mail validado por REGEX
    val email = Validador.lerEmail()

    // Lê o telefone validado por REGEX
    val telefone = Validador.lerTelefone()

    // Monta o objeto Cliente sem nenhuma parcela em aberto
    val cliente = Cliente(
        nome = nome,
        documento = cpf,
        idade = idade,
        email = email,
        telefone = telefone
    )

    // Manda o CRUD gravar no banco
    CRUDCliente().salvar(cliente)
}


// Edita os dados de um cliente já cadastrado
fun editarCliente() {

    // Cria o CRUD
    val crud = CRUDCliente()

    // Mostra a lista de clientes
    crud.listar()

    // Lê o ID do cliente
    val id = Validador.lerInteiro("ID do cliente a editar", 1)

    // Busca o cliente no banco
    val atual = crud.buscarPorId(id)

    // Se não encontrou, avisa e sai
    if (atual == null) {

        // Mensagem de registro inexistente
        println("[AVISO] Nenhum cliente com o ID $id.")
        return
    }

    // Mostra quem está sendo editado
    println("Editando: ${atual.nome}")

    // Lê os novos dados de contato
    val nome = Validador.lerNome("Novo nome")
    val email = Validador.lerEmail("Novo e-mail")
    val telefone = Validador.lerTelefone("Novo telefone")

    // Monta o objeto novo preservando CPF, idade e parcelas.
    //
    // As parcelas NÃO são editadas aqui de propósito:
    // dívida só muda por venda ou por recebimento,
    // nunca por digitação manual. Isso protege o caixa.
    val novo = Cliente(
        nome = nome,
        documento = atual.documento,
        idade = atual.idade,
        email = email,
        telefone = telefone,
        parcelasAPagar = atual.listarParcelas().toMutableList(),
        idCliente = id
    )

    // Manda o CRUD atualizar
    crud.editar(novo, id)

    // Confirma para o usuário
    println("[OK] Cliente $id atualizado.")
}


// Exclui um cliente do sistema
fun excluirCliente() {

    // Cria o CRUD
    val crud = CRUDCliente()

    // Mostra a lista de clientes
    crud.listar()

    // Lê o ID do cliente a excluir
    val id = Validador.lerInteiro("ID do cliente a excluir", 1)

    // Pede confirmação antes de apagar
    if (Validador.confirmar("Confirma a exclusão do cliente $id")) {

        // O próprio CRUD bloqueia a exclusão de quem deve
        crud.excluir(id)

    } else {

        // Desistiu: avisa
        println("Operação cancelada.")
    }
}


// =============================================================
// FORNECEDORES
// =============================================================

// Cadastra um fornecedor novo
fun cadastrarFornecedor() {

    // Título da operação
    println()
    println("--- NOVO FORNECEDOR ---")

    // Lê a razão social.
    //
    // Aqui usamos lerTexto (e não lerNome) porque nome
    // de empresa pode ter número e símbolos, como "Caixas 3M Ltda".
    val razaoSocial = Validador.lerTexto("Razão social", 3)

    // Lê o CNPJ validado por REGEX (14 dígitos)
    val cnpj = Validador.lerCnpj()

    // Lê o e-mail validado por REGEX
    val email = Validador.lerEmail()

    // Lê o telefone validado por REGEX
    val telefone = Validador.lerTelefone()

    // Monta o objeto Fornecedor
    val fornecedor = Fornecedor(
        razaoSocial = razaoSocial,
        cnpj = cnpj,
        email = email,
        telefone = telefone
    )

    // Manda o CRUD gravar no banco
    CRUDFornecedor().salvar(fornecedor)
}


// Edita os dados de um fornecedor
fun editarFornecedor() {

    // Cria o CRUD
    val crud = CRUDFornecedor()

    // Mostra a lista de fornecedores
    crud.listar()

    // Lê o ID do fornecedor
    val id = Validador.lerInteiro("ID do fornecedor a editar", 1)

    // Busca o fornecedor no banco
    val atual = crud.buscarPorId(id)

    // Se não encontrou, avisa e sai
    if (atual == null) {

        // Mensagem de registro inexistente
        println("[AVISO] Nenhum fornecedor com o ID $id.")
        return
    }

    // Mostra quem está sendo editado
    println("Editando: ${atual.nome}")

    // Lê os novos dados
    val razaoSocial = Validador.lerTexto("Nova razão social", 3)
    val email = Validador.lerEmail("Novo e-mail")
    val telefone = Validador.lerTelefone("Novo telefone")

    // Monta o objeto novo preservando o CNPJ
    val novo = Fornecedor(
        razaoSocial = razaoSocial,
        cnpj = atual.documento,
        email = email,
        telefone = telefone,
        idFornecedor = id
    )

    // Manda o CRUD atualizar
    crud.editar(novo, id)
}


// Exclui um fornecedor do sistema
fun excluirFornecedor() {

    // Cria o CRUD
    val crud = CRUDFornecedor()

    // Mostra a lista de fornecedores
    crud.listar()

    // Lê o ID a excluir
    val id = Validador.lerInteiro("ID do fornecedor a excluir", 1)

    // Pede confirmação antes de apagar
    if (Validador.confirmar("Confirma a exclusão do fornecedor $id")) {

        // Confirmou: apaga
        crud.excluir(id)

    } else {

        // Desistiu: avisa
        println("Operação cancelada.")
    }
}
