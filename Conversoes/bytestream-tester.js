/* bytestream-tester.js — lógica da página de teste interativa */

(function () {
  'use strict';

  // ─────────────────────────────────────────────
  // Configuração por tipo: placeholder, hint e parser de entrada
  // ─────────────────────────────────────────────
  const TYPE_CONFIG = {
    byte:     { placeholder: 'ex: -128 a 127',           hint: '42',              inputRow: true  },
    short:    { placeholder: 'ex: -32768 a 32767',        hint: '1000',            inputRow: true  },
    int:      { placeholder: 'ex: -2147483648 a 2147483647', hint: '305419896',   inputRow: true  },
    long:     { placeholder: 'ex: 0 a 9007199254740991',  hint: '72623859790382856', inputRow: true },
    boolean:  { placeholder: '',                           hint: '',               inputRow: false },
    char:     { placeholder: 'ex: A  ã  中',              hint: 'A',              inputRow: true  },
    float:    { placeholder: 'ex: 3.14',                  hint: '3.14',           inputRow: true  },
    double:   { placeholder: 'ex: 3.141592653589793',     hint: '3.141592653589793', inputRow: true },
    string:   { placeholder: 'ex: Olá, mundo!',           hint: 'AED3',           inputRow: true  },
    date:     { placeholder: 'dd/mm/aaaa',                hint: '01/01/2000',     inputRow: true  },
    datetime: { placeholder: 'dd/mm/aaaa hh:mm:ss',       hint: '01/01/2000 00:00:00', inputRow: true },
  };

  // ─────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────
  function hexOf(int8arr) {
    return Array.from(int8arr)
      .map(b => ('00' + (b & 0xFF).toString(16)).slice(-2).toUpperCase())
      .join(' ');
  }

  function hexToInt8Array(hexStr) {
    const tokens = hexStr.trim().split(/\s+/);
    if (tokens.some(t => !/^[0-9a-fA-F]{1,2}$/.test(t))) {
      throw new Error('Formato inválido. Use pares hex separados por espaço, ex: 00 1A FF');
    }
    const arr = new Int8Array(tokens.length);
    tokens.forEach((t, i) => { arr[i] = parseInt(t, 16); });
    return arr;
  }

  function parseWriteValue(type, rawValue, boolValue) {
    switch (type) {
      case 'boolean': return boolValue;
      case 'byte':
      case 'short':
      case 'int': {
        const n = Number(rawValue);
        if (!Number.isInteger(n)) throw new Error('Valor deve ser um número inteiro.');
        return n;
      }
      case 'long': {
        if (rawValue.trim() === '') throw new Error('Informe um valor.');
        return BigInt(rawValue.trim());
      }
      case 'float':
      case 'double': {
        const n = parseFloat(rawValue);
        if (isNaN(n)) throw new Error('Valor deve ser um número real.');
        return n;
      }
      case 'char': {
        if (rawValue.length === 0) throw new Error('Informe um caractere.');
        return rawValue[0];
      }
      case 'string': return rawValue;
      case 'date':
      case 'datetime': {
        if (rawValue.trim() === '') throw new Error('Informe uma data.');
        return rawValue.trim();
      }
    }
  }

  function callWrite(type, value) {
    switch (type) {
      case 'byte':     return ByteStream.writeByte(value);
      case 'short':    return ByteStream.writeShort(value);
      case 'int':      return ByteStream.writeInt(value);
      case 'long':     return ByteStream.writeLong(value);
      case 'boolean':  return ByteStream.writeBoolean(value);
      case 'char':     return ByteStream.writeChar(value);
      case 'float':    return ByteStream.writeFloat(value);
      case 'double':   return ByteStream.writeDouble(value);
      case 'string':   return ByteStream.writeString(value);
      case 'date':     return ByteStream.writeDate(value);
      case 'datetime': return ByteStream.writeDateTime(value);
    }
  }

  function callRead(type, int8arr, offset) {
    switch (type) {
      case 'byte':     return ByteStream.readByte(int8arr, offset);
      case 'short':    return ByteStream.readShort(int8arr, offset);
      case 'int':      return ByteStream.readInt(int8arr, offset);
      case 'long':     return ByteStream.readLong(int8arr, offset);
      case 'boolean':  return ByteStream.readBoolean(int8arr, offset);
      case 'char':     return ByteStream.readChar(int8arr, offset);
      case 'float':    return ByteStream.readFloat(int8arr, offset);
      case 'double':   return ByteStream.readDouble(int8arr, offset);
      case 'string':   return ByteStream.readString(int8arr, offset);
      case 'date':     return ByteStream.readDate(int8arr, offset);
      case 'datetime': return ByteStream.readDateTime(int8arr, offset);
    }
  }

  function formatReadResult(type, value) {
    if (type === 'date' || type === 'datetime') {
      const d = value; // Date object
      if (type === 'date') {
        const dd = String(d.getUTCDate()).padStart(2, '0');
        const mm = String(d.getUTCMonth() + 1).padStart(2, '0');
        const yyyy = d.getUTCFullYear();
        return { display: `${dd}/${mm}/${yyyy}`, meta: `getTime() = ${d.getTime()} ms` };
      } else {
        const pad = n => String(n).padStart(2, '0');
        const dStr = `${pad(d.getUTCDate())}/${pad(d.getUTCMonth()+1)}/${d.getUTCFullYear()}`;
        const tStr = `${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}:${pad(d.getUTCSeconds())}`;
        return { display: `${dStr} ${tStr} UTC`, meta: `getTime() = ${d.getTime()} ms` };
      }
    }
    if (type === 'long') {
      return { display: value.toString() + 'n', meta: 'BigInt' };
    }
    if (type === 'float') {
      return { display: value.toPrecision(7), meta: 'float32 (precisão limitada)' };
    }
    if (type === 'char') {
      const cp = value.codePointAt(0);
      return { display: `'${value}'`, meta: `U+${cp.toString(16).toUpperCase().padStart(4,'0')}` };
    }
    if (type === 'string') {
      return { display: `"${value}"`, meta: `${value.length} char(s)` };
    }
    return { display: String(value), meta: '' };
  }

  // ─────────────────────────────────────────────
  // Renderizadores de resultado
  // ─────────────────────────────────────────────
  function renderWriteResult(container, int8arr, hexStr) {
    const cells = Array.from(int8arr).map((b, i) => {
      const hex = ('00' + (b & 0xFF).toString(16)).slice(-2).toUpperCase();
      return `<div class="byte-cell" style="animation-delay:${i * 0.025}s">
        <div class="byte-hex">${hex}</div>
        <div class="byte-idx">[${i}]</div>
      </div>`;
    }).join('');

    container.innerHTML = `
      <div class="byte-cells-label">BYTES (big-endian · hex)</div>
      <div class="byte-cells">${cells}</div>
      <div class="byte-summary">
        <div class="summary-item">
          <span class="summary-key">Comprimento</span>
          <span class="summary-val">${int8arr.length} byte${int8arr.length !== 1 ? 's' : ''}</span>
        </div>
      </div>
      <div class="copy-strip">
        <span class="copy-hex-text">${hexStr}</span>
        <button class="copy-btn" id="copy-hex-btn">Copiar hex</button>
        <button class="copy-btn" id="copy-read-btn" title="Preenche o painel READ com esses bytes">→ READ</button>
      </div>`;

    document.getElementById('copy-hex-btn').addEventListener('click', function () {
      navigator.clipboard.writeText(hexStr).then(() => {
        this.textContent = '✓ Copiado';
        this.classList.add('copied');
        setTimeout(() => { this.textContent = 'Copiar hex'; this.classList.remove('copied'); }, 1500);
      });
    });

    document.getElementById('copy-read-btn').addEventListener('click', function () {
      document.getElementById('read-input').value = hexStr;
      // Sincroniza o tipo
      const writeType = document.getElementById('write-type').value;
      document.getElementById('read-type').value = writeType;
      document.getElementById('read-offset').value = '0';
      document.getElementById('read-input').focus();
      this.textContent = '✓ Enviado';
      this.classList.add('copied');
      setTimeout(() => { this.textContent = '→ READ'; this.classList.remove('copied'); }, 1200);
    });
  }

  function renderReadResult(container, type, value) {
    const { display, meta } = formatReadResult(type, value);
    container.innerHTML = `
      <div class="read-value-wrap">
        <span class="read-value-label">VALOR DECODIFICADO</span>
        <span class="read-value">${escapeHtml(display)}</span>
        ${meta ? `<span class="read-meta">${escapeHtml(meta)}</span>` : ''}
      </div>`;
  }

  function renderError(container, msg) {
    container.innerHTML = `<div class="result-error">${escapeHtml(msg)}</div>`;
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // ─────────────────────────────────────────────
  // Inicialização
  // ─────────────────────────────────────────────
  document.addEventListener('DOMContentLoaded', function () {

    // ── Elementos WRITE
    const writeType    = document.getElementById('write-type');
    const writeInput   = document.getElementById('write-input');
    const writeInputRow = document.getElementById('write-input-row');
    const writeBoolRow = document.getElementById('write-bool-row');
    const boolTrueBtn  = document.getElementById('bool-true');
    const boolFalseBtn = document.getElementById('bool-false');
    const writeBtn     = document.getElementById('write-btn');
    const writeResult  = document.getElementById('write-result');
    const writeLabel   = document.getElementById('write-input-label');

    // ── Elementos READ
    const readType   = document.getElementById('read-type');
    const readInput  = document.getElementById('read-input');
    const readOffset = document.getElementById('read-offset');
    const readBtn    = document.getElementById('read-btn');
    const readResult = document.getElementById('read-result');

    // Estado do boolean
    let boolValue = true;

    // ── Atualiza UI quando o tipo WRITE muda
    function onWriteTypeChange() {
      const type = writeType.value;
      const cfg = TYPE_CONFIG[type];

      if (type === 'boolean') {
        writeInputRow.classList.add('hidden');
        writeBoolRow.classList.remove('hidden');
      } else {
        writeInputRow.classList.remove('hidden');
        writeBoolRow.classList.add('hidden');
        writeInput.placeholder = cfg.placeholder;
        writeLabel.textContent = type === 'char' ? 'Caractere' :
                                 type === 'string' ? 'String' :
                                 type === 'date' ? 'Data' :
                                 type === 'datetime' ? 'Data/Hora' : 'Valor';
      }

      // Limpa resultado anterior
      writeResult.innerHTML = `<div class="result-empty">
        <span class="result-empty-icon">⬡</span>
        <span>Aguardando conversão…</span>
      </div>`;
      writeInput.value = '';
      writeInput.classList.remove('error');
    }

    writeType.addEventListener('change', onWriteTypeChange);
    onWriteTypeChange();

    // ── Boolean toggle
    boolTrueBtn.addEventListener('click', () => {
      boolValue = true;
      boolTrueBtn.classList.add('active');
      boolFalseBtn.classList.remove('active');
    });
    boolFalseBtn.addEventListener('click', () => {
      boolValue = false;
      boolFalseBtn.classList.add('active');
      boolTrueBtn.classList.remove('active');
    });

    // ── Ação WRITE
    function doWrite() {
      const type = writeType.value;
      writeInput.classList.remove('error');
      try {
        const value = parseWriteValue(type, writeInput.value, boolValue);
        const result = callWrite(type, value);
        const hex = hexOf(result);
        renderWriteResult(writeResult, result, hex);
      } catch (e) {
        writeInput.classList.add('error');
        renderError(writeResult, e.message);
      }
    }

    writeBtn.addEventListener('click', doWrite);
    writeInput.addEventListener('keydown', e => { if (e.key === 'Enter') doWrite(); });

    // ── Ação READ
    function doRead() {
      const type = readType.value;
      const offset = parseInt(readOffset.value) || 0;
      readInput.classList.remove('error');
      try {
        const arr = hexToInt8Array(readInput.value);
        const value = callRead(type, arr, offset);
        renderReadResult(readResult, type, value);
      } catch (e) {
        readInput.classList.add('error');
        renderError(readResult, e.message);
      }
    }

    readBtn.addEventListener('click', doRead);
    readInput.addEventListener('keydown', e => { if (e.key === 'Enter') doRead(); });

  });

})();
