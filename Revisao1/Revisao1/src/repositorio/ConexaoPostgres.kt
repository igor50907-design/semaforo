// Define o pacote responsável por falar com o banco de dados
package repositorio

// Importa Connection, que representa a conexão aberta com o banco
import java.sql.Connection

// Importa DriverManager, responsável por CRIAR a conexão
import java.sql.DriverManager

// Importa SQLException, o erro lançado quando algo dá errado no banco
import java.sql.SQLException


// Classe ABSTRATA responsável pela conexão com o PostgreSQL.
//
// "abstract" significa que ela é uma classe BASE:
// não pode ser instanciada com ConexaoPostgres() diretamente.
// Ela existe para ser HERDADA pelas classes de CRUD.
//
// Assim, a lógica de conexão fica escrita UMA VEZ SÓ
// e é reaproveitada por todos os CRUDs do sistema.
abstract class ConexaoPostgres(

    // Usuário do PostgreSQL
    val user: String = "postgres",

    // Senha do PostgreSQL
    val senha: String = "postgres",

    // Endereço de conexão (URL JDBC).
    //
    // jdbc:postgresql  -> protocolo do driver
    // localhost        -> o banco está nesta máquina
    // 5432             -> porta padrão do PostgreSQL
    // caixadagua       -> nome do banco de dados
    val url: String = "jdbc:postgresql://localhost:5432/caixadagua"
) {

    // Guarda a conexão ativa.
    //
    // O tipo "Connection?" é NULLABLE: a variável pode
    // conter uma conexão OU conter null (nenhuma conexão).
    //
    // "protected" = só esta classe e as classes que herdam
    // dela conseguem enxergar. O resto do sistema não mexe
    // na conexão direto, o que evita bagunça.
    protected var c: Connection? = null


    // Função que ABRE a conexão com o banco.
    //
    // Devolve Boolean para informar se conseguiu conectar.
    // Assim quem chama pode desistir da operação com elegância
    // em vez de o programa quebrar.
    fun conectar(): Boolean {

        // try/catch protege contra banco desligado,
        // senha errada, driver ausente, etc.
        return try {

            // Carrega o driver do PostgreSQL na memória.
            //
            // O driver é o "tradutor" que permite o Kotlin
            // conversar com o PostgreSQL.
            Class.forName("org.postgresql.Driver")

            // Abre a conexão de verdade e guarda na variável c
            c = DriverManager.getConnection(url, user, senha)

            // Deu certo: devolve true
            true

        } catch (e: ClassNotFoundException) {

            // Esse erro acontece quando o arquivo .jar do driver
            // não foi adicionado às bibliotecas do projeto.
            println("[BANCO] Driver do PostgreSQL não encontrado.")
            println("        Adicione o postgresql-42.x.jar nas libraries.")

            // Não conectou: devolve false
            false

        } catch (e: SQLException) {

            // Esse erro acontece quando o banco está desligado,
            // a senha está errada ou o banco não existe.
            println("[BANCO] Não foi possível conectar: ${e.message}")

            // Não conectou: devolve false
            false
        }
    }


    // Função que FECHA a conexão com o banco.
    //
    // Deixar conexões abertas esgota o servidor,
    // por isso toda operação precisa fechar no final.
    fun desconectar() {

        // try/catch porque fechar também pode falhar
        try {

            // "?." é a CHAMADA SEGURA do Kotlin.
            //
            // Se c for null, a linha inteira é ignorada
            // e nenhum erro acontece. Se c não for null,
            // o close() é executado normalmente.
            c?.close()

        } catch (e: SQLException) {

            // Informa que não conseguiu fechar
            println("[BANCO] Erro ao fechar a conexão: ${e.message}")

        } finally {

            // "finally" SEMPRE executa, com erro ou sem erro.
            //
            // Limpamos a variável para deixar claro que
            // não existe mais conexão ativa.
            c = null
        }
    }
}
