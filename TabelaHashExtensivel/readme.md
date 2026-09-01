# TABELA HASH EXTENSÍVEL

Os nomes dos métodos foram mantidos em inglês apenas para manter a coerência com o resto da disciplina:

- boolean create(T elemento)
- T read(int hashcode)
- boolean update(T novoElemento) // a chave (hashcode) deve ser a mesma
- boolean delete(int hashcode)

Implementado pelo Prof. Marcos Kutova
v1.1 - 2021

Implementação da tabela hash extensível para a disciplina Algoritmos e Estruturas de Dados 3 do curso de Ciência da computação da PUC Minas.

Esta tabela tem uma implementação ligeiramente diferente das tabelas tradicionais, em que contaríamos com as seguintes operações:

- create(C, V)
- V <- read(C)
- update(C, V)
- delete(C)

Nessas operações, usamos explicitamente uma chave (C) e um valor (V).

Neste projeto, porém, podemos armazenar qualquer tipo de objeto. Esse objeto precisa ter um atributo que será identificado como chave e que terá o seu hash calculado por meio do método hashCode().

Para assegurar o funcionamento correto da tabela hash extensível, esse objeto deve implementar a interface RegistroHashExtensível.
