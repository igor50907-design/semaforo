-- =====================================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS
-- Sistema de Gestão de Caixas d'Água (Kotlin + PostgreSQL)
-- =====================================================================
--
-- COMO USAR:
--   1. Abra o pgAdmin ou o terminal psql
--   2. Crie o banco:      CREATE DATABASE caixadagua;
--   3. Conecte no banco:  \c caixadagua
--   4. Rode este arquivo inteiro
--
-- O nome do banco precisa bater com a URL que está em
-- src/repositorio/ConexaoPostgres.kt
-- =====================================================================


-- Apaga as tabelas antigas antes de recriar.
--
-- A ordem importa: primeiro as tabelas que dependem
-- das outras (servico), depois as independentes.
-- CASCADE derruba junto tudo que aponta para elas.
DROP TABLE IF EXISTS servico CASCADE;
DROP TABLE IF EXISTS movimentacao CASCADE;
DROP TABLE IF EXISTS caixa_da_agua CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS funcionario CASCADE;
DROP TABLE IF EXISTS fornecedor CASCADE;


-- =====================================================================
-- TABELA: fornecedor
-- Empresa (pessoa jurídica) de quem a nossa empresa COMPRA as caixas
-- =====================================================================
CREATE TABLE fornecedor (

    -- SERIAL cria um número que se incrementa sozinho a cada INSERT.
    -- PRIMARY KEY faz dele o identificador único da linha.
    id            SERIAL PRIMARY KEY,

    -- Nome da empresa. NOT NULL = não pode ficar em branco.
    razao_social  VARCHAR(120) NOT NULL,

    -- CNPJ com 14 dígitos.
    -- UNIQUE impede cadastrar o mesmo CNPJ duas vezes.
    -- CHECK aplica a mesma REGEX que existe no Kotlin,
    -- garantindo a regra também no banco.
    cnpj          CHAR(14) NOT NULL UNIQUE
                  CHECK (cnpj ~ '^[0-9]{14}$'),

    -- E-mail validado por expressão regular no próprio banco.
    -- O operador ~ no PostgreSQL significa "casa com esta regex".
    email         VARCHAR(120) NOT NULL
                  CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),

    -- Telefone com 10 ou 11 dígitos
    telefone      VARCHAR(11) NOT NULL
                  CHECK (telefone ~ '^[0-9]{10,11}$')
);


-- =====================================================================
-- TABELA: funcionario
-- As pessoas que trabalham na empresa, divididas em SETORES
-- =====================================================================
CREATE TABLE funcionario (

    -- Identificador único gerado pelo banco
    id           SERIAL PRIMARY KEY,

    -- Nome do funcionário
    nome         VARCHAR(80) NOT NULL,

    -- CPF com 11 dígitos, único e validado por regex
    cpf          CHAR(11) NOT NULL UNIQUE
                 CHECK (cpf ~ '^[0-9]{11}$'),

    -- Idade entre 16 e 90 anos.
    -- 16 é a idade mínima legal para trabalhar no Brasil.
    idade        INTEGER NOT NULL CHECK (idade BETWEEN 16 AND 90),

    -- E-mail validado por regex
    email        VARCHAR(120) NOT NULL
                 CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),

    -- Telefone validado por regex
    telefone     VARCHAR(11) NOT NULL
                 CHECK (telefone ~ '^[0-9]{10,11}$'),

    -- Salário mensal.
    -- NUMERIC(12,2) guarda até 12 dígitos, sendo 2 de centavos.
    -- É o tipo equivalente ao BigDecimal do Kotlin.
    -- Nunca use FLOAT para dinheiro: perde centavos.
    salario      NUMERIC(12,2) NOT NULL CHECK (salario >= 0),

    -- SETOR do funcionário.
    -- O CHECK IN só aceita os valores que existem no enum Setor.kt,
    -- impedindo que um setor inventado entre no banco.
    setor        VARCHAR(20) NOT NULL
                 CHECK (setor IN ('FINANCEIRO', 'ADMINISTRATIVO',
                                  'LOGISTICA', 'INSTALACAO')),

    -- Turno de trabalho, espelhando o enum Turno.kt
    turno        VARCHAR(20) NOT NULL
                 CHECK (turno IN ('MATUTINO', 'VESPERTINO', 'NOTURNO')),

    -- Habilidade técnica, espelhando o enum Habilidade.kt
    habilidade   VARCHAR(20) NOT NULL
                 CHECK (habilidade IN ('INSTALACAO', 'FINANCEIRO',
                                       'ADMINISTRATIVO', 'LOGISTICA'))
);


