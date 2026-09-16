// Define o pacote responsável por falar com o banco de dados
package repositorio

// Importa a classe Servico
import produto.Servico

// Importa ResultSet, o resultado de um SELECT
import java.sql.ResultSet

// Importa SQLException, o erro do banco
import java.sql.SQLException

// Importa Statement, usado para pedir o ID gerado
import java.sql.Statement


// Classe responsável pelo CRUD da tabela servico.
//
// O serviço é a INSTALAÇÃO da caixa d'água na casa do cliente.
// Ele liga cliente + instalador + caixa em um único registro.
class CRUDServico : InterfaceJPA<Servico>, ConexaoPostgres() {


    // =========================================================
    // CREATE (SALVAR)
    // =========================================================

    // Agenda um novo serviço de instalação
    override fun salvar(item: Servico) {

        // Se não conectar, desiste
        if (!conectar()) return

        // Protege a operação
        try {

            // Comando SQL de inserção
            val sql = """
                INSERT INTO servico
                (cliente_id, instalador_id, caixa_id,
                 data_instalacao, preco, status)
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

            // Prepara o comando pedindo o ID gerado
            val stmt = c!!.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            // Preenche o ID do cliente que contratou
            stmt.setInt(1, item.clienteId)

            // Preenche o ID do instalador responsável
            stmt.setInt(2, item.instaladorId)

            // Preenche o ID da caixa que será instalada
            stmt.setInt(3, item.caixaId)

            // Preenche a data da instalação.
            //
            // java.sql.Date.valueOf converte o LocalDate do Kotlin
            // no tipo date que o PostgreSQL entende.
            stmt.setDate(4, java.sql.Date.valueOf(item.dataInstalacao))

            // Preenche o preço da mão de obra
            stmt.setBigDecimal(5, item.preco)

            // Preenche a situação (AGENDADO, CONCLUIDO ou CANCELADO)
            stmt.setString(6, item.status)

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
            println("[OK] Serviço agendado com o ID ${item.id}.")

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // O erro mais comum aqui é informar um ID
            // de cliente, instalador ou caixa que não existe.
            println("[ERRO] Não foi possível agendar: ${e.message}")

        } finally {

            // Fecha a conexão sempre
            desconectar()
        }
    }


    // =========================================================
    // READ (LISTAR NA TELA)
    // =========================================================

    // Mostra todos os serviços com os NOMES em vez dos IDs.
    //
    // Aqui usamos JOIN para juntar quatro tabelas,
    // deixando o relatório legível para o usuário.
    override fun listar() {

        // Se não conectar, sai da função
        if (!conectar()) return

        // Protege a operação
        try {

            // Comando SQL com JOIN.
            //
            // JOIN liga a tabela servico com as tabelas
            // cliente, funcionario e caixa_da_agua,
            // usando as colunas de ID como ponte.
            val sql = """
                SELECT s.id, s.data_instalacao, s.preco, s.status,
                       cl.nome  AS cliente,
                       f.nome   AS instalador,
                       cx.marca AS marca, cx.modelo AS modelo
                FROM servico s
                JOIN cliente      cl ON cl.id = s.cliente_id
                JOIN funcionario  f  ON f.id  = s.instalador_id
                JOIN caixa_da_agua cx ON cx.id = s.caixa_id
                ORDER BY s.data_instalacao
            """.trimIndent()

            // Cria o comando
            val stmt = c!!.createStatement()

            // Executa a consulta
            val rs = stmt.executeQuery(sql)

            // Marca se encontrou pelo menos um serviço
            var achou = false

            // Percorre todas as linhas do resultado
            while (rs.next()) {

                // Marca que achou algo
                achou = true

                // Monta e mostra a linha do relatório
                println(
                    "[${rs.getInt("id")}] " +
                    "${rs.getDate("data_instalacao")} | " +
                    "Cliente: ${rs.getString("cliente")} | " +
                    "Instalador: ${rs.getString("instalador")} | " +
                    "Caixa: ${rs.getString("marca")} ${rs.getString("modelo")} | " +
                    "R$ ${rs.getBigDecimal("preco")} | " +
                    "${rs.getString("status")}"
                )
            }

            // Se nada foi encontrado, avisa
            if (!achou) println("Nenhum serviço agendado.")

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível listar: ${e.message}")

        } finally {

            // Fecha a conexão
            desconectar()
        }
    }


    // =========================================================
    // READ (DEVOLVER OS OBJETOS)
    // =========================================================

    // Devolve todos os serviços como lista de objetos
    override fun listarTodos(): List<Servico> {

        // Lista que será preenchida
        val lista = mutableListOf<Servico>()

        // Se não conectar, devolve vazia
        if (!conectar()) return lista

        // Protege a operação
        try {

            // Busca tudo ordenado pela data
            val sql = "SELECT * FROM servico ORDER BY data_instalacao"

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

    // Busca um serviço pelo ID
    override fun buscarPorId(id: Int): Servico? {

        // Se não conectar, devolve null
        if (!conectar()) return null

        // Protege a operação
        try {

            // SQL com filtro pelo ID
            val sql = "SELECT * FROM servico WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o ID procurado
            stmt.setInt(1, id)

            // Executa a consulta
            val rs = stmt.executeQuery()

            // Se achou, devolve o serviço
            if (rs.next()) {

                // Converte a linha em objeto
                val servico = montarObjeto(rs)

                // Fecha o comando
                stmt.close()

                // Devolve o serviço encontrado
                return servico
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

    // Altera a data, o preço e a situação de um serviço
    override fun editar(item: Servico, id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // Comando SQL de atualização com WHERE
            val sql = """
                UPDATE servico SET
                data_instalacao = ?, preco = ?, status = ?
                WHERE id = ?
            """.trimIndent()

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche a nova data
            stmt.setDate(1, java.sql.Date.valueOf(item.dataInstalacao))

            // Preenche o novo preço
            stmt.setBigDecimal(2, item.preco)

            // Preenche a nova situação
            stmt.setString(3, item.status)

            // Preenche o ID de qual serviço será alterado
            stmt.setInt(4, id)

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

    // Apaga um serviço do banco
    override fun excluir(id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // SQL de exclusão com WHERE
            val sql = "DELETE FROM servico WHERE id = ?"

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
                println("[OK] Serviço $id excluído.")

            } else {

                // Não achou
                println("[AVISO] Nenhum serviço com o ID $id.")
            }

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

    // Converte uma linha do ResultSet em um objeto Servico
    private fun montarObjeto(rs: ResultSet): Servico {

        // Cria e devolve o serviço preenchido
        return Servico(

            // Lê o ID do cliente
            clienteId = rs.getInt("cliente_id"),

            // Lê o ID do instalador
            instaladorId = rs.getInt("instalador_id"),

            // Lê o ID da caixa
            caixaId = rs.getInt("caixa_id"),

            // Lê a data e converte para LocalDate do Kotlin
            dataInstalacao = rs.getDate("data_instalacao").toLocalDate(),

            // Lê o preço preservando os centavos
            preco = rs.getBigDecimal("preco"),

            // Lê a situação do serviço
            statusInicial = rs.getString("status"),

            // Lê o ID do registro
            id = rs.getInt("id")
        )
    }
}
