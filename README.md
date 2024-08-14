<h1 align="center"> GedApi </h1>

<p align="center">
  <img loading="lazy" src="http://img.shields.io/static/v1?label=STATUS&message=EM%20DESENVOLVIMENTO&color=GREEN&style=for-the-badge"/>
</p>

## Resumo do projeto
Projeto em desenvolvimento que será utilizado no meu TCC da ETEC Professor Camargo Aranha sobre o gerenciamento de documentos, para auxiliar no pedido do benefício aduaneiro Drawback. É uma API que gerencia os documentos dos usuários e suas validades. Permiti a criação de usuários e vinculação deles entre si. Possui segurança via JWT e sistema de permissões.

## 🔨 Funcionalidades do projeto

- `Funcionalidade 1` `CRUD dos Documentos`: É possível inserir novos documentos, fazer download deles, atualiza-los e também excluir, é feita uma exclusão lógica dos documentos;
- `Funcionalidade 2` `Criação, vinculação e exclusão de Usuários`: É possível criar um novo usuário, se vincular a um outro usuário para ter ascesso aos documentos dele (caso ele permita), e excluir o seu usuário, é feita uma exclusão lógica;
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
Este endpoint permite a criação de um novo usuário no sistema. Ele requer informações básicas como nome, email, senha e CNPJ ou CPF, caso o CNPJ já exista no database a criação de usuário se tornará um pedido de vinculação a uma pessoa jurídica.
```
  POST /user/create
```
##### Exemplo de Requisição JSON para criar um Usuário com CNPJ
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
##### Exemplo de Requisição JSON para criar um Usuário com CPF
```json
{
  "username": "user",
  "nameCorporateReason": null,
  "email": "user@gmail.com",
  "password": "user123",
  "cnpjCpf": "12345678910",
  "cnae": null
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
  POST /user/allowUserLink?usernameToAllowLinking=
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

### Deleter Usuário

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
    "email": "user@gmail.com",
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
### Listar Documentos do Usuário

```
  GET /document/find
```
#### Descrição
Necessário Token, lista todos os Documentos do Usuário, até aqueles que foram excluídos.

##### Exemplo de Retorno

```json
[
    {
        "uuid": "3252f6c0-996a-429c-8598-14000976cc81",
        "name": "test",
        "guideName": "test-comp",
        "extension": "txt",
        "version": 2,
        "validity": "2001-07-10",
        "creation": "2024-07-24",
        "updated": null,
        "exclusion": null
    },
    {
        "uuid": "78e383d9-e1a5-4bf9-95e7-58a8584d6c76",
        "name": "test",
        "guideName": "test-comp_V1",
        "extension": "txt",
        "version": 1,
        "validity": "2024-09-30",
        "creation": "2024-07-24",
        "updated": "2024-07-24",
        "exclusion": null
    },
    {
        "uuid": "864dd701-198a-44b9-8dbf-dd24a8ee76ec",
        "name": "test",
        "guideName": "EXCLUDED_DOCUMENT",
        "extension": "txt",
        "version": 2,
        "validity": "2001-07-10",
        "creation": "2024-07-24",
        "updated": null,
        "exclusion": "2024-07-24"
    }
]
```

### Listar Documentos por nome do Usuário

```
  GET /document/findName?documentName=
```
#### Descrição
Necessário Token, lista os Documentos e suas versões de acordo com o nome informado.

| Parâmetro   | Tipo       | Descrição                                   |
| :---------- | :--------- | :------------------------------------------ |
| `documentName`      | `string` | **Obrigatório**. Nome do Documento que quer listar |

##### Exemplo de Retorno

```json
[
    {
        "uuid": "3252f6c0-996a-429c-8598-14000976cc81",
        "name": "test",
        "guideName": "test-comp",
        "extension": "txt",
        "version": 2,
        "validity": "2001-07-10",
        "creation": "2024-07-24",
        "updated": null,
        "exclusion": null
    },
    {
        "uuid": "78e383d9-e1a5-4bf9-95e7-58a8584d6c76",
        "name": "test",
        "guideName": "test-comp_V1",
        "extension": "txt",
        "version": 1,
        "validity": "2024-09-30",
        "creation": "2024-07-24",
        "updated": "2024-07-24",
        "exclusion": null
    }
]
```

### Upload

```
  POST /document/upload
```
#### Descrição
Necessário Token, armazena o documento informado pelo usuário via multipart/form-data e insere a validade informada por application/json. Ele modifica o nome do Documento para haver uma distinção de Owners.

##### Exemplo de Requisição

```json
{ 
    "validity":"2024-09-30"
}
```

##### Exemplo de Retorno

```json
{
    "name": "test",
    "originalDocument": null,
    "extension": "txt",
    "version": 1,
    "validity": "2024-09-30",
    "creation": "2024-07-24",
    "updated": null,
    "exclusion": null
}
```

### Atualizar Documento 

```
  PUT /document/upload
```
#### Descrição
Necessário Token, atualiza o documento passado por multipart/form-data e insere a nova validade informada por application/json. Ele modifica o nome do Documento para haver uma distinção de Owners.

##### Exemplo de Requisição

```json
{ 
    "validity":"2001-07-10"
}
```

##### Exemplo de Retorno

```json
{
    "name": "test",
    "originalDocument": {
        "uuid": "e7b34415-326e-4900-b415-0325a7280acf",
        "name": "test",
        "guideName": "test-comp_V1",
        "extension": "txt",
        "version": 1,
        "validity": "2024-09-30",
        "creation": "2024-07-24",
        "updated": "2024-07-24",
        "exclusion": null
    },
    "extension": "txt",
    "version": 2,
    "validity": "2001-07-10",
    "creation": "2024-07-24",
    "updated": null,
    "exclusion": null
}
```

### Usar Versão anterior do Documento

```
  DELETE /document/previousVersion?documentName=
```
#### Descrição
Necessário Token, pega a versão anterior do Documento passado pelo nome e exclui a versão atual.

| Parâmetro   | Tipo       | Descrição                                   |
| :---------- | :--------- | :------------------------------------------ |
| `documentName`      | `string` | **Obrigatório**. Nome do Documento que quer pegar a versão anterior |

### Deletar Documentos por nome

```
  DELETE /document?documentName=
```
#### Descrição
Necessário Token, deleta todos os Documentos do Usuário que tenham o nome informado.

| Parâmetro   | Tipo       | Descrição                                   |
| :---------- | :--------- | :------------------------------------------ |
| `documentName`      | `string` | **Obrigatório**. Nome do Documento que quer deletar |

### Download

```
  GET /document/{documentName:.+}
```
#### Descrição
Necessário Token, faz Download do Documento informado pelo Usuário.

| Parâmetro   | Tipo       | Descrição                                   |
| :---------- | :--------- | :------------------------------------------ |
| `documentName`      | `string` | **Obrigatório**. Nome do Documento que quer baixar e a extensão dele junto, exemplo: test.txt |

## Próximos passos
- [x] Fazer requisição para passar username e password em JSON e retornar o token de acesso
- [ ] Implementar lógica de RefreshToken para o JWT
- [ ] Substituir atributo version da entidade Document para se tornar uma nova entidade e criar um relacionamento mais limpo e organizado
- [x] Corrigir Método de listar documentos para não listar documentos já excluídos

## 📁 Acesso ao projeto
Você pode acessar os arquivos do projeto clicando [aqui](https://github.com/DaviMunhoz1005/GedApi/tree/main/src).
