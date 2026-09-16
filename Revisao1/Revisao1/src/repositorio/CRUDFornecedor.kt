// Define o pacote responsável por falar com o banco de dados
package repositorio

// Importa a classe Fornecedor
import pessoas.Fornecedor

// Importa ResultSet, o resultado de um SELECT
import java.sql.ResultSet

// Importa SQLException, o erro do banco
import java.sql.SQLException

// Importa Statement, usado para pedir o ID gerado
import java.sql.Statement


// Classe responsável pelo CRUD da tabela fornecedor.
//
// O fornecedor é a empresa que vende as caixas d'água
// para a nossa empresa (é de quem a gente COMPRA).
class CRUDFornecedor : InterfaceJPA<Fornecedor>, ConexaoPostgres() {


    // =========================================================
    // CREATE (SALVAR)
    // =========================================================

    // Cadastra um novo fornecedor
    override fun salvar(item: Fornecedor) {

        // Se não conectar, desiste
        if (!conectar()) return

        // Protege a operação
        try {

            // Comando SQL de inserção
            val sql = """
                INSERT INTO fornecedor
                (razao_social, cnpj, email, telefone)
                VALUES (?, ?, ?, ?)
            """.trimIndent()

            // Prepara o comando pedindo o ID gerado
            val stmt = c!!.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            // Preenche a razão social (o nome da empresa)
            stmt.setString(1, item.nome)

            // Preenche o CNPJ
            stmt.setString(2, item.documento)

            // Preenche o e-mail
            stmt.setString(3, item.email)

            // Preenche o telefone
            stmt.setString(4, item.telefone)

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
            println("[OK] Fornecedor cadastrado com o ID ${item.id}.")

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // O erro mais comum é CNPJ duplicado (UNIQUE)
            println("[ERRO] Não foi possível salvar: ${e.message}")

        } finally {

            // Fecha a conexão sempre
            desconectar()
        }
    }


    // =========================================================
    // READ (LISTAR NA TELA)
    // =========================================================

    // Mostra todos os fornecedores cadastrados
    override fun listar() {

        // Busca os fornecedores
        val lista = listarTodos()

        // Se não tem nenhum, avisa e sai
        if (lista.isEmpty()) {

            // Mensagem de tabela vazia
            println("Nenhum fornecedor cadastrado.")
            return
        }

        // Linha separadora
        println("-".repeat(70))

        // Mostra cada fornecedor usando o toString de Pessoa
        lista.forEach { fornecedor -> println(fornecedor) }

        // Linha separadora final
        println("-".repeat(70))
    }


    // =========================================================
    // READ (DEVOLVER OS OBJETOS)
    // =========================================================

    // Devolve todos os fornecedores como lista de objetos
    override fun listarTodos(): List<Fornecedor> {

        // Lista que será preenchida
        val lista = mutableListOf<Fornecedor>()

        // Se não conectar, devolve vazia
        if (!conectar()) return lista

        // Protege a operação
        try {

            // Busca tudo ordenado pela razão social
            val sql = "SELECT * FROM fornecedor ORDER BY razao_social"

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

    // Busca um fornecedor pelo ID
    override fun buscarPorId(id: Int): Fornecedor? {

        // Se não conectar, devolve null
        if (!conectar()) return null

        // Protege a operação
        try {

            // SQL com filtro pelo ID
            val sql = "SELECT * FROM fornecedor WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o ID procurado
            stmt.setInt(1, id)

            // Executa a consulta
            val rs = stmt.executeQuery()

            // Se achou, devolve o fornecedor
            if (rs.next()) {

                // Converte a linha em objeto
                val fornecedor = montarObjeto(rs)

                // Fecha o comando
                stmt.close()

                // Devolve o fornecedor encontrado
                return fornecedor
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

    // Altera os dados de um fornecedor
    override fun editar(item: Fornecedor, id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // Comando SQL de atualização com WHERE
            val sql = """
                UPDATE fornecedor SET
                razao_social = ?, email = ?, telefone = ?
                WHERE id = ?
            """.trimIndent()

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche a nova razão social
            stmt.setString(1, item.nome)

            // Preenche o novo e-mail
            stmt.setString(2, item.email)

            // Preenche o novo telefone
            stmt.setString(3, item.telefone)

            // Preenche o ID de qual fornecedor será alterado
            stmt.setInt(4, id)

            // Executa e guarda quantas linhas mudaram
            val linhas = stmt.executeUpdate()

            // Fecha o comando
            stmt.close()

            // Avisa o resultado
            if (linhas > 0) println("[OK] Fornecedor $id atualizado.")

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

    // Apaga um fornecedor do banco
    override fun excluir(id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // SQL de exclusão com WHERE
            val sql = "DELETE FROM fornecedor WHERE id = ?"

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
                println("[OK] Fornecedor $id excluído.")

            } else {

                // Não achou
                println("[AVISO] Nenhum fornecedor com o ID $id.")
            }

            // Devolve true se apagou algo
            return linhas > 0

        } catch (e: SQLException) {

            // Esse erro aparece quando ainda existem caixas
            // ligadas a esse fornecedor (chave estrangeira).
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

    // Converte uma linha do ResultSet em um objeto Fornecedor
    private fun montarObjeto(rs: ResultSet): Fornecedor {

        // Cria e devolve o fornecedor preenchido
        return Fornecedor(

            // Lê a razão social
            razaoSocial = rs.getString("razao_social"),

            // Lê o CNPJ
            cnpj = rs.getString("cnpj"),

            // Lê o e-mail
            email = rs.getString("email"),

            // Lê o telefone
            telefone = rs.getString("telefone"),

            // Lê o ID do registro
            idFornecedor = rs.getInt("id")
        )
    }
}