-- =====================================================================
-- TABELA: cliente
-- As pessoas que COMPRAM a caixa d'água e contratam a instalação
-- =====================================================================
CREATE TABLE cliente (

    -- Identificador único gerado pelo banco
    id                SERIAL PRIMARY KEY,

    -- Nome do cliente
    nome              VARCHAR(80) NOT NULL,

    -- CPF único, validado por regex
    cpf               CHAR(11) NOT NULL UNIQUE
                      CHECK (cpf ~ '^[0-9]{11}$'),

    -- Idade a partir de 18 anos (maioridade para contratar)
    idade             INTEGER NOT NULL CHECK (idade BETWEEN 18 AND 120),

    -- E-mail validado por regex
    email             VARCHAR(120) NOT NULL
                      CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),

    -- Telefone validado por regex
    telefone          VARCHAR(11) NOT NULL
                      CHECK (telefone ~ '^[0-9]{10,11}$'),

    -- ARRAY com as parcelas que o cliente ainda deve.
    --
    -- NUMERIC(12,2)[] é uma COLUNA QUE GUARDA UMA LISTA.
    -- No Kotlin isso vira MutableList<BigDecimal>.
    --
    -- DEFAULT '{}' faz o cliente nascer sem nenhuma dívida.
    parcelas_a_pagar  NUMERIC(12,2)[] NOT NULL DEFAULT '{}'
);


-- =====================================================================
-- TABELA: caixa_da_agua
-- O PRODUTO vendido pela empresa, com controle de estoque
-- =====================================================================
CREATE TABLE caixa_da_agua (

    -- Identificador único gerado pelo banco
    id                  SERIAL PRIMARY KEY,

    -- Marca do fabricante
    marca               VARCHAR(60) NOT NULL,

    -- Modelo da caixa
    modelo              VARCHAR(60) NOT NULL,

    -- ARRAY com as três dimensões: largura, altura e profundidade.
    --
    -- FLOAT8[] é um array de "double precision".
    -- No Kotlin isso vira MutableList<Double>.
    --
    -- O CHECK garante que sempre existam exatamente 3 medidas.
    -- array_length(dimensao, 1) conta os itens do array.
    dimensao            FLOAT8[] NOT NULL
                        CHECK (array_length(dimensao, 1) = 3),

    -- Cor, espelhando o enum Cor.kt
    cor                 VARCHAR(20) NOT NULL
                        CHECK (cor IN ('AZUL_FORTE', 'AZUL_FRACO',
                                       'BRANCO', 'CINZA')),

    -- Material, espelhando o enum Material.kt
    material            VARCHAR(20) NOT NULL
                        CHECK (material IN ('POLIETILENO',
                                            'FIBRA_DE_VIDRO', 'INOX')),

    -- Formato físico da caixa
    formato             VARCHAR(40) NOT NULL,

    -- Quanto a empresa paga por unidade
    preco_custo         NUMERIC(12,2) NOT NULL CHECK (preco_custo >= 0),

    -- Por quanto a empresa vende por unidade
    preco_venda         NUMERIC(12,2) NOT NULL CHECK (preco_venda >= 0),

    -- Quantidade em estoque.
    --
    -- O CHECK >= 0 é a trava FINAL do estoque: mesmo que
    -- alguém tente rodar um UPDATE errado direto no banco,
    -- o PostgreSQL recusa deixar o estoque negativo.
    quantidade_estoque  INTEGER NOT NULL DEFAULT 0
                        CHECK (quantidade_estoque >= 0),

    -- De qual fornecedor essa caixa veio.
    --
    -- REFERENCES cria uma CHAVE ESTRANGEIRA: o número aqui
    -- precisa existir na tabela fornecedor.
    --
    -- ON DELETE SET NULL: se o fornecedor for apagado,
    -- a caixa continua existindo, só fica sem fornecedor.
    --
    -- Pode ser NULL (não tem NOT NULL), o que no Kotlin
    -- corresponde ao tipo Int? (nullable).
    fornecedor_id       INTEGER REFERENCES fornecedor(id)
                        ON DELETE SET NULL
);


