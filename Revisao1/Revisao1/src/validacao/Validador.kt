// Define o pacote responsável por TODA a validação de entrada do sistema
package validacao

// Importa BigDecimal, usado para valores monetários com precisão
import java.math.BigDecimal

// Importa LocalDate, usado para datas sem horário
import java.time.LocalDate

// Importa o formatador de data, usado para ler datas no padrão dd/MM/yyyy
import java.time.format.DateTimeFormatter

// Importa a exceção lançada quando a data digitada é inválida
import java.time.format.DateTimeParseException


// "object" cria um SINGLETON em Kotlin.
//
// Singleton = existe apenas UMA instância do Validador
// em todo o programa. Não é preciso escrever Validador().
// Basta chamar Validador.lerTexto(...).
//
// Este objeto é o "porteiro" do sistema: nenhum dado entra
// no programa sem passar por aqui. É ele que deixa a aplicação
// à prova de falha humana.
object Validador {

    // =========================================================
    // 1) EXPRESSÕES REGULARES (REGEX)
    // =========================================================
    //
    // REGEX é um "molde" de texto. Ele descreve o formato
    // que a informação PRECISA ter para ser aceita.

    // Molde do CPF: exatamente 11 dígitos.
    //
    // ^      -> começo do texto
    // \\d    -> um dígito de 0 a 9
    // {11}   -> exatamente 11 vezes
    // $      -> fim do texto
    private val REGEX_CPF = Regex("^\\d{11}$")

    // Molde do CNPJ: exatamente 14 dígitos
    private val REGEX_CNPJ = Regex("^\\d{14}$")

    // Molde do e-mail.
    //
    // [A-Za-z0-9._%+-]+ -> um ou mais caracteres válidos antes do @
    // @                 -> o arroba é obrigatório
    // [A-Za-z0-9.-]+    -> o domínio (exemplo: gmail)
    // \\.               -> um ponto literal
    // [A-Za-z]{2,}      -> a terminação com 2 letras ou mais (com, br)
    private val REGEX_EMAIL =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Molde do telefone: 10 ou 11 dígitos (DDD + número).
    //
    // {10,11} -> no mínimo 10 e no máximo 11 dígitos
    private val REGEX_TELEFONE = Regex("^\\d{10,11}$")

    // Molde de nome: só letras (com acento) e espaços, mínimo 2 caracteres.
    //
    // À-ÿ -> faixa de caracteres acentuados da tabela Unicode
    private val REGEX_NOME = Regex("^[A-Za-zÀ-ÿ ]{2,60}$")

    // Formato usado para ler e mostrar datas no padrão brasileiro
    private val FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")


    // =========================================================
    // 2) LEITURA SEGURA DO TECLADO
    // =========================================================

    // Função interna que lê uma linha do teclado SEM quebrar.
    //
    // readlnOrNull() devolve String? (String NULLABLE),
    // ou seja, pode devolver null se a entrada acabar (Ctrl+D / Ctrl+Z).
    //
    // ?: é o operador ELVIS: "se der null, use o valor da direita".
    // .trim() remove espaços em branco antes e depois do texto.
    private fun lerLinha(): String = readlnOrNull()?.trim() ?: ""


    // ---------------------------------------------------------
    // TEXTO OBRIGATÓRIO
    // ---------------------------------------------------------

    // Lê um texto simples que não pode ficar vazio.
    //
    // rotulo = a pergunta mostrada para o usuário
    fun lerTexto(rotulo: String, minimo: Int = 1): String {

        // while(true) = repete PARA SEMPRE até o dado ser válido.
        // Esse laço é o que impede o usuário de errar.
        while (true) {

            // Mostra a pergunta na tela
            print("$rotulo: ")

            // Lê o que foi digitado
            val entrada = lerLinha()

            // Se o texto tiver o tamanho mínimo, aceita e SAI da função
            if (entrada.length >= minimo) return entrada

            // Se não tiver, avisa e o laço repete a pergunta
            println("[ERRO] Digite ao menos $minimo caractere(s).")
        }
    }


    // ---------------------------------------------------------
    // NOME (validado por REGEX)
    // ---------------------------------------------------------

