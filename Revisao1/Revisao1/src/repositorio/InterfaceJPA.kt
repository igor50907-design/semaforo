// Define o pacote responsável por falar com o banco de dados
package repositorio


// INTERFACE GENÉRICA que define o contrato de um repositório.
//
// Uma interface funciona como um CONTRATO: ela diz QUAIS
// funções precisam existir, mas não diz COMO elas funcionam.
// Quem implementa a interface é obrigado a escrever o corpo
// de todas as funções, usando a palavra "override".
//
// <T> é um TIPO GENÉRICO. O T é um espaço em branco que
// será preenchido na hora do uso:
//
//   InterfaceJPA<CaixaDaAgua>  -> aqui T = CaixaDaAgua
//   InterfaceJPA<Movimentacao> -> aqui T = Movimentacao
//
// Graças ao genérico, escrevemos UM contrato só e ele
// serve para todas as entidades do sistema.
//
// JPA = Java Persistence API, o padrão do mercado que
// estamos imitando em versão simplificada.
interface InterfaceJPA<T> {

    // CREATE -> grava um novo registro no banco.
    //
    // "item" é do tipo T, ou seja, do tipo definido no uso.
    fun salvar(item: T)

    // READ -> imprime na tela todos os registros da tabela
    fun listar()

    // READ -> devolve os registros como uma lista de objetos.
    //
    // Diferente do listar(), que só mostra na tela,
    // este devolve os dados para o programa usar.
    fun listarTodos(): List<T>

    // READ -> busca UM registro pelo ID.
    //
    // O retorno é "T?" (NULLABLE) porque o ID informado
    // pode simplesmente não existir no banco. Nesse caso
    // devolvemos null em vez de quebrar o programa.
    fun buscarPorId(id: Int): T?

    // UPDATE -> altera um registro existente.
    //
    // "item" traz os dados novos e "id" diz qual linha alterar.
    // Devolve Boolean para informar se alguma linha foi alterada.
    fun editar(item: T, id: Int): Boolean

    // DELETE -> apaga o registro do ID informado.
    //
    // Devolve Boolean para informar se alguma linha foi apagada.
    fun excluir(id: Int): Boolean
}
