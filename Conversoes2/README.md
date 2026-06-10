# ByteCodec

Biblioteca didática em JavaScript para converter valores primitivos em vetores de bytes (`Int8Array`) e reconstruir os valores a partir desses bytes.

## Arquivos

- `byte-codec.js`: biblioteca reutilizável.
- `index.html`: página de teste interativo.
- `app.js`: lógica da página de teste.
- `style.css`: estilos da página.

## Convenções

- Retorno de todos os métodos `write*`: `Int8Array`.
- Ordem padrão dos bytes: big-endian.
- `writeChar()`: grava um code unit UTF-16 em 2 bytes.
- `writeString()`: grava a string em UTF-8, sem prefixo de tamanho.
- `writeDate()`: grava o epoch day em 4 bytes.
- `writeDateTime()`: grava os milissegundos desde o epoch em 8 bytes.

## Exemplo

```js
const bytes = ByteCodec.writeInt(42);
console.log(ByteCodec.toHex(bytes)); // 00 00 00 2A
console.log(ByteCodec.readInt(bytes)); // 42
```
