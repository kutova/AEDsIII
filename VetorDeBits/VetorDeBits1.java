public class VetorDeBits1 {
    byte[] vetor;
    int nBits;

    public VetorDeBits1(int n) {
        nBits = n;
        vetor = new byte[(int)Math.ceil(n/8.0)];
    }

    public void set(int i) {
        if(i>=0 && i<nBits) {
            int b = (int)(i/8);
            vetor[b] = (byte) (vetor[b] | (1<<(7-i%8)));
        }
    }

    public void clear(int i) {
        if(i>=0 && i<nBits) {
            int b = (int)(i/8);
            vetor[b] = (byte) (vetor[b] & ~(1<<(7-i%8)));
        }
    }

    public boolean get(int i) {
        if(i>=0 && i<nBits) {
            int b = (int)(i/8);
            return (vetor[b] & (1<<(7-i%8)))>0;
        }
        return false;
    }

    public int size() {
        return nBits;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<nBits; i++)
            if(get(i))
                sb.append('1');
            else
                sb.append('0');
        return sb.toString();
    }

    public static void main(String[] args) {
        VetorDeBits1 vb = new VetorDeBits1(25);
        System.out.println(vb);
        vb.set(10);
        vb.set(20);
        System.out.println(vb);
    }

}