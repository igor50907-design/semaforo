// Define o pacote das classes que representam gente
package pessoas

// Importa BigDecimal, usado nos valores em dinheiro
import java.math.BigDecimal


// Classe-MÃE (superclasse) de todas as pessoas do sistema.
//
// "open" é OBRIGATÓRIO em Kotlin para que a classe possa ser herdada.
// Sem o "open", toda classe em Kotlin é final (fechada).
//
// Quem herda dela: Funcionario, Cliente e Fornecedor.
open class Pessoa(

    // Nome da pessoa.
    // "val" = valor imutável, não muda depois de criado.
    val nome: String,

    // Documento da pessoa (CPF para pessoa física, CNPJ para empresa)
    val documento: String,

    // Idade da pessoa
    val idade: Int,

    // E-mail para contato
    val email: String,

    // Telefone para contato
    val telefone: String,

    // Identificador da pessoa no banco de dados.
    //
    // "var" porque o banco gera o ID depois que o registro é salvo,
    // então esse valor precisa poder mudar.
    // Começa em 0 = ainda não foi salvo no banco.
    var id: Int = 0
) {

    // Função que representa a pessoa RECEBENDO dinheiro.
    //
    // "open" permite que as classes filhas SOBRESCREVAM
    // esse comportamento (isso é POLIMORFISMO).
    //
    // Na classe-mãe, o comportamento padrão é apenas
    // devolver o valor como entrada positiva.
    open fun receberConta(dinheiro: BigDecimal): BigDecimal {

        // Devolve o valor exatamente como recebeu
        return dinheiro
    }

    // Função que devolve o papel da pessoa dentro da empresa.
    //
    // Também é "open" para cada filha responder de um jeito.
    open fun papel(): String = "Pessoa"

    // Sobrescreve o toString() para imprimir a pessoa
    // de forma legível no console.
    //
    // "override" = estamos trocando o comportamento herdado de Any.
    override fun toString(): String =
        "[$id] $nome | ${papel()} | Doc: $documento | $email | $telefone"
}
