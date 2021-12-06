public class dstANDstr {
    private String if_seq, else_seq, dst;
    public dstANDstr(String a, String b, String c){
        this.if_seq = a;
        this.else_seq = b;
        this.dst = c;
    }
    public String getIf_seq(){return this.if_seq;}
    public String getElse_seq(){return this.else_seq;}
    public String getDst(){return this.dst;}
    public void setIf_seq(String a){this.if_seq = a;}
    public void setElse_seq(String b){this.else_seq = b;}
    public void setDst(String c){this.dst = c;}
}
