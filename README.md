<h1 align="center"> GedApi </h1>

<p align="center">
  <img loading="lazy" src="http://img.shields.io/static/v1?label=STATUS&message=EM%20DESENVOLVIMENTO&color=GREEN&style=for-the-badge"/>
</p>

## Resumo do projeto
Projeto em desenvolvimento que será utilizado no meu TCC da ETEC Professor Camargo Aranha. É uma API que gerencia os documentos dos usuários e suas validades. Permiti a criação de usuários e vinculação deles entre si. Possui segurança via JWT e sistema de permissões.

## 🔨 Funcionalidades do projeto

- `Funcionalidade 1` `CRUD dos Documentos`: É possível inserir novos documentos, fazer download deles, atualiza-los e também excluir eles, é feita uma exclusão lógica dos documentos;
- `Funcionalidade 2` `Criação, vinculação e exclusão de Usuários`: É possível criar um novo usuário, se vincular a um outro usuário para ter ascesso aos documentos dele, caso ele permita, e excluir o seu usuário, é feita uma exclusão lógica;
- `Funcionalidade 3` `Gerenciamento de permissões dos Usuários`: Caso o usuário tenha se vinculado a outro, as permissões desse usuário são alteradas e ele apenas pode consultar os documentos, sem poder fazer criação, deleção ou atualização deles;
- `Funcionalidade 4` `Requisições via JWT`: Para todas as requisições, com exceção de criação e vinculação de usuários, é necessário informar um JWT válido para que a requisição ocorra com sucesso.

## ✔️ Técnicas e Tecnologias Utilizadas

<p align="center">
  <img loading="lazy" src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img loading="lazy" src="https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white"/>
  <img loading="lazy" src="https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens"/>
  <img loading="lazy" src="https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img loading="lazy" src="https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white"/>
</p>

<p align="center">
  <code>Paradigma de orientação a objetos</code>
  <code>Padrão Arquitetural MVC</code>
</p>

## Documentação da API

### Criar novo Usuário

#### Descrição
Este endpoint permite a criação de um novo usuário no sistema. Ele requer informações básicas como nome, email, senha e cnpj ou cpf, caso o cnpj ou cpf já exista no database a criação de usuário se tornará um pedido de vinculaç
ão a uma pessoa jurídica.

```
  POST /user/create
```
##### Exemplo de Requisição JSON para criar um Usuário

```json
{
  "username": "company",
  "nameCorporateReason": "company cool",
  "email": "comp@gmail.com",
  "password": "comp123",
  "cnpjCpf": "12345678901234",
  "cnae": "1234567"
}
```

##### Exemplo de Requisição JSON para se vincular a um Usuário

```json
{
  "username": "employee",
  "email": "emp@gmail.com",
  "password": "emp123",
  "cnpjCpf": "12345678901234"
}
```

### Gerar um novo Token de Acesso

```
  POST /user/token
```
#### Descrição
Necessário uma autenticação básica com username e senha.

##### Exemplo de Retorno

```json
{
    "accessToken": "eyJhbGciOiJSUzI1NiJ9",
    "expiresIn": "18:52:34"
}
```

### Listar Usuários que querem se Vincular

```
  GET /user/allowUserLink
```
#### Descrição
Necessário Token, lista os usuários que querem se vincular a sua conta.

##### Exemplo de Retorno

```json
[
    {
        "username": "employee",
        "email": "emp@gmail.com",
        "excluded": false,
        "approvedRequest": false
    }
]
```

### Permitir vínculo de Usuário pelo Username

```
  POST /user/allowUserLink
```
#### Descrição
Necessário Token, permiti o vínculo de um Usuário aos seus documentos.

##### Exemplo de Retorno

```json
[
    {
        "username": "employee",
        "email": "emp@gmail.com",
        "excluded": false,
        "approvedRequest": true
    }
]
```

| Parâmetro   | Tipo       | Descrição                                   |
| :---------- | :--------- | :------------------------------------------ |
| `usernameToAllowLinking`      | `string` | **Obrigatório**. Nome do usuário que quer permitir |

### Permitir vínculo de Usuário pelo Username

```
  DELETE /user
```
#### Descrição
Necessário Token, deleta a conta atual do Usuário informado no token e impossibilita de realizar outras requisições.

##### Exemplo de Retorno

```json
{
    "userId": "730f7df2-650f-45e8-838e-c849e6981f9f",
    "clientId": "4a6da36c-c5f2-445e-b5e1-3284804d76c2",
    "username": "user",
    "nameCorporateReason": null,
    "email": "EXCLUDED",
    "cnpjCpf": "2",
    "cnae": null,
    "excluded": true,
    "role": {
        "id": 1,
        "roleName": "CLIENT",
        "description": "This permission grants access to all API methods"
    }
}
```
## Próximos passos
- [ ] Implementar lógica de RefreshToken para o JWT
- [ ] Substituir atributo version da entidade Document para se tornar uma nova entidade e criar um relacionamento mais limpo e organizado

## 📁 Acesso ao projeto
Você pode acessar os arquivos do projeto clicando [aqui](https://github.com/gui-lirasilva/Edige-POO/tree/master/src).
