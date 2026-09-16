// Importa a função do menu principal, que está no pacote sistema
import sistema.menuInicial


// Função main: o PONTO DE ENTRADA do programa.
//
// Quando você aperta "Run" na IDE, é esta função
// que o Kotlin executa primeiro. Todo programa
// Kotlin precisa ter exatamente uma função main.
fun main() {

    // try/catch em volta de TUDO.
    //
    // Essa é a última rede de proteção do sistema:
    // se algum erro inesperado escapar de todas as
    // validações internas, ele é capturado aqui e o
    // programa termina com uma mensagem educada,
    // em vez de despejar um stack trace na cara do usuário.
    try {

        // Chama o menu principal, que controla todo o sistema
        menuInicial()

    } catch (e: Exception) {

        // Exception é a classe-mãe da maioria dos erros.
        // Capturá-la aqui pega qualquer imprevisto.
        println()
        println("=".repeat(60))
        println("ERRO INESPERADO NO SISTEMA")
        println("Mensagem: ${e.message}")
        println("=".repeat(60))
        println("Verifique se o PostgreSQL está ligado e se o")
        println("banco 'caixadagua' foi criado com o script")
        println("que está na pasta banco/schema.sql.")

    } finally {

        // "finally" SEMPRE executa, com erro ou sem erro.
        //
        // É o lugar certo para a mensagem de encerramento.
        println()
        println("Programa finalizado.")
    }
}
