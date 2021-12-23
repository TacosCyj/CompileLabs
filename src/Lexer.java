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
    //part12-03
    public boolean checkforLbrace(int i){
        int s = i, numoflbrace = 0, numoflbrace_part12_03 = 0;
        while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r'){
            if(this.content.charAt(s) == '{'){
                numoflbrace++;
                break;
            }
            else s++;
        }
       if(numoflbrace == 0){
           while(this.content.charAt(s) != ')') s++;
           s++;
           while(this.content.charAt(s) == ' ' || this.content.charAt(s) == '\t' || this.content.charAt(s) == '\n'|| this.content.charAt(s) == '\r') s++;
           if(this.content.charAt(s) == '{') numoflbrace_part12_03++;
       }
       return numoflbrace > 0 || numoflbrace_part12_03 > 0;
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
    public void addRbrace(int i, int numoftabs, int while_sign){
        int s = i, j = 0, tabs;
        while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r') s++;
        s++;
        while(this.content.charAt(s) == ' ' || this.content.charAt(s) == '\t') s++;
        if(!Character.isAlphabetic(this.content.charAt(s))){
            while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r') s++;
            this.content.insert(s, '}');
        }
        else if(while_sign == 0){
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
        else{
            String temp = "";
            int cnt = 4;
            temp += this.content.charAt(s);
            while(cnt > 0){
                temp += this.content.charAt(++s);
                cnt--;
            }
            if(!temp.equals("while")){
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
    public void addLbrace(int i, int numoftabs, int while_sign){
        int s = i;
        while(this.content.charAt(s) != '\n' && this.content.charAt(s) != '\r')
            s++;
        this.content.insert(s, '{');
        addRbrace(s, numoftabs, while_sign);
    }
    //part12-03
    public boolean findLbrace(int i){
        int s = i;
        while(this.content.charAt(s) == ' ' || this.content.charAt(s) == '\t' || this.content.charAt(s) == '\n' || this.content.charAt(s) == '\r')s++;
        if(this.content.charAt(s) == '(') return true;
        else return false;
    }
    //part13 检查函数定义时没有参数的情况
    public boolean isEmptyParams(int i){
        int others = 0, j;
        for(j = i; j < this.content.length(); j++){
            if(this.content.charAt(j) == '{')
                break;
            else if(this.content.charAt(j) != '(' && this.content.charAt(j) != ')'
            && this.content.charAt(j) != ' ' && this.content.charAt(j) != '\t')
                others++;
        }
        return others == 0;
    }
    //part13 获取参数个数
    //传入参数是函数参数定义部分的(索引值，有逗号作为分隔符
    public int numofParams(int i){
        int j = 0;
        for(j = i; j < this.content.length(); j++){
            if(this.content.charAt(j) == '{')
                break;
        }
        String temp = this.content.toString().substring(i, j);
        String [] sp = temp.split(",");
        return isEmptyParams(i) ? 0 : sp.length;
    }
    //part13 检查使用函数的语句出现时，该函数是否已经被定义
    public function getFuncDecl(String funcname){
        for(token t : tokenList){
            if(t instanceof function f && Objects.equals(f.getFuncName(), funcname))
                return f;
        }
        return null;
    }
    public boolean getIdent(String ident, int i){
        int symbol = 0, j = i;
        // a function call
        this.jump_i = i;
        //part13 加入对函数ident的识别
        if((this.content.charAt(j) == '(' || (this.content.charAt(j + 1) == '(' && this.content.charAt(j) == ' ') || findLbrace(i)) && !Objects.equals(ident, "return")){
            int k;
            boolean f = false;
            if(this.content.charAt(j) == '(') k = j;
            else k = j + 1;
            if(Objects.equals(ident, "getint") && checkFuncRParamsNum(k, 0)){
                symbol = 15;
                function func = new function("getint", "int", "Function", symbol);
                this.tokenList.offer(func);
                f = true;
            }
            else if(Objects.equals(ident, "getch") && checkFuncRParamsNum(k, 0)){
                symbol = 16;
                function func = new function("getch", "int", "Function", symbol);
                this.tokenList.offer(func);
                f = true;
            }
            else if(Objects.equals(ident, "putint") && checkFuncRParamsNum(k, 1)){
                symbol = 17;
                function func = new function("putint", "void","Function", symbol);
                func.setSelfDecl(true);
                func.setParams_num(1);
                this.tokenList.offer(func);
                f = true;
            }
            else if(Objects.equals(ident, "putch") && checkFuncRParamsNum(k, 1)){
                symbol = 18;
                function func = new function("putch", "void", "Function", symbol);
                func.setSelfDecl(true);
                func.setParams_num(1);
                this.tokenList.offer(func);
                f = true;
            }
            //part13 对getarray和putarray的支持
            else if(Objects.equals(ident, "getarray") && checkFuncRParamsNum(k, 1)){
                symbol = 11;
                function func = new function("getarray", "int", "Function", symbol);
                this.tokenList.offer(func);
                f = true;
            }
            else if(Objects.equals(ident, "putarray") && checkFuncRParamsNum(k, 2)){
                //System.out.println("youyouyouoyuoyu");
                symbol = 11;
                function func = new function("putarray", "void", "Function", symbol);
                func.setSelfDecl(true);
                func.setParams_num(2);
                this.tokenList.offer(func);
                f = true;
            }
            else if(Objects.equals(ident, "main")){
                symbol = 11;
                function func = new function("main", "int", "Function", symbol);
                this.tokenList.offer(func);
                f = true;
            }
            //需要检查if是否以{结束，若不是则加上
            else if(Objects.equals(ident, "if")){
                symbol = 19;
                cond cond_if = new cond("if", "Cond_if", symbol, getnumofTab2(i));
                this.tokenList.offer(cond_if);
                if(!checkforLbrace(i)){
                    addLbrace(i, getnumofTab(i), 0);
                }
                f = true;
            }
            //while循环
            else if(Objects.equals(ident, "while")){
                symbol = 36;
                circulation cir_while = new circulation("Circulation", symbol);
                this.tokenList.offer(cir_while);
                if(!checkforLbrace(i)){
                    addLbrace(i, getnumofTab(i), 1);
                }
                f = true;
            }
            //part13 普通函数
            else{
                //函数定义
                if(tokenList.getLast() instanceof ident id && (Objects.equals(id.getId(), "int") || Objects.equals(id.getId(), "void"))){
                    //获得函数的返回值
                    String ret_type = id.getId();
                    int params_decl = numofParams(k);
                    //for debug
                    symbol = 11;
                    function func = new function(ident, ret_type, "Function", symbol);
                    func.setSelfDecl(true);
                    func.setParams_num(params_decl);
                    this.tokenList.offer(func);
                    f = true;
                }
                //函数使用
                //先检查这个函数有没有被定义过(根据func.getfuncname检查)
                //若存在则获得相应值（还需要检查参数个数的匹配性）
                //若不存在则直接错误退出
                else{
                    function func = getFuncDecl(ident);
                    if(func != null){
                        symbol = 11;
                        function _use = new function(ident, func.getTypeOfRetValue(), "Function", symbol);
                        _use.setSelfDecl(true);
                        _use.setParams_num(func.getParams_num());
                        this.tokenList.offer(_use);
                        f = true;
                    }
                }
            }
            if(!f) return false;
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
                    }
                }
                else{
                    symbol = 20;
                    cond cond_else = new cond("else", "Cond_else", symbol, getnumofTab2(i));
                    this.tokenList.offer(cond_else);
                    this.elsenum++;
                    if(!checkforLbrace(i)){
                        addLbrace(i, getnumofTab(i), 0);
                    }
                }
            }
            else if(Objects.equals(ident, "continue")){
                symbol = 37;
                ident id = new ident(ident, "Ident", symbol, 0, is_neg);
                this.tokenList.offer(id);
                is_neg = 1;
            }
            else if(Objects.equals(ident, "break")){
                symbol = 38;
                ident id = new ident(ident, "Ident", symbol, 0, is_neg);
                this.tokenList.offer(id);
                is_neg = 1;
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
                    case '[' -> {
                        s = 36;
                        operator o = new operator(String.valueOf(c), "Op_array", s);
                        this.tokenList.offer(o);
                    }
                    case ']' -> {
                        s = 37;
                        operator o = new operator(String.valueOf(c), "Op_array", s);
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
            //System.out.println(this.content.charAt(i) + "||");
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
               if(!flag) {
                   //System.out.println("Ishere");
                   return false;
               }
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