    // Lê um nome de pessoa aceitando apenas letras e espaços
    fun lerNome(rotulo: String = "Nome"): String {

        // Repete até o nome bater com o molde REGEX_NOME
        while (true) {

            // Pergunta o nome
            print("$rotulo: ")

            // Lê a resposta
            val entrada = lerLinha()

            // matches() devolve true quando o texto encaixa no molde
            if (REGEX_NOME.matches(entrada)) return entrada

            // Se não encaixou, explica o erro e pergunta de novo
            println("[ERRO] Nome inválido. Use apenas letras e espaços (2 a 60).")
        }
    }


    // ---------------------------------------------------------
    // CPF (validado por REGEX + dígito verificador)
    // ---------------------------------------------------------

    // Lê um CPF, aceitando com ou sem pontuação
    fun lerCpf(rotulo: String = "CPF (somente números)"): String {

        // Repete até o CPF ser realmente válido
        while (true) {

            // Pergunta o CPF
            print("$rotulo: ")

            // Lê o CPF e remove tudo que NÃO for dígito.
            //
            // Regex("\\D") = qualquer caractere que não é número.
            // replace(...,"") = troca por nada, ou seja, apaga.
            // Assim "123.456.789-09" vira "12345678909".
            val entrada = lerLinha().replace(Regex("\\D"), "")

            // Primeiro teste: tem 11 dígitos?
            if (!REGEX_CPF.matches(entrada)) {

                // Se não tem, avisa e volta ao começo do laço
                println("[ERRO] CPF deve conter 11 dígitos.")
                continue
            }

            // Segundo teste: o dígito verificador confere?
            if (!cpfValido(entrada)) {

                // Se não confere, avisa e volta ao começo do laço
                println("[ERRO] CPF inválido (dígito verificador não confere).")
                continue
            }

            // Passou nos dois testes: devolve o CPF limpo
            return entrada
        }
    }


    // Função que calcula os dois dígitos verificadores do CPF.
    //
    // Ela é "private" porque só interessa dentro do Validador.
    private fun cpfValido(cpf: String): Boolean {

        // CPFs com todos os dígitos iguais (11111111111) são inválidos.
        //
        // all { } percorre todos os caracteres e verifica a condição.
        if (cpf.all { it == cpf[0] }) return false

        // Calcula os dois dígitos verificadores em um laço.
        //
        // A primeira volta (j = 9) calcula o 1º dígito.
        // A segunda volta (j = 10) calcula o 2º dígito.
        for (j in 9..10) {

            // soma acumula o total da multiplicação
            var soma = 0

            // peso começa em j+1 e vai diminuindo
            var peso = j + 1

            // Percorre os dígitos que participam do cálculo
            for (i in 0 until j) {

                // Converte o caractere para número inteiro
                // e multiplica pelo peso atual
                soma += Character.getNumericValue(cpf[i]) * peso

                // Diminui o peso para o próximo dígito
                peso--
            }

            // Calcula o resto da divisão por 11
            val resto = soma % 11

            // Regra oficial: se o resto for 0 ou 1, o dígito é 0.
            // Caso contrário, o dígito é 11 menos o resto.
            val digito = if (resto < 2) 0 else 11 - resto

            // Compara o dígito calculado com o dígito digitado.
            // Se for diferente, o CPF é falso.
            if (Character.getNumericValue(cpf[j]) != digito) return false
        }

        // Passou pelos dois dígitos: o CPF é válido
        return true
    }


    // ---------------------------------------------------------
    // CNPJ (validado por REGEX)
    // ---------------------------------------------------------

    // Lê o CNPJ do fornecedor
    fun lerCnpj(rotulo: String = "CNPJ (somente números)"): String {

        // Repete até ter 14 dígitos
        while (true) {

            // Pergunta o CNPJ
            print("$rotulo: ")

            // Lê e apaga tudo que não for número
            val entrada = lerLinha().replace(Regex("\\D"), "")

            // Se encaixar no molde de 14 dígitos, aceita
            if (REGEX_CNPJ.matches(entrada)) return entrada

            // Se não, avisa e repete
            println("[ERRO] CNPJ deve conter 14 dígitos.")
        }
    }


    // ---------------------------------------------------------
    // E-MAIL (validado por REGEX)
    // ---------------------------------------------------------

