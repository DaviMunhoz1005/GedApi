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

## 📁 Acesso ao projeto
Você pode acessar os arquivos do projeto clicando [aqui](https://github.com/gui-lirasilva/Edige-POO/tree/master/src).
