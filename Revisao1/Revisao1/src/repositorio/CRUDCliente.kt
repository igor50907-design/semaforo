// Define o pacote responsável por falar com o banco de dados
package repositorio

// Importa a classe Cliente
import pessoas.Cliente

// Importa BigDecimal, usado nas parcelas
import java.math.BigDecimal

// Importa ResultSet, o resultado de um SELECT
import java.sql.ResultSet

// Importa SQLException, o erro do banco
import java.sql.SQLException

// Importa Statement, usado para pedir o ID gerado
import java.sql.Statement


// Classe responsável pelo CRUD da tabela cliente.
//
// Além dos dados pessoais, ela guarda as parcelas
// que o cliente ainda deve, em uma coluna do tipo
// numeric[] (array de números) do PostgreSQL.
class CRUDCliente : InterfaceJPA<Cliente>, ConexaoPostgres() {


    // =========================================================
    // CREATE (SALVAR)
    // =========================================================

    // Cadastra um novo cliente
    override fun salvar(item: Cliente) {

        // Se não conectar, desiste
        if (!conectar()) return

        // Protege a operação
        try {

            // Comando SQL de inserção
            val sql = """
                INSERT INTO cliente
                (nome, cpf, idade, email, telefone, parcelas_a_pagar)
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

            // Prepara o comando pedindo o ID gerado
            val stmt = c!!.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            // Preenche o nome
            stmt.setString(1, item.nome)

            // Preenche o CPF
            stmt.setString(2, item.documento)

            // Preenche a idade
            stmt.setInt(3, item.idade)

            // Preenche o e-mail
            stmt.setString(4, item.email)

            // Preenche o telefone
            stmt.setString(5, item.telefone)

            // Monta o array de parcelas para o PostgreSQL.
            //
            // "numeric" é o tipo do banco equivalente ao BigDecimal.
            val arrayParcelas = c!!.createArrayOf(
                "numeric",
                item.listarParcelas().toTypedArray()
            )

            // Preenche a coluna de parcelas
            stmt.setArray(6, arrayParcelas)

            // Executa o INSERT
            stmt.executeUpdate()

            // Pega o ID gerado pelo banco
            val chaves = stmt.generatedKeys

            // Se veio resultado, guarda o ID no objeto
            if (chaves.next()) {

                // Lê a primeira coluna, que é o ID
                item.id = chaves.getInt(1)
            }

            // Confirma para o usuário
            println("[OK] Cliente cadastrado com o ID ${item.id}.")

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // O erro mais comum é CPF já cadastrado (UNIQUE)
            println("[ERRO] Não foi possível salvar: ${e.message}")

        } finally {

            // Fecha a conexão sempre
            desconectar()
        }
    }


    // =========================================================
    // READ (LISTAR NA TELA)
    // =========================================================

    // Mostra todos os clientes cadastrados
    override fun listar() {

        // Busca os clientes
        val lista = listarTodos()

        // Se não tem ninguém, avisa e sai
        if (lista.isEmpty()) {

            // Mensagem de tabela vazia
            println("Nenhum cliente cadastrado.")
            return
        }

        // Linha separadora
        println("-".repeat(70))

        // Mostra cada cliente com a situação financeira
        lista.forEach { cliente ->

            // Define o texto da situação conforme as dívidas
            val situacao =
                if (cliente.dividasAbertas)
                    "DEVENDO R$ ${cliente.totalDevido} " +
                    "(${cliente.listarParcelas().size} parcela(s))"
                else
                    "EM DIA"

            // Mostra o cliente e a situação
            println("$cliente | $situacao")
        }

        // Linha separadora final
        println("-".repeat(70))
    }


    // =========================================================
    // READ (DEVOLVER OS OBJETOS)
    // =========================================================

    // Devolve todos os clientes como lista de objetos
    override fun listarTodos(): List<Cliente> {

        // Lista que será preenchida
        val lista = mutableListOf<Cliente>()

        // Se não conectar, devolve vazia
        if (!conectar()) return lista

        // Protege a operação
        try {

            // Busca tudo ordenado pelo nome
            val sql = "SELECT * FROM cliente ORDER BY nome"

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

    // Busca um cliente pelo ID
    override fun buscarPorId(id: Int): Cliente? {

        // Se não conectar, devolve null
        if (!conectar()) return null

        // Protege a operação
        try {

            // SQL com filtro pelo ID
            val sql = "SELECT * FROM cliente WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o ID procurado
            stmt.setInt(1, id)

            // Executa a consulta
            val rs = stmt.executeQuery()

            // Se achou, devolve o cliente
            if (rs.next()) {

                // Converte a linha em objeto
                val cliente = montarObjeto(rs)

                // Fecha o comando
                stmt.close()

                // Devolve o cliente encontrado
                return cliente
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

    // Altera os dados de um cliente, incluindo as parcelas
    override fun editar(item: Cliente, id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // Comando SQL de atualização com WHERE
            val sql = """
                UPDATE cliente SET
                nome = ?, email = ?, telefone = ?, parcelas_a_pagar = ?
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

            // Monta o array atualizado de parcelas
            val arrayParcelas = c!!.createArrayOf(
                "numeric",
                item.listarParcelas().toTypedArray()
            )

            // Preenche a coluna de parcelas
            stmt.setArray(4, arrayParcelas)

            // Preenche o ID de qual cliente será alterado
            stmt.setInt(5, id)

            // Executa e guarda quantas linhas mudaram
            val linhas = stmt.executeUpdate()

            // Fecha o comando
            stmt.close()

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

    // Apaga um cliente do banco
    override fun excluir(id: Int): Boolean {

        // Antes de apagar, verifica se o cliente deve algo.
        //
        // Essa é uma regra de negócio: não se apaga
        // um cliente que ainda tem parcelas em aberto.
        val cliente = buscarPorId(id)

        // Se não encontrou o cliente, avisa e sai
        if (cliente == null) {

            // Mensagem de registro inexistente
            println("[AVISO] Nenhum cliente com o ID $id.")
            return false
        }

        // Se ainda tem dívidas, bloqueia a exclusão
        if (cliente.dividasAbertas) {

            // Explica o motivo da recusa
            println("[BLOQUEADO] O cliente ${cliente.nome} ainda deve " +
                    "R$ ${cliente.totalDevido}. Quite as parcelas primeiro.")

            // Informa que nada foi apagado
            return false
        }

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // SQL de exclusão com WHERE
            val sql = "DELETE FROM cliente WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o ID que será apagado
            stmt.setInt(1, id)

            // Executa e guarda quantas linhas foram apagadas
            val linhas = stmt.executeUpdate()

            // Fecha o comando
            stmt.close()

            // Avisa o resultado
            if (linhas > 0) println("[OK] Cliente $id excluído.")

            // Devolve true se apagou algo
            return linhas > 0

        } catch (e: SQLException) {

            // Mostra o erro
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

    // Converte uma linha do ResultSet em um objeto Cliente
    private fun montarObjeto(rs: ResultSet): Cliente {

        // Lê a coluna com o array de parcelas
        val arrayBanco = rs.getArray("parcelas_a_pagar")

        // Converte o array do banco em MutableList<BigDecimal>.
        //
        // "as? Array<*>" é um cast SEGURO: devolve null
        // em vez de quebrar quando a conversão não é possível.
        val parcelas = (arrayBanco?.array as? Array<*>)
            ?.map { it as BigDecimal }
            ?.toMutableList()
            ?: mutableListOf()

        // Cria e devolve o cliente preenchido
        return Cliente(

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

            // Usa a lista de parcelas montada acima
            parcelasAPagar = parcelas,

            // Lê o ID do registro
            idCliente = rs.getInt("id")
        )
    }
}
