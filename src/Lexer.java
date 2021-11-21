import javax.sound.midi.SysexMessage;
import java.io.*;
import java.util.Objects;
import java.util.LinkedList;

public class Lexer {
    private static Lexer lexer;
    private StringBuilder input_file;
    private BufferedReader reader;
    private FileReader r;
    private StringBuilder content = new StringBuilder();
    private int jump_i = 0, is_neg = 1;
    private LinkedList<token> tokenList = new LinkedList<token>();
    private Lexer(){}
    static{
        lexer = new Lexer();
    }
    public static Lexer getLexerInstance(){
        return lexer;
    }
    public void setFile(StringBuilder input) throws IOException {
        this.input_file = input;
        try{
           r = new FileReader(this.input_file.toString());
           this.reader = new BufferedReader(r);
        }catch(IOException e){
           e.printStackTrace();
       }

    }
    public void getContent() throws IOException {
        String str = null;
        try{
            while((str = reader.readLine()) != null){
                content.append(str);
                content.append('\n');
            }
            content.append('#');
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    //检查函数参数列表长度
    public boolean checkFuncRParamsNum(int start, int num){
        int n = 0, j = ++start;
        if(this.content.charAt(j) == ')') n = 0;
        else{
            while(this.content.charAt(j) != ';' && this.content.charAt(j) != ',') j++;
            if(this.content.charAt(j) == ';') n = 1;
            else if(this.content.charAt(j - 1) == ')') n = 1;
            else n = 2;
        }
        if(n == num) return true;
        else return false;
    }
    public boolean getIdent(String ident, int i){
        int symbol = 0, j = i;
        // a function call
        if(this.content.charAt(j) == '('){
            if(Objects.equals(ident, "getint") && checkFuncRParamsNum(j, 0)){
                symbol = 15;
                function func = new function("getint", "int", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "getch") && checkFuncRParamsNum(j, 0)){
                symbol = 16;
                function func = new function("getch", "int", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "putint") && checkFuncRParamsNum(j, 1)){
                symbol = 17;
                function func = new function("putint", "void", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "putch") && checkFuncRParamsNum(j, 1)){
                symbol = 18;
                function func = new function("putch", "void", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "main") && checkFuncRParamsNum(j, 0)){
                symbol = 11;
                function func = new function("main", "int", "Function", symbol);
                this.tokenList.offer(func);
            }
            else return false;
        }
        else{
            if(Objects.equals(ident, "int")){
                symbol = 12;
                ident id = new ident(ident, "Ident", symbol, 0, is_neg);
                this.tokenList.offer(id);
                is_neg = 1;
            }
            else if(Objects.equals(ident, "const")){
                symbol = 13;
                ident id = new ident(ident, "Ident", symbol, 0, is_neg);
                this.tokenList.offer(id);
                is_neg = 1;
            }
            else if(Objects.equals(ident, "return")) {
                symbol = 14;
                ident id = new ident(ident, "Ident", symbol, 0, is_neg);
                this.tokenList.offer(id);
                is_neg = 1;
            }
            else {
                symbol = 19;
                ident id = new ident(ident, "Ident", symbol, 0, is_neg);
                this.tokenList.offer(id);
                is_neg = 1;
            }
        }
        //assign times
        return true;
    }
    public boolean getNumber(String number){
        int i = 0, sum = 0;
        //16进制
        if(number.charAt(i) == '0' && number.length() > 2 && (number.charAt(i + 1) == 'x' || number.charAt(i + 1) == 'X')){
            i += 2;
            while(i < number.length() && ((Character.toLowerCase(number.charAt(i)) >= 'a' && Character.toLowerCase(number.charAt(i)) <= 'f') || Character.isDigit(number.charAt(i)))){
                if(number.charAt(i) >= '0' && number.charAt(i) <= '9') sum = sum * 16 + (number.charAt(i) - '0');
                else sum = sum * 16 + (Character.toLowerCase(number.charAt(i)) - 'a' + 10);
                i++;
            }
            if(i != number.length()) return false;
        }
        //8进制
        else if(number.charAt(i) == '0'){
            if(number.length() == 1){
                sum = 0;
            }
           else if(Character.isDigit(number.charAt(1))){
                i++;
                while( i < number.length() && number.charAt(i) >= '0' && number.charAt(i) <= '7'){
                    sum = sum * 8 + (number.charAt(i) - '0');
                    i++;
                }
                if(i != number.length()) return false;
            }
           else return false;
        }
        //10进制
        else{
            while(i < number.length() && number.charAt(i) >= '0' && number.charAt(i) <= '9'){
                sum = sum * 10 + (number.charAt(i) - '0');
                i++;
            }
            if(i != number.length()) return false;
        }
        number n = new number(is_neg * sum, "Number", 10);
        this.tokenList.offer(n);
        is_neg = 1;
        return true;
    }
    public boolean getOperator(char c, int l){
        int s = 0, j;
        int num_p = 0, num_m = 0;
        boolean flag = true;
        if(c == '+' || c == '-'){
            j = l;
            while((this.content.charAt(j) == '-' || this.content.charAt(j) == '+') && j < this.content.length()){
                if((this.content.charAt(j) == '-')) num_m++;
                else num_p++;
                j++;
            }
            //前面的符号表示正负
            //有多于一个的连续的+、-符号
            if(Character.isDigit(this.content.charAt(j)) || Character.isAlphabetic(this.content.charAt(j)) || this.content.charAt(j) == '_'){
                if((num_p == 1 || num_m == 1) && (this.tokenList.getLast() instanceof ident || this.tokenList.getLast() instanceof number)){
                    switch (c) {
                        case '+' -> s = 16;
                        case '-' -> s = 17;
                    }
                    operator o = new operator(c, "Op", s);
                    this.tokenList.offer(o);
                    this.jump_i = j;
                }
                else if((num_p == 1 || num_m == 1) && this.tokenList.getLast() instanceof operator){
                    operator t = (operator) this.tokenList.getLast();
                    if(Objects.equals(t.getOperator(), "+")){
                        if(c == '+') this.jump_i = j;
                        else{
                            this.tokenList.removeLast();
                            operator o = new operator('-', "Op", 17);
                            this.tokenList.offer(o);
                            this.jump_i = j;
                        }
                    }
                    else if(Objects.equals(t.getOperator(), "-")){
                        if(c == '+') this.jump_i = j;
                        else{
                            this.tokenList.removeLast();
                            operator o = new operator('+', "Op", 16);
                            this.tokenList.offer(o);
                            this.jump_i = j;
                        }
                    }
                    else{
                        switch (c) {
                            case '+' -> s = 16;
                            case '-' -> s = 17;
                        }
                        operator o = new operator(c, "Op", s);
                        this.tokenList.offer(o);
                        this.jump_i = j;
                    }
                }
                else if(num_m % 2 == 0) is_neg = 1;
                else is_neg = -1;
                this.jump_i = j;
            }
            else if(this.content.charAt(j) == '('){
                if(num_m % 2 != 0){
                    operator o = new operator('-', "Op", 17);
                    this.tokenList.offer(o);
                    this.jump_i = j;
                }
                else{
                    this.jump_i = j;
                }
            }
            //+ -表示运算符号
            else if(this.content.charAt(j) == ' ' || this.content.charAt(j) == '\t'){
                switch (c) {
                    case '+' -> s = 16;
                    case '-' -> s = 17;
                }
                operator o = new operator(c, "Op", s);
                this.tokenList.offer(o);
                this.jump_i = j;
            }
        }
        else{
            switch (c) {
                case '*' -> s = 18;
                case '%' -> s = 20;
                case '(' -> s = 21;
                case ')' -> s = 22;
                case '{' -> s = 23;
                case '}' -> s = 24;
                case ',' -> s = 25;
                case ';' -> s = 26;
                case '=' -> s = 27;
                default -> flag = false;
            }
            this.jump_i = ++l;
            operator o = new operator(c, "Op", s);
            this.tokenList.offer(o);
        }
        return flag;
    }
    public boolean lexerAnalysis(){
        boolean flag = true;
        StringBuilder temp = new StringBuilder();
        int i = 0, j = 0;
        for(i = 0; i < this.content.length() && this.content.charAt(i) != '#'; ){
            if(this.content.charAt(i) == ' ' || this.content.charAt(i) ==  '\t' || this.content.charAt(i) == '\n' || this.content.charAt(i) == '\r') i++;
            //deal with number
            else if(Character.isDigit(this.content.charAt(i))){
                temp.delete(0, temp.length());
                while(Character.isDigit(this.content.charAt(i)) || Character.isAlphabetic(this.content.charAt(i))){
                    temp.append(this.content.charAt(i));
                    i++;
                }
                flag = getNumber(temp.toString());
                if(!flag) return false;
            }
            //deal with ident
            //scan for next ( to judge whether this is a function call
            else if(Character.isAlphabetic(this.content.charAt(i)) || this.content.charAt(i) == '_'){
                temp.delete(0, temp.length());
                while(Character.isDigit(this.content.charAt(i)) || Character.isAlphabetic(this.content.charAt(i)) || this.content.charAt(i) == '_'){
                    temp.append(this.content.charAt(i));
                    i++;
                }
                flag = getIdent(temp.toString(), i);
                if(!flag) return false;
            }
            //deal with 注释 /
            else if(this.content.charAt(i) == '/'){
                if(this.content.charAt(i + 1) == '/'){
                    i++;
                    while(this.content.charAt(i) != '\n' && this.content.charAt(i) != '\r' && this.content.charAt(i) != '#') i++;
                }
                else if(this.content.charAt(i + 1) == '*'){
                    j = i + 1;
                    while((this.content.charAt(j) != '/' || this.content.charAt(i) != '*') && this.content.charAt(j) != '#'){
                        i++;
                        j++;
                    }
                    if(this.content.charAt(j) == '#') return false;
                    else i = ++j;
                }
                else{
                    getOperator('/', i);
                    i = jump_i;
                }
            }
            //deal with operators
            else{
               flag = getOperator(this.content.charAt(i), i);
               if(!flag) return false;
               else i = jump_i;
            }
        }
        return true;
    }
    public LinkedList<token> getTokenList(){
        return this.tokenList;
    }
   public StringBuilder getcon(){return this.content;}
}