-- =====================================================================
-- TABELA: movimentacao
-- O CORAÇÃO DO TRABALHO: o registro de todo dinheiro
-- que entra e sai da empresa
-- =====================================================================
CREATE TABLE movimentacao (

    -- Identificador único gerado pelo banco
    id            SERIAL PRIMARY KEY,

    -- REQUISITO 1: QUANTO DINHEIRO FOI USADO.
    --
    -- O sinal já vem aplicado pelo Kotlin:
    --   positivo = entrada de dinheiro
    --   negativo = saída de dinheiro
    --
    -- Por isso NÃO existe CHECK (valor > 0) aqui.
    -- O CHECK <> 0 impede apenas lançamento de valor zero,
    -- que não faria sentido nenhum.
    valor         NUMERIC(12,2) NOT NULL CHECK (valor <> 0),

    -- REQUISITO 2: QUEM FOI O PAGADOR
    pagador       VARCHAR(120) NOT NULL,

    -- REQUISITO 3: QUEM RECEBEU
    recebedor     VARCHAR(120) NOT NULL,

    -- REQUISITO 4: A DATA E A HORA.
    --
    -- TIMESTAMP guarda data E hora juntas.
    -- DEFAULT CURRENT_TIMESTAMP preenche sozinho se o
    -- programa esquecer de mandar o horário.
    data_hora     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- REQUISITO 5: O MOTIVO / DESCRIÇÃO
    motivo        VARCHAR(200) NOT NULL,

    -- REQUISITO 6: O RESPONSÁVEL PELA TRANSAÇÃO
    responsavel   VARCHAR(120) NOT NULL,

    -- Classificação da operação, espelhando o enum TipoMovimentacao.kt
    tipo          VARCHAR(30) NOT NULL
                  CHECK (tipo IN ('VENDA_PRODUTO', 'VENDA_SERVICO',
                                  'RECEBIMENTO_PAGAMENTO',
                                  'COMPRA_PRODUTO', 'PAGAMENTO_SALARIO',
                                  'PAGAMENTO_DESPESA'))
);


-- Índice que acelera as consultas por data.
--
-- Sem ele, o banco leria a tabela inteira toda vez
-- que o extrato fosse ordenado por data.
CREATE INDEX idx_movimentacao_data ON movimentacao (data_hora);


-- =====================================================================
-- TABELA: servico
-- A INSTALAÇÃO da caixa d'água na casa do cliente
-- =====================================================================
CREATE TABLE servico (

    -- Identificador único gerado pelo banco
    id               SERIAL PRIMARY KEY,

    -- Cliente que contratou.
    --
    -- ON DELETE RESTRICT: o banco RECUSA apagar um cliente
    -- que ainda tem serviço vinculado. É uma trava de
    -- integridade contra perda de histórico.
    cliente_id       INTEGER NOT NULL
                     REFERENCES cliente(id) ON DELETE RESTRICT,

    -- Funcionário (instalador) responsável pela execução
    instalador_id    INTEGER NOT NULL
                     REFERENCES funcionario(id) ON DELETE RESTRICT,

    -- Caixa d'água que será instalada
    caixa_id         INTEGER NOT NULL
                     REFERENCES caixa_da_agua(id) ON DELETE RESTRICT,

    -- Data marcada para a instalação (só data, sem hora)
    data_instalacao  DATE NOT NULL,

    -- Preço da mão de obra
    preco            NUMERIC(12,2) NOT NULL CHECK (preco >= 0),

    -- Situação atual do serviço
    status           VARCHAR(20) NOT NULL DEFAULT 'AGENDADO'
                     CHECK (status IN ('AGENDADO', 'CONCLUIDO', 'CANCELADO'))
);


