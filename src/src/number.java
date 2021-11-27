public class number extends token{
    int value;
    public number(int v, String t, int s){
        super(t, s);
        this.value = v;
    }
    public int getValue(){return this.value;}
}
