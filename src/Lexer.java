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
    private int elsenum = 0;
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
    public boolean checkforLbrace(int i){
        int s = i, numoflbrace = 0;
        while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r'){
            if(this.content.charAt(s) == '{'){
                numoflbrace++;
                break;
            }
            else s++;
        }
        return numoflbrace > 0;
    }
    public int getnumofTab(int i){
        int s = i - 1;
        int numofTabs = 0;
        while(this.content.charAt(s) != '\r' && this.content.charAt(s) != '\n'){
            if(this.content.charAt(s) == ' ') numofTabs++;
            s--;
        }
        return numofTabs;
    }
    public int getnumofTab2(int i){
        int s = i - 1;
        int numofTabs = 0;
        StringBuilder temp = new StringBuilder();
        StringBuilder temp2 = new StringBuilder();
        while(this.content.charAt(s) != '\r' && this.content.charAt(s) != '\n'){
            if(this.content.charAt(s) == ' ') numofTabs++;
            if(this.content.charAt(s) == '}') numofTabs--;
            if(Character.isAlphabetic(this.content.charAt(s)) && this.content.charAt(s) != 'i' &&this.content.charAt(s) != 'f'){
                temp.append(this.content.charAt(s));
            }
            if(this.content.charAt(s) == 'i' || this.content.charAt(s) == 'f'){
                temp2.append(this.content.charAt(s));
            }
            s--;
        }
        if(temp.reverse().toString().equals("else") && temp2.reverse().toString().equals("if")) numofTabs--;
        return numofTabs;
    }
    public void addRbrace(int i, int numoftabs){
        int s = i, j = 0, tabs;
        while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r') s++;
        s++;
        while(this.content.charAt(s) == ' ' || this.content.charAt(s) == '\t') s++;
        if(!Character.isAlphabetic(this.content.charAt(s))){
            while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r') s++;
            this.content.insert(s, '}');
        }
        else{
            String temp = "";
            temp += this.content.charAt(s);
            temp += this.content.charAt(++s);
            if(!temp.equals("if")){
                while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r') s++;
                this.content.insert(s, '}');
            }
            else{
                for(; s < this.content.length();){
                    if(this.content.charAt(s) == ' ' && (this.content.charAt(s - 1) == '\n' || this.content.charAt(s - 1) == '\r')){
                        j = s;
                        tabs = 0;
                        while(this.content.charAt(j) == ' '){
                            tabs++;
                            j++;
                        }
                        if(tabs == numoftabs) break;
                        else s = j;
                    }
                    else s++;
                }
                this.content.insert(j, '}');
            }


        }
    }
    public void addLbrace(int i, int numoftabs){
        int s = i;
        while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r')
            s++;
        this.content.insert(s, '{');
        addRbrace(s, numoftabs);
    }
    public boolean getIdent(String ident, int i){
        int symbol = 0, j = i;
        // a function call
        this.jump_i = i;
        if(this.content.charAt(j) == '(' || this.content.charAt(j + 1) == '('){
            int k;
            if(this.content.charAt(j) == '(') k = j;
            else k = j + 1;
            if(Objects.equals(ident, "getint") && checkFuncRParamsNum(k, 0)){
                symbol = 15;
                function func = new function("getint", "int", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "getch") && checkFuncRParamsNum(k, 0)){
                symbol = 16;
                function func = new function("getch", "int", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "putint") && checkFuncRParamsNum(k, 1)){
                symbol = 17;
                function func = new function("putint", "void", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "putch") && checkFuncRParamsNum(k, 1)){
                symbol = 18;
                function func = new function("putch", "void", "Function", symbol);
                this.tokenList.offer(func);
            }
            else if(Objects.equals(ident, "main") && checkFuncRParamsNum(k, 0)){
                symbol = 11;
                function func = new function("main", "int", "Function", symbol);
                this.tokenList.offer(func);
            }
            //需要检查if是否以{结束，若不是则加上
            else if(Objects.equals(ident, "if")){
                symbol = 19;
                cond cond_if = new cond("if", "Cond_if", symbol, getnumofTab2(i));
                this.tokenList.offer(cond_if);
                if(!checkforLbrace(i)){
                    addLbrace(i, getnumofTab(i));
                }
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
            else if(Objects.equals(ident, "else")){
                if(this.content.charAt(j + 1) == 'i'){
                    if(this.content.charAt(j + 2) == 'f'){
                        symbol = 19;
                        cond cond_else_if = new cond("else if", "Cond_if", symbol, getnumofTab2(i));
                        this.elsenum++;
                        this.tokenList.offer(cond_else_if);
                        //this.jump_i = j + 3;
                    }
                }
                else{
                    symbol = 20;
                    cond cond_else = new cond("else", "Cond_else", symbol, getnumofTab2(i));
                    this.tokenList.offer(cond_else);
                    this.elsenum++;
                    if(!checkforLbrace(i)){
                        addLbrace(i, getnumofTab(i));
                    }
                }
            }
            else {
                symbol = 21;
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
            if(Character.isDigit(this.content.charAt(j)) || Character.isAlphabetic(this.content.charAt(j)) || this.content.charAt(j) == '_' || this.content.charAt(j) == '('){
                if((num_p == 1 || num_m == 1) && (this.tokenList.getLast() instanceof ident || this.tokenList.getLast() instanceof number)){
                    switch (c) {
                        case '+' -> s = 21;
                        case '-' -> s = 22;
                    }
                    operator o = new operator(String.valueOf(c), "Op", s);
                    this.tokenList.offer(o);
                    this.jump_i = j;
                }
                else if((num_p == 1 || num_m == 1) && this.tokenList.getLast() instanceof operator){
                    operator t = (operator) this.tokenList.getLast();
                    if(Objects.equals(t.getOperator(), "+")){
                        if(c == '+') this.jump_i = j;
                        else{
                            this.tokenList.removeLast();
                            operator o = new operator("-", "Op", 17);
                            this.tokenList.offer(o);
                            this.jump_i = j;
                        }
                    }
                    else if(Objects.equals(t.getOperator(), "-")){
                        if(c == '+') this.jump_i = j;
                        else{
                            this.tokenList.removeLast();
                            operator o = new operator("+", "Op", 16);
                            this.tokenList.offer(o);
                            this.jump_i = j;
                        }
                    }
                    else if(Objects.equals(t.getOperator(), "=")){
                        if(num_m % 2 == 0){
                            operator o = new operator("+", "Op", 16);
                            this.tokenList.offer(o);
                        }
                        else{
                            operator o = new operator("-", "Op", 17);
                            this.tokenList.offer(o);
                        }
                    }
                    else if(Objects.equals(t.getOperator(), "==") || Objects.equals(t.getOperator(), "!=") || Objects.equals(t.getOperator(), "<")
                            || Objects.equals(t.getOperator(), ">") || Objects.equals(t.getOperator(), "<=") || Objects.equals(t.getOperator(), ">=")){
                        if(num_m % 2 == 0) is_neg = 1;
                        else is_neg = -1;
                    }
                    else{
                        switch (c) {
                            case '+' -> s = 21;
                            case '-' -> s = 22;
                        }
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                        this.jump_i = j;
                    }
                }
                else if(num_m % 2 == 0) is_neg = 1;
                else is_neg = -1;
                this.jump_i = j;
            }
            else if(this.content.charAt(j) == '(' || this.content.charAt(j) == '!'){
                if(num_m % 2 != 0){
                    operator o = new operator("-", "Op", 17);
                    this.tokenList.offer(o);
                    this.jump_i = j;
                }
                else{
                    this.jump_i = j;
                }
            }
            //+ -表示运算符号
            else if(this.content.charAt(j) == ' ' || this.content.charAt(j) == '\t'){
                token temp = this.tokenList.getLast();
                if(temp instanceof operator && Objects.equals(((operator) temp).getOperator(), "=")){
                    number temp_n = new number(0, "Number", 10);
                    this.tokenList.offer(temp_n);
                }
                if(temp instanceof operator && (Objects.equals(((operator) temp).getOperator(), "+") || Objects.equals(((operator) temp).getOperator(), "-"))){
                    if(c == '+'){
                        jump_i = j;
                    }
                    else{
                        if(Objects.equals(((operator) temp).getOperator(), "+")) jump_i = j;
                        else{
                            this.tokenList.removeLast();
                            operator o = new operator("+", "Op", 16);
                            this.tokenList.offer(o);
                            this.jump_i = j;
                        }
                    }
                }
                else{
                    switch (c) {
                        case '+' -> s = 21;
                        case '-' -> s = 22;
                    }
                    operator o = new operator(String.valueOf(c), "Op", s);
                    this.tokenList.offer(o);
                    this.jump_i = j;
                }
            }
        }
        else{
            j = l + 1;
            if(c == '&' && this.content.charAt(j) == '&'){
                operator o = new operator("&&", "Op_cond", s);
                this.tokenList.offer(o);
                jump_i = ++j;
            }
            else if(c == '|' && this.content.charAt(j) == '|'){
                operator o = new operator("||", "Op_cond", s);
                this.tokenList.offer(o);
                jump_i = ++j;
            }
            else if(c == '<' && this.content.charAt(j) == '='){
                operator o = new operator("<=", "Op_cond", s);
                this.tokenList.offer(o);
                jump_i = ++j;
            }
            else if(c ==  '>' && this.content.charAt(j) == '='){
                operator o = new operator(">=", "Op_cond", s);
                this.tokenList.offer(o);
                jump_i = ++j;
            }
            else if(c == '!' && this.content.charAt(j) == '='){
                operator o = new operator("!=", "Op_cond", s);
                this.tokenList.offer(o);
                jump_i = ++j;
            }
            else if(c == '=' && this.content.charAt(j) == '='){
                operator o = new operator("==", "Op_cond", s);
                this.tokenList.offer(o);
                jump_i = ++j;
            }
            else{
                switch (c) {
                    case '*' -> {
                        s = 23;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case '/' -> {
                        s = 24;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case '%' -> {
                        s = 25;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case '(' -> {
                        s = 26;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case ')' -> {
                        s = 27;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case '{' -> {
                        s = 28;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case '}' -> {
                        s = 29;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case ',' -> {
                        s = 30;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case ';' -> {
                        s = 31;
                        operator o = new operator(String.valueOf(c), "Op", s);
                        this.tokenList.offer(o);
                    }
                    case '=' -> {
                        s = 32;
                        operator o = new operator(String.valueOf(c), "Op_cond", s);
                        this.tokenList.offer(o);
                    }
                    case '<' -> {
                        s = 33;
                        operator o = new operator(String.valueOf(c), "Op_cond", s);
                        this.tokenList.offer(o);
                    }
                    case '>' -> {
                        s = 34;
                        operator o = new operator(String.valueOf(c), "Op_cond", s);
                        this.tokenList.offer(o);
                    }
                    case '!' -> {
                        s = 35;
                        operator o = new operator(String.valueOf(c), "Op_cond", s);
                        this.tokenList.offer(o);
                    }
                    default -> flag = false;
                }
                this.jump_i = ++l;
            }
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
                i = jump_i;
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
    public int getElsenum(){return this.elsenum;}
}