-- =====================================================================
-- VIEW: v_saldo_caixa
-- Uma VIEW é uma consulta salva que se comporta como tabela.
-- Serve para conferir o saldo direto no pgAdmin.
-- =====================================================================
CREATE OR REPLACE VIEW v_saldo_caixa AS
SELECT
    -- COALESCE troca NULL por 0.
    -- Necessário porque SUM() devolve NULL na tabela vazia.
    COALESCE(SUM(valor), 0) AS saldo_atual,

    -- FILTER aplica a soma só nas linhas que passam no teste.
    -- Aqui somamos apenas as entradas (valor positivo).
    COALESCE(SUM(valor) FILTER (WHERE valor > 0), 0) AS total_entradas,

    -- E aqui somamos apenas as saídas (valor negativo)
    COALESCE(SUM(valor) FILTER (WHERE valor < 0), 0) AS total_saidas,

    -- Conta quantos lançamentos existem no total
    COUNT(*) AS total_lancamentos
FROM movimentacao;


-- =====================================================================
-- DADOS INICIAIS DE TESTE
-- Servem para o sistema já abrir com algo cadastrado.
-- =====================================================================

-- Dois funcionários: um de cada setor que pode aprovar caixa,
-- mais um instalador e um da logística.
INSERT INTO funcionario
    (nome, cpf, idade, email, telefone, salario, setor, turno, habilidade)
VALUES
    ('Ana Souza',    '52998224725', 34, 'ana@empresa.com.br',
     '11987654321', 4500.00, 'FINANCEIRO',     'MATUTINO',   'FINANCEIRO'),

    ('Bruno Lima',   '11144477735', 41, 'bruno@empresa.com.br',
     '11912345678', 5200.00, 'ADMINISTRATIVO', 'MATUTINO',   'ADMINISTRATIVO'),

    ('Carlos Dias',  '39053344705', 28, 'carlos@empresa.com.br',
     '11955554444', 2800.00, 'INSTALACAO',     'VESPERTINO', 'INSTALACAO'),

    ('Diego Rocha',  '12345678909', 30, 'diego@empresa.com.br',
     '11933332222', 3100.00, 'LOGISTICA',      'NOTURNO',    'LOGISTICA');


-- Um fornecedor de exemplo
INSERT INTO fornecedor (razao_social, cnpj, email, telefone)
VALUES ('Fortlev Distribuidora LTDA', '11222333000181',
        'vendas@fortlev.com.br', '1133224455');


-- Dois clientes de exemplo, ambos sem dívidas
INSERT INTO cliente (nome, cpf, idade, email, telefone)
VALUES
    ('Maria Silva',  '82286181000', 45, 'maria@gmail.com',  '11988887777'),
    ('Joao Pereira', '65496114014', 52, 'joao@hotmail.com', '11977776666');


-- Dois produtos, ambos começando com estoque ZERO.
--
-- O estoque só sobe quando uma COMPRA for registrada
-- pelo menu financeiro, gerando a movimentação correspondente.
INSERT INTO caixa_da_agua
    (marca, modelo, dimensao, cor, material, formato,
     preco_custo, preco_venda, quantidade_estoque, fornecedor_id)
VALUES
    ('Fortlev', '1000L', ARRAY[1.45, 0.95, 1.45], 'AZUL_FORTE',
     'POLIETILENO', 'Redonda', 380.00, 620.00, 0, 1),

    ('Acqualimp', '500L', ARRAY[1.10, 0.80, 1.10], 'BRANCO',
     'FIBRA_DE_VIDRO', 'Cilindrica', 260.00, 440.00, 0, 1);


-- Um aporte inicial do dono para o caixa não começar zerado.
--
-- Sem esse dinheiro, a primeira COMPRA seria recusada
-- pela trava de saldo insuficiente do objeto Caixa.
INSERT INTO movimentacao
    (valor, pagador, recebedor, motivo, responsavel, tipo)
VALUES
    (10000.00, 'Socio investidor', 'EMPRESA',
     'Capital inicial da empresa', 'Bruno Lima',
     'RECEBIMENTO_PAGAMENTO');


-- =====================================================================
-- CONSULTAS ÚTEIS PARA CONFERIR SE DEU CERTO
-- =====================================================================
-- SELECT * FROM v_saldo_caixa;
-- SELECT * FROM movimentacao ORDER BY data_hora;
-- SELECT setor, COUNT(*) FROM funcionario GROUP BY setor;
-- SELECT marca, modelo, quantidade_estoque FROM caixa_da_agua;
-- =====================================================================
