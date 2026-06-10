'use strict';

const typeConfig = {
  byte: { label: 'Byte', input: 'number', sample: '-42', write: 'writeByte', read: 'readByte', parser: parseNumber },
  short: { label: 'Short', input: 'number', sample: '32000', write: 'writeShort', read: 'readShort', parser: parseNumber },
  int: { label: 'Int', input: 'number', sample: '123456789', write: 'writeInt', read: 'readInt', parser: parseNumber },
  long: { label: 'Long', input: 'text', sample: '9223372036854775807', write: 'writeLong', read: 'readLong', parser: parseText },
  boolean: { label: 'Boolean', input: 'select', sample: 'true', write: 'writeBoolean', read: 'readBoolean', parser: parseBoolean },
  char: { label: 'Char UTF-16', input: 'text', sample: 'A', write: 'writeChar', read: 'readChar', parser: parseText },
  float: { label: 'Float', input: 'number', sample: '3.14', write: 'writeFloat', read: 'readFloat', parser: parseFloatStrict },
  double: { label: 'Double', input: 'number', sample: '3.141592653589793', write: 'writeDouble', read: 'readDouble', parser: parseFloatStrict },
  string: { label: 'String UTF-8', input: 'text', sample: 'Olá, mundo!', write: 'writeString', read: 'readString', parser: parseText },
  date: { label: 'Date / epoch day', input: 'date', sample: '2026-06-10', write: 'writeDate', read: 'readDate', parser: parseText },
  datetime: { label: 'DateTime / epoch millis', input: 'datetime-local', sample: '2026-06-10T12:00:00', write: 'writeDateTime', read: 'readDateTime', parser: parseDateTimeLocal }
};

function parseNumber(value) {
  if (!/^[-+]?\d+$/.test(value.trim())) throw new Error('Digite um número inteiro válido.');
  return Number(value);
}

function parseFloatStrict(value) {
  if (!/^[-+]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][-+]?\d+)?$/.test(value.trim())) {
    throw new Error('Digite um número real válido.');
  }
  return Number(value);
}

function parseText(value) {
  return value;
}

function parseBoolean(value) {
  return value === 'true';
}

function parseDateTimeLocal(value) {
  if (!value) throw new Error('Informe a data/hora.');
  return new Date(value);
}

function currentEndian() {
  return document.getElementById('littleEndian').checked ? ByteCodec.LITTLE_ENDIAN : ByteCodec.BIG_ENDIAN;
}

function formatValue(value) {
  if (typeof value === 'bigint') return `${value}n`;
  if (typeof value === 'string') return value;
  return JSON.stringify(value);
}

function showResult(target, bytes, valueRead) {
  target.classList.remove('error');
  target.textContent = `Int8Array: [${Array.from(bytes).join(', ')}]\nHex: ${ByteCodec.toHex(bytes)}\nLeitura: ${formatValue(valueRead)}`;
}

function showError(target, error) {
  target.classList.add('error');
  target.textContent = error.message || String(error);
}

function buildCards() {
  const grid = document.getElementById('typeGrid');

  Object.entries(typeConfig).forEach(([key, cfg]) => {
    const card = document.createElement('article');
    card.className = 'card';

    const inputHtml = cfg.input === 'select'
      ? `<select id="${key}Input"><option value="true">true</option><option value="false">false</option></select>`
      : `<input id="${key}Input" type="${cfg.input}" value="${cfg.sample}" />`;

    card.innerHTML = `
      <h3>${cfg.label}</h3>
      <div class="row">
        <div class="field">
          <label for="${key}Input">Valor</label>
          ${inputHtml}
        </div>
        <button id="${key}Btn">write + read</button>
      </div>
      <div id="${key}Output" class="output"></div>
    `;

    grid.appendChild(card);

    document.getElementById(`${key}Btn`).addEventListener('click', () => {
      const output = document.getElementById(`${key}Output`);
      try {
        const raw = document.getElementById(`${key}Input`).value;
        const parsed = cfg.parser(raw);
        const endian = currentEndian();
        const usesEndian = ['short', 'int', 'long', 'char', 'float', 'double', 'date', 'datetime'].includes(key);
        const bytes = usesEndian ? ByteCodec[cfg.write](parsed, endian) : ByteCodec[cfg.write](parsed);
        const valueRead = usesEndian ? ByteCodec[cfg.read](bytes, endian) : ByteCodec[cfg.read](bytes);
        showResult(output, bytes, valueRead);
      } catch (error) {
        showError(output, error);
      }
    });
  });
}

function setupGenericReader() {
  const readBtn = document.getElementById('genericReadBtn');
  readBtn.addEventListener('click', () => {
    const out = document.getElementById('genericReadOutput');
    try {
      const type = document.getElementById('genericType').value;
      const hex = document.getElementById('genericBytes').value;
      const bytes = ByteCodec.fromHex(hex);
      const cfg = typeConfig[type];
      const endian = currentEndian();
      const usesEndian = ['short', 'int', 'long', 'char', 'float', 'double', 'date', 'datetime'].includes(type);
      const value = usesEndian ? ByteCodec[cfg.read](bytes, endian) : ByteCodec[cfg.read](bytes);
      out.classList.remove('error');
      out.textContent = `Valor lido: ${formatValue(value)}`;
    } catch (error) {
      showError(out, error);
    }
  });
}

function fillSamples() {
  Object.keys(typeConfig).forEach(key => {
    const btn = document.getElementById(`${key}Btn`);
    if (btn) btn.click();
  });
}

document.addEventListener('DOMContentLoaded', () => {
  buildCards();
  setupGenericReader();
  document.getElementById('fillSamplesBtn').addEventListener('click', fillSamples);
  fillSamples();
});
