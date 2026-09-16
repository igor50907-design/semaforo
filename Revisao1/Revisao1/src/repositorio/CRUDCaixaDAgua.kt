// Define o pacote responsável por falar com o banco de dados
package repositorio

// Importa os enums usados nas colunas cor e material
import enumeradores.Cor
import enumeradores.Material

// Importa a classe do produto
import produto.CaixaDaAgua

// Importa ResultSet, que representa o resultado de um SELECT
import java.sql.ResultSet

// Importa SQLException, o erro do banco
import java.sql.SQLException

// Importa Statement, usado para pedir o ID gerado pelo banco
import java.sql.Statement


// Classe responsável por TODO o CRUD da tabela caixa_da_agua.
//
// CRUD significa:
//   C = Create -> criar/salvar
//   R = Read   -> ler/listar
//   U = Update -> editar
//   D = Delete -> excluir
//
// ": InterfaceJPA<CaixaDaAgua>" = assina o contrato,
//   então é OBRIGADA a escrever salvar, listar, editar e excluir.
//
// ", ConexaoPostgres()" = HERDA a conexão,
//   ganhando de graça as funções conectar() e desconectar().
class CRUDCaixaDAgua : InterfaceJPA<CaixaDaAgua>, ConexaoPostgres() {


    // =========================================================
    // CREATE (SALVAR)
    // =========================================================

