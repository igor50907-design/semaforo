// Define o pacote responsável por falar com o banco de dados
package repositorio

// Importa o enum que classifica a movimentação
import enumeradores.TipoMovimentacao

// Importa a classe de movimentação financeira
import financeiro.Movimentacao

// Importa BigDecimal, usado nos valores
import java.math.BigDecimal

// Importa ResultSet, o resultado de um SELECT
import java.sql.ResultSet

// Importa SQLException, o erro do banco
import java.sql.SQLException

// Importa Statement, usado para pedir o ID gerado
import java.sql.Statement

// Importa Timestamp, o tipo do banco que guarda DATA + HORA
import java.sql.Timestamp


// Classe responsável pelo CRUD da tabela movimentacao.
//
// Esta é a tabela mais importante do trabalho: é ela que
// guarda quanto, quem pagou, quem recebeu, quando,
// por qual motivo e quem foi o responsável.
//
// Repare que aqui NÃO existe editar nem excluir de verdade:
// lançamento financeiro é IMUTÁVEL por regra contábil.
class CRUDMovimentacao : InterfaceJPA<Movimentacao>, ConexaoPostgres() {


    // =========================================================
    // CREATE (SALVAR)
    // =========================================================

    // Salva a movimentação sem devolver o ID.
    //
    // Existe apenas para cumprir o contrato da interface.
    override fun salvar(item: Movimentacao) {

        // Reaproveita a função abaixo, ignorando o retorno
        salvarRetornandoId(item)
    }


