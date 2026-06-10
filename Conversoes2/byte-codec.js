/**
 * ByteCodec
 * Biblioteca didática para escrever/ler valores primitivos como bytes.
 *
 * Convenções adotadas:
 * - Todos os métodos write* retornam Int8Array, como solicitado.
 * - Internamente, bytes são manipulados como unsigned quando necessário.
 * - A ordem dos bytes é big-endian por padrão, isto é, byte mais significativo primeiro.
 * - Boolean: 0 = false, 1 = true.
 * - Char: um code unit UTF-16, com 2 bytes. Caracteres fora do BMP, como emojis,
 *   exigem par substituto e não cabem em apenas 2 bytes.
 * - String: UTF-8, sem prefixo de tamanho e sem terminador nulo.
 * - Date: epoch day, isto é, dias desde 1970-01-01 UTC, gravados como int32.
 * - DateTime: milissegundos desde 1970-01-01T00:00:00.000Z, gravados como int64.
 */
(function (global) {
  'use strict';

  const ByteCodec = {};

  ByteCodec.BIG_ENDIAN = false;      // DataView littleEndian=false
  ByteCodec.LITTLE_ENDIAN = true;
  ByteCodec.defaultLittleEndian = false;

  const INT8_MIN = -128n;
  const INT8_MAX = 127n;
  const INT16_MIN = -32768n;
  const INT16_MAX = 32767n;
  const INT32_MIN = -2147483648n;
  const INT32_MAX = 2147483647n;
  const INT64_MIN = -(1n << 63n);
  const INT64_MAX = (1n << 63n) - 1n;
  const DAY_MS = 86400000;

  function ensureInt8Array(bytes, expectedLength, methodName) {
    if (!(bytes instanceof Int8Array)) {
      throw new TypeError(`${methodName} espera um Int8Array.`);
    }
    if (typeof expectedLength === 'number' && bytes.length !== expectedLength) {
      throw new RangeError(`${methodName} espera ${expectedLength} byte(s), mas recebeu ${bytes.length}.`);
    }
  }

  function toUnsignedByteArray(bytes) {
    ensureInt8Array(bytes, undefined, 'leitura');
    const out = new Uint8Array(bytes.length);
    for (let i = 0; i < bytes.length; i++) out[i] = bytes[i] & 0xff;
    return out;
  }

  function newView(byteLength) {
    const buffer = new ArrayBuffer(byteLength);
    return new DataView(buffer);
  }

  function viewFromInt8Array(bytes, expectedLength, methodName) {
    ensureInt8Array(bytes, expectedLength, methodName);
    const unsigned = toUnsignedByteArray(bytes);
    return new DataView(unsigned.buffer, unsigned.byteOffset, unsigned.byteLength);
  }

  function intViewResult(view) {
    return new Int8Array(view.buffer);
  }

  function assertIntegerNumber(value, min, max, methodName) {
    if (typeof value !== 'number' || !Number.isInteger(value)) {
      throw new TypeError(`${methodName} espera um número inteiro.`);
    }
    if (BigInt(value) < min || BigInt(value) > max) {
      throw new RangeError(`${methodName} espera valor entre ${min} e ${max}.`);
    }
  }

  function assertBigIntRange(value, min, max, methodName) {
    let big;
    if (typeof value === 'bigint') {
      big = value;
    } else if (typeof value === 'number' && Number.isInteger(value)) {
      if (!Number.isSafeInteger(value)) {
        throw new RangeError(`${methodName} recebeu Number fora da faixa segura. Use BigInt ou string.`);
      }
      big = BigInt(value);
    } else if (typeof value === 'string' && /^[-+]?\d+$/.test(value.trim())) {
      big = BigInt(value.trim());
    } else {
      throw new TypeError(`${methodName} espera BigInt, inteiro seguro ou string inteira.`);
    }

    if (big < min || big > max) {
      throw new RangeError(`${methodName} espera valor entre ${min} e ${max}.`);
    }
    return big;
  }

  function parseDateOnly(value, methodName) {
    if (value instanceof Date) {
      if (Number.isNaN(value.getTime())) throw new TypeError(`${methodName} recebeu uma data inválida.`);
      return {
        year: value.getUTCFullYear(),
        month: value.getUTCMonth() + 1,
        day: value.getUTCDate()
      };
    }

    if (typeof value !== 'string') {
      throw new TypeError(`${methodName} espera Date ou string no formato YYYY-MM-DD.`);
    }

    const match = value.trim().match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (!match) throw new TypeError(`${methodName} espera string no formato YYYY-MM-DD.`);

    const year = Number(match[1]);
    const month = Number(match[2]);
    const day = Number(match[3]);
    const date = new Date(Date.UTC(year, month - 1, day));

    if (
      date.getUTCFullYear() !== year ||
      date.getUTCMonth() + 1 !== month ||
      date.getUTCDate() !== day
    ) {
      throw new RangeError(`${methodName} recebeu uma data inexistente.`);
    }

    return { year, month, day };
  }

  function parseDateTime(value, methodName) {
    if (value instanceof Date) {
      const millis = value.getTime();
      if (Number.isNaN(millis)) throw new TypeError(`${methodName} recebeu uma data/hora inválida.`);
      return BigInt(millis);
    }

    if (typeof value === 'bigint' || (typeof value === 'number' && Number.isInteger(value)) || typeof value === 'string') {
      if (typeof value === 'string') {
        const trimmed = value.trim();
        if (/^[-+]?\d+$/.test(trimmed)) return assertBigIntRange(trimmed, INT64_MIN, INT64_MAX, methodName);
        const millis = Date.parse(trimmed);
        if (Number.isNaN(millis)) throw new TypeError(`${methodName} recebeu string inválida. Use ISO 8601 ou milissegundos.`);
        return BigInt(millis);
      }
      return assertBigIntRange(value, INT64_MIN, INT64_MAX, methodName);
    }

    throw new TypeError(`${methodName} espera Date, ISO 8601, inteiro seguro, BigInt ou string inteira.`);
  }

  function pad(n, size = 2) {
    return String(n).padStart(size, '0');
  }

  ByteCodec.toHex = function toHex(bytes) {
    ensureInt8Array(bytes, undefined, 'toHex');
    return Array.from(bytes, b => (b & 0xff).toString(16).padStart(2, '0').toUpperCase()).join(' ');
  };

  ByteCodec.fromHex = function fromHex(hex) {
    if (typeof hex !== 'string') throw new TypeError('fromHex espera uma string hexadecimal.');
    const clean = hex.trim();
    if (clean === '') return new Int8Array(0);
    const parts = clean.split(/[\s,;:-]+/).filter(Boolean);
    const values = parts.map(part => {
      if (!/^[0-9a-fA-F]{1,2}$/.test(part)) throw new TypeError(`Byte hexadecimal inválido: ${part}`);
      const n = parseInt(part, 16);
      return n > 127 ? n - 256 : n;
    });
    return new Int8Array(values);
  };

  ByteCodec.writeByte = function writeByte(value) {
    assertIntegerNumber(value, INT8_MIN, INT8_MAX, 'writeByte');
    const out = new Int8Array(1);
    out[0] = value;
    return out;
  };

  ByteCodec.readByte = function readByte(bytes) {
    ensureInt8Array(bytes, 1, 'readByte');
    return bytes[0];
  };

  ByteCodec.writeShort = function writeShort(value, littleEndian = ByteCodec.defaultLittleEndian) {
    assertIntegerNumber(value, INT16_MIN, INT16_MAX, 'writeShort');
    const view = newView(2);
    view.setInt16(0, value, littleEndian);
    return intViewResult(view);
  };

  ByteCodec.readShort = function readShort(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    return viewFromInt8Array(bytes, 2, 'readShort').getInt16(0, littleEndian);
  };

  ByteCodec.writeInt = function writeInt(value, littleEndian = ByteCodec.defaultLittleEndian) {
    assertIntegerNumber(value, INT32_MIN, INT32_MAX, 'writeInt');
    const view = newView(4);
    view.setInt32(0, value, littleEndian);
    return intViewResult(view);
  };

  ByteCodec.readInt = function readInt(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    return viewFromInt8Array(bytes, 4, 'readInt').getInt32(0, littleEndian);
  };

  ByteCodec.writeLong = function writeLong(value, littleEndian = ByteCodec.defaultLittleEndian) {
    const big = assertBigIntRange(value, INT64_MIN, INT64_MAX, 'writeLong');
    const view = newView(8);
    view.setBigInt64(0, big, littleEndian);
    return intViewResult(view);
  };

  ByteCodec.readLong = function readLong(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    return viewFromInt8Array(bytes, 8, 'readLong').getBigInt64(0, littleEndian);
  };

  ByteCodec.writeBoolean = function writeBoolean(value) {
    if (typeof value !== 'boolean') throw new TypeError('writeBoolean espera true ou false.');
    return new Int8Array([value ? 1 : 0]);
  };

  ByteCodec.readBoolean = function readBoolean(bytes) {
    ensureInt8Array(bytes, 1, 'readBoolean');
    if (bytes[0] === 0) return false;
    if (bytes[0] === 1) return true;
    throw new RangeError('readBoolean espera byte 0 ou 1.');
  };

  ByteCodec.writeChar = function writeChar(value, littleEndian = ByteCodec.defaultLittleEndian) {
    if (typeof value !== 'string' || value.length !== 1) {
      throw new TypeError('writeChar espera uma string com exatamente um code unit UTF-16. Caracteres fora do BMP, como emojis, usam dois code units.');
    }
    const view = newView(2);
    view.setUint16(0, value.charCodeAt(0), littleEndian);
    return intViewResult(view);
  };

  ByteCodec.readChar = function readChar(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    const codeUnit = viewFromInt8Array(bytes, 2, 'readChar').getUint16(0, littleEndian);
    return String.fromCharCode(codeUnit);
  };

  ByteCodec.writeFloat = function writeFloat(value, littleEndian = ByteCodec.defaultLittleEndian) {
    if (typeof value !== 'number' || Number.isNaN(value)) throw new TypeError('writeFloat espera um número real.');
    const view = newView(4);
    view.setFloat32(0, value, littleEndian);
    return intViewResult(view);
  };

  ByteCodec.readFloat = function readFloat(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    return viewFromInt8Array(bytes, 4, 'readFloat').getFloat32(0, littleEndian);
  };

  ByteCodec.writeDouble = function writeDouble(value, littleEndian = ByteCodec.defaultLittleEndian) {
    if (typeof value !== 'number' || Number.isNaN(value)) throw new TypeError('writeDouble espera um número real.');
    const view = newView(8);
    view.setFloat64(0, value, littleEndian);
    return intViewResult(view);
  };

  ByteCodec.readDouble = function readDouble(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    return viewFromInt8Array(bytes, 8, 'readDouble').getFloat64(0, littleEndian);
  };

  ByteCodec.writeString = function writeString(value) {
    if (typeof value !== 'string') throw new TypeError('writeString espera string.');
    return new Int8Array(new TextEncoder().encode(value).buffer);
  };

  ByteCodec.readString = function readString(bytes) {
    ensureInt8Array(bytes, undefined, 'readString');
    return new TextDecoder('utf-8', { fatal: true }).decode(toUnsignedByteArray(bytes));
  };

  ByteCodec.writeDate = function writeDate(value, littleEndian = ByteCodec.defaultLittleEndian) {
    const { year, month, day } = parseDateOnly(value, 'writeDate');
    const epochDay = Math.floor(Date.UTC(year, month - 1, day) / DAY_MS);
    return ByteCodec.writeInt(epochDay, littleEndian);
  };

  ByteCodec.readDate = function readDate(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    const epochDay = ByteCodec.readInt(bytes, littleEndian);
    const date = new Date(epochDay * DAY_MS);
    return `${pad(date.getUTCFullYear(), 4)}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCDate())}`;
  };

  ByteCodec.writeDateTime = function writeDateTime(value, littleEndian = ByteCodec.defaultLittleEndian) {
    const millis = parseDateTime(value, 'writeDateTime');
    return ByteCodec.writeLong(millis, littleEndian);
  };

  ByteCodec.readDateTime = function readDateTime(bytes, littleEndian = ByteCodec.defaultLittleEndian) {
    const millis = ByteCodec.readLong(bytes, littleEndian);
    if (millis < BigInt(Number.MIN_SAFE_INTEGER) || millis > BigInt(Number.MAX_SAFE_INTEGER)) {
      return millis; // Fora da faixa prática de Date do JavaScript; devolve BigInt.
    }
    return new Date(Number(millis)).toISOString();
  };

  if (typeof module !== 'undefined' && module.exports) module.exports = ByteCodec;
  global.ByteCodec = ByteCodec;
})(typeof window !== 'undefined' ? window : globalThis);
