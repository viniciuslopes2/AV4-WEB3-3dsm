# AutoBots - Automanager

Este projeto é a atividade prática ATVIV da disciplina de Desenvolvimento Web III, da FATEC São José dos Campos, sob orientação do Prof. Dr. Eng. Gerson Penha. Ele dá continuidade à ATVIII, em que foi feita a "atualização de base" do micro-serviço automanager, construído com Java e Spring Boot.

Nesta etapa foi feita a "atualização segurança": o sistema passou a ter autenticação e autorização via Json Web Token (JWT). O usuário faz login com nome de usuário e senha, recebe um token e precisa enviá-lo em todas as requisições. Cada rota só pode ser acessada pelos perfis definidos pelos investidores (Tabela 1 da atividade).

## Versões utilizadas

- Java 17
- Spring Boot 2.7.0
- Spring Security
- Spring HATEOAS
- jjwt 0.9.1
- Maven (gerenciador de dependências e build)
- Banco de dados H2 (em memória)

## Como rodar o projeto

### Opção 1: Spring Boot Dashboard (VS Code)

1. Instale a extensão **Spring Boot Dashboard** na aba de extensões do VS Code.
2. Abra a pasta `automanager` no VS Code.
3. Clique no ícone da folha do Spring Boot na barra lateral esquerda.
4. Localize o projeto `automanager` na lista e clique no botão de play ao lado dele.
5. Aguarde o log mostrar que a aplicação subiu na porta `8080`.

### Opção 2: Terminal

Na pasta `automanager`:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

O projeto usa H2 em memória, então não é preciso instalar nada. Os dados voltam ao estado inicial sempre que a aplicação reinicia.

## Usuários cadastrados ao iniciar

Ao iniciar, a aplicação cadastra uma empresa de exemplo com mercadorias, serviços, um veículo, duas vendas e um usuário para cada perfil. A senha de todos é `123456`. As senhas são gravadas criptografadas com BCrypt e nunca aparecem nas respostas.

| Perfil | Nome de usuário |
|---|---|
| Administrador | `admin` |
| Gerente | `donateresagerente` |
| Vendedor | `dompedrovendedor` |
| Cliente | `dompedrocliente` |
| Fornecedor | `dompedrofornecedor` |

## Autenticação

`POST /login`

```json
{ "nomeUsuario": "admin", "senha": "123456" }
```

Se o usuário e a senha estiverem corretos, a resposta vem com status `200` e o token no cabeçalho `Authorization`, no formato `Bearer <token>`. Esse valor deve ser enviado no cabeçalho `Authorization` de todas as outras requisições. O token expira em 10 minutos (`jwt.expiration` no `application.properties`).

Login com senha errada, usuário inexistente ou credencial inativa responde `401 Unauthorized`.

## Perfis e autorizações

Perfis possíveis: `ROLE_ADMIN`, `ROLE_GERENTE`, `ROLE_VENDEDOR`, `ROLE_CLIENTE` e `ROLE_FORNECEDOR`.

| Perfil | Autorizações |
|---|---|
| Administrador | Todas as operações de CRUD na aplicação, incluindo adicionar ou remover usuários administradores. |
| Gerente | CRUD de usuários dos perfis gerente, vendedor e cliente. CRUD de serviços, vendas e mercadorias. |
| Vendedor | CRUD de usuários do perfil cliente. Leitura de serviços e mercadorias. Cadastro de vendas feitas por ele mesmo e leitura das suas vendas. |
| Cliente | Leitura do seu próprio cadastro. Leitura das vendas em que ele foi o cliente. |
| Fornecedor | Continua existindo para vincular mercadorias, como na ATVIII, mas não tem autorização em nenhuma rota. Só o administrador gerencia fornecedores. |

O CRUD de usuários vale também para as credenciais desses usuários. Um gerente ou vendedor não pode cadastrar, ver, alterar ou excluir usuários de outros perfis, nem dar a um usuário um perfil que ele não pode gerenciar (um gerente não consegue criar ou promover um administrador, por exemplo).

## Rotas disponíveis

| Entidade | Buscar por id | Listar | Cadastrar | Atualizar | Excluir |
|---|---|---|---|---|---|
| Empresa | `GET /empresa/empresa/{id}` | `GET /empresa/empresas` | `POST /empresa/cadastro` | `PUT /empresa/atualizar` | `DELETE /empresa/excluir` |
| Usuário | `GET /usuario/usuario/{id}` | `GET /usuario/usuarios` | `POST /usuario/cadastro/{idEmpresa}` | `PUT /usuario/atualizar` | `DELETE /usuario/excluir` |
| Credencial | `GET /credencial/credencial/{id}` | `GET /credencial/credenciais` | `POST /credencial/cadastro/{idUsuario}` | `PUT /credencial/atualizar` | `DELETE /credencial/excluir` |
| Veículo | `GET /veiculo/veiculo/{id}` | `GET /veiculo/veiculos` | `POST /veiculo/cadastro/{idProprietario}` | `PUT /veiculo/atualizar` | `DELETE /veiculo/excluir` |
| Mercadoria | `GET /mercadoria/mercadoria/{id}` | `GET /mercadoria/mercadorias` | `POST /mercadoria/cadastro/{idEmpresa}` | `PUT /mercadoria/atualizar` | `DELETE /mercadoria/excluir` |
| Serviço | `GET /servico/servico/{id}` | `GET /servico/servicos` | `POST /servico/cadastro/{idEmpresa}` | `PUT /servico/atualizar` | `DELETE /servico/excluir` |
| Venda | `GET /venda/venda/{id}` | `GET /venda/vendas` | `POST /venda/cadastro/{idEmpresa}` | `PUT /venda/atualizar` | `DELETE /venda/excluir` |