    // Salva a movimentação e devolve o ID gerado pelo banco.
    //
    // O retorno "Int?" é NULLABLE: devolve null quando
    // a gravação falhar. O Caixa usa esse null para saber
    // que NÃO deve atualizar o saldo em memória.
    fun salvarRetornandoId(item: Movimentacao): Int? {

        // Se não conseguir conectar, devolve null
        if (!conectar()) return null

        // Protege a operação
        try {

            // Comando SQL de inserção com as 6 informações exigidas
            val sql = """
                INSERT INTO movimentacao
                (valor, pagador, recebedor, data_hora,
                 motivo, responsavel, tipo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            // Prepara o comando pedindo o ID gerado de volta
            val stmt = c!!.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            // 1º ? -> QUANTO dinheiro foi usado.
            //
            // setBigDecimal preserva os centavos com exatidão.
            stmt.setBigDecimal(1, item.valor)

            // 2º ? -> QUEM PAGOU
            stmt.setString(2, item.pagador)

            // 3º ? -> QUEM RECEBEU
            stmt.setString(3, item.recebedor)

            // 4º ? -> A DATA E A HORA.
            //
            // Timestamp.valueOf converte o LocalDateTime do Kotlin
            // no tipo timestamp que o PostgreSQL entende.
            stmt.setTimestamp(4, Timestamp.valueOf(item.dataHora))

            // 5º ? -> O MOTIVO/DESCRIÇÃO
            stmt.setString(5, item.motivo)

            // 6º ? -> O RESPONSÁVEL pela transação
            stmt.setString(6, item.responsavel)

            // 7º ? -> A classificação da operação.
            //
            // .name grava o nome da constante do enum como texto.
            stmt.setString(7, item.tipo.name)

            // Executa o INSERT
            stmt.executeUpdate()

            // Pega as chaves geradas automaticamente
            val chaves = stmt.generatedKeys

            // Variável que vai guardar o ID.
            //
            // Começa como null e só recebe valor se o banco devolver.
            var idGerado: Int? = null

            // Se existe uma linha de resultado
            if (chaves.next()) {

                // Lê a primeira coluna, que é o ID
                idGerado = chaves.getInt(1)
            }

            // Fecha o comando
            stmt.close()

            // Devolve o ID (ou null, se o banco não devolveu)
            return idGerado

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível gravar a movimentação: ${e.message}")

            // Devolve null para o Caixa saber que falhou
            return null

        } finally {

            // Fecha a conexão sempre
            desconectar()
        }
    }


    // =========================================================
    // READ (LISTAR NA TELA)
    // =========================================================

    // Mostra o extrato de todas as movimentações
    override fun listar() {

        // Busca as movimentações
        val lista = listarTodos()

        // Se não tem nada, avisa e sai
        if (lista.isEmpty()) {

            // Mensagem de tabela vazia
            println("Nenhuma movimentação registrada.")
            return
        }

        // Mostra cada movimentação usando o toString da classe
        lista.forEach { movimentacao -> println(movimentacao) }
    }


    // =========================================================
    // READ (DEVOLVER OS OBJETOS)
    // =========================================================

    // Devolve todas as movimentações como lista de objetos
    override fun listarTodos(): List<Movimentacao> {

        // Lista que será preenchida
        val lista = mutableListOf<Movimentacao>()

        // Se não conectar, devolve vazia
        if (!conectar()) return lista

        // Protege a operação
        try {

            // Busca ordenando da mais antiga para a mais nova
            val sql = "SELECT * FROM movimentacao ORDER BY data_hora, id"

            // Cria o comando
            val stmt = c!!.createStatement()

            // Executa a consulta
            val rs = stmt.executeQuery(sql)

            // Percorre todas as linhas do resultado
            while (rs.next()) {

                // Converte cada linha em objeto e guarda na lista
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

    // Busca uma movimentação pelo ID
    override fun buscarPorId(id: Int): Movimentacao? {

        // Se não conectar, devolve null
        if (!conectar()) return null

        // Protege a operação
        try {

            // SQL com filtro pelo ID
            val sql = "SELECT * FROM movimentacao WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche o ID procurado
            stmt.setInt(1, id)

            // Executa a consulta
            val rs = stmt.executeQuery()

            // Se achou, monta e devolve o objeto
            if (rs.next()) {

                // Converte a linha em objeto
                val movimentacao = montarObjeto(rs)

                // Fecha o comando
                stmt.close()

                // Devolve o resultado
                return movimentacao
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
    // UPDATE E DELETE (BLOQUEADOS DE PROPÓSITO)
    // =========================================================

    // Editar uma movimentação é PROIBIDO por regra contábil.
    //
    // Se um lançamento estiver errado, o correto é fazer
    // um novo lançamento de estorno, e não apagar o histórico.
    override fun editar(item: Movimentacao, id: Int): Boolean {

        // Explica o motivo da recusa
        println("[BLOQUEADO] Movimentação financeira não pode ser editada.")
        println("            Registre um estorno no lugar.")

        // Informa que nada foi alterado
        return false
    }


    // Excluir também é proibido, pelo mesmo motivo
    override fun excluir(id: Int): Boolean {

        // Explica o motivo da recusa
        println("[BLOQUEADO] Movimentação financeira não pode ser excluída.")
        println("            O histórico do caixa precisa ser permanente.")

        // Informa que nada foi apagado
        return false
    }


    // =========================================================
    // CONSULTAS FINANCEIRAS
    // =========================================================

    // Soma TODAS as movimentações e devolve o saldo real do caixa.
    //
    // Como as entradas estão gravadas positivas e as saídas
    // negativas, basta somar tudo para ter o saldo.
    fun somarSaldo(): BigDecimal {

        // Se não conectar, devolve zero
        if (!conectar()) return BigDecimal.ZERO

        // Protege a operação
        try {

            // COALESCE troca NULL por 0.
            //
            // Isso é necessário porque SUM() devolve NULL
            // quando a tabela está vazia, e NULL quebraria a conta.
            val sql = "SELECT COALESCE(SUM(valor), 0) AS saldo FROM movimentacao"

            // Cria o comando
            val stmt = c!!.createStatement()

            // Executa a consulta
            val rs = stmt.executeQuery(sql)

            // Variável que guarda o resultado
            var saldo = BigDecimal.ZERO

            // Se veio resultado, lê a coluna calculada
            if (rs.next()) {

                // Lê a coluna com o apelido "saldo"
                saldo = rs.getBigDecimal("saldo")
            }

            // Fecha o comando
            stmt.close()

            // Devolve o saldo somado
            return saldo

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível calcular o saldo: ${e.message}")

            // Devolve zero em caso de falha
            return BigDecimal.ZERO

        } finally {

            // Fecha a conexão
            desconectar()
        }
    }


    // Mostra um relatório com o total agrupado por tipo.
    //
    // Serve para responder perguntas como
    // "quanto a empresa gastou com salário?".
    fun relatorioPorTipo() {

        // Se não conectar, sai da função
        if (!conectar()) return

        // Protege a operação
        try {

            // GROUP BY agrupa as linhas que têm o mesmo tipo,
            // e SUM soma o valor dentro de cada grupo.
            val sql = """
                SELECT tipo,
                       COUNT(*) AS quantidade,
                       SUM(valor) AS total
                FROM movimentacao
                GROUP BY tipo
                ORDER BY total DESC
            """.trimIndent()

            // Cria o comando
            val stmt = c!!.createStatement()

            // Executa a consulta
            val rs = stmt.executeQuery(sql)

            // Cabeçalho do relatório
            println("-".repeat(60))
            println("RELATÓRIO POR TIPO DE MOVIMENTAÇÃO")
            println("-".repeat(60))

            // Percorre cada grupo devolvido pelo banco
            while (rs.next()) {

                // Lê o nome do tipo
                val tipo = rs.getString("tipo")

                // Lê quantas movimentações existem nesse tipo
                val quantidade = rs.getInt("quantidade")

                // Lê o total somado do grupo
                val total = rs.getBigDecimal("total")

                // Mostra a linha do relatório.
                //
                // padEnd(26) completa o texto com espaços
                // até ter 26 caracteres, deixando as colunas alinhadas.
                println("${tipo.padEnd(26)} | $quantidade lanç. | R$ $total")
            }

            // Rodapé
            println("-".repeat(60))

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível gerar o relatório: ${e.message}")

        } finally {

            // Fecha a conexão
            desconectar()
        }
    }


    // =========================================================
    // FUNÇÃO AUXILIAR
    // =========================================================

    // Converte uma linha do ResultSet em um objeto Movimentacao
    private fun montarObjeto(rs: ResultSet): Movimentacao {

        // Cria e devolve o objeto preenchido com os dados da linha
        return Movimentacao(

            // Lê o valor preservando os centavos
            valor = rs.getBigDecimal("valor"),

            // Lê quem pagou
            pagador = rs.getString("pagador"),

            // Lê quem recebeu
            recebedor = rs.getString("recebedor"),

            // Lê a data e a hora.
            //
            // toLocalDateTime() converte o Timestamp do banco
            // de volta para o LocalDateTime do Kotlin.
            dataHora = rs.getTimestamp("data_hora").toLocalDateTime(),

            // Lê o motivo
            motivo = rs.getString("motivo"),

            // Lê o responsável
            responsavel = rs.getString("responsavel"),

            // Converte o texto do banco de volta para o enum.
            //
            // valueOf() faz o caminho inverso do .name
            tipo = TipoMovimentacao.valueOf(rs.getString("tipo")),

            // Lê o ID do registro
            id = rs.getInt("id")
        )
    }
}
