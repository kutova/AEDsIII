/*********
 * ARVORE B+ 
 * 
 * Os nomes dos métodos foram mantidos em inglês
 * apenas para manter a coerência com o resto da
 * disciplina:
 * - boolean create(RegistroArvoreBMais objeto)   
 * - T read(T objeto)
 * - ArrayList<T> readAll(T objeto)
 * - ArrayList<T> readPage(T objeto, T depoisDe, int quantidade)
 * - boolean delete(RegistroArvoreBMais objeto)
 * 
 * Implementado pelo Prof. Marcos Kutova
 * v2.1 - 2026
 */
package aed3;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

// Esta versão da árvore funciona apenas como um conjunto de par de chaves.
// A primeira chave pode repetir na árvore, mas não o par de chaves, 
// isto é, quando a primeira chave de dois elementos for igual, a segunda chave,
// deve ser necessariamente diferente.

public class ArvoreBMais<T extends InterfaceArvoreBMais<T>> {

    private int ordem; // Número máximo de filhos que uma página pode conter
    private int maxElementos; // Variável igual a ordem - 1 para facilitar a clareza do código
    private int maxFilhos; // Variável igual a ordem para facilitar a clareza do código
    private RandomAccessFile arquivo; // Arquivo em que a árvore será armazenada
    private String nomeArquivo;
    private Constructor<T> construtor;

    // Variáveis usadas nas funções recursivas (já que não é possível passar valores
    // por referência)
    private T auxElemento;
    private long auxPonteiroPagina;
    private boolean cresceu;
    private boolean diminuiu;

    // Esta classe representa uma página da árvore (folha ou não folha).
    private class Pagina {

        protected int ordem; // Número máximo de filhos que uma página pode ter
        protected Constructor<T> construtor;
        protected int maxElementos; // Variável igual a ordem - 1 para facilitar a clareza do código
        protected int maxFilhos; // Variável igual a ordem para facilitar a clareza do código
        protected int TAMANHO_ELEMENTO; // Os elementos são de tamanho fixo
        protected int TAMANHO_PAGINA; // A página será de tamanho fixo, calculado a partir da ordem

        protected ArrayList<T> elementos; // Elementos da página
        protected ArrayList<Long> filhos; // Vetor de ponteiros para os filhos
        protected long proxima; // Próxima folha, quando a página for uma folha

        // Construtor da página
        public Pagina(Constructor<T> ct, int o) throws Exception {

            // Inicialização dos atributos
            this.construtor = ct;
            this.ordem = o;
            this.maxFilhos = this.ordem;
            this.maxElementos = this.ordem - 1;
            this.elementos = new ArrayList<>(this.maxElementos);
            this.filhos = new ArrayList<>(this.maxFilhos);
            this.proxima = -1;

            // Cálculo do tamanho (fixo) da página
            // cada elemento -> depende do objeto
            // cada ponteiro de filho -> 8 bytes
            // último filho -> 8 bytes
            // ponteiro próximo -> 8 bytes
            this.TAMANHO_ELEMENTO = this.construtor.newInstance().size();
            this.TAMANHO_PAGINA = 4 + this.maxElementos * this.TAMANHO_ELEMENTO + this.maxFilhos * 8 + 8;
        }

        // Retorna o vetor de bytes que representa a página para armazenamento em
        // arquivo
        protected byte[] serialize() throws IOException {

            // Um fluxo de bytes é usado para construção do vetor de bytes
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);

            // Quantidade de elementos presentes na página
            out.writeInt(this.elementos.size());

            // Escreve todos os elementos
            int i = 0;
            while (i < this.elementos.size()) {
                out.writeLong(this.filhos.get(i).longValue());
                out.write(this.elementos.get(i).serialize());
                i++;
            }
            if (this.filhos.size() > 0)
                out.writeLong(this.filhos.get(i).longValue());
            else
                out.writeLong(-1L);

            // Completa o restante da página com registros vazios
            byte[] registroVazio = new byte[TAMANHO_ELEMENTO];
            while (i < this.maxElementos) {
                out.write(registroVazio);
                out.writeLong(-1L);
                i++;
            }