Quem pode acessar cada rota:

| Entidade | Leitura | Cadastro | Atualização e exclusão |
|---|---|---|---|
| Empresa | Administrador | Administrador | Administrador |
| Veículo | Administrador | Administrador | Administrador |
| Usuário | Administrador, gerente e vendedor (só os perfis que gerenciam). Cliente só o próprio cadastro | Administrador, gerente e vendedor (só os perfis que gerenciam) | Administrador, gerente e vendedor (só os perfis que gerenciam) |
| Credencial | Igual a usuário, sem o cliente | Igual a usuário | Igual a usuário |
| Mercadoria | Administrador, gerente e vendedor | Administrador e gerente | Administrador e gerente |
| Serviço | Administrador, gerente e vendedor | Administrador e gerente | Administrador e gerente |
| Venda | Administrador e gerente (todas), vendedor (as que ele fez), cliente (as que ele comprou) | Administrador, gerente e vendedor (só em nome dele mesmo) | Administrador e gerente |

As listagens já vêm filtradas: um vendedor que chama `GET /usuario/usuarios` recebe só os clientes, e um cliente que chama `GET /venda/vendas` recebe só as suas compras.

Regras gerais (mantidas da ATVIII):

- No cadastro, o corpo não deve conter `id`, pois ele é gerado pelo banco.
- Em `PUT` e `DELETE`, o corpo precisa conter o `id` do registro.
- No `PUT`, apenas os campos enviados são alterados.
- Na venda, o cliente precisa ter o perfil `ROLE_CLIENTE` e o funcionário, o perfil `ROLE_VENDEDOR`.
- Para vincular uma mercadoria a um fornecedor, acrescente `?fornecedor={idUsuario}` na rota de cadastro. O usuário precisa ter o perfil `ROLE_FORNECEDOR`.

## Exemplos de corpo das requisições

### Cadastrar usuário com credencial

`POST /usuario/cadastro/{idEmpresa}`

```json
{
  "nome": "Maria da Silva",
  "nomeSocial": "Maria",
  "perfis": ["ROLE_CLIENTE"],
  "emails": [{ "endereco": "maria@email.com" }],
  "telefones": [{ "ddd": "012", "numero": "988887777" }],
  "documentos": [{ "tipo": "CPF", "dataEmissao": "2020-05-10", "numero": "11122233344" }],
  "credenciais": [{ "tipo": "usuarioSenha", "nomeUsuario": "maria", "senha": "123456", "inativo": false }]
}
```

### Trocar senha ou desativar uma credencial

`PUT /credencial/atualizar`

```json
{ "tipo": "usuarioSenha", "id": 1, "senha": "novaSenha", "inativo": false }
```

Com `"inativo": true` o usuário não consegue mais fazer login com essa credencial.

### Cadastrar venda (logado como vendedor)

`POST /venda/cadastro/{idEmpresa}`

```json
{
  "identificacao": "VENDA-0001",
  "cliente": { "id": 3 },
  "funcionario": { "id": 1 },
  "veiculo": { "id": 1 },
  "mercadorias": [{ "id": 1 }],
  "servicos": [{ "id": 1 }]
}
```

Se o vendedor logado informar outro funcionário, a resposta é `403 Forbidden`.

## Status de resposta

| Situação | Status |
|---|---|
| Login correto | `200 OK` com o token no cabeçalho `Authorization` |
| Login incorreto ou credencial inativa | `401 Unauthorized` |
| Requisição sem token, com token inválido ou expirado | `403 Forbidden` |
| Perfil sem autorização para a rota ou para o registro | `403 Forbidden` |
| Buscar e listar | `302 Found` ou `404 Not Found` |
| Cadastrar | `201 Created`, `409 Conflict` ou `400 Bad Request` |
| Atualizar e excluir | `200 OK` ou `400 Bad Request` |

## Testando a API

As buscas respondem com status `302 Found`. Desative a opção de seguir redirecionamentos na ferramenta de testes (no Insomnia: Preferences, General, desmarcar "Follow redirects").

Roteiro sugerido:

1. `POST /login` com `admin` e copiar o valor do cabeçalho `Authorization` da resposta.
2. Enviar esse valor no cabeçalho `Authorization` e chamar `GET /usuario/usuarios` para ver todos os usuários.
3. Fazer login com `dompedrovendedor` e chamar `GET /usuario/usuarios`: só aparecem clientes.
4. Ainda como vendedor, tentar `POST /servico/cadastro/1`: a resposta é `403`.
5. Fazer login com `dompedrocliente` e chamar `GET /venda/vendas`: só aparecem as vendas desse cliente.
6. Ainda como cliente, chamar `GET /usuario/usuario/{id}` com o próprio id (`302`) e com o id de outro usuário (`403`).
7. Fazer login com `donateresagerente` e tentar cadastrar um usuário com perfil `ROLE_ADMIN`: a resposta é `403`.

## Troubleshooting

### `403 Forbidden` em todas as rotas

O token não foi enviado, está com o formato errado ou expirou. O cabeçalho precisa ser `Authorization: Bearer <token>`. Faça login novamente para gerar outro token.

### `401 Unauthorized` no login

Usuário ou senha incorretos, ou a credencial está inativa.

### Resposta vazia ou estranha nas buscas

A ferramenta está seguindo o redirecionamento do status `302`. Desative a opção "Follow redirects".

### `Port 8080 was already in use`

Já existe uma execução anterior do projeto na porta `8080`. Encerre-a e inicie novamente.

### Erros de getters e setters não encontrados

O Lombok não está ativo na IDE. No VS Code, instale o **Extension Pack for Java**, que já inclui o suporte.
