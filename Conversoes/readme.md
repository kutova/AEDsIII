# ByteStream.js — Guia de Uso

**Biblioteca para serialização e deserialização de tipos primitivos em JavaScript**
Disciplina AED3 · Ciência da Computação · PUC Minas
Prof. Marcos André S. Kutova

---

## O que esta biblioteca faz?

`ByteStream.js` converte valores de diferentes tipos (inteiros, decimais, textos, datas) em **sequências de bytes** — e faz o caminho inverso, reconstruindo o valor original a partir de bytes.

Isso é útil quando você precisa gravar dados em arquivos binários ou garantir que JavaScript e Java representem os mesmos dados da mesma forma na memória.

---

## Como incluir a biblioteca no seu projeto

### Em uma página HTML

Baixe o arquivo `ByteStream.js` e coloque-o na mesma pasta do seu HTML. Adicione esta linha no `<head>` ou antes do seu script:

```html
<script src="ByteStream.js"></script>
```

A partir daí, o objeto `ByteStream` estará disponível em qualquer script da página:

```html
<!DOCTYPE html>
<html>
  <head>
    <script src="ByteStream.js"></script>
  </head>
  <body>
    <script>
      // já está disponível aqui
      const bytes = ByteStream.writeInt(42);
      console.log(bytes); // Int8Array(4) [0, 0, 0, 42]
    </script>
  </body>
</html>
```

### Em um projeto Node.js

Coloque `ByteStream.js` na pasta do seu projeto e importe com `require`:

```js
const ByteStream = require('./ByteStream.js');

const bytes = ByteStream.writeInt(42);
console.log(bytes); // Int8Array(4) [0, 0, 0, 42]
```

---

## Conceitos básicos antes de começar

### O que é um `Int8Array`?

É o tipo de dado que a biblioteca usa para representar uma sequência de bytes. Pense nele como uma lista de números inteiros, onde cada número ocupa exatamente 1 byte (valores de -128 a 127).

```js
const bytes = ByteStream.writeInt(1);
// bytes é um Int8Array com 4 posições: [0, 0, 0, 1]
console.log(bytes.length); // 4
console.log(bytes[0]);     // 0
console.log(bytes[3]);     // 1
```

### O que é "big-endian"?

Quando um número ocupa mais de 1 byte, a biblioteca sempre coloca o byte mais significativo primeiro. Isso é o padrão da linguagem Java e se chama **big-endian**.

Por exemplo, o número `256` em `short` (2 bytes) fica assim:

```
byte[0] = 01  ← byte mais significativo (o "mais à esquerda")
byte[1] = 00  ← byte menos significativo
```

Você não precisa se preocupar com isso no dia a dia — a biblioteca cuida disso automaticamente.

### O que é `BigInt`?

JavaScript tem dificuldade em representar números inteiros muito grandes (maiores que 2⁵³) com precisão total. Para o tipo `long` (8 bytes), a biblioteca usa `BigInt`, que é um tipo especial do JavaScript para inteiros arbitrariamente grandes. Você o reconhece pelo sufixo `n`:

```js
const numero = 9007199254740993n; // isso é um BigInt
```

Você só precisará lidar com `BigInt` ao usar `writeLong` e `readLong`.

---

## Referência dos métodos

### Visão geral

| Método | Tipo de entrada | Tamanho do resultado |
|---|---|---|
| `writeByte(valor)` | inteiro de -128 a 127 | 1 byte |
| `writeShort(valor)` | inteiro de -32.768 a 32.767 | 2 bytes |
| `writeInt(valor)` | inteiro de -2.147.483.648 a 2.147.483.647 | 4 bytes |
| `writeLong(valor)` | inteiro (Number ou BigInt) | 8 bytes |
| `writeBoolean(valor)` | `true` ou `false` | 1 byte |
| `writeChar(valor)` | string de 1 caractere | 2 bytes (UTF-16BE) |
| `writeFloat(valor)` | número decimal (precisão simples) | 4 bytes |
| `writeDouble(valor)` | número decimal (precisão dupla) | 8 bytes |
| `writeString(valor)` | qualquer string | 2 + N bytes (UTF-8) |
| `writeDate(valor)` | string `"dd/mm/aaaa"` ou objeto `Date` | 4 bytes |
| `writeDateTime(valor)` | string `"dd/mm/aaaa hh:mm:ss"` ou objeto `Date` | 8 bytes |

