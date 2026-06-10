/**
 * ByteStream.js
 * Biblioteca para serialização e deserialização de tipos primitivos Java
 * em vetores de bytes (Int8Array), compatível com DataOutputStream / DataInputStream.
 *
 * Convenções:
 *  - Todos os tipos numéricos são armazenados em big-endian (Java padrão).
 *  - writeChar   → UTF-16BE (2 bytes), como em Java.
 *  - writeString → prefixado por 2 bytes (short) com o comprimento em bytes UTF-8,
 *                  seguido dos bytes UTF-8 da string (formato writeUTF do Java).
 *  - writeDate   → int de 4 bytes: número de dias desde 01/01/1970 (epoch day).
 *  - writeDateTime → long de 8 bytes: milissegundos desde 01/01/1970 00:00:00.000 UTC.
 *
 * Autor: Marcos Kutova
 * gerado com auxílio de Claude (Anthropic), 2025.
 */

const ByteStream = (() => {
  // ─────────────────────────────────────────────
  // Utilitários internos
  // ─────────────────────────────────────────────

  /**
   * Cria um Int8Array a partir de um ArrayBuffer.
   */
  function bufferToInt8(buffer) {
    return new Int8Array(buffer);
  }

  /**
   * Garante que o argumento seja um Int8Array.
   * Aceita também Uint8Array, Buffer (Node) ou ArrayBuffer.
   */
  function toInt8Array(bytes) {
    if (bytes instanceof Int8Array) return bytes;
    if (bytes instanceof ArrayBuffer) return new Int8Array(bytes);
    if (ArrayBuffer.isView(bytes))
      return new Int8Array(bytes.buffer, bytes.byteOffset, bytes.byteLength);
    throw new TypeError("Esperado um Int8Array, Uint8Array ou ArrayBuffer.");
  }

  /**
   * Retorna um DataView sobre um Int8Array.
   */
  function viewOf(bytes) {
    return new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
  }

  // ─────────────────────────────────────────────
  // Métodos WRITE
  // ─────────────────────────────────────────────

  /**
   * Converte um valor inteiro de 8 bits com sinal (byte) em Int8Array de 1 byte.
   * Intervalo válido: -128 a 127.
   */
  function writeByte(value) {
    const buf = new ArrayBuffer(1);
    new DataView(buf).setInt8(0, value);
    return bufferToInt8(buf);
  }

  /**
   * Converte um valor inteiro de 16 bits com sinal (short) em Int8Array de 2 bytes (big-endian).
   * Intervalo válido: -32.768 a 32.767.
   */
  function writeShort(value) {
    const buf = new ArrayBuffer(2);
    new DataView(buf).setInt16(0, value, false); // false = big-endian
    return bufferToInt8(buf);
  }

  /**
   * Converte um valor inteiro de 32 bits com sinal (int) em Int8Array de 4 bytes (big-endian).
   * Intervalo válido: -2.147.483.648 a 2.147.483.647.
   */
  function writeInt(value) {
    const buf = new ArrayBuffer(4);
    new DataView(buf).setInt32(0, value, false);
    return bufferToInt8(buf);
  }

  /**
   * Converte um valor inteiro de 64 bits com sinal (long) em Int8Array de 8 bytes (big-endian).
   * Aceita Number (até 2^53-1) ou BigInt.
   */
  function writeLong(value) {
    const buf = new ArrayBuffer(8);
    new DataView(buf).setBigInt64(0, BigInt(value), false);
    return bufferToInt8(buf);
  }

  /**
   * Converte um valor booleano em Int8Array de 1 byte: 1 para true, 0 para false.
   */
  function writeBoolean(value) {
    const buf = new ArrayBuffer(1);
    new DataView(buf).setInt8(0, value ? 1 : 0);
    return bufferToInt8(buf);
  }

  /**
   * Converte um caractere em Int8Array de 2 bytes (UTF-16BE, big-endian).
   * Apenas BMP (Basic Multilingual Plane): caracteres com code point U+0000 a U+FFFF.
   * Aceita string de 1 caractere ou número (code point).
   */
  function writeChar(value) {
    const codePoint = typeof value === "string" ? value.charCodeAt(0) : value;
    const buf = new ArrayBuffer(2);
    new DataView(buf).setUint16(0, codePoint, false); // big-endian, sem sinal (UTF-16 code unit)
    return bufferToInt8(buf);
  }

  /**
   * Converte um valor de ponto flutuante de 32 bits (float) em Int8Array de 4 bytes (big-endian).
   */
  function writeFloat(value) {
    const buf = new ArrayBuffer(4);
    new DataView(buf).setFloat32(0, value, false);
    return bufferToInt8(buf);
  }

  /**
   * Converte um valor de ponto flutuante de 64 bits (double) em Int8Array de 8 bytes (big-endian).
   */
  function writeDouble(value) {
    const buf = new ArrayBuffer(8);
    new DataView(buf).setFloat64(0, value, false);
    return bufferToInt8(buf);
  }

  /**
   * Converte uma string em Int8Array usando o formato writeUTF do Java:
   *   [2 bytes: comprimento em bytes UTF-8 (short sem sinal)] + [bytes UTF-8 da string]
   * Comprimento máximo: 65.535 bytes UTF-8.
   */
  function writeString(value) {
    const encoded = new TextEncoder().encode(String(value)); // Uint8Array UTF-8
    const length = encoded.length;
    if (length > 0xffff) {
      throw new RangeError(
        `String muito longa para writeUTF: ${length} bytes (máximo 65535).`,
      );
    }
    const buf = new ArrayBuffer(2 + length);
    const view = new DataView(buf);
    view.setUint16(0, length, false); // 2 bytes big-endian com o comprimento
    const result = new Int8Array(buf);
    result.set(encoded, 2); // bytes UTF-8 a partir do offset 2
    return result;
  }

  /**
   * Converte um objeto Date (ou string "dd/mm/aaaa") em Int8Array de 4 bytes,
   * representando o número de dias desde 01/01/1970 (epoch day), como int com sinal.
   */
  function writeDate(value) {
    let date;
    if (typeof value === "string") {
      const parts = value.split("/");
      if (parts.length !== 3)
        throw new TypeError("Formato esperado: dd/mm/aaaa");
      // Usa UTC para evitar deslocamento de fuso horário
      date = new Date(
        Date.UTC(
          parseInt(parts[2]),
          parseInt(parts[1]) - 1,
          parseInt(parts[0]),
        ),
      );
    } else {
      // Se for um objeto Date, normaliza para meia-noite UTC
      date = new Date(
        Date.UTC(value.getFullYear(), value.getMonth(), value.getDate()),
      );
    }
    const epochDay = Math.round(date.getTime() / 86_400_000);
    return writeInt(epochDay);
  }

  /**
   * Converte um objeto Date (ou string "dd/mm/aaaa hh:mm:ss") em Int8Array de 8 bytes,
   * representando os milissegundos desde 01/01/1970 00:00:00.000 UTC, como long com sinal.
   */
  function writeDateTime(value) {
    let millis;
    if (typeof value === "string") {
      const [datePart, timePart] = value.split(" ");
      if (!datePart || !timePart)
        throw new TypeError("Formato esperado: dd/mm/aaaa hh:mm:ss");
      const [d, m, y] = datePart.split("/");
      const [h, min, s] = timePart.split(":");
      millis = Date.UTC(
        parseInt(y),
        parseInt(m) - 1,
        parseInt(d),
        parseInt(h),
        parseInt(min),
        parseInt(s),
      );
    } else {
      millis = value.getTime();
    }
    return writeLong(millis);
  }

  // ─────────────────────────────────────────────
  // Métodos READ
  // ─────────────────────────────────────────────

  /**
   * Lê 1 byte de um Int8Array e retorna um inteiro com sinal (-128 a 127).
   * @param {Int8Array} bytes
   * @param {number} [offset=0] posição inicial
   */
  function readByte(bytes, offset = 0) {
    return viewOf(toInt8Array(bytes)).getInt8(offset);
  }

  /**
   * Lê 2 bytes (big-endian) de um Int8Array e retorna um short com sinal.
   */
  function readShort(bytes, offset = 0) {
    return viewOf(toInt8Array(bytes)).getInt16(offset, false);
  }

  /**
   * Lê 4 bytes (big-endian) de um Int8Array e retorna um int com sinal.
   */
  function readInt(bytes, offset = 0) {
    return viewOf(toInt8Array(bytes)).getInt32(offset, false);
  }

  /**
   * Lê 8 bytes (big-endian) de um Int8Array e retorna um BigInt com sinal (long).
   */
  function readLong(bytes, offset = 0) {
    return viewOf(toInt8Array(bytes)).getBigInt64(offset, false);
  }

  /**
   * Lê 1 byte de um Int8Array e retorna um booleano (0 → false, qualquer outro → true).
   */
  function readBoolean(bytes, offset = 0) {
    return viewOf(toInt8Array(bytes)).getInt8(offset) !== 0;
  }

  /**
   * Lê 2 bytes (UTF-16BE) de um Int8Array e retorna uma string de 1 caractere.
   */
  function readChar(bytes, offset = 0) {
    const codePoint = viewOf(toInt8Array(bytes)).getUint16(offset, false);
    return String.fromCharCode(codePoint);
  }

  /**
   * Lê 4 bytes (big-endian) de um Int8Array e retorna um float de 32 bits.
   */
  function readFloat(bytes, offset = 0) {
    return viewOf(toInt8Array(bytes)).getFloat32(offset, false);
  }

  /**
   * Lê 8 bytes (big-endian) de um Int8Array e retorna um double de 64 bits.
   */
  function readDouble(bytes, offset = 0) {
    return viewOf(toInt8Array(bytes)).getFloat64(offset, false);
  }

  /**
   * Lê uma string no formato writeUTF do Java de um Int8Array:
   *   [2 bytes: comprimento em bytes UTF-8] + [bytes UTF-8]
   * @param {Int8Array} bytes
   * @param {number} [offset=0] posição inicial
   * @returns {string}
   */
  function readString(bytes, offset = 0) {
    const arr = toInt8Array(bytes);
    const view = viewOf(arr);
    const length = view.getUint16(offset, false);
    // TextDecoder espera Uint8Array; cria uma view sobre os mesmos bytes
    const uint8 = new Uint8Array(
      arr.buffer,
      arr.byteOffset + offset + 2,
      length,
    );
    return new TextDecoder("utf-8").decode(uint8);
  }

  /**
   * Lê 4 bytes (big-endian) de um Int8Array interpretando-os como epoch day
   * e retorna um objeto Date correspondente à meia-noite UTC daquele dia.
   */
  function readDate(bytes, offset = 0) {
    const epochDay = readInt(bytes, offset);
    return new Date(epochDay * 86_400_000);
  }

  /**
   * Lê 8 bytes (big-endian) de um Int8Array interpretando-os como milissegundos
   * desde o epoch Unix e retorna um objeto Date.
   */
  function readDateTime(bytes, offset = 0) {
    const millis = readLong(bytes, offset); // BigInt
    return new Date(Number(millis));
  }

  // ─────────────────────────────────────────────
  // API pública
  // ─────────────────────────────────────────────

  return {
    // Write
    writeByte,
    writeShort,
    writeInt,
    writeLong,
    writeBoolean,
    writeChar,
    writeFloat,
    writeDouble,
    writeString,
    writeDate,
    writeDateTime,
    // Read
    readByte,
    readShort,
    readInt,
    readLong,
    readBoolean,
    readChar,
    readFloat,
    readDouble,
    readString,
    readDate,
    readDateTime,
  };
})();

// ─────────────────────────────────────────────
// Exportação para ambientes Node.js / ES Modules
// ─────────────────────────────────────────────
if (typeof module !== "undefined" && module.exports) {
  module.exports = ByteStream;
}