    // Lê um e-mail no formato correto
    fun lerEmail(rotulo: String = "E-mail"): String {

        // Repete até o e-mail bater com o molde
        while (true) {

            // Pergunta o e-mail
            print("$rotulo: ")

            // Lê a resposta e converte para minúsculas,
            // porque e-mail não diferencia maiúscula de minúscula
            val entrada = lerLinha().lowercase()

            // Se encaixar no molde REGEX_EMAIL, aceita
            if (REGEX_EMAIL.matches(entrada)) return entrada

            // Se não, avisa e repete
            println("[ERRO] E-mail inválido. Exemplo: nome@empresa.com.br")
        }
    }


    // ---------------------------------------------------------
    // TELEFONE (validado por REGEX)
    // ---------------------------------------------------------

    // Lê um telefone com DDD
    fun lerTelefone(rotulo: String = "Telefone com DDD"): String {

        // Repete até ter 10 ou 11 dígitos
        while (true) {

            // Pergunta o telefone
            print("$rotulo: ")

            // Lê e apaga tudo que não for número
            val entrada = lerLinha().replace(Regex("\\D"), "")

            // Se encaixar no molde, aceita
            if (REGEX_TELEFONE.matches(entrada)) return entrada

            // Se não, avisa e repete
            println("[ERRO] Telefone deve ter 10 ou 11 dígitos (com DDD).")
        }
    }


    // ---------------------------------------------------------
    // NÚMERO INTEIRO
    // ---------------------------------------------------------

    // Lê um número inteiro dentro de uma faixa permitida.
    //
    // minimo e maximo têm valores padrão, então podem ser omitidos.
    fun lerInteiro(
        rotulo: String,
        minimo: Int = Int.MIN_VALUE,
        maximo: Int = Int.MAX_VALUE
    ): Int {

        // Repete até o número ser válido
        while (true) {

            // Pergunta o número
            print("$rotulo: ")

            // toIntOrNull() é a versão SEGURA do toInt().
            //
            // toInt()       -> QUEBRA o programa se o texto não for número.
            // toIntOrNull() -> devolve null, e o programa continua vivo.
            //
            // O tipo Int? indica que a variável pode ser null.
            val numero: Int? = lerLinha().toIntOrNull()

            // Se veio null, o usuário digitou letra em vez de número
            if (numero == null) {

                // Avisa e volta ao começo do laço
                println("[ERRO] Digite um número inteiro válido.")
                continue
            }

            // Verifica se o número está dentro da faixa permitida
            if (numero < minimo || numero > maximo) {

                // Avisa qual é a faixa aceita e repete
                println("[ERRO] O valor deve estar entre $minimo e $maximo.")
                continue
            }

            // Número válido: devolve
            return numero
        }
    }


    // ---------------------------------------------------------
    // VALOR MONETÁRIO
    // ---------------------------------------------------------

    // Lê um valor em dinheiro usando BigDecimal.
    //
    // Usamos BigDecimal (e não Double) porque Double
    // erra em contas de centavos: 0.1 + 0.2 dá 0.30000000000000004.
    fun lerDinheiro(
        rotulo: String,
        permiteZero: Boolean = false
    ): BigDecimal {

        // Repete até o valor ser válido
        while (true) {

            // Pergunta o valor
            print("$rotulo (ex: 1250.90): ")

            // Lê o texto e troca vírgula por ponto,
            // porque o brasileiro digita 1250,90 mas o
            // BigDecimal só entende 1250.90
            val texto = lerLinha().replace(",", ".")

            // try/catch: TENTA converter; se falhar, trata o erro
            // em vez de deixar o programa quebrar.
            val valor: BigDecimal = try {

                // Tenta transformar o texto em BigDecimal
                BigDecimal(texto)

            } catch (e: NumberFormatException) {

                // NumberFormatException é o erro lançado quando
                // o texto não é um número válido.
                println("[ERRO] Valor inválido. Use apenas números.")

                // continue = volta ao começo do laço e pergunta de novo
                continue
            }

            // signum() devolve -1 (negativo), 0 (zero) ou 1 (positivo).
            // Dinheiro negativo nunca é aceito na digitação.
            if (valor.signum() < 0) {

                // Avisa e repete
                println("[ERRO] O valor não pode ser negativo.")
                continue
            }

            // Se zero não é permitido e o usuário digitou zero
            if (!permiteZero && valor.signum() == 0) {

                // Avisa e repete
                println("[ERRO] O valor precisa ser maior que zero.")
                continue
            }

            // Valor válido: arredonda para 2 casas decimais e devolve.
            //
            // setScale(2, HALF_UP) = 2 centavos, arredondando
            // 0.5 para cima (regra comercial).
            return valor.setScale(2, java.math.RoundingMode.HALF_UP)
        }
    }


