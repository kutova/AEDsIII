# TABELA HASH EXTENSÍVEL

Implementação da tabela hash extensível para a disciplina Algoritmos e Estruturas de Dados 3 do curso de Ciência da computação da PUC Minas.

Esta tabela tem uma implementação ligeiramente diferente das tabelas tradicionais, em que contamos com as seguintes operações:

- create(C, V)
- V <- read(C)
- update(C, V)
- delete(C)

Nessas operações, usamos explicitamente uma chave (C) e um valor (V).

Neste projeto, porém, podemos armazenar qualquer tipo de objeto. Esse objeto precisa ter um atributo que será identificado como chave e que terá o seu hash calculado por meio do método hashCode().

Para assegurar o funcionamento correto da tabela hash extensível, esse objeto deve implementar a interface RegistroHashExtensível.

_Implementado pelo Prof. Marcos Kutova_
_v1.1 - 2021_
