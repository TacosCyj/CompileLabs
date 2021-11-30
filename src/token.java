public class token {
    private String type;
    private int symbol;
    public token(String t, int s){
        this.type = t;
        this.symbol = s;
    }
    public String getType(){return this.type;}
    public int getSymbol(){return this.symbol;}
}