Cada método `write*` tem um método `read*` correspondente que faz o caminho inverso.

---

### `writeByte` / `readByte`

Converte um número inteiro pequeno (de **-128 a 127**) em 1 byte.

```js
const bytes = ByteStream.writeByte(65);
// Int8Array(1) [65]

const valor = ByteStream.readByte(bytes);
// 65
```

> ⚠️ Se você passar um valor fora do intervalo, o resultado será truncado (os bits excedentes são descartados), sem aviso de erro.

---

### `writeShort` / `readShort`

Converte um inteiro de **-32.768 a 32.767** em 2 bytes.

```js
const bytes = ByteStream.writeShort(1000);
// Int8Array(2) [3, -24]   →  hex: 03 E8

const valor = ByteStream.readShort(bytes);
// 1000
```

---

### `writeInt` / `readInt`

Converte um inteiro de **-2.147.483.648 a 2.147.483.647** em 4 bytes. É o tipo inteiro mais comum em Java.

```js
const bytes = ByteStream.writeInt(100000);
// Int8Array(4) [0, 1, -122, -96]   →  hex: 00 01 86 A0

const valor = ByteStream.readInt(bytes);
// 100000
```

---

### `writeLong` / `readLong`

Converte um inteiro grande em 8 bytes. Aceita tanto `Number` quanto `BigInt` na escrita. **A leitura sempre retorna `BigInt`.**

```js
// Escrita com Number comum (funciona até 2^53 - 1)
const bytes1 = ByteStream.writeLong(9999999999);

// Escrita com BigInt (recomendado para valores muito grandes)
const bytes2 = ByteStream.writeLong(9007199254740993n);

// Leitura — sempre retorna BigInt
const valor = ByteStream.readLong(bytes1);
// 9999999999n   ← note o "n" no final

// Para usar em operações comuns, converta com Number():
const numero = Number(ByteStream.readLong(bytes1));
// 9999999999  (sem o "n")
```

> ⚠️ Use `Number()` para converter o resultado de `readLong` apenas se tiver certeza de que o valor cabe em um inteiro JavaScript seguro (até 9.007.199.254.740.991). Para valores maiores, mantenha como `BigInt`.

---

### `writeBoolean` / `readBoolean`

Converte `true` ou `false` em 1 byte (`1` ou `0`).

```js
const bytes = ByteStream.writeBoolean(true);
// Int8Array(1) [1]

const valor = ByteStream.readBoolean(bytes);
// true
```

---

### `writeChar` / `readChar`

Converte um **único caractere** em 2 bytes no formato UTF-16BE — o mesmo padrão usado pelo Java. Funciona com letras, acentos e caracteres de outras línguas (desde que estejam no BMP, o que abrange praticamente todos os caracteres do uso cotidiano).

```js
// Caractere ASCII
const b1 = ByteStream.writeChar('A');
// Int8Array(2) [0, 65]   →  hex: 00 41

// Caractere acentuado
const b2 = ByteStream.writeChar('ã');
// Int8Array(2) [0, -29]   →  hex: 00 E3

// Caractere CJK (chinês/japonês/coreano)
const b3 = ByteStream.writeChar('中');
// Int8Array(2) [78, 45]   →  hex: 4E 2D

const c = ByteStream.readChar(b2);
// 'ã'
```

> Se você passar uma string com mais de um caractere, somente o primeiro será usado.

---

### `writeFloat` / `readFloat`

Converte um número decimal em 4 bytes (ponto flutuante de precisão simples, padrão IEEE 754).

```js
const bytes = ByteStream.writeFloat(3.14);
// Int8Array(4) [64, 72, -11, -61]   →  hex: 40 48 F5 C3

const valor = ByteStream.readFloat(bytes);
// 3.140000104904175   ← ligeiramente diferente de 3.14!
```

> ⚠️ `float` tem apenas ~7 dígitos de precisão. O valor lido pode não ser exatamente igual ao escrito. Se precisar de precisão total, use `writeDouble`.

---

### `writeDouble` / `readDouble`

Converte um número decimal em 8 bytes (ponto flutuante de precisão dupla). Este é o tipo padrão de números decimais no JavaScript.