    // ---------------------------------------------------------
    // NÚMERO COM CASAS DECIMAIS (usado nas dimensões)
    // ---------------------------------------------------------

    // Lê um número decimal positivo (largura, altura, profundidade)
    fun lerDouble(rotulo: String): Double {

        // Repete até ser um decimal positivo válido
        while (true) {

            // Pergunta o número
            print("$rotulo: ")

            // toDoubleOrNull() é a versão segura do toDouble():
            // devolve null em vez de quebrar o programa.
            val numero: Double? = lerLinha().replace(",", ".").toDoubleOrNull()

            // Se veio null OU o número não é positivo
            if (numero == null || numero <= 0.0) {

                // Avisa e repete
                println("[ERRO] Digite um número decimal maior que zero.")
                continue
            }

            // Número válido: devolve
            return numero
        }
    }


    // ---------------------------------------------------------
    // DATA
    // ---------------------------------------------------------

    // Lê uma data no formato dd/MM/yyyy
    fun lerData(rotulo: String = "Data (dd/MM/aaaa)"): LocalDate {

        // Repete até a data ser válida
        while (true) {

            // Pergunta a data
            print("$rotulo: ")

            // Lê o texto digitado
            val texto = lerLinha()

            // try/catch para capturar data impossível (ex: 32/13/2025)
            try {

                // parse() transforma o texto em LocalDate
                // usando o formato brasileiro definido lá em cima
                return LocalDate.parse(texto, FORMATO_DATA)

            } catch (e: DateTimeParseException) {

                // DateTimeParseException = a data digitada não existe
                // ou está fora do formato esperado
                println("[ERRO] Data inválida. Use o formato dd/MM/aaaa.")
            }
        }
    }


    // ---------------------------------------------------------
    // ESCOLHA DENTRO DE UM ENUM
    // ---------------------------------------------------------

    // Função GENÉRICA que monta um menu a partir de qualquer enum.
    //
    // <T : Enum<T>> significa: T pode ser qualquer tipo,
    // desde que seja um enum. Assim a mesma função serve
    // para Cor, Material, Turno, Setor e TipoMovimentacao.
    fun <T : Enum<T>> lerEnum(rotulo: String, opcoes: Array<T>): T {

        // Mostra o título da escolha
        println("$rotulo:")

        // forEachIndexed percorre a lista dando o índice (i)
        // e o elemento (opcao) de cada volta
        opcoes.forEachIndexed { i, opcao ->

            // Mostra o número e o nome da opção.
            //
            // replace("_", " ") troca o underline por espaço,
            // deixando AZUL_FORTE mais legível como AZUL FORTE.
            println("  $i - ${opcao.name.replace("_", " ")}")
        }

        // Reaproveita lerInteiro para garantir que o número
        // digitado esteja dentro da faixa de opções válidas.
        //
        // opcoes.size - 1 porque a contagem começa em 0.
        val escolha = lerInteiro("Opção", 0, opcoes.size - 1)

        // Devolve a constante do enum na posição escolhida
        return opcoes[escolha]
    }


    // ---------------------------------------------------------
    // CONFIRMAÇÃO SIM / NÃO
    // ---------------------------------------------------------

    // Pergunta algo que só aceita S ou N
    fun confirmar(rotulo: String): Boolean {

        // Repete até o usuário digitar S ou N
        while (true) {

            // Mostra a pergunta
            print("$rotulo (S/N): ")

            // Lê a resposta e converte para MAIÚSCULA,
            // assim "s" e "S" funcionam igual
            val resposta = lerLinha().uppercase()

            // when funciona como um "switch" mais poderoso
            when (resposta) {

                // Se for S ou SIM, devolve verdadeiro
                "S", "SIM" -> return true

                // Se for N ou NAO, devolve falso
                "N", "NAO", "NÃO" -> return false

                // Qualquer outra coisa: avisa e repete
                else -> println("[ERRO] Responda apenas S ou N.")
            }
        }
    }
}
