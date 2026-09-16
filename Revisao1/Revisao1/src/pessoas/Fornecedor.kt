// Define o pacote das classes que representam gente
package pessoas

// Importa BigDecimal, usado nos valores em dinheiro
import java.math.BigDecimal


// Classe FILHA de Pessoa que representa o fornecedor.
//
// O fornecedor é a EMPRESA que vende as caixas d'água
// para a nossa empresa. Ele é uma pessoa JURÍDICA,
// por isso o documento dele é um CNPJ e não um CPF.
class Fornecedor(

    // Nome fantasia da empresa fornecedora
    razaoSocial: String,

    // CNPJ da empresa (14 dígitos)
    cnpj: String,

    // E-mail comercial
    email: String,

    // Telefone comercial
    telefone: String,

    // ID do banco de dados
    idFornecedor: Int = 0

// Chama o construtor de Pessoa.
//
// Repare na idade fixada em 0: empresa não tem idade,
// mas a classe-mãe exige esse campo. Passamos 0 para
// indicar que a informação não se aplica.
) : Pessoa(
    nome = razaoSocial,
    documento = cnpj,
    idade = 0,
    email = email,
    telefone = telefone,
    id = idFornecedor
) {

    // Sobrescreve receberConta.
    //
    // Quando a empresa paga o fornecedor, o dinheiro SAI.
    // Por isso o valor volta negativo, igual ao Funcionario.
    override fun receberConta(dinheiro: BigDecimal): BigDecimal {

        // negate() inverte o sinal do valor
        return dinheiro.negate()
    }

    // Sobrescreve o papel do fornecedor
    override fun papel(): String = "Fornecedor"
}
