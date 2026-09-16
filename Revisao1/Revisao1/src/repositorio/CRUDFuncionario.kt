// Define o pacote responsável por falar com o banco de dados
package repositorio

// Importa os enums usados nas colunas do funcionário
import enumeradores.Habilidade
import enumeradores.Setor
import enumeradores.Turno

// Importa a classe Funcionario
import pessoas.Funcionario

// Importa ResultSet, o resultado de um SELECT
import java.sql.ResultSet

// Importa SQLException, o erro do banco
import java.sql.SQLException

// Importa Statement, usado para pedir o ID gerado
import java.sql.Statement


// Classe responsável pelo CRUD da tabela funcionario.
//
// É esta classe que atende o requisito de "gerenciar as
// pessoas envolvidas no negócio" e de "dividir os
// funcionários em setores".
class CRUDFuncionario : InterfaceJPA<Funcionario>, ConexaoPostgres() {


    // =========================================================
    // CREATE (SALVAR)
    // =========================================================

    // Cadastra um novo funcionário
    override fun salvar(item: Funcionario) {

        // Se não conectar, desiste
        if (!conectar()) return

        // Protege a operação
        try {

            // Comando SQL de inserção
            val sql = """
                INSERT INTO funcionario
                (nome, cpf, idade, email, telefone,
                 salario, setor, turno, habilidade)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            // Prepara o comando pedindo o ID gerado de volta
            val stmt = c!!.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            // Preenche o nome
            stmt.setString(1, item.nome)

            // Preenche o CPF (já validado pelo Validador)
            stmt.setString(2, item.documento)

            // Preenche a idade
            stmt.setInt(3, item.idade)

            // Preenche o e-mail
            stmt.setString(4, item.email)

            // Preenche o telefone
            stmt.setString(5, item.telefone)

            // Preenche o salário preservando os centavos
            stmt.setBigDecimal(6, item.salario)

            // Preenche o setor gravando o NOME da constante do enum
            stmt.setString(7, item.setor.name)

            // Preenche o turno
            stmt.setString(8, item.turno.name)

            // Preenche a habilidade
            stmt.setString(9, item.habilidade.name)

            // Executa o INSERT
            stmt.executeUpdate()

            // Pega o ID gerado automaticamente pelo banco
            val chaves = stmt.generatedKeys

            // Se veio resultado, guarda o ID no objeto
            if (chaves.next()) {

                // Lê a primeira coluna, que é o ID
                item.id = chaves.getInt(1)
            }

            // Confirma para o usuário
            println("[OK] Funcionário cadastrado com o ID ${item.id}.")

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // O erro mais comum aqui é CPF duplicado,
            // barrado pela restrição UNIQUE da tabela.
            println("[ERRO] Não foi possível salvar: ${e.message}")

        } finally {

            // Fecha a conexão sempre
            desconectar()
        }
    }


    // =========================================================
    // READ (LISTAR NA TELA)
    // =========================================================

    // Mostra todos os funcionários cadastrados
    override fun listar() {

        // Busca os funcionários
        val lista = listarTodos()

        // Se não tem ninguém, avisa e sai
        if (lista.isEmpty()) {

            // Mensagem de tabela vazia
            println("Nenhum funcionário cadastrado.")
            return
        }

        // Linha separadora
        println("-".repeat(70))

        // Mostra cada funcionário com o salário e o turno
        lista.forEach { funcionario ->

            // O toString() herdado de Pessoa já mostra
            // nome, papel, documento, e-mail e telefone.
            println("$funcionario | Salário: R$ ${funcionario.salario} " +
                    "| Turno: ${funcionario.turno}")
        }

        // Linha separadora final
        println("-".repeat(70))
    }


    // Mostra os funcionários AGRUPADOS por setor.
    //
    // Atende diretamente ao requisito de dividir
    // os funcionários em setores.
    fun listarPorSetor() {

        // Busca todos os funcionários
        val lista = listarTodos()

        // Se não tem ninguém, avisa e sai
        if (lista.isEmpty()) {

            // Mensagem de tabela vazia
            println("Nenhum funcionário cadastrado.")
            return
        }

        // groupBy monta um mapa: cada setor vira uma chave
        // e o valor é a lista de funcionários daquele setor.
        val porSetor = lista.groupBy { funcionario -> funcionario.setor }

        // Percorre cada setor existente no enum,
        // assim a ordem do relatório fica sempre igual.
        Setor.entries.forEach { setor ->

            // Pega a lista daquele setor.
            //
            // "?: emptyList()" usa uma lista vazia quando
            // nenhum funcionário pertence a esse setor.
            val funcionarios = porSetor[setor] ?: emptyList()

            // Cabeçalho do setor com a contagem de pessoas
            println()
            println("### SETOR ${setor.descricao.uppercase()} " +
                    "(${funcionarios.size} funcionário(s))")

            // Informa se o setor pode aprovar movimentações
            println("    Autoriza caixa: ${if (setor.podeAprovar) "SIM" else "NÃO"}")

            // Se o setor está vazio, avisa
            if (funcionarios.isEmpty()) {

                // Mensagem de setor sem gente
                println("    (nenhum funcionário neste setor)")

            } else {

                // Mostra cada funcionário do setor
                funcionarios.forEach { funcionario ->

                    // Linha com nome, habilidade e salário
                    println("    - ${funcionario.nome} " +
                            "| ${funcionario.habilidade} " +
                            "| R$ ${funcionario.salario}")
                }
            }
        }

        // Linha em branco no final
        println()
    }


    // =========================================================
    // READ (DEVOLVER OS OBJETOS)
    // =========================================================

    // Devolve todos os funcionários como lista de objetos
    override fun listarTodos(): List<Funcionario> {

        // Lista que será preenchida
        val lista = mutableListOf<Funcionario>()

        // Se não conectar, devolve vazia
        if (!conectar()) return lista

        // Protege a operação
        try {

            // Busca tudo ordenado por setor e depois por nome
            val sql = "SELECT * FROM funcionario ORDER BY setor, nome"

            // Cria o comando
            val stmt = c!!.createStatement()

            // Executa a consulta
            val rs = stmt.executeQuery(sql)

            // Percorre todas as linhas
            while (rs.next()) {

                // Converte cada linha em objeto
                lista.add(montarObjeto(rs))
            }

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível listar: ${e.message}")

        } finally {

            // Fecha a conexão
            desconectar()
        }

        // Devolve a lista
        return lista
    }


    // =========================================================
    // READ (BUSCAR UM ÚNICO REGISTRO)
    // =========================================================

    // Busca um funcionário pelo ID
    override fun buscarPorId(id: Int): Funcionario? {

        // Se não conectar, devolve null
        if (!conectar()) return null

        // Protege a operação
        try {

            // SQL com filtro pelo ID
            val sql = "SELECT * FROM funcionario WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o ID procurado
            stmt.setInt(1, id)

            // Executa a consulta
            val rs = stmt.executeQuery()

            // Se achou, devolve o objeto montado
            if (rs.next()) {

                // Converte a linha em objeto
                val funcionario = montarObjeto(rs)

                // Fecha o comando
                stmt.close()

                // Devolve o funcionário encontrado
                return funcionario
            }

            // Não achou: fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível buscar: ${e.message}")

        } finally {

            // Fecha a conexão
            desconectar()
        }

        // Não encontrou nada
        return null
    }


    // =========================================================
    // UPDATE (EDITAR)
    // =========================================================

    // Altera os dados de um funcionário
    override fun editar(item: Funcionario, id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // Comando SQL de atualização, sempre com WHERE
            val sql = """
                UPDATE funcionario SET
                nome = ?, email = ?, telefone = ?,
                salario = ?, setor = ?, turno = ?, habilidade = ?
                WHERE id = ?
            """.trimIndent()

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o novo nome
            stmt.setString(1, item.nome)

            // Preenche o novo e-mail
            stmt.setString(2, item.email)

            // Preenche o novo telefone
            stmt.setString(3, item.telefone)

            // Preenche o novo salário
            stmt.setBigDecimal(4, item.salario)

            // Preenche o novo setor
            stmt.setString(5, item.setor.name)

            // Preenche o novo turno
            stmt.setString(6, item.turno.name)

            // Preenche a nova habilidade
            stmt.setString(7, item.habilidade.name)

            // Preenche o ID de qual funcionário será alterado
            stmt.setInt(8, id)

            // Executa e guarda quantas linhas foram alteradas
            val linhas = stmt.executeUpdate()

            // Fecha o comando
            stmt.close()

            // Avisa o resultado
            if (linhas > 0) {

                // Deu certo
                println("[OK] Funcionário $id atualizado.")

            } else {

                // Não achou o registro
                println("[AVISO] Nenhum funcionário com o ID $id.")
            }

            // Devolve true se alterou alguma linha
            return linhas > 0

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível editar: ${e.message}")

            // Informa a falha
            return false

        } finally {

            // Fecha a conexão
            desconectar()
        }
    }


    // =========================================================
    // DELETE (EXCLUIR)
    // =========================================================

    // Apaga um funcionário do banco
    override fun excluir(id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // SQL de exclusão com WHERE
            val sql = "DELETE FROM funcionario WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o ID que será apagado
            stmt.setInt(1, id)

            // Executa e guarda quantas linhas foram apagadas
            val linhas = stmt.executeUpdate()

            // Fecha o comando
            stmt.close()

            // Avisa o resultado
            if (linhas > 0) {

                // Apagou
                println("[OK] Funcionário $id excluído.")

            } else {

                // Não achou
                println("[AVISO] Nenhum funcionário com o ID $id.")
            }

            // Devolve true se apagou algo
            return linhas > 0

        } catch (e: SQLException) {

            // Esse erro aparece quando o funcionário ainda
            // está ligado a um serviço (chave estrangeira).
            println("[ERRO] Não foi possível excluir: ${e.message}")

            // Informa a falha
            return false

        } finally {

            // Fecha a conexão
            desconectar()
        }
    }


    // =========================================================
    // FUNÇÃO AUXILIAR
    // =========================================================

    // Converte uma linha do ResultSet em um objeto Funcionario
    private fun montarObjeto(rs: ResultSet): Funcionario {

        // Cria e devolve o funcionário preenchido
        return Funcionario(

            // Lê o nome
            nome = rs.getString("nome"),

            // Lê o CPF
            documento = rs.getString("cpf"),

            // Lê a idade
            idade = rs.getInt("idade"),

            // Lê o e-mail
            email = rs.getString("email"),

            // Lê o telefone
            telefone = rs.getString("telefone"),

            // Lê o salário preservando os centavos
            salario = rs.getBigDecimal("salario"),

            // Converte o texto do banco de volta para o enum Setor
            setor = Setor.valueOf(rs.getString("setor")),

            // Converte o texto de volta para o enum Turno
            turno = Turno.valueOf(rs.getString("turno")),

            // Converte o texto de volta para o enum Habilidade
            habilidade = Habilidade.valueOf(rs.getString("habilidade")),

            // Lê o ID do registro
            idFuncionario = rs.getInt("id")
        )
    }
}
