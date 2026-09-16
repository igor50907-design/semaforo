# Sistema de Gestão — Caixas d'Água (Kotlin + PostgreSQL)

Trabalho de Kotlin com persistência em PostgreSQL, menus interativos via
console, controle de estoque, gestão de pessoas por setores e fluxo de
caixa encapsulado.

---

## Como rodar

### 1. Criar o banco

Abra o pgAdmin (ou o `psql`) e execute:

```sql
CREATE DATABASE caixadagua;
```

Conecte-se ao banco `caixadagua` e rode o arquivo `banco/schema.sql` inteiro.
Ele cria as 6 tabelas, a view de saldo e já insere dados de teste
(4 funcionários, 1 fornecedor, 2 clientes, 2 produtos e um aporte de
R$ 10.000,00 no caixa).

### 2. Conferir a conexão

O arquivo `src/repositorio/ConexaoPostgres.kt` traz os valores padrão:

| Item    | Valor                                            |
|---------|--------------------------------------------------|
| usuário | `postgres`                                       |
| senha   | `postgres`                                       |
| URL     | `jdbc:postgresql://localhost:5432/caixadagua`    |

Se a sua senha do PostgreSQL for outra, altere lá.

### 3. Adicionar o driver JDBC

O projeto precisa do `postgresql-42.x.jar` nas bibliotecas do módulo
(**File → Project Structure → Libraries**). Sem ele o sistema mostra a
mensagem "Driver do PostgreSQL não encontrado".

### 4. Executar

Rode a função `main()` em `src/Main.kt`.

---

## Estrutura do projeto

```
src/
├── Main.kt                    Ponto de entrada, try/catch/finally global
│
├── enumeradores/              Valores fixos do domínio
│   ├── Cor.kt                 4 cores da caixa
│   ├── Material.kt            3 materiais
│   ├── Turno.kt               3 turnos de trabalho
│   ├── Habilidade.kt          4 especialidades
│   ├── Setor.kt               4 setores + regra de quem aprova caixa
│   └── TipoMovimentacao.kt    6 tipos + sinal (+1 entra / -1 sai)
│
├── pessoas/                   Herança: Pessoa é a classe-mãe
│   ├── Pessoa.kt              open, receberConta() polimórfica
│   ├── Funcionario.kt         : Pessoa   — tem setor, salário, turno
│   ├── Instalador.kt          : Funcionario — 3º nível de herança
│   ├── Cliente.kt             : Pessoa   — parcelas encapsuladas
│   └── Fornecedor.kt          : Pessoa   — pessoa jurídica (CNPJ)
│
├── produto/
│   ├── CaixaDaAgua.kt         Produto com estoque de escrita privada
│   └── Servico.kt             Instalação com status controlado
│
├── financeiro/
│   ├── Movimentacao.kt        Os 6 dados exigidos, todos imutáveis
│   └── Caixa.kt               SINGLETON: único ponto que mexe em dinheiro
│
├── repositorio/               Camada de acesso ao banco (JDBC puro)
│   ├── ConexaoPostgres.kt     abstract — conectar() / desconectar()
│   ├── InterfaceJPA.kt        interface genérica <T> — o contrato CRUD
│   ├── CRUDCaixaDAgua.kt
│   ├── CRUDMovimentacao.kt    editar/excluir BLOQUEADOS de propósito
│   ├── CRUDFuncionario.kt     + listarPorSetor()
│   ├── CRUDCliente.kt
│   ├── CRUDFornecedor.kt
│   └── CRUDServico.kt         usa JOIN de 4 tabelas
│
├── validacao/
│   └── Validador.kt           REGEX + TRY + NULLABLE — o "porteiro"
│
└── sistema/                   Camada de apresentação (menus)
    ├── Sessao.kt              Quem está operando = o responsável
    ├── MenuInicial.kt         Menu principal
    ├── MenuPessoas.kt         Funcionários, clientes e fornecedores
    ├── MenuProduto.kt         Produto e estoque
    ├── MenuServico.kt         Agendar / concluir instalação
    └── MenuFinanceiro.kt      Compra, venda, salário, despesa, extrato

banco/
└── schema.sql                 Criação das tabelas + dados de teste
```

---

## Onde cada requisito foi atendido

| Requisito do enunciado                        | Onde está |
|-----------------------------------------------|-----------|
| Menus interativos via console                 | `sistema/Menu*.kt` |
| Todos os dados persistidos no banco           | `repositorio/CRUD*.kt` |
| Fluxo de produto (compra, venda, estoque)     | `MenuFinanceiro.kt` + `CaixaDaAgua.kt` |
| Fluxo de serviço (montagem/instalação)        | `MenuServico.kt` + `Servico.kt` |
| Gestão de pessoas do negócio                  | `pessoas/` + `MenuPessoas.kt` |
| Funcionários divididos em setores (mín. 2)    | `enumeradores/Setor.kt` (4 setores) |
| Fluxo de caixa seguro / encapsulamento        | `financeiro/Caixa.kt` (singleton, saldo privado) |
| Quanto dinheiro foi usado                     | `Movimentacao.valor` |
| Quem foi o pagador                            | `Movimentacao.pagador` |
| Quem recebeu                                  | `Movimentacao.recebedor` |
| A data e a hora                               | `Movimentacao.dataHora` (LocalDateTime) |
| O motivo / descrição                          | `Movimentacao.motivo` |
| Um responsável pela transação                 | `Movimentacao.responsavel` (vem da `Sessao`) |
| À prova de falha humana (REGEX/TRY/NULLABLE)  | `validacao/Validador.kt` + CHECKs no `schema.sql` |

---

## Regras de segurança do caixa

1. O saldo é `private` dentro do `object Caixa`. Nenhuma outra classe consegue
   escrever nele.
2. Só os setores **FINANCEIRO** e **ADMINISTRATIVO** autorizam movimentações.
3. Quem chama o caixa passa o valor **sempre positivo**. Quem decide o sinal é
   o próprio `Caixa`, usando o `sinal` do enum `TipoMovimentacao`.
4. O caixa nunca fica negativo: a operação é recusada antes de gravar.
5. O saldo em memória só é atualizado **depois** que o banco confirma o INSERT.
   Se o banco falhar, nada muda.
6. Estoque só sobe com compra registrada e só desce com venda registrada.
7. Movimentação financeira **não pode ser editada nem excluída** — a correção
   se faz por estorno.