    // Grava uma nova caixa d'água no banco
    override fun salvar(item: CaixaDaAgua) {

        // Se não conseguir conectar, avisa e desiste da operação
        if (!conectar()) return

        // try/finally: o finally garante que a conexão será
        // fechada MESMO se acontecer um erro no meio do caminho.
        try {

            // Comando SQL de inserção.
            //
            // Os "?" são PLACEHOLDERS (espaços reservados).
            // Eles serão preenchidos depois, um a um.
            //
            // Usar "?" em vez de juntar texto com + é o que
            // protege o sistema contra SQL INJECTION, que é
            // quando o usuário digita comando SQL no lugar do dado.
            val sql = """
                INSERT INTO caixa_da_agua
                (marca, modelo, dimensao, cor, material, formato,
                 preco_custo, preco_venda, quantidade_estoque, fornecedor_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            // Prepara o comando.
            //
            // RETURN_GENERATED_KEYS pede ao banco que devolva
            // o ID que ele gerou automaticamente (SERIAL).
            val stmt = c!!.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            // Converte a lista de dimensões em um array
            // do tipo float8 (double precision) do PostgreSQL.
            //
            // toTypedArray() transforma MutableList<Double>
            // em Array<Double>, que é o formato que o driver aceita.
            val arrayDimensao = c!!.createArrayOf(
                "float8",
                item.dimensao.toTypedArray()
            )

            // Preenche o 1º "?" com a marca
            stmt.setString(1, item.marca)

            // Preenche o 2º "?" com o modelo
            stmt.setString(2, item.modelo)

            // Preenche o 3º "?" com o array de dimensões
            stmt.setArray(3, arrayDimensao)

            // Preenche o 4º "?" com a cor.
            //
            // ".name" pega o NOME da constante do enum
            // (por exemplo AZUL_FORTE) como texto.
            stmt.setString(4, item.cor.name)

            // Preenche o 5º "?" com o material
            stmt.setString(5, item.material.name)

            // Preenche o 6º "?" com o formato
            stmt.setString(6, item.formato)

            // Preenche o 7º "?" com o preço de custo.
            //
            // setBigDecimal preserva os centavos exatos,
            // diferente de setString ou setDouble.
            stmt.setBigDecimal(7, item.precoCusto)

            // Preenche o 8º "?" com o preço de venda
            stmt.setBigDecimal(8, item.precoVenda)

            // Preenche o 9º "?" com a quantidade em estoque
            stmt.setInt(9, item.quantidadeEstoque)

            // Preenche o 10º "?" com o fornecedor.
            //
            // Como fornecedorId é NULLABLE, precisamos tratar
            // os dois casos: se for null, gravamos NULL no banco.
            if (item.fornecedorId == null) {

                // setNull grava um NULL de verdade na coluna
                stmt.setNull(10, java.sql.Types.INTEGER)

            } else {

                // Se tem valor, grava o número normalmente
                stmt.setInt(10, item.fornecedorId)
            }

            // Executa o INSERT no banco
            stmt.executeUpdate()

            // Pega o ID que o banco gerou automaticamente
            val chaves = stmt.generatedKeys

            // next() avança para a primeira linha do resultado.
            // Devolve true se existe resultado.
            if (chaves.next()) {

                // Guarda o ID gerado dentro do objeto em memória
                item.id = chaves.getInt(1)
            }

            // Confirma para o usuário
            println("[OK] Caixa d'água cadastrada com o ID ${item.id}.")

            // Fecha o comando SQL
            stmt.close()

        } catch (e: SQLException) {

            // Mostra a mensagem do erro sem derrubar o programa
            println("[ERRO] Não foi possível salvar: ${e.message}")

        } finally {

            // Fecha a conexão aconteça o que acontecer
            desconectar()
        }
    }


    // =========================================================
    // READ (LISTAR NA TELA)
    // =========================================================

    // Mostra todas as caixas cadastradas
    override fun listar() {

        // Busca os objetos usando a função abaixo
        val lista = listarTodos()

        // Se a lista estiver vazia, avisa e sai da função
        if (lista.isEmpty()) {

            // Mensagem para o caso de banco vazio
            println("Nenhuma caixa d'água cadastrada.")
            return
        }

        // Linha separadora
        println("-".repeat(70))

        // forEach percorre a lista mostrando cada caixa.
        //
        // O toString() da classe CaixaDaAgua é usado
        // automaticamente aqui dentro do println.
        lista.forEach { caixa -> println(caixa) }

        // Linha separadora final
        println("-".repeat(70))
    }


    // =========================================================
    // READ (DEVOLVER OS OBJETOS)
    // =========================================================

    // Devolve todas as caixas como uma lista de objetos Kotlin
    override fun listarTodos(): List<CaixaDaAgua> {

        // Lista vazia que será preenchida com o resultado
        val lista = mutableListOf<CaixaDaAgua>()

        // Se não conectar, devolve a lista vazia
        if (!conectar()) return lista

        // Protege a operação contra erros
        try {

            // Comando SQL que busca tudo, ordenado pelo ID
            val sql = "SELECT * FROM caixa_da_agua ORDER BY id"

            // Cria o comando (sem "?", então createStatement basta)
            val stmt = c!!.createStatement()

            // Executa o SELECT.
            //
            // O ResultSet é como uma tabela em memória,
            // com um "cursor" que aponta para uma linha por vez.
            val rs = stmt.executeQuery(sql)

            // while(rs.next()) percorre linha por linha.
            //
            // next() move o cursor para a próxima linha e
            // devolve false quando acabaram as linhas.
            while (rs.next()) {

                // Converte a linha atual em um objeto e guarda
                lista.add(montarObjeto(rs))
            }

            // Fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // Mostra o erro sem derrubar o programa
            println("[ERRO] Não foi possível listar: ${e.message}")

        } finally {

            // Fecha a conexão
            desconectar()
        }

        // Devolve a lista preenchida (ou vazia, se deu erro)
        return lista
    }


    // =========================================================
    // READ (BUSCAR UM ÚNICO REGISTRO)
    // =========================================================

    // Busca uma caixa pelo ID.
    //
    // Devolve CaixaDaAgua? porque o ID pode não existir.
    override fun buscarPorId(id: Int): CaixaDaAgua? {

        // Se não conectar, devolve null
        if (!conectar()) return null

        // Protege a operação
        try {

            // SQL com filtro pelo ID
            val sql = "SELECT * FROM caixa_da_agua WHERE id = ?"

            // Prepara o comando com placeholder
            val stmt = c!!.prepareStatement(sql)

            // Preenche o "?" com o ID procurado
            stmt.setInt(1, id)

            // Executa a consulta
            val rs = stmt.executeQuery()

            // Se encontrou uma linha, converte em objeto e devolve
            if (rs.next()) {

                // Monta o objeto a partir da linha
                val caixa = montarObjeto(rs)

                // Fecha o comando antes de sair
                stmt.close()

                // Devolve a caixa encontrada
                return caixa
            }

            // Não encontrou nada: fecha o comando
            stmt.close()

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível buscar: ${e.message}")

        } finally {

            // Fecha a conexão
            desconectar()
        }

        // Chegou aqui = não achou nada, devolve null
        return null
    }


    // =========================================================
    // UPDATE (EDITAR)
    // =========================================================

    // Altera os dados de uma caixa já cadastrada
    override fun editar(item: CaixaDaAgua, id: Int): Boolean {

        // Se não conectar, informa que não deu certo
        if (!conectar()) return false

        // Protege a operação
        try {

            // Comando SQL de atualização.
            //
            // O WHERE id = ? é ESSENCIAL: sem ele, o UPDATE
            // alteraria TODAS as linhas da tabela de uma vez.
            val sql = """
                UPDATE caixa_da_agua SET
                marca = ?, modelo = ?, dimensao = ?, cor = ?,
                material = ?, formato = ?, preco_custo = ?,
                preco_venda = ?, quantidade_estoque = ?
                WHERE id = ?
            """.trimIndent()

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Monta o array de dimensões para o PostgreSQL
            val arrayDimensao = c!!.createArrayOf(
                "float8",
                item.dimensao.toTypedArray()
            )

            // Preenche a nova marca
            stmt.setString(1, item.marca)

            // Preenche o novo modelo
            stmt.setString(2, item.modelo)

            // Preenche as novas dimensões
            stmt.setArray(3, arrayDimensao)

            // Preenche a nova cor
            stmt.setString(4, item.cor.name)

            // Preenche o novo material
            stmt.setString(5, item.material.name)

            // Preenche o novo formato
            stmt.setString(6, item.formato)

            // Preenche o novo preço de custo
            stmt.setBigDecimal(7, item.precoCusto)

            // Preenche o novo preço de venda
            stmt.setBigDecimal(8, item.precoVenda)

            // Preenche a nova quantidade em estoque
            stmt.setInt(9, item.quantidadeEstoque)

            // Preenche o ID de qual linha será alterada
            stmt.setInt(10, id)

            // executeUpdate() devolve QUANTAS linhas foram afetadas.
            //
            // Se devolver 0, o ID informado não existia.
            val linhas = stmt.executeUpdate()

            // Fecha o comando
            stmt.close()

            // Avisa o resultado ao usuário
            if (linhas > 0) {

                // Deu certo
                println("[OK] Caixa d'água $id atualizada.")

            } else {

                // Não achou o registro
                println("[AVISO] Nenhuma caixa encontrada com o ID $id.")
            }

            // Devolve true se alterou ao menos uma linha
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

    // Apaga uma caixa do banco pelo ID
    override fun excluir(id: Int): Boolean {

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // SQL de exclusão, sempre com WHERE
            val sql = "DELETE FROM caixa_da_agua WHERE id = ?"

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
                println("[OK] Caixa d'água $id excluída.")

            } else {

                // Não achou
                println("[AVISO] Nenhuma caixa encontrada com o ID $id.")
            }

            // Devolve true se apagou algo
            return linhas > 0

        } catch (e: SQLException) {

            // Esse erro aparece quando existe um serviço
            // ligado a essa caixa (chave estrangeira).
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

    // Transforma UMA linha do ResultSet em um objeto CaixaDaAgua.
    //
    // É "private" porque só interessa dentro deste CRUD.
    // Ela existe para não repetir esse código em listarTodos()
    // e em buscarPorId().
    private fun montarObjeto(rs: ResultSet): CaixaDaAgua {

        // Lê a coluna "dimensao" do banco.
        //
        // getArray devolve um objeto Array do JDBC,
        // e ".array" pega o array Java de verdade dentro dele.
        val arrayBanco = rs.getArray("dimensao")

        // Converte o array do banco em uma MutableList<Double>.
        //
        // "as? Array<*>" é um CAST SEGURO: se a conversão
        // não for possível, o resultado é null em vez de erro.
        //
        // ?.map converte cada item para Double.
        // ?: mutableListOf() usa uma lista vazia se algo der null.
        val dimensoes = (arrayBanco?.array as? Array<*>)
            ?.map { (it as Number).toDouble() }
            ?.toMutableList()
            ?: mutableListOf(0.0, 0.0, 0.0)

        // Lê o fornecedor_id da linha
        val fornecedor = rs.getInt("fornecedor_id")

        // rs.wasNull() informa se a ÚLTIMA coluna lida era NULL.
        //
        // Isso é necessário porque getInt() devolve 0 tanto
        // para o número zero quanto para NULL.
        val fornecedorId = if (rs.wasNull()) null else fornecedor

        // Cria e devolve o objeto preenchido
        return CaixaDaAgua(

            // Lê a coluna marca como texto
            marca = rs.getString("marca"),

            // Lê a coluna modelo como texto
            modelo = rs.getString("modelo"),

            // Usa a lista de dimensões montada acima
            dimensao = dimensoes,

            // Converte o texto do banco de volta para o enum Cor.
            //
            // valueOf() faz o caminho inverso do .name
            cor = Cor.valueOf(rs.getString("cor")),

            // Converte o texto do banco de volta para o enum Material
            material = Material.valueOf(rs.getString("material")),

            // Lê o formato
            formato = rs.getString("formato"),

            // Lê o preço de custo preservando os centavos
            precoCusto = rs.getBigDecimal("preco_custo"),

            // Lê o preço de venda
            precoVenda = rs.getBigDecimal("preco_venda"),

            // Lê a quantidade em estoque
            quantidadeInicial = rs.getInt("quantidade_estoque"),

            // Usa o fornecedor tratado acima (pode ser null)
            fornecedorId = fornecedorId,

            // Lê o ID do registro
            id = rs.getInt("id")
        )
    }


    // =========================================================
    // OPERAÇÃO DE ESTOQUE
    // =========================================================

    // Atualiza SOMENTE a quantidade em estoque de uma caixa.
    //
    // Usada na compra (entrada) e na venda (saída).
    fun atualizarEstoque(id: Int, novaQuantidade: Int): Boolean {

        // Não permite estoque negativo nem por engano
        if (novaQuantidade < 0) return false

        // Se não conectar, informa a falha
        if (!conectar()) return false

        // Protege a operação
        try {

            // SQL que altera só a coluna de estoque
            val sql = "UPDATE caixa_da_agua SET quantidade_estoque = ? WHERE id = ?"

            // Prepara o comando
            val stmt = c!!.prepareStatement(sql)

            // Preenche a nova quantidade
            stmt.setInt(1, novaQuantidade)

            // Preenche o ID da caixa
            stmt.setInt(2, id)

            // Executa e guarda quantas linhas mudaram
            val linhas = stmt.executeUpdate()

            // Fecha o comando
            stmt.close()

            // Devolve true se alterou alguma linha
            return linhas > 0

        } catch (e: SQLException) {

            // Mostra o erro
            println("[ERRO] Não foi possível atualizar o estoque: ${e.message}")

            // Informa a falha
            return false

        } finally {

            // Fecha a conexão
            desconectar()
        }
    }
}
