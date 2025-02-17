const toHex = i => ('00' + i.toString(16)).slice(-2);

onload = () => {
  byBtn.onclick = () => {
    let i = parseInt(byValor.value);
    limpaCampos('byte');
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é um número inteiro';
    else
      resultado.innerHTML = convertInt(i, 1);
  }
  shBtn.onclick = () => {
    let i = parseInt(shValor.value);
    limpaCampos('short');
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é um número inteiro';
    else
      resultado.innerHTML = convertInt(i, 2);
  }
  iBtn.onclick = () => {
    let i = parseInt(iValor.value);
    limpaCampos('int');
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é um número inteiro';
    else
      resultado.innerHTML = convertInt(i, 4);
  }
  loBtn.onclick = () => {
    let i = parseInt(loValor.value);
    limpaCampos('long');
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é um número inteiro';
    else
      resultado.innerHTML = convertInt(i, 8);
  }
  fBtn.onclick = () => {
    limpaCampos('float');
    let i = parseFloat(fValor.value);
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é um número real';
    else
      resultado.innerHTML = convertFloat(i,4);
  }
  dbBtn.onclick = () => {
    limpaCampos('double');
    let i = parseFloat(dbValor.value);
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é um número real';
    else
      resultado.innerHTML = convertFloat(i, 8);
  }
  sBtn.onclick = () => {
    limpaCampos('string');
    resultado.innerHTML = convertString(sValor.value);
  }

  dtBtn.onclick = () => {
    try{
      let dtPartes = dtValor.value.split('/');
      let data = new Date(
        parseInt(dtPartes[2]),
        parseInt(dtPartes[1]-1),
        parseInt(dtPartes[0])
      );
      let millis = data.getTime() - data.getTimezoneOffset()*60000;
      let dias = millis/86400000;
    let i = parseInt(dias);
    limpaCampos('date');
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é uma data válida';
    else
      resultado.innerHTML = convertInt(i, 4);
    } catch(e){
      resultado.innerHTML = 'O valor informado não é uma data válida';
    }
  }

  diBtn.onclick = () => {
    try{
      let [data, hora] = diValor.value.split(' ');
      let [dia, mes, ano] = data.split('/');
      let [hs, mins, secs] = hora.split(':');
      let dt = new Date(
        ano, mes-1, dia, hs, mins, secs
      );
      let millis = dt.getTime() - dt.getTimezoneOffset()*60000;
    let i = parseInt(millis);
    limpaCampos('datetime');
    if(isNaN(i))
      resultado.innerHTML = 'O valor informado não é uma data/hora válida';
    else
      resultado.innerHTML = convertInt(i, 8);
    } catch(e){
      resultado.innerHTML = 'O valor informado não é uma data/hora válida';
    }
  }

  
}

function convertString(str) {
  return unescape(encodeURIComponent(str))
          .split('')
          .map(v => v.charCodeAt(0).toString(16))
          .join(' ')
          .toUpperCase();
}

function convertInt(i, n) {
  const buffer = new ArrayBuffer(n);
  const view = new DataView(buffer);
  if(n==1)
    view.setInt8(0, i);
  else if(n==2)
    view.setInt16(0, i);
  else if(n==4)
    view.setInt32(0, i);
  else
    view.setBigInt64(0, BigInt(i));
  return Array
    .apply(null, { length: n })
    .map((_, i) => toHex(view.getUint8(i)))
    .join(' ')
    .toUpperCase();
}

function convertFloat(i, n) {
  const buffer = new ArrayBuffer(n);
  const view = new DataView(buffer);
  if(n==4)
    view.setFloat32(0, i);
  else
    view.setFloat64(0, i);
  return Array
    .apply(null, { length: n })
    .map((_, i) => toHex(view.getUint8(i)))
    .join(' ')
    .toUpperCase();
}

function limpaCampos(exceto) {
  if(exceto != 'byte') 
    byValor.value = '';
  if(exceto != 'short') 
    shValor.value = '';
  if(exceto != 'int') 
    iValor.value = '';
  if(exceto != 'long') 
    loValor.value = '';
  if(exceto != 'float') 
    fValor.value = '';
  if(exceto != 'double') 
    dbValor.value = '';
  if(exceto != 'string')
    sValor.value = '';
  if(exceto != 'date')
    dtValor.value = '';
  if(exceto != 'datetime')
    diValor.value = '';
}