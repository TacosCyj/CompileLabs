public class dstANDstr {
    private int if_seq, else_seq, dst;
    public dstANDstr(int a, int b, int c){
        this.if_seq = a;
        this.else_seq = b;
        this.dst = c;
    }
    public int getIf_seq(){return this.if_seq;}
    public int getElse_seq(){return this.else_seq;}
    public int getDst(){return this.dst;}
    public void setIf_seq(int a){this.if_seq = a;}
    public void setElse_seq(int b){this.else_seq = b;}
    public void setDst(int c){this.dst = c;}
}
