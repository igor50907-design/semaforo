// Define o pacote das classes que representam gente
package pessoas

// Importa o enum Habilidade (a especialidade do funcionário)
import enumeradores.Habilidade

// Importa o enum Setor (o departamento onde ele trabalha)
import enumeradores.Setor

// Importa o enum Turno (manhã, tarde ou noite)
import enumeradores.Turno

// Importa BigDecimal, usado no salário
import java.math.BigDecimal


// Classe FILHA de Pessoa que representa um funcionário da empresa.
//
// ": Pessoa(...)" indica a HERANÇA: Funcionario É UMA Pessoa,
// então herda nome, documento, idade, email, telefone e id.
//
// "open" porque a classe Instalador ainda vai herdar desta aqui,
// formando três níveis de herança: Pessoa -> Funcionario -> Instalador.
open class Funcionario(

    // Os parâmetros abaixo SEM "val" existem só para
    // serem repassados ao construtor da classe-mãe.
    nome: String,
    documento: String,
    idade: Int,
    email: String,
    telefone: String,

    // Salário mensal do funcionário.
    // "val" porque aqui ele vira uma propriedade da classe filha.
    val salario: BigDecimal,

    // Setor onde o funcionário trabalha.
    // Atende ao requisito "dividir funcionários em setores".
    val setor: Setor,

    // Turno de trabalho
    val turno: Turno,

    // Especialidade técnica do funcionário
    val habilidade: Habilidade,

    // ID do banco de dados, com valor padrão 0
    idFuncionario: Int = 0

// Chama o construtor da classe-mãe repassando os dados comuns
) : Pessoa(nome, documento, idade, email, telefone, idFuncionario) {


    // Sobrescreve receberConta da classe Pessoa.
    //
    // Do ponto de vista da EMPRESA, pagar o funcionário é SAÍDA
    // de dinheiro. Por isso o valor volta NEGATIVO.
    //
    // Esse é o exemplo clássico de polimorfismo do trabalho:
    // a mesma chamada receberConta() se comporta de um jeito
    // no Cliente (entra dinheiro) e de outro no Funcionario (sai dinheiro).
    override fun receberConta(dinheiro: BigDecimal): BigDecimal {

        // negate() troca o sinal do BigDecimal.
        // 2000 vira -2000.
        return dinheiro.negate()
    }


    // Sobrescreve o papel para identificar o funcionário
    // junto com o setor dele.
    override fun papel(): String = "Funcionário/$setor"


    // Função que responde se este funcionário pode
    // autorizar uma movimentação financeira.
    //
    // Ela apenas repassa a regra que já está guardada
    // dentro do próprio enum Setor. Isso é ENCAPSULAMENTO:
    // a regra mora em um lugar só.
    fun podeAutorizarCaixa(): Boolean = setor.podeAprovar
}