            // Escreve o ponteiro para a próxima página
            out.writeLong(this.proxima);

            // Retorna o vetor de bytes que representa a página
            return ba.toByteArray();
        }

        // Reconstrói uma página a partir de um vetor de bytes lido no arquivo
        public void deserialize(byte[] buffer) throws Exception {

            // Usa um fluxo de bytes para leitura dos atributos
            ByteArrayInputStream ba = new ByteArrayInputStream(buffer);
            DataInputStream in = new DataInputStream(ba);

            // Lê a quantidade de elementos da página
            int n = in.readInt();

            // Lê todos os elementos (reais ou vazios)
            int i = 0;
            this.elementos = new ArrayList<>(this.maxElementos);
            this.filhos = new ArrayList<>(this.maxFilhos);
            T elem;
            while (i < n) {
                this.filhos.add(in.readLong());
                byte[] registro = new byte[TAMANHO_ELEMENTO];
                in.read(registro);
                elem = this.construtor.newInstance();
                elem.deserialize(registro);
                this.elementos.add(elem);
                i++;
            }
            this.filhos.add(in.readLong());
            in.skipBytes((this.maxElementos - i) * (TAMANHO_ELEMENTO + 8));
            this.proxima = in.readLong();
        }
    }

    // ------------------------------------------------------------------------------

    public ArvoreBMais(Constructor<T> c, int o, String na) throws Exception {

        // Inicializa os atributos da árvore
        construtor = c;
        ordem = o;
        maxElementos = o - 1;
        maxFilhos = o;
        nomeArquivo = na;

        // Abre (ou cria) o arquivo, escrevendo uma raiz empty, se necessário.
        arquivo = new RandomAccessFile(nomeArquivo, "rw");
        if (arquivo.length() < 16) {
            arquivo.writeLong(-1); // raiz empty
            arquivo.writeLong(-1); // pointeiro lista excluídos
        }
    }

    // Testa se a árvore está empty. Uma árvore empty é identificada pela raiz == -1
    public boolean empty() throws IOException {
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        return raiz == -1;
    }

    // Busca um elemento pela chave completa. Como o par completo de chaves é
    // único na árvore, retorna o elemento encontrado ou null.
    public T read(T elem) throws Exception {

        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();

        if (raiz == -1)
            return null;

        return read1(elem, raiz);
    }

    // Busca recursiva pela chave completa.
    private T read1(T elem, long enderecoPagina) throws Exception {

        if (enderecoPagina == -1)
            return null;

        arquivo.seek(enderecoPagina);
        Pagina pagina = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pagina.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pagina.deserialize(buffer);

        int i = 0;
        while (i < pagina.elementos.size() && elem.compareTo(pagina.elementos.get(i)) > 0)
            i++;

        // Nas folhas estão todos os elementos válidos.
        if (pagina.filhos.get(0) == -1) {
            if (i < pagina.elementos.size() && elem.compareTo(pagina.elementos.get(i)) == 0)
                return pagina.elementos.get(i);

            // Em uma B+, uma chave separadora pode coincidir com o primeiro elemento
            // da folha seguinte. Este teste também torna a busca robusta nesse limite.
            if (i == pagina.elementos.size() && pagina.proxima != -1) {
                arquivo.seek(pagina.proxima);
                arquivo.read(buffer);
                pagina.deserialize(buffer);
                if (!pagina.elementos.isEmpty() && elem.compareTo(pagina.elementos.get(0)) == 0)
                    return pagina.elementos.get(0);
            }
            return null;
        }

        // Em páginas internas, uma chave igual à separadora pertence ao filho da direita.
        if (i < pagina.elementos.size() && elem.compareTo(pagina.elementos.get(i)) == 0)
            return read1(elem, pagina.filhos.get(i + 1));
        else
            return read1(elem, pagina.filhos.get(i));
    }

    // Retorna todos os elementos armazenados na árvore, em ordem.
    // Este método é útil para listagens completas e testes. Como a resposta
    // inteira é materializada em memória, não deve ser usado para paginação.
    public ArrayList<T> readAll() throws Exception {

        ArrayList<T> lista = new ArrayList<>();

        arquivo.seek(0);
        long enderecoPagina = arquivo.readLong();
        if (enderecoPagina == -1)
            return lista;

        Pagina pagina = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pagina.TAMANHO_PAGINA];

        // Desce sempre pelo primeiro filho até alcançar a folha mais à esquerda.
        while (enderecoPagina != -1) {
            arquivo.seek(enderecoPagina);
            arquivo.read(buffer);
            pagina.deserialize(buffer);
            if (pagina.filhos.get(0) == -1)
                break;
            enderecoPagina = pagina.filhos.get(0);
        }

        // Percorre as folhas encadeadas.
        while (enderecoPagina != -1) {
            arquivo.seek(enderecoPagina);
            arquivo.read(buffer);
            pagina.deserialize(buffer);
            lista.addAll(pagina.elementos);
            enderecoPagina = pagina.proxima;
        }

        return lista;
    }

    // Retorna todos os elementos cuja chave principal corresponde à chave de leitura de elem.
    public ArrayList<T> readAll(T elem) throws Exception {
        return readPage(elem, null, Integer.MAX_VALUE);
    }

    // Retorna, no máximo, quantidade elementos cuja chave principal corresponde
    // à chave de leitura de elem. Se depoisDe for diferente de null, a leitura começa no primeiro
    // elemento posterior a depoisDe. Assim, o último elemento de uma página pode
    // ser usado como cursor para a página seguinte.
    public ArrayList<T> readPage(T elem, T depoisDe, int quantidade) throws Exception {

        if (quantidade <= 0)
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");

        if (depoisDe != null && elem.compareToKeyRead(depoisDe) != 0)
            throw new IllegalArgumentException("O cursor deve possuir a mesma chave principal da busca.");

        ArrayList<T> lista = new ArrayList<>();

        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        if (raiz == -1)
            return lista;

        // Na primeira página, procura a primeira folha que pode conter a chave principal.
        // Nas páginas seguintes, usa o cursor completo para chegar diretamente à região
        // em que a leitura deve continuar.
        long enderecoFolha = depoisDe == null
                ? findLeafByKey(elem, raiz)
                : findLeafByElement(depoisDe, raiz);

        if (enderecoFolha == -1)
            return lista;

        Pagina pagina = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pagina.TAMANHO_PAGINA];

        while (enderecoFolha != -1 && lista.size() < quantidade) {
            arquivo.seek(enderecoFolha);
            arquivo.read(buffer);
            pagina.deserialize(buffer);

            for (int i = 0; i < pagina.elementos.size() && lista.size() < quantidade; i++) {
                T atual = pagina.elementos.get(i);
                int c = elem.compareToKeyRead(atual);

                // A chave procurada ainda está adiante.
                if (c > 0)
                    continue;

                // A chave procurada já foi ultrapassada.
                if (c < 0)
                    return lista;

                // Na continuação da paginação, ignora o cursor e tudo o que o antecede.
                if (depoisDe == null || atual.compareTo(depoisDe) > 0)
                    lista.add(atual);
            }

            enderecoFolha = pagina.proxima;
        }

        return lista;
    }

    // Localiza a primeira folha que pode conter uma determinada chave principal.
    private long findLeafByKey(T elem, long enderecoPagina) throws Exception {

        if (enderecoPagina == -1)
            return -1;

        arquivo.seek(enderecoPagina);
        Pagina pagina = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pagina.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pagina.deserialize(buffer);

        if (pagina.filhos.get(0) == -1)
            return enderecoPagina;

        int i = 0;
        while (i < pagina.elementos.size() && elem.compareToKeyRead(pagina.elementos.get(i)) > 0)
            i++;

        // Se a chave principal for igual à separadora, desce pela esquerda para não
        // perder ocorrências da mesma chave que possam ter ficado na folha anterior.
        return findLeafByKey(elem, pagina.filhos.get(i));
    }

    // Localiza a folha correspondente à posição de um elemento completo.
    private long findLeafByElement(T elem, long enderecoPagina) throws Exception {

        if (enderecoPagina == -1)
            return -1;

        arquivo.seek(enderecoPagina);
        Pagina pagina = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pagina.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pagina.deserialize(buffer);

        if (pagina.filhos.get(0) == -1)
            return enderecoPagina;

        int i = 0;
        while (i < pagina.elementos.size() && elem.compareTo(pagina.elementos.get(i)) > 0)
            i++;

        if (i < pagina.elementos.size() && elem.compareTo(pagina.elementos.get(i)) == 0)
            i++;

        return findLeafByElement(elem, pagina.filhos.get(i));
    }

    // Inclusão de novos elementos na árvore. A inclusão é recursiva. A primeira
    // função chama a segunda recursivamente, passando a raiz como referência.
    // Eventualmente, a árvore pode crescer para cima.
    public boolean create(T elem) throws Exception {

        // Carrega a raiz
        arquivo.seek(0);
        long pagina;
        pagina = arquivo.readLong();

        // O processo de inclusão permite que os valores passados como referência
        // sejam substituídos por outros valores, para permitir a divisão de páginas
        // e crescimento da árvore. Assim, o elemento promovido é mantido na
        // variável global auxElemento.
        auxElemento = elem.clone();

        // Se houver crescimento, então será criada uma página extra e será mantido um
        // ponteiro para essa página. Os valores também são globais.
        auxPonteiroPagina = -1;
        cresceu = false;

        // Chamada recursiva para a inserção do par de chaves
        boolean inserido = create1(pagina);

        // Testa a necessidade de criação de uma nova raiz.
        if (cresceu) {

            // Cria a nova página que será a raiz. O ponteiro esquerdo da raiz
            // será a raiz antiga e o seu ponteiro direito será para a nova página.
            Pagina novaRaiz = new Pagina(construtor, ordem);
            novaRaiz.elementos = new ArrayList<>(this.maxElementos);
            novaRaiz.elementos.add(auxElemento);
            novaRaiz.filhos = new ArrayList<>(this.maxFilhos);
            novaRaiz.filhos.add(pagina);
            novaRaiz.filhos.add(auxPonteiroPagina);

            // Acha o espaço em disco. Testa se há páginas excluídas.
            arquivo.seek(8);
            long end = arquivo.readLong();
            if(end==-1) {
                end = arquivo.length();
            } else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
                arquivo.seek(end);
                Pagina pa_excluida = new Pagina(construtor, ordem);
                byte[] buffer = new byte[pa_excluida.TAMANHO_PAGINA];
                arquivo.read(buffer);
                pa_excluida.deserialize(buffer);
                arquivo.seek(8);
                arquivo.writeLong(pa_excluida.proxima);
            }
            arquivo.seek(end);
            long ponteiroRaiz = arquivo.getFilePointer();
            arquivo.write(novaRaiz.serialize());

            // Atualiza o ponteiro para a raiz no cabeçalho do arquivo
            arquivo.seek(0);
            arquivo.writeLong(ponteiroRaiz);
            inserido = true;
        }

        return inserido;
    }

    // Função recursiva de inclusão. A função passa uma página de referência.
    // As inclusões são sempre feitas em uma folha.
    private boolean create1(long pagina) throws Exception {

        // Testa se passou para o filho de uma página folha. Nesse caso,
        // inicializa as variáveis globais de controle.
        if (pagina == -1) {
            cresceu = true;
            auxPonteiroPagina = -1;
            return false;
        }

        // Lê a página passada como referência
        arquivo.seek(pagina);
        Pagina pa = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.deserialize(buffer);

        // Busca o próximo ponteiro de descida. Como pode haver repetição
        // da primeira chave, a segunda também é usada como referência.
        // Nesse primeiro passo, todos os pares menores são ultrapassados.
        int i = 0;
        while (i < pa.elementos.size() && (auxElemento.compareTo(pa.elementos.get(i)) > 0)) {
            i++;
        }

        // Testa se o registro já existe em uma folha. Se isso acontecer, então
        // a inclusão é cancelada.
        if (i < pa.elementos.size() && pa.filhos.get(0) == -1 && auxElemento.compareTo(pa.elementos.get(i)) == 0) {
            cresceu = false;
            return false;
        }

        // Continua a busca recursiva por uma nova página. A busca continuará até o
        // filho inexistente de uma página folha ser alcançado.
        boolean inserido;
        if (i == pa.elementos.size() || auxElemento.compareTo(pa.elementos.get(i)) < 0)
            inserido = create1(pa.filhos.get(i));
        else
            inserido = create1(pa.filhos.get(i + 1));

        // A partir deste ponto, as chamadas recursivas já foram encerradas.
        // Assim, o próximo código só é executado ao retornar das chamadas recursivas.

        // A inclusão já foi resolvida por meio de uma das chamadas recursivas. Nesse
        // caso, apenas retorna para encerrar a recursão.
        // A inclusão pode ter sido resolvida porque o par de chaves já existia
        // (inclusão inválida)
        // ou porque o novo elemento coube em uma página existente.
        if (!cresceu)
            return inserido;

        // Se tiver espaço na página, faz a inclusão nela mesmo
        if (pa.elementos.size() < maxElementos) {

            // Puxa todos elementos para a direita, começando do último
            // para gerar o espaço para o novo elemento e insere o novo elemento
            pa.elementos.add(i, auxElemento);
            pa.filhos.add(i + 1, auxPonteiroPagina);

            // Escreve a página atualizada no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.serialize());

            // Encerra o processo de crescimento e retorna
            cresceu = false;
            return true;
        }

        // O elemento não cabe na página. A página deve ser dividida e o elemento
        // do meio deve ser promovido (sem retirar a referência da folha).

        // Cria uma nova página
        Pagina np = new Pagina(construtor, ordem);

        // Move a metade superior dos elementos para a nova página,
        // considerando que maxElementos pode ser ímpar
        int meio = maxElementos / 2;
        np.filhos.add(pa.filhos.get(meio)); // COPIA o primeiro ponteiro
        for (int j = 0; j < (maxElementos - meio); j++) {
            np.elementos.add(pa.elementos.remove(meio)); // MOVE os elementos
            np.filhos.add(pa.filhos.remove(meio + 1)); // MOVE os demais ponteiros
        }

        // Testa o lado de inserção
        // Caso 1 - Novo registro deve ficar na página da esquerda
        if (i <= meio) {
            pa.elementos.add(i, auxElemento);
            pa.filhos.add(i + 1, auxPonteiroPagina);

            // Se a página for folha, seleciona o primeiro elemento da página
            // da direita para ser promovido, mantendo-o na folha
            if (pa.filhos.get(0) == -1)
                auxElemento = np.elementos.get(0).clone();

            // caso contrário, promove o maior elemento da página esquerda
            // removendo-o da página
            else {
                auxElemento = pa.elementos.remove(pa.elementos.size() - 1);
                pa.filhos.remove(pa.filhos.size() - 1);
            }
        }

        // Caso 2 - Novo registro deve ficar na página da direita
        else {

            int j = maxElementos - meio;
            while (auxElemento.compareTo(np.elementos.get(j - 1)) < 0)
                j--;
            np.elementos.add(j, auxElemento);
            np.filhos.add(j + 1, auxPonteiroPagina);

            // Seleciona o primeiro elemento da página da direita para ser promovido
            auxElemento = np.elementos.get(0).clone();

            // Se não for folha, remove o elemento promovido da página
            if (pa.filhos.get(0) != -1) {
                np.elementos.remove(0);
                np.filhos.remove(0);
            }

        }

        // Obtém um endereço para a nova página (página excluída ou fim do arquivo)
        arquivo.seek(8);
        long end = arquivo.readLong();
        if(end==-1) {
            end = arquivo.length();
        } else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
            arquivo.seek(end);
            Pagina pa_excluida = new Pagina(construtor, ordem);
            buffer = new byte[pa_excluida.TAMANHO_PAGINA];
            arquivo.read(buffer);
            pa_excluida.deserialize(buffer);
            arquivo.seek(8);
            arquivo.writeLong(pa_excluida.proxima);
        }

        // Se a página era uma folha e apontava para outra folha,
        // então atualiza os ponteiros dessa página e da página nova
        if (pa.filhos.get(0) == -1) {
            np.proxima = pa.proxima;
            pa.proxima = end;
        }

        // Grava as páginas no arquivo
        auxPonteiroPagina = end;
        arquivo.seek(auxPonteiroPagina);
        arquivo.write(np.serialize());

        arquivo.seek(pagina);
        arquivo.write(pa.serialize());

        return true;
    }

    // Remoção elementos na árvore. A remoção é recursiva. A primeira
    // função chama a segunda recursivamente, passando a raiz como referência.
    // Eventualmente, a árvore pode reduzir seu tamanho, por meio da exclusão da
    // raiz.
    public boolean delete(T elem) throws Exception {

        // Encontra a raiz da árvore
        arquivo.seek(0);
        long pagina;
        pagina = arquivo.readLong();

        // variável global de controle da redução do tamanho da árvore
        diminuiu = false;

        // Chama recursivamente a exclusão do elemento, passando uma página como referência
        boolean excluido = delete1(elem, pagina);

        // Se a exclusão tiver sido possível e a página tiver reduzido seu tamanho,
        // por meio da fusão das duas páginas filhas da raiz, elimina essa raiz
        if (excluido && diminuiu) {

            // Lê a raiz
            arquivo.seek(pagina);
            Pagina pa = new Pagina(construtor, ordem);
            byte[] buffer = new byte[pa.TAMANHO_PAGINA];
            arquivo.read(buffer);
            pa.deserialize(buffer);

            // Se a página tiver 0 elementos, apenas atualiza o ponteiro para a raiz,
            // no cabeçalho do arquivo, para o seu primeiro filho e insere a raiz velha
            // na lista de páginas excluídas
            if (pa.elementos.size() == 0) {
                arquivo.seek(0);
                arquivo.writeLong(pa.filhos.get(0));

                arquivo.seek(8);
                long end = arquivo.readLong();  // cabeça da lista de páginas excluídas
                pa.proxima = end;
                arquivo.seek(8);
                arquivo.writeLong(pagina);
                arquivo.seek(pagina);
                arquivo.write(pa.serialize());
            }
        }

        return excluido;
    }

    // Função recursiva de exclusão. A função passa uma página de referência.
    // As exclusões são sempre feitas em folhas e a fusão é propagada para cima.
    private boolean delete1(T elem, long pagina) throws Exception {

        // Declaração de variáveis
        boolean excluido = false;
        int diminuido;

        // Testa se o registro não foi encontrado na árvore, ao alcançar uma folha
        // inexistente (filho de uma folha real)
        if (pagina == -1) {
            diminuiu = false;
            return false;
        }

        // Lê o registro da página no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.deserialize(buffer);

        // Encontra a página em que o par de chaves está presente
        // Nesse primeiro passo, salta todas os pares de chaves menores
        int i = 0;
        while (i < pa.elementos.size() && elem.compareTo(pa.elementos.get(i)) > 0) {
            i++;
        }

        // Chaves encontradas em uma folha
        if (i < pa.elementos.size() && pa.filhos.get(0) == -1 && elem.compareTo(pa.elementos.get(i)) == 0) {

            // Puxa todas os elementos seguintes para uma posição anterior, sobrescrevendo
            // o elemento a ser excluído
            pa.elementos.remove(i);
            pa.filhos.remove(i + 1);

            // Atualiza o registro da página no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.serialize());

            // Se a página contiver menos elementos do que o mínimo necessário,
            // indica a necessidade de fusão de páginas
            diminuiu = pa.elementos.size() < maxElementos / 2;
            return true;
        }

        // Se a chave não tiver sido encontrada (observar o return true logo acima),
        // continua a busca recursiva por uma nova página. A busca continuará até o
        // filho inexistente de uma página folha ser alcançado.
        // A variável diminuído mantem um registro de qual página eventualmente
        // pode ter ficado com menos elementos do que o mínimo necessário.
        // Essa página será filha da página atual
        if (i == pa.elementos.size() || elem.compareTo(pa.elementos.get(i)) < 0) {
            excluido = delete1(elem, pa.filhos.get(i));
            diminuido = i;
        } else {
            excluido = delete1(elem, pa.filhos.get(i + 1));
            diminuido = i + 1;
        }

        // A partir deste ponto, o código é executado após o retorno das chamadas
        // recursivas do método

        // Testa se há necessidade de fusão de páginas
        if (diminuiu) {

            // Carrega a página filho que ficou com menos elementos do
            // do que o mínimo necessário
            long paginaFilho = pa.filhos.get(diminuido);
            Pagina pFilho = new Pagina(construtor, ordem);
            arquivo.seek(paginaFilho);
            arquivo.read(buffer);
            pFilho.deserialize(buffer);

            // Cria uma página para o irmão (da esquerda ou direita)
            long paginaIrmaoEsq = -1, paginaIrmaoDir = -1;
            Pagina pIrmaoEsq = null, pIrmaoDir = null; // inicializados com null para controle de existência

            // Carrega os irmãos (que existirem)
            if (diminuido > 0) { // possui um irmão esquerdo, pois não é a primeira filho do pai
                paginaIrmaoEsq = pa.filhos.get(diminuido - 1);
                pIrmaoEsq = new Pagina(construtor, ordem);
                arquivo.seek(paginaIrmaoEsq);
                arquivo.read(buffer);
                pIrmaoEsq.deserialize(buffer);
            }
            if (diminuido < pa.elementos.size()) { // possui um irmão direito, pois não é o último filho do pai
                paginaIrmaoDir = pa.filhos.get(diminuido + 1);
                pIrmaoDir = new Pagina(construtor, ordem);
                arquivo.seek(paginaIrmaoDir);
                arquivo.read(buffer);
                pIrmaoDir.deserialize(buffer);
            }

            // Verifica se o irmão esquerdo existe e pode ceder algum elemento
            if (pIrmaoEsq != null && pIrmaoEsq.elementos.size() > maxElementos / 2) {

                // Se for folha, copia o elemento do irmão, já que o do pai será extinto ou
                // repetido
                if (pFilho.filhos.get(0) == -1)
                    pFilho.elementos.add(0, pIrmaoEsq.elementos.remove(pIrmaoEsq.elementos.size() - 1));

                // Se não for folha, desce o elemento do pai
                else
                    pFilho.elementos.add(0, pa.elementos.get(diminuido - 1));

                // Copia o elemento vindo do irmão para o pai (página atual)
                pa.elementos.set(diminuido - 1, pFilho.elementos.get(0));

                // Reduz o elemento no irmão (copia o ponteiro também)
                pFilho.filhos.add(0, pIrmaoEsq.filhos.remove(pIrmaoEsq.filhos.size() - 1));

            }

            // Senão, verifica se o irmão direito existe e pode ceder algum elemento
            else if (pIrmaoDir != null && pIrmaoDir.elementos.size() > maxElementos / 2) {
                // Se for folha
                if (pFilho.filhos.get(0) == -1) {

                    // move o elemento do irmão
                    pFilho.elementos.add(pIrmaoDir.elementos.remove(0));
                    pFilho.filhos.add(pIrmaoDir.filhos.remove(0));

                    // sobe o próximo elemento do irmão
                    pa.elementos.set(diminuido, pIrmaoDir.elementos.get(0));
                }

                // Se não for folha, rotaciona os elementos
                else {
                    // Copia o elemento do pai, com o ponteiro esquerdo do irmão
                    pFilho.elementos.add(pa.elementos.get(diminuido));
                    pFilho.filhos.add(pIrmaoDir.filhos.remove(0));

                    // Sobe o elemento esquerdo do irmão para o pai
                    pa.elementos.set(diminuido, pIrmaoDir.elementos.remove(0));
                }
            }

            // Senão, faz a fusão com o irmão esquerdo, se ele existir
            else if (pIrmaoEsq != null) {
                // Se a página reduzida não for folha, então o elemento
                // do pai deve descer para o irmão
                if (pFilho.filhos.get(0) != -1) {
                    pIrmaoEsq.elementos.add(pa.elementos.remove(diminuido - 1));
                    pIrmaoEsq.filhos.add(pFilho.filhos.remove(0));
                }
                // Senão, apenas remove o elemento do pai
                else {
                    pa.elementos.remove(diminuido - 1);
                    pFilho.filhos.remove(0);
                }
                pa.filhos.remove(diminuido); // remove o ponteiro para a própria página

                // Copia todos os registros para o irmão da esquerda
                pIrmaoEsq.elementos.addAll(pFilho.elementos);
                pIrmaoEsq.filhos.addAll(pFilho.filhos);
                pFilho.elementos.clear(); 
                pFilho.filhos.clear();

                // Se as páginas forem folhas, copia o ponteiro para a folha seguinte
                if (pIrmaoEsq.filhos.get(0) == -1)
                    pIrmaoEsq.proxima = pFilho.proxima;

                // Insere o filho na lista de páginas excluídas
                arquivo.seek(8);
                pFilho.proxima = arquivo.readLong();
                arquivo.seek(8);
                arquivo.writeLong(paginaFilho);

            }

            // Senão, faz a fusão com o irmão direito, assumindo que ele existe
            else {
                // Se a página reduzida não for folha, então o elemento
                // do pai deve descer para o irmão
                if (pFilho.filhos.get(0) != -1) {
                    pFilho.elementos.add(pa.elementos.remove(diminuido));
                    pFilho.filhos.add(pIrmaoDir.filhos.remove(0));
                }
                // Senão, apenas remove o elemento do pai
                else {
                    pa.elementos.remove(diminuido);
                    pFilho.filhos.remove(0);
                }
                pa.filhos.remove(diminuido + 1); // remove o ponteiro para o irmão direito

                // Move todos os registros do irmão da direita
                pFilho.elementos.addAll(pIrmaoDir.elementos);
                pFilho.filhos.addAll(pIrmaoDir.filhos);
                pIrmaoDir.elementos.clear(); 
                pIrmaoDir.filhos.clear();

                // Se a página for folha, copia o ponteiro para a próxima página
                pFilho.proxima = pIrmaoDir.proxima;

                // Insere o irmão da direita na lista de páginas excluídas
                arquivo.seek(8);
                pIrmaoDir.proxima = arquivo.readLong();
                arquivo.seek(8);
                arquivo.writeLong(paginaIrmaoDir);

            }

            // testa se o pai também ficou sem o número mínimo de elementos
            diminuiu = pa.elementos.size() < maxElementos / 2;

            // Atualiza os demais registros
            arquivo.seek(pagina);
            arquivo.write(pa.serialize());
            arquivo.seek(paginaFilho);
            arquivo.write(pFilho.serialize());
            if (pIrmaoEsq != null) {
                arquivo.seek(paginaIrmaoEsq);
                arquivo.write(pIrmaoEsq.serialize());
            }
            if (pIrmaoDir != null) {
                arquivo.seek(paginaIrmaoDir);
                arquivo.write(pIrmaoDir.serialize());
            }
        }
        return excluido;
    }

    // Imprime a árvore, usando uma chamada recursiva.
    // A função recursiva é chamada com uma página de referência (raiz)
    public void print() throws Exception {
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        System.out.println("Raiz: " + String.format("%04d", raiz));
        if (raiz != -1)
            print1(raiz);
        System.out.println();
    }

    // Impressão recursiva
    private void print1(long pagina) throws Exception {

        // Retorna das chamadas recursivas
        if (pagina == -1)
            return;
        int i;

        // Lê o registro da página passada como referência no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.deserialize(buffer);

        // Imprime a página
        String endereco = String.format("%04d", pagina);
        System.out.print(endereco + "  " + pa.elementos.size() + ":"); // endereço e número de elementos
        for (i = 0; i < pa.elementos.size(); i++) {
            System.out.print("(" + String.format("%04d", pa.filhos.get(i)) + ") " + pa.elementos.get(i) + " ");
        }
        if (i > 0)
            System.out.print("(" + String.format("%04d", pa.filhos.get(i)) + ")");
        else
            System.out.print("(-001)");
        for (; i < maxElementos; i++) {
            System.out.print(" ------- (-001)");
        }
        if (pa.proxima == -1)
            System.out.println();
        else
            System.out.println(" --> (" + String.format("%04d", pa.proxima) + ")");

        // Chama recursivamente cada filho, se a página não for folha
        if (pa.filhos.get(0) != -1) {
            for (i = 0; i < pa.elementos.size(); i++)
                print1(pa.filhos.get(i));
            print1(pa.filhos.get(i));
        }
    }

}