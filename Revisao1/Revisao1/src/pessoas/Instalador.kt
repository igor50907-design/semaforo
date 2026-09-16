// Define o pacote das classes que representam gente
package pessoas

// Importa o enum Habilidade
import enumeradores.Habilidade

// Importa o enum Setor
import enumeradores.Setor

// Importa o enum Turno
import enumeradores.Turno

// Importa BigDecimal, usado no salário
import java.math.BigDecimal


// Classe FILHA de Funcionario (que por sua vez é filha de Pessoa).
//
// Aqui temos TRÊS níveis de herança:
//   Pessoa -> Funcionario -> Instalador
//
// O Instalador é o funcionário que vai até a casa do cliente
// montar a caixa d'água.
class Instalador(

    // Dados repassados para o construtor da classe-mãe
    nome: String,
    documento: String,
    idade: Int,
    email: String,
    telefone: String,
    salario: BigDecimal,
    turno: Turno,

    // Indica se o instalador tem carteira de motorista.
    // Só o Instalador tem essa informação, por isso ela
    // não está na classe Funcionario.
    val motorista: Boolean,

    // ID do banco de dados
    idInstalador: Int = 0

// Chama o construtor de Funcionario.
//
// Repare que o setor e a habilidade são FIXOS aqui:
// todo Instalador pertence ao setor INSTALACAO.
) : Funcionario(
    nome = nome,
    documento = documento,
    idade = idade,
    email = email,
    telefone = telefone,
    salario = salario,
    setor = Setor.INSTALACAO,
    turno = turno,
    habilidade = Habilidade.INSTALACAO,
    idFuncionario = idInstalador
) {

    // Sobrescreve o papel novamente, agora no terceiro nível.
    //
    // O operador "if" em Kotlin é uma EXPRESSÃO: ele devolve
    // um valor, então pode ser usado direto no retorno.
    override fun papel(): String =
        if (motorista) "Instalador (motorista)" else "Instalador"
}