```js
const bytes = ByteStream.writeDouble(3.141592653589793);
// Int8Array(8) [64, 9, 33, -5, 84, 68, 45, 24]

const valor = ByteStream.readDouble(bytes);
// 3.141592653589793   ← exato
```

---

### `writeString` / `readString`

Converte uma string em bytes usando o formato `writeUTF` do Java: os primeiros **2 bytes** armazenam o comprimento (em bytes) da string, e os bytes seguintes contêm o texto em UTF-8.

```js
const bytes = ByteStream.writeString('AED');
// Int8Array(5): [0, 3, 65, 69, 68]
//               ^^^^  ^^^^^^^^^^^^
//               tamanho=3  'A','E','D' em ASCII

const texto = ByteStream.readString(bytes);
// 'AED'
```

Funciona com acentos e caracteres especiais (cada um pode ocupar mais de 1 byte em UTF-8):

```js
const bytes = ByteStream.writeString('ção');
// 'ç' e 'ã' ocupam 2 bytes cada em UTF-8 → 5 bytes de conteúdo + 2 de tamanho = 7 bytes no total

const texto = ByteStream.readString(bytes);
// 'ção'
```

String vazia gera 2 bytes (só o campo de tamanho, com valor `0`):

```js
const bytes = ByteStream.writeString('');
// Int8Array(2): [0, 0]
```

> ⚠️ O comprimento máximo é **65.535 bytes UTF-8** (não caracteres). Strings muito longas lançarão um erro `RangeError`.

---

### `writeDate` / `readDate`

Converte uma data em **4 bytes**, representando o número de dias desde 01/01/1970 (chamado de *epoch day*). Aceita uma string no formato `"dd/mm/aaaa"` ou um objeto `Date` do JavaScript.

```js
// A data de origem (epoch): 0 dias desde 01/01/1970
const b1 = ByteStream.writeDate('01/01/1970');
// Int8Array(4) [0, 0, 0, 0]

// 01/01/2000 = dia 10957 desde o epoch
const b2 = ByteStream.writeDate('01/01/2000');
// Int8Array(4) [0, 0, 42, -51]   →  hex: 00 00 2A CD

// Leitura retorna um objeto Date (meia-noite UTC)
const data = ByteStream.readDate(b2);
data.getUTCFullYear();  // 2000
data.getUTCMonth();     // 0  (janeiro — meses começam em 0 no JavaScript)
data.getUTCDate();      // 1

// Também aceita objeto Date
const hoje = new Date(2025, 0, 15); // 15/01/2025
const b3 = ByteStream.writeDate(hoje);
```

> Os cálculos são feitos em **UTC** para evitar problemas com fuso horário.

---

### `writeDateTime` / `readDateTime`

Converte uma data e hora em **8 bytes**, representando o número de milissegundos desde 01/01/1970 às 00:00:00.000 UTC (chamado de *Unix timestamp*). Aceita uma string no formato `"dd/mm/aaaa hh:mm:ss"` ou um objeto `Date`.

```js
// O instante de origem: 0 milissegundos desde 01/01/1970 00:00:00 UTC
const b1 = ByteStream.writeDateTime('01/01/1970 00:00:00');
// Int8Array(8) [0, 0, 0, 0, 0, 0, 0, 0]

// Um segundo depois do epoch = 1000 ms
const b2 = ByteStream.writeDateTime('01/01/1970 00:00:01');

// Leitura retorna um objeto Date
const dt = ByteStream.readDateTime(b2);
dt.getTime(); // 1000  (milissegundos)

// Usando objeto Date diretamente
const agora = new Date();
const bytes = ByteStream.writeDateTime(agora);
const recuperado = ByteStream.readDateTime(bytes);
recuperado.getTime() === agora.getTime(); // true
```

> As strings são interpretadas como **UTC**. Se você passar um objeto `Date`, o instante exato (incluindo fuso horário) é preservado via `getTime()`.

---

## O parâmetro `offset` nos métodos de leitura

Todos os métodos `read*` aceitam um segundo argumento opcional chamado `offset`. Ele indica **a partir de qual posição** do array de bytes a leitura deve começar (padrão: `0`).

Isso é útil quando você tem um buffer que contém vários campos consecutivos:

```js
// Monta um buffer com: int(7) seguido de short(3)
// int usa 4 bytes, short usa 2 bytes → buffer total de 6 bytes
const buffer = new Int8Array(6);
buffer.set(ByteStream.writeInt(7),   0); // posições 0–3
buffer.set(ByteStream.writeShort(3), 4); // posições 4–5

// Leitura de cada campo com o offset correto
const campo1 = ByteStream.readInt(buffer, 0);   // 7
const campo2 = ByteStream.readShort(buffer, 4); // 3
```

---

## Combinando múltiplos campos em um único buffer

Em projetos reais você frequentemente vai querer gravar vários campos em sequência. A forma mais prática é criar um array auxiliar, escrever cada campo com seu método correspondente, e depois concatenar os pedaços.

```js
// Exemplo: registro com id (int), ativo (boolean) e nome (string)
const id     = ByteStream.writeInt(42);        // 4 bytes
const ativo  = ByteStream.writeBoolean(true);  // 1 byte
const nome   = ByteStream.writeString('Ana');  // 2 + 3 = 5 bytes

// Tamanho total
const total = id.length + ativo.length + nome.length; // 10

// Monta o buffer final
const registro = new Int8Array(total);
registro.set(id,    0);
registro.set(ativo, 4);
registro.set(nome,  5);

// Para ler de volta, use os offsets corretos:
const lerId    = ByteStream.readInt(registro,     0); // 42
const lerAtivo = ByteStream.readBoolean(registro, 4); // true
const lerNome  = ByteStream.readString(registro,  5); // 'Ana'
```

---

## Erros comuns

**Valor fora do intervalo do tipo**
`writeByte`, `writeShort` e `writeInt` não lançam erro — eles silenciosamente truncam o valor. Se você escrever `writeShort(99999)`, o resultado não será 99999. Use sempre valores dentro do intervalo documentado.

**`readLong` retorna `BigInt`, não `Number`**
Operações como `+` entre `BigInt` e `Number` geram erro em JavaScript. Se você quiser usar o resultado de `readLong` em contas normais, converta com `Number(valor)` — mas somente quando o valor couber em um inteiro seguro.

**String lida com `readString` parece errada**
Certifique-se de que os bytes foram gerados por `writeString`. Esse método usa um prefixo de 2 bytes para o tamanho, então tentar ler com `readString` bytes que não vieram de `writeString` vai produzir resultados incorretos.

**Data lida com `readDate` está com um dia a menos**
Isso costuma acontecer quando se mistura UTC e fuso horário local. A biblioteca trabalha internamente em UTC. Ao exibir a data, use sempre `getUTCDate()`, `getUTCMonth()` e `getUTCFullYear()` — e não as versões sem `UTC`, que usam o fuso local do computador.

---

## Tabela rápida de referência

| Método write | Método read | Bytes | Tipo de entrada | Tipo de saída |
|---|---|---|---|---|
| `writeByte(v)` | `readByte(b, offset?)` | 1 | `Number` | `Number` |
| `writeShort(v)` | `readShort(b, offset?)` | 2 | `Number` | `Number` |
| `writeInt(v)` | `readInt(b, offset?)` | 4 | `Number` | `Number` |
| `writeLong(v)` | `readLong(b, offset?)` | 8 | `Number` ou `BigInt` | `BigInt` |
| `writeBoolean(v)` | `readBoolean(b, offset?)` | 1 | `Boolean` | `Boolean` |
| `writeChar(v)` | `readChar(b, offset?)` | 2 | `String` (1 char) | `String` |
| `writeFloat(v)` | `readFloat(b, offset?)` | 4 | `Number` | `Number` |
| `writeDouble(v)` | `readDouble(b, offset?)` | 8 | `Number` | `Number` |
| `writeString(v)` | `readString(b, offset?)` | 2 + N | `String` | `String` |
| `writeDate(v)` | `readDate(b, offset?)` | 4 | `String` ou `Date` | `Date` |
| `writeDateTime(v)` | `readDateTime(b, offset?)` | 8 | `String` ou `Date` | `Date` |

`b` = `Int8Array` com os bytes · `offset` = posição de início (padrão: `0`)

---

*ByteStream.js · Recurso didático AED3 · Prof. Marcos André S. Kutova · PUC Minas*
