import javax.sound.midi.SysexMessage;
import java.util.*;

public class Grammar {
    private LinkedList<token> tokenList;
    private static Grammar grammar;
    private StringBuilder answer = new StringBuilder();
    //part13 定义函数时生成的中间代码
    private StringBuilder answer_decl = new StringBuilder();

    //主函数块表
    private HashMap<String, Integer> funclist = new HashMap<>();
    private HashMap<String, register> reglist = new HashMap<>();
    private HashMap<String, function> ff = new HashMap<>();
    private Stack<String> strblockeach = new Stack<>();
    private Stack<dstANDstr> three = new Stack<>();
    private expression exper;
    public LinkedList<token> expList = new LinkedList<>();
    //part8,Hashmap储存不同块符号表的键值
    private HashMap<String, HashMap<String, Integer>> blocklist = new HashMap<>();
    //part10 储存每个while的信息栈
    private Stack<circulation> while_info = new Stack<>();
    //part13 在处理函数参数的时候，把ident名加入，以便在Lbrace前处理时使用
    private Queue<ident> pre_funcdecl = new LinkedList<>();
    //不同块符号表名称(键值)
    //同时也标记main函数的
    private int listnum = 0;
    private int reg_seq = 0;
    private int ans = 0;
    private String label = "x";
    private String label_while = "y";
    private int labelseq = 1;
    private int labelseq_while = 1;
    private int func_var = 1;
    private int arr_len = 0;
    private int arrayname = 0;
    private token t_judge;
    //for debug
    private String content;
    //for func decl judge
    private int func_sign = 0;
    private int func_seq = 6;
    private StringBuilder funcstr = new StringBuilder();
    private function func_now;
    private Grammar(){}
    static{
        grammar = new Grammar();
    }
    public static Grammar getInstance(){
        return grammar;
    }
    public void setExper(expression exp){
        this.exper = exp;
    }
    public void setTokenList(LinkedList<token> q){
        this.tokenList = q;
    }
    public String setNewVarlist(){
        HashMap<String, Integer> sub_varlist = new HashMap<>();
        this.listnum++;
        this.blocklist.put("var" + this.listnum, sub_varlist);
        return "var" + this.listnum;
    }
    public String setNewVarlist_global(){
        HashMap<String, Integer> sub_varlist = new HashMap<>();
        this.blocklist.put("var" + 0, sub_varlist);
        return "var" + 0;
    }
    //退出一个块时要做的删除工作
    public void deletelist(String key1){
        HashMap<String, Integer> temp = this.blocklist.get(key1);
        for(String keys : temp.keySet()){
            //移除块中的在寄存表中的变量
            //这些变量有统一的命名形式，即变量名加块号
            this.reglist.remove(keys + this.listnum);
        }
        this.blocklist.remove(key1);
        this.listnum--;
    }
    public void deletelist_inparams(){
        //全局变量表
        int loc = this.listnum + 1;
        System.out.println(loc);
        HashMap<String, Integer> temp = this.blocklist.get("var" + 0);
        for(String keys : temp.keySet()){
            //移除块中的在寄存表中的变量
            //这些变量有统一的命名形式，即变量名加块号
            this.reglist.remove(keys + loc);
        }
    }
    //查看这个变量属于那一层
    //从内层向外层搜索
    public int forJudgeNum(ident id){
        int i;
        for(i = this.listnum; i >= 0; i--){
            if(reglist.containsKey(id.getId() + i)){
                return i;
            }
        }
        return -1;
    }
    public String getArrayName(){
        this.arrayname++;
        return "Array" + arrayname;
    }
    public int getdemensionDecl(int d){
        if(d == 1){
            if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), "[") && this.tokenList.get(1) instanceof operator opp && Objects.equals(opp.getOperator(), "]")){
                this.tokenList.poll();
                this.tokenList.poll();
                return 0;
            }
            else return -1;
        }
        else{
            if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), "[") && this.tokenList.get(1) instanceof operator opp && Objects.equals(opp.getOperator(), "]")){
                this.tokenList.poll();
                this.tokenList.poll();
                return -1;
            }
            else if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), "[")){
                this.tokenList.poll();
                isArrayExp(new LinkedList<>());
                return arr_len;
            }
            else
                return -1;
        }
    }
    public void declfunc(String ret_type, String name){
        this.answer.append("define dso_local ").append(Objects.equals(ret_type, "int") ? "i32" : "void").append(" @").append(name);
    }
    public void dealwithParams(function func_target){
        int i;
        //System.out.println("Yeah" + func_target.getFuncName());
        if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), "(")){
            this.answer.append("(");
            this.tokenList.poll();
        }
        while(true){
            if(this.tokenList.peek() instanceof ident id && Objects.equals(id.getId(), "int")){
                this.tokenList.poll();
                int Array = isArray();
                //判断这个参数是不是数组
                //是数组行参数
                //一维数组
                if(Array == 1 && this.tokenList.peek() instanceof ident ida){
                    func_target.setFuncVar("one_Array");
                    register reg = new register();
                    reg.setSeq(this.reg_seq);
                    reg.setIsArray(true);
                    reg.setDemension(1);
                    reg.setIsConst(false);
                    this.reglist.put(ida.getId() + (this.listnum + 1), reg);
                    this.answer.append(" i32* %").append((this.reg_seq++)).append(",");
                    this.pre_funcdecl.offer(ida);
                    while(true){
                        if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), "]")){
                            this.tokenList.poll();
                            break;
                        }
                        else
                            this.tokenList.poll();
                    }
                }
                //二维数组
                else if(Array == 2 && this.tokenList.peek() instanceof ident ida){
                    func_target.setFuncVar("two_Array");
                    register reg = new register();
                    reg.setSeq(this.reg_seq);
                    reg.setIsArray(true);
                    reg.setDemension(2);
                    reg.setIsConst(false);
                    this.pre_funcdecl.offer(ida);
                    this.tokenList.poll();
                    int x = getdemensionDecl(1);
                    if(x == -1)  System.exit(11);
                    //如果二维数组作为参数时，两个维度都没有定义，则非法
                    else{
                        int y = getdemensionDecl(2);
                        if(y == -1)
                            System.exit(11);
                        else{
                            this.answer.append(" [").append(y).append(" x i32]").append("* %").append((this.reg_seq++)).append(",");
                            reg.setY_d(y);
                            this.reglist.put(ida.getId() + (this.listnum + 1), reg);
                        }
                    }
                }
                //不是数组型参数
                else if(this.tokenList.peek() instanceof ident idd){
                    func_target.setFuncVar("one_var");
                    register reg = new register();
                    reg.setIsConst(false);
                    reg.setHasValue();
                    reg.setCreatedWhenOp(0);
                    reg.setSeq(this.reg_seq);
                    //System.out.println(this.listnum + 1);
                    this.reglist.put(idd.getId() + (this.listnum + 1), reg);
                    this.answer.append(" i32 %").append((this.reg_seq++)).append(",");
                    this.pre_funcdecl.offer(idd);
                    this.tokenList.poll();
                }
            }
            else if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), ","))
                this.tokenList.poll();
            else if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), ")"))
                if(this.tokenList.get(1) instanceof operator opp && Objects.equals(opp.getOperator(), "{")){
                    if(this.answer.toString().charAt(this.answer.length() - 1) == ',')
                        this.answer.delete(this.answer.length() - 1, this.answer.length());
                    this.answer.append("){");
                    this.tokenList.poll();
                    break;
                }
        }
    }
    public void checkForFunc(){
        int i;
        for(i = 0; i < this.tokenList.toArray().length; i++){
            token temp = this.tokenList.get(i);
            if(temp instanceof function){
                if(Objects.equals(((function) temp).getFuncName(), "getint") && !this.funclist.containsKey("getint")){
                    this.answer.append("\ndeclare i32 @getint()\n");
                    this.funclist.put("getint", 1);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "getch") && !this.funclist.containsKey("getch")){
                    this.answer.append("\ndeclare i32 @getch()\n");
                    this.funclist.put("getch", 2);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "putint") && !this.funclist.containsKey("putint")){
                    this.answer.append("\ndeclare void @putint(i32)\n");
                    this.funclist.put("putint", 3);
                    function f = new function("putint", "void", "Function", 11);
                    f.setParams_num(1);
                    f.setFuncVar("one_var");
                    this.ff.put("putint", f);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "putch") && !this.funclist.containsKey("putch")){
                    this.answer.append("\ndeclare void @putch(i32)\n");
                    this.funclist.put("putch", 4);
                    function f = new function("putch", "void", "Function", 11);
                    f.setParams_num(1);
                    f.setFuncVar("one_var");
                    this.ff.put("putch", f);
                }
                if(Objects.equals(((function) temp).getFuncName(), "getarray") && !this.funclist.containsKey("getarray")){
                    this.answer.append("\ndeclare i32 @getarray(i32*)\n");
                    this.funclist.put("getarray", 5);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "putarray") && !this.funclist.containsKey("putarray")){
                    this.answer.append("\ndeclare void @putarray(i32, i32*)\n");
                    this.funclist.put("putarray", 6);
                    function f = new function("putarray", "void", "Function", 11);
                    f.setParams_num(2);
                    f.setFuncVar("one_var");
                    f.setFuncVar("one_Array");
                    this.ff.put("putarray", f);
                }
            }
        }
        //part7 中引进memset 函数
        this.answer.append("declare void @memset(i32*, i32, i32)\n");
    }
    public void addLabel(int kind){
        //kind = 1为if else
        //kind = 2为while
        if(kind == 1){
            this.label = "x" + String.valueOf(labelseq);
            labelseq++;
        }
        else{
            this.label_while = "y" + String.valueOf(labelseq_while);
            labelseq_while++;
        }
    }
    //分隔main函数区和全局区
    //全局区包括全局变量的定义和函数定义
    public void addRbrace(){
        int i;
        for(i = 0; i < this.tokenList.toArray().length; i++){
            if(this.tokenList.get(i) instanceof function id1){
                if(Objects.equals(id1.getFuncName(), "main")){
                    break;
                }
            }
        }
        if(i == this.tokenList.toArray().length) System.exit(6);
        else{
            operator op = new operator("}", "Op", 29);
            this.tokenList.add(i - 1, op);
        }
    }
    //递归下降
    public int detect(){
        //若返回flag为2, 则说明有全局变量区，需要在int main前加'}'
        //若为1， 则直接进isInt
        int flag = 0;
        if(this.tokenList.peek() instanceof ident){
            if(Objects.equals(((ident) this.tokenList.peek()).getId(), "int")){
                token temp = this.tokenList.poll();
                if(this.tokenList.peek() instanceof function){
                    function temp_id = (function)this.tokenList.peek();
                    if(Objects.equals(temp_id.getFuncName(), "main")){
                        this.tokenList.addFirst(temp);
                        flag = 1;
                    }
                    else{
                        this.tokenList.addFirst(temp);
                        addRbrace();
                        flag = 2;
                    }
                }
                else{
                    this.tokenList.addFirst(temp);
                    addRbrace();
                    flag = 2;
                }
            }
            //返回值为void的函数
            else if(Objects.equals(((ident) this.tokenList.peek()).getId(), "void")) {
                token temp = this.tokenList.poll();
                if(this.tokenList.peek() instanceof function){
                    function temp_id = (function)this.tokenList.peek();
                    if(!Objects.equals(temp_id.getFuncName(), "main")){
                        this.tokenList.addFirst(temp);
                        addRbrace();
                        flag = 2;
                    }
                    else{
                        flag = 0;
                    }
                }
                else{
                    flag = 0;
                }
            }
            //说明为const
            else if(Objects.equals(((ident) this.tokenList.peek()).getId(), "const")){
                addRbrace();
                flag = 2;
            }
            else flag = 0;
        }
        else flag = 0;
        return flag;
    }
    public String initGlobalArea(){
        checkForFunc();
        return setNewVarlist_global();
    }
    public boolean isGivenOldValue_Global(ident obj, int target) {
        boolean flag = true;
        HashMap<String, Integer> vl = this.blocklist.get("var" + target);
        String name = obj.getId() + target;
        //变量赋值
        if (!this.reglist.get(name).getIsConst() && isExp(vl, 1, 0, 0, 0, 0)) {
            register reg = this.reglist.get(obj.getId() + target);
            reg.setValueOfReg(ans);
            reg.setHasValue();
            reg.setGlobalname("@" + obj.getId());
        }
        //未赋值的常量赋值
        else if (this.reglist.get(name).getIsConst() && !this.reglist.get(name).getHasValue() && isExp(vl, 1, 0, 0, 0, 0)) {
            register reg = this.reglist.get(obj.getId() + target);
            reg.setValueOfReg(ans);
            reg.setHasValue();
            reg.setGlobalname("@" + obj.getId());
        } else {
            flag = false;
        }
        return flag;
    }
    public boolean isIndentifyNew_Global(boolean is_const, HashMap<String, Integer> vl){
        boolean flag = true;
        ident temp_token = (ident) this.tokenList.poll();
        int isArray = isArray();
        if((vl.isEmpty() || !vl.containsKey(temp_token.getId())) && isArray == 0){
            //加入变量表
            vl.put(temp_token.getId(), -1);
            register reg = new register();
            reg.setCreatedWhenOp(0);
            reg.setIsGlobal(true);
            if(is_const) reg.setIsConst(true);
            else reg.setIsConst(false);
            //为不同块中的同名变量分配不同的寄存器
            this.reglist.put(temp_token.getId() + 0, reg);
            if(!is_const) this.answer.append("@").append(temp_token.getId()).append(" = dso_local global i32 ");
            if(this.tokenList.peek() instanceof operator){
                operator temp_op = (operator) this.tokenList.peek();
                if(Objects.equals(temp_op.getOperator(), ",")){
                    this.tokenList.poll();
                    this.reglist.get(temp_token.getId() + 0).setValueOfReg(0);
                    this.reglist.get(temp_token.getId() + 0).setGlobalname("@" + temp_token.getId());
                    this.reglist.get(temp_token.getId() + 0).setHasValue();
                    this.answer.append(0).append("\n");
                    flag = isIndentifyNew_Global(is_const, vl);
                }
                else if(Objects.equals(temp_op.getOperator(), ";")){
                    this.tokenList.poll();
                    this.reglist.get(temp_token.getId() + 0).setValueOfReg(0);
                    this.reglist.get(temp_token.getId() + 0).setGlobalname("@" + temp_token.getId());
                    this.reglist.get(temp_token.getId() + 0).setHasValue();
                    this.answer.append(0).append("\n");
                    flag = true;
                    return flag;
                }
                else if(Objects.equals(temp_op.getOperator(), "=")){
                    this.reglist.get(temp_token.getId() + 0).setGlobalname("@" + temp_token.getId());
                    this.tokenList.poll();
                    flag = isGivenOldValue_Global(temp_token, 0);
                    if(this.tokenList.peek() instanceof operator && flag){
                        if(!is_const) this.answer.append(ans).append("\n");
                        operator temp_op_2 = (operator) this.tokenList.peek();
                        if(Objects.equals(temp_op_2.getOperator(), ",")){
                            this.tokenList.poll();
                            flag = isIndentifyNew_Global(is_const, vl);
                        }
                        else if(Objects.equals(temp_op_2.getOperator(), ";")){
                            this.tokenList.poll();
                            flag = true;
                            return flag;
                        }
                        else flag = false;
                    }
                    else flag = false;
                }
                else flag= false;
            }
        }
        else if((vl.isEmpty() || !vl.containsKey(temp_token.getId())) && isArray > 0) {
            int x = getdemension(isArray, 1, vl), y = getdemension(isArray, 2, vl);
            if(x <= 0 || (isArray == 2 && y <= 0)){
                System.out.println(this.content);
                //System.out.println(this.answer);
                System.exit(8);
            }
            vl.put(temp_token.getId(), -1);
            this.reg_seq++;
            register reg = new register();
            reg.setSeq(this.reg_seq);
            reg.setIsArray(true);
            reg.setX_d(x);
            reg.setY_d(y);
            reg.setIsGlobal(true);
            if(is_const) reg.setIsConst(true);
            else reg.setIsConst(false);
            if (y == 0) reg.setDemension(1);
            else reg.setDemension(2);
            this.reglist.put(temp_token.getId() + 0, reg);
            this.answer.append("@" + temp_token.getId() + reg.initArray());
            String temp = this.answer.toString();
            this.answer.append("    %" + (++this.reg_seq) + " = " + reg.getArray_firstaddr(this.reg_seq));
            if (reg.getDemension() == 2) this.reg_seq++;
            this.answer.append(reg.getArray_useaddr(this.reg_seq));
            this.answer.append(reg.memsetStep());
            this.answer.replace(0, this.answer.length(), temp);
            if(this.tokenList.peek() instanceof operator){
                operator temp_op = (operator) this.tokenList.peek();
                if(Objects.equals(temp_op.getOperator(), ",")){
                    this.tokenList.poll();
                    this.reglist.get(temp_token.getId() + 0).setGlobalname("@" + temp_token.getId());
                    this.reglist.get(temp_token.getId() + 0).setHasValue();
                    this.answer.append("zeroinitializer").append("\n");
                    flag = isIndentifyNew_Global(is_const, vl);
                }
                else if(Objects.equals(temp_op.getOperator(), ";")){
                    this.tokenList.poll();
                    this.reglist.get(temp_token.getId() + 0).setGlobalname("@" + temp_token.getId());
                    this.reglist.get(temp_token.getId() + 0).setHasValue();
                    this.answer.append("zeroinitializer").append("\n");
                    flag = true;
                    return flag;
                }
                else if(Objects.equals(temp_op.getOperator(), "=")){
                    this.reglist.get(temp_token.getId() + 0).setGlobalname("@" + temp_token.getId());
                    this.tokenList.poll();
                    token temp1 = this.tokenList.get(0);
                    token temp2 = this.tokenList.get(1);
                    if(temp1 instanceof operator op1 && temp2 instanceof operator op2 && Objects.equals(op1.getOperator(), "{") && Objects.equals(op2.getOperator(), "}")){
                        this.answer.append("zeroinitializer");
                        this.tokenList.poll();
                        this.tokenList.poll();
                    }
                    else flag = giveArrayValue(temp_token, 0, 0, 0, this.reglist.get(temp_token.getId()+forJudgeNum(temp_token)).getDemension(), 0);
                    if(this.tokenList.peek() instanceof operator && flag){
                        this.answer.append("\n");
                        operator temp_op_2 = (operator) this.tokenList.peek();
                        if(Objects.equals(temp_op_2.getOperator(), ",")){
                            this.tokenList.poll();
                            flag = isIndentifyNew_Global(is_const, vl);
                        }
                        else if(Objects.equals(temp_op_2.getOperator(), ";")){
                            this.tokenList.poll();
                            flag = true;
                            return flag;
                        }
                        else flag = false;
                    }
                    else flag = false;
                }
                else flag= false;
            }
        }
        else{
            flag = false;
        }
        return flag;
    }
    public boolean isGlobal(){
        boolean flag = true;
        String key0 = initGlobalArea();
        HashMap<String, Integer> vl = this.blocklist.get(key0);
        while(flag){
            if(this.tokenList.peek() instanceof ident) {
                ident temp_ident = (ident) this.tokenList.peek();
                //int
                //块内定义一个变量
                if (Objects.equals(temp_ident.getId(), "int")) {
                    this.tokenList.poll();
                    if (this.tokenList.peek() instanceof ident) flag = isIndentifyNew_Global(false, vl);
                    //part13 加入对返回值是int的函数的定义
                    else if(this.tokenList.peek() instanceof function f && (vl.isEmpty() || !vl.containsKey(f.getFuncName()))){
                        vl.put(f.getFuncName(), -1);
                        this.funclist.put(f.getFuncName(), ++this.func_seq);
                        this.ff.put(f.getFuncName(), f);
                        func_sign = this.answer.length() == 0 ? 0 : this.answer.length() - 1;
                        declfunc(f.getTypeOfRetValue(), f.getFuncName());
                        this.tokenList.poll();
                        dealwithParams(f);
                        func_now = f;
                        //进入函数块
                        this.reg_seq++;
                        flag = isLbrace(5, 0, 0, false, true);
                    }
                    else flag = false;
                }
                //const int
                //块内定义一个常量
                else if (Objects.equals(temp_ident.getId(), "const")) {
                    this.tokenList.poll();
                    if (this.tokenList.peek() instanceof ident) {
                        if (Objects.equals(((ident) this.tokenList.peek()).getId(), "int")) {
                            this.tokenList.poll();
                            if (this.tokenList.peek() instanceof ident) flag = isIndentifyNew_Global(true, vl);
                            else flag = false;
                        } else flag = false;
                    } else flag = false;
                }
                //返回值是void的函数的定义
                else if(Objects.equals(temp_ident.getId(), "void")){
                    this.tokenList.poll();
                    func_sign = this.answer.length() == 0 ? 0 : this.answer.length() - 1;
                    if(this.tokenList.peek() instanceof function f){
                        this.funclist.put(f.getFuncName(), ++this.func_seq);
                        this.ff.put(f.getFuncName(), f);
                        declfunc(f.getTypeOfRetValue(), f.getFuncName());
                        this.tokenList.poll();
                        dealwithParams(f);
                        func_now = f;
                        //进入函数块
                        this.reg_seq++;
                        flag = isLbrace(5, 0, 0, false, true);
                    }
                    else{
                        flag = false;
                    }
                }
            }
            else if(this.tokenList.peek() instanceof operator){
                operator temp_op = (operator) this.tokenList.poll();
                //全局变量去结束，进入函数区
                if(Objects.equals(temp_op.getOperator(), "}")){
                    //checkForFunc();
                    this.reg_seq = 0;
                    return isInt();
                }
                else{
                    flag= false;
                }
            }
        }
        return flag;
    }
    public boolean isInt(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof ident){
            if(Objects.equals(((ident) this.tokenList.peek()).getId(), "int")){
                this.answer.append("define dso_local i32 ");
                this.tokenList.poll();
                flag = isMain();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isMain(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof function){
            if(Objects.equals(((function) this.tokenList.peek()).getFuncName(), "main")){
                this.answer.append("@main");
                this.tokenList.poll();
                flag = isLbar();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isLbar(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "(")){
                this.answer.append("(");
                this.tokenList.poll();
                flag = isRbar();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isRbar(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), ")")){
                this.answer.append(")");
                this.tokenList.poll();
                //实际上main块的开始
                flag = isLbrace(0, 0, 0, false, false);
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    //先将参数在函数体内store，以便后面使用
    public void putParamsIntoFunc(String key){
        HashMap<String, Integer> vl = this.blocklist.get(key);
        int loc = this.listnum;
        System.out.println(loc);
        while(!pre_funcdecl.isEmpty()){
            ident temp = pre_funcdecl.poll();
            //一个int 型变量
            if(this.reglist.containsKey(temp.getId() + loc) && !this.reglist.get(temp.getId() + loc).getIsArray()){
                vl.put(temp.getId() + listnum, -1);
                this.answer.append("    %").append(this.reg_seq).append(" = alloca i32\n");
                this.answer.append("    store i32 %").append(this.reglist.get(temp.getId() + loc).getSeq()).append(", i32* %").append(this.reg_seq).append("\n");
                this.reglist.get(temp.getId() + loc).setSeq(this.reg_seq++);
                this.reglist.get(temp.getId() + loc).setHasValue();
            }
            else if(this.reglist.containsKey(temp.getId() + loc) && this.reglist.get(temp.getId() + loc).getIsArray() && this.reglist.get(temp.getId() + loc).getDemension() == 1){
                vl.put(temp.getId() + listnum, -1);
                this.answer.append("    %").append(this.reg_seq).append(" = alloca i32*\n");
                this.answer.append("    store i32* %").append(this.reglist.get(temp.getId() + loc).getSeq()).append(", i32* * %").append(this.reg_seq).append("\n");
                this.answer.append("    %").append(++this.reg_seq).append(" = load i32* , i32* * %").append(this.reg_seq - 1).append("\n");
                this.reglist.get(temp.getId() + loc).setSeq(this.reg_seq);
                this.reglist.get(temp.getId() + loc).setUseaddr(this.reg_seq++);
            }
            else if(this.reglist.containsKey(temp.getId() + loc) && this.reglist.get(temp.getId() + loc).getIsArray() && this.reglist.get(temp.getId() + loc).getDemension() == 2){
                vl.put(temp.getId() + listnum, -1);
                this.answer.append("    %").append(this.reg_seq).append(" = alloca i32*\n");
                this.answer.append("    store [").append(this.reglist.get(temp.getId() + loc).getY_d()).append(" x i32]* %").append(this.reglist.get(temp.getId() + loc).getSeq()).append(", [").append(this.reglist.get(temp.getId() + loc).getY_d()).append(" x i32]* * %").append(this.reg_seq).append("\n");
                this.answer.append("    %").append(++this.reg_seq).append(" = load i32* , i32* * %").append(this.reg_seq - 1).append("\n");
                this.reglist.get(temp.getId() + loc).setSeq(this.reg_seq);
                this.reglist.get(temp.getId() + loc).setUseaddr(this.reg_seq++);
            }
        }
        this.reg_seq--;
    }
    //块中出现return，则置mark_return 为1
    //part8, {为块开始的标志，初始化一个符号表varlist;
    //会出现多次递归调用Lbrace
    public boolean isLbrace(int is_one, int mark_return, int mark_isElseIf, boolean hasFollowingElse, boolean isfuncdecl){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "{")){
                if(is_one == 0){
                    this.answer.append("{\n");
                }
                else if(is_one == 1 || is_one == 5){
                    this.answer.append("\n");
                }
                this.tokenList.poll();
                //块起始
                //获得一个新的变量表
                String key1 = setNewVarlist();
                if(is_one == 5)
                    putParamsIntoFunc(key1);
                //进入块中
                flag = isStmt(is_one, mark_return, mark_isElseIf, hasFollowingElse, key1, isfuncdecl);
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isExp(HashMap<String, Integer> vl, int is_global, int isArray, int is_func_last, int is_funcInfunc, int dealparams){
        boolean flag = true;
        int situa = 0;
        token check = null;
        this.expList.clear();
        if(is_func_last == 1){
            operator o = new operator("(", "Op", 17);
            this.expList.offer(o);
        }
        if(is_global == 0){
            while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident || this.tokenList.peek() instanceof function)){
                check = this.tokenList.peek();
                //是一个数组元素
                if(check instanceof ident id && this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray()){
                    if(dealparams == 0){
                        this.tokenList.poll();
                        int x, y;
                        int Array = isArray();
                        if(Array != this.reglist.get(id.getId() + forJudgeNum(id)).getDemension()){
                            System.exit(8);
                        }
                        x = getdemension(Array, 1, vl);
                        //使用全局数据的准备工作
                        if(Array == 2){
                            int temp_n_x = -1, temp_reg_x = -1;
                            int temp_n_y = -1, temp_reg_y = -1;
                            if(t_judge instanceof number n)
                                temp_n_x = n.getValue();
                            else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                temp_reg_x = ++this.reg_seq;
                                if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                    this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                else
                                    this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                            }
                            else temp_reg_x = this.reg_seq;
                            y = getdemension(Array, 2, vl);
                            if(t_judge instanceof number n)
                                temp_n_y = n.getValue();
                            else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                temp_reg_y = ++this.reg_seq;
                                if(this.reglist.get(d.getId() + forJudgeNum(d)).getCreatedWhenOp() == 0){
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                }
                            }
                            else temp_reg_y = this.reg_seq;
                            String str_x = temp_n_x == -1 ? ("%" + temp_reg_x) : String.valueOf(temp_n_x);
                            String str_y = temp_n_y == -1 ? ("%" + temp_reg_y) : String.valueOf(temp_n_y);
                            this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(id.getId() + forJudgeNum(id)).getArray_DD());
                            this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(id.getId() + forJudgeNum(id)).getArray_ONE_in_DD(this.reg_seq - 1)).append("i32 " + str_x + ", i32 " +  str_y +"\n");
                        }
                        else{
                            if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                                dealWithGlobalArray(id);
                            }
                            int old_seq = this.reg_seq;
                            this.reg_seq++;
                            if(t_judge instanceof number n){
                                this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 " + n.getValue() + "\n");
                            }
                            //本来是一个已经被定义过的变量，而不是计算时产生的变量
                            else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                if(this.reglist.get(d.getId() + forJudgeNum(d)).getCreatedWhenOp() == 0){
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                    this.answer.append("    %" + (++this.reg_seq) + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + (this.reg_seq - 1) + "\n");
                                }
                                else
                                    this.answer.append("    %" + (this.reg_seq) + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + (this.reg_seq - 1) + "\n");
                            }
                            else{
                                this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + old_seq + "\n");
                            }
                        }
                        ident idd = new ident(getArrayName(), "Ident", 9, 1, 1);
                        register reg = new register();
                        reg.setSeq(this.reg_seq);
                        reg.setIsConst(false);
                        reg.setHasValue();
                        //数组型变量在表达式中使用，需要被load出来
                        reg.setCreatedWhenOp(0);
                        this.reglist.put(idd.getId() + this.listnum, reg);
                        this.expList.offer(idd);
                    }
                    else if(this.tokenList.get(1) instanceof operator op && (Objects.equals(op.getOperator(), ",") || Objects.equals(op.getOperator(), ")"))){
                        //System.out.println("youyouyouyou");
                        //参数先处理一维数组情况
                        this.tokenList.poll();
                        if(this.reglist.get(id.getId() + forJudgeNum(id)).getDemension() == 1){
                            if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                                dealWithGlobalArray(id);
                            }
                            this.reg_seq++;
                            this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() + "\n");
                        }
                        ident idd = new ident(getArrayName(), "Ident", 9, 1, 1);
                        register reg = new register();
                        reg.setSeq(this.reg_seq);
                        reg.setIsConst(false);
                        reg.setIsArray(true);
                        reg.setHasValue();
                        reg.setDemension(1);
                        //数组型变量在表达式中使用，需要被load出来
                        reg.setCreatedWhenOp(0);
                        this.reglist.put(idd.getId() + this.listnum, reg);
                        this.expList.offer(idd);
                    }
                }
                else if(check instanceof function fc && Objects.equals(fc.getFuncName(), "getarray")) {
                    //System.out.println("Hello");
                    this.tokenList.poll();
                    deal_getarray(true);
                    ident temp = new ident("func" + this.func_var, "Ident", 11, 1, 1);
                    this.func_var++;
                    register reg = new register();
                    reg.setSeq(this.reg_seq);
                    reg.setValueOfReg(-1);
                    reg.setCreatedWhenOp(1);
                    reg.setIsConst(false);
                    reg.setHasValue();
                    this.reglist.put(temp.getId() + this.listnum, reg);
                    this.expList.offer(temp);
                }
                else{
                    this.expList.offer(this.tokenList.poll());
                    if(check instanceof operator ch){
                        if(ch.getOperator().charAt(0) == ';'|| ch.getOperator().charAt(0) == ',') break;
                        //part13对于表达式中的函数调用做特殊处理
                        else if(is_func_last == 1 && !Objects.equals(ch.getOperator(), ";")  && !Objects.equals(ch.getOperator(), ",") && !Objects.equals(ch.getOperator(), ")")){
                            this.expList.removeLast();
                            operator o = new operator(";", "Op", 31);
                            this.expList.offer(o);
                            //this.tokenList.addFirst(ch);
                            situa = 1;
                            //((operator) check).setOperator(";");
                            break;
                        }
                        else if(ch.getOperator().charAt(0) == '}' && isArray == 1){
                            this.expList.removeLast();
                            operator o = new operator(";", "Op", 31);
                            this.expList.offer(o);
                            break;
                        }
                    }
                }
            }
        }
        else{
            while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || (this.tokenList.peek() instanceof ident id1 && this.reglist.get(id1.getId() + 0).getIsConst() && this.reglist.get(id1.getId() + 0).getHasValue()))){
                check = this.tokenList.peek();
                this.expList.offer(this.tokenList.poll());
                if(check instanceof operator){
                    if(((operator) check).getOperator().charAt(0) == ';'|| ((operator) check).getOperator().charAt(0) == ',') break;
                    else if(((operator) check).getOperator().charAt(0) == '}' && isArray == 1){
                        this.expList.removeLast();
                        operator o = new operator(";", "Op", 31);
                        this.expList.offer(o);
                        break;
                    }
                }
            }
        }
        if((check instanceof operator && (Objects.equals(((operator) check).getOperator(), ";") || Objects.equals(((operator) check).getOperator(), ",")) || (((operator) check).getOperator().charAt(0) == '}' && isArray == 1)) || (situa == 1)){
            if(is_funcInfunc == 1 && is_func_last == 1){
                expList.remove(expList.size() - 2);
            }
            String temp_ans = this.answer.toString();
            //forBug(this.expList);
            exper.getExp(expList);
            exper.setFinal_layer(this.listnum);
            exper.getMap(vl, reglist, reg_seq, answer);
            char end = ((operator) check).getOperator().charAt(0) == '}' ? ';': ((operator) check).getOperator().charAt(0);
            //System.out.println(end);
            flag = exper.dealExp(situa == 1 ? ';' : end, expList, false);
            if(flag){
                this.ans = exper.passAns();
                this.reg_seq = exper.passRegSeq();
                this.t_judge = exper.forJudge();
                this.tokenList.addFirst(check);
                if(is_funcInfunc == 1 && is_func_last == 1){
                    operator o = new operator(")", "Op", 19);
                    this.tokenList.addFirst(o);
                }
                if(is_global == 0) this.answer = exper.getAns();
                else{
                    this.answer.delete(0, this.answer.length());
                    this.answer.insert(0, temp_ans);
                }
                exper.clearExp();
            }
        }
        else {
            flag = false;
        }
        return flag;
    }
    public boolean isGiveValueOld(ident obj, int target){
        boolean flag = true;
        int old_value = this.reg_seq;
        HashMap<String, Integer> vl = this.blocklist.get("var" + target);
        String name = obj.getId() + target;
        boolean is_g = this.reglist.get(name).getIsGlobal();
        //变量赋值
        if(!this.reglist.get(name).getIsConst() && isExp(vl, 0, 0, 0, 0, 0)){
            register reg = this.reglist.get(obj.getId() + target);
            reg.setValueOfReg(ans);
            reg.setHasValue();
            this.reglist.get(name).setHasValue();
            if(t_judge instanceof number){
                if(!this.reglist.get((obj).getId() + forJudgeNum(obj)).getIsGlobal()){
                    if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                        this.answer.append("    store i32 ").append(ans).append(", i32* %").append(reg.getSeq()).append("\n");
                    else
                        this.answer.append("    store i32 ").append(ans).append(", i32* %").append(reg.getPresent_use()).append("\n");
                }
                else{
                    if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                        this.answer.append("    store i32 ").append(ans).append(", i32* ").append(this.reglist.get(obj.getId() + forJudgeNum(obj)).getGlobalname()).append("\n");
                    else
                        this.answer.append("    store i32 ").append(ans).append(", i32* %").append(reg.getPresent_use()).append("\n");
                }
            }
            else if(t_judge instanceof ident id && this.reglist.containsKey((id).getId() + forJudgeNum(id))){
                if(!this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                    int s = this.reglist.get(id.getId() + forJudgeNum(id)).getSeq();
                    if(this.reglist.get(id.getId() + forJudgeNum(id)).getCreatedWhenOp() == 0)
                        this.answer.append("    %" + (++this.reg_seq) + " = load i32, i32* %" + s + "\n");
                    if(!reg.getIsGlobal())
                        if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                            this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                        else
                            this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getPresent_use()).append("\n");
                    else
                    if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                        this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* ").append(reg.getGlobalname()).append("\n");
                    else
                        this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getPresent_use()).append("\n");
                }
                else{
                    String s = this.reglist.get(id.getId() + forJudgeNum(id)).getGlobalname();
                    if(this.reglist.get(id.getId() + forJudgeNum(id)).getCreatedWhenOp() == 0)
                        this.answer.append("    %" + (++this.reg_seq) + " = load i32, i32* " + s + "\n");
                    if(!reg.getIsGlobal())
                        if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                            this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                        else
                            this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getPresent_use()).append("\n");
                    else
                        if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                            this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* ").append(reg.getGlobalname()).append("\n");
                        else
                            this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getPresent_use()).append("\n");
                }
            }
            else{
                if(!this.reglist.get((obj).getId() + forJudgeNum(obj)).getIsGlobal())
                    if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                        this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                    else
                        this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getPresent_use()).append("\n");
                else
                if(!this.reglist.get(obj.getId()+forJudgeNum(obj)).getIsArray())
                    this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* ").append(this.reglist.get((obj).getId() + forJudgeNum(obj)).getGlobalname()).append("\n");
                else
                    this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getPresent_use()).append("\n");

            }
        }
        //未赋值的常量赋值
        else if(this.reglist.get(name).getIsConst() && !this.reglist.get(name).getHasValue() && isExp(vl, 0, 0, 0, 0, 0)){
            register reg = this.reglist.get(obj.getId() + target);
            reg.setValueOfReg(ans);
            reg.setHasValue();
            this.reglist.get(name).setHasValue();
            if(this.reg_seq == old_value){
                if(t_judge instanceof number) this.answer.append("    store i32 ").append(ans).append(", i32* %").append(reg.getSeq()).append("\n");
                else if(t_judge instanceof ident){
                    if(!this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getIsGlobal()){
                        int s = this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getSeq();
                        this.reg_seq++;
                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + s + "\n");
                        if(!reg.getIsGlobal()) this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                        else this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* ").append(reg.getGlobalname()).append("\n");
                    }
                    else{
                        String s = this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getGlobalname();
                        this.reg_seq++;
                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + s + "\n");
                        if(!reg.getIsGlobal()) this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                        else this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* ").append(reg.getGlobalname()).append("\n");
                    }
                }
            }
            else{
                if(!reg.getIsGlobal()) this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                else this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* ").append(reg.getGlobalname()).append("\n");
            }
        }
        else{
            flag = false;
        }
        return flag;
    }
    public boolean isArrayExp(LinkedList<token> expl){
        boolean flag = true;
        token check = null;
        while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || (this.tokenList.peek() instanceof ident id1 && this.reglist.get(id1.getId() + forJudgeNum(id1)).getHasValue()) || this.tokenList.peek() instanceof function)){
            check = this.tokenList.peek();
            //对全局数组赋值时的长度表达式进行检查
            if(check instanceof ident id && listnum == 0 && !this.reglist.get(id.getId() + listnum).getIsConst())
                break;
            if(check instanceof ident id && this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray()){
                this.tokenList.poll();
                int x, y;
                int Array = isArray();
                if(Array != this.reglist.get(id.getId() + forJudgeNum(id)).getDemension()){
                    System.exit(8);
                }
                x = getdemension(Array, 1, new HashMap<String, Integer>());
                //使用全局数据的准备工作
                if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                    dealWithGlobalArray(id);
                }
                //如果是二维
                if(Array == 2){
                    y = getdemension(Array, 2, new HashMap<String, Integer>());
                    this.reg_seq++;
                    this.answer.append("    %" + this.reg_seq + " =" + this.reglist.get(id.getId() + forJudgeNum(id)).getArray_certainaddr(x, y));
                }
                else{
                    int old_seq = this.reg_seq;
                    this.reg_seq++;
                    if(t_judge instanceof number n){
                        this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 " + n.getValue() + "\n");
                    }
                    //本来是一个已经被定义过的变量，而不是计算时产生的变量
                    else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                        if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                        else
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                        this.answer.append("    %" + (++this.reg_seq) + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + (this.reg_seq - 1) + "\n");
                    }
                    else{
                        this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + old_seq + "\n");
                    }
                }
                ident idd = new ident(getArrayName(), "Ident", 9, 1, 1);
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setIsConst(false);
                reg.setHasValue();
                //数组型变量在表达式中使用，需要被load出来
                reg.setCreatedWhenOp(0);
                this.reglist.put(idd.getId() + this.listnum, reg);
                expl.offer(idd);
            }
            else if(check instanceof function fc && Objects.equals(fc.getFuncName(), "getarray")){
                this.tokenList.poll();
                deal_getarray(true);
                ident temp = new ident("func" + this.func_var, "Ident", 11, 1, 1);
                this.func_var++;
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setValueOfReg(-1);
                reg.setCreatedWhenOp(1);
                reg.setIsConst(false);
                reg.setHasValue();
                this.reglist.put(temp.getId() + this.listnum,  reg);
            }
            else{
                expl.offer(this.tokenList.poll());
                if(check instanceof operator){
                    if(((operator) check).getOperator().charAt(0) == ']'){
                        expl.removeLast();
                        operator o = new operator(";", "Op", 31);
                        expl.offer(o);
                        break;
                    }
                }
            }
        }
        if(check instanceof operator && (Objects.equals(((operator) check).getOperator(), "]"))){
            String temp_ans = this.answer.toString();
            exper.getExp(expl);
            exper.setFinal_layer(this.listnum);
            exper.getMap(null, reglist, reg_seq, answer);
            flag = exper.dealExp(';', expl, false);
            if(flag){
                this.arr_len = exper.passAns();
                this.reg_seq = exper.passRegSeq();
                this.t_judge = exper.forJudge();
                if(this.listnum == 0){
                    this.answer.delete(0, this.answer.length());
                    this.answer.insert(0, temp_ans);
                }
                exper.clearExp();
            }
        }
        else flag = false;
        return flag;
    }
    //在数组初始化的时候求数组各维度的长度
    public int getdemension(int d, int target, HashMap<String, Integer> vl){
        boolean flag = true;
        if(target == 1){
            //弹出'['
            this.tokenList.poll();
            dealWithFuncInExp(this.listnum, vl, 0);
            //System.out.print("Sign || ");
            //this.forBug();
            flag = isArrayExp(new LinkedList<token>());
            if(!flag){
                //System.out.println(this.answer);
                System.exit(7);
            }
            //arr_len是isArrayExp计算出来的值
            return arr_len;
        }
        else{
            if(d == 1) return 0;
            else{
                //弹出'['
                this.tokenList.poll();
                dealWithFuncInExp(this.listnum, vl, 0);
                //System.out.print("Sign || ");
                //this.forBug();
                flag = isArrayExp(new LinkedList<token>());
                if(!flag){
                    //System.out.println(this.answer);
                    System.exit(7);
                }
                return arr_len;
            }
        }
    }
    //判断一个变量是数组型变量还是只是int型变量
    public int isArray(){
        int i, numofL = 0, numofR = 0;
        char check = ' ';
        for(i = 0; i < this.tokenList.toArray().length; i++){
            if(this.tokenList.get(i) instanceof ident) continue;
            else if(this.tokenList.get(i) instanceof operator op){
                if(Objects.equals(op.getOperator(), "[") && check != '['){
                    check = '[';
                    numofL++;
                }
                else if(Objects.equals(op.getOperator(), "]") && check != ']') {
                    check = ']';
                    numofR++;
                }
                else if(Objects.equals(op.getOperator(), ";") || Objects.equals(op.getOperator(), ",") || Objects.equals(op.getOperator(), "=")
                        || (this.tokenList.get(i - 1) instanceof operator opp && Objects.equals(opp.getOperator(), "]") && !Objects.equals(op.getOperator(), "[")))
                    break;
            }
        }
        if(numofL == numofR && numofL != 0 && numofR != 0) return numofL;
        else return 0;
    }
    public boolean giveArrayValue(ident obj, int listnum, int x, int y, int de, int isfrom) {
        //初始化，用于计算对应的数组元素位置
        int x_i = x, y_i = y;
        int x_d = this.reglist.get(obj.getId() + forJudgeNum(obj)).getX_d();
        int y_d = this.reglist.get(obj.getId() + forJudgeNum(obj)).getY_d();
        int numofL = 0, numofR = 0, d = de;
        boolean is_global = this.reglist.get(obj.getId() + forJudgeNum(obj)).getIsGlobal();
        boolean flag = true;
        //一维数组
        if(d == 1){
            if(isfrom == 1) x_d = y_d;
            if(is_global) this.answer.append("[");
            while((numofL != 1 || numofR != 1) && flag && y_i < x_d){
                token temp = this.tokenList.peek();
                if(temp instanceof operator op){
                    if(Objects.equals(op.getOperator(), "{")) {
                        numofL++;
                        this.tokenList.poll();
                    }
                    else if(Objects.equals(op.getOperator(), "}")) {
                        numofR++;
                        this.tokenList.poll();
                    }
                    else if(Objects.equals(op.getOperator(), ",")) {
                        y_i++;
                        this.tokenList.poll();
                    }
                    else{
                        flag = isExp(null, is_global ? 1 : 0, 1, 0, 0, 0);
                        if(flag){
                            if(!is_global){
                                int old2 = this.reg_seq;
                                ++this.reg_seq;
                                //得到要赋值的项
                                this.answer.append("    %").append(this.reg_seq).append(" =").append(this.reglist.get(obj.getId() + forJudgeNum(obj)).getArray_certainaddr(x_i, y_i));
                                if(t_judge instanceof number)
                                    this.answer.append("    store i32 ").append(this.ans).append(", i32* %").append(this.reg_seq).append("\n");
                                else if(this.reglist.containsKey(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)) && this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getCreatedWhenOp() == 0){
                                    this.answer.append("    %" + (++this.reg_seq) + " = load i32, i32* %" + this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getSeq() + "\n");
                                    this.answer.append("    store i32 %").append(this.reg_seq).append(", i32* %").append(this.reg_seq - 1).append("\n");
                                }
                                else{
                                    this.answer.append("    store i32 %").append(old2).append(", i32* %").append(this.reg_seq).append("\n");
                                }
                            }
                            else{
                                this.answer.append("i32 ").append(ans).append(", ");
                            }
                        }
                    }
                }
                else{
                    flag = isExp(null, is_global ? 1 : 0, 1, 0, 0, 0);
                    if(flag){
                        if(!is_global){
                            int old2 = this.reg_seq;
                            ++this.reg_seq;
                            //得到要赋值的项
                            this.answer.append("    %").append(this.reg_seq).append(" =").append(this.reglist.get(obj.getId() + forJudgeNum(obj)).getArray_certainaddr(x_i, y_i));
                            if(t_judge instanceof number)
                                this.answer.append("    store i32 ").append(this.ans).append(", i32* %").append(this.reg_seq).append("\n");
                            else if(this.reglist.containsKey(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)) && this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getCreatedWhenOp() == 0){
                                this.answer.append("    %" + (++this.reg_seq) + " = load i32, i32* %" + this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getSeq() + "\n");
                                this.answer.append("    store i32 %").append(this.reg_seq).append(", i32* %").append(this.reg_seq - 1).append("\n");
                            }
                            else{
                                this.answer.append("    store i32 %").append(old2).append(", i32* %").append(this.reg_seq).append("\n");
                            }
                        }
                        else{
                            this.answer.append("i32 ").append(ans).append(", ");
                        }
                    }
                }
            }
            //检查花括号中赋值的项数是否超过数组中元素个数
            if(y_i >= x_d) flag = false;
            if(y_i < x_d - 1 && is_global){
                do{
                    this.answer.append("i32 ").append(0).append(", ");
                    y_i++;
                }while(y_i < x_d - 1);
            }
            if(is_global) this.answer.replace(this.answer.length() - 2, this.answer.length(), "]");
        }
        //二维数组
        //递归调用giveArrayValue
        else{
            if(is_global) this.answer.append("[");
            while((numofL != 1 || numofR != 1) && x_i < x_d && flag){
                token temp = this.tokenList.peek();
                if(temp instanceof operator op && Objects.equals(op.getOperator(), "{")){
                    if(numofL == 0) {
                        numofL++;
                        this.tokenList.poll();
                    }
                    else{
                        if(is_global) this.answer.append("[").append(y_d).append(" x i32] ");
                        flag = giveArrayValue(obj, listnum, x_i, 0, 1, 1);
                        if(is_global) this.answer.append(", ");
                    }
                }
                else if(temp instanceof operator op && Objects.equals(op.getOperator(), ",")){
                    x_i++;
                    this.tokenList.poll();
                }
                else if(temp instanceof operator op && Objects.equals(op.getOperator(), "}")) {
                    numofR++;
                    this.tokenList.poll();
                }

            }
            if(x_i >= x_d) flag = false;
            if(x_i < x_d - 1 && is_global){
                do{
                    this.answer.append("[").append(y_d).append(" x i32] zeroinitializer, ");
                    x_i++;
                }while(x_i < x_d - 1);
            }
            if(is_global){
                this.answer.replace(this.answer.length() - 2, this.answer.length() - 1, "]");
            }
        }
        return flag;
    }
    public boolean isIndentifyNew(boolean is_const, HashMap<String, Integer> vl){
        boolean flag = true;
        int isArray = isArray();
        //普通的int型变量;
        if(this.tokenList.peek() instanceof ident && isArray == 0){
            ident temp_token = (ident) this.tokenList.poll();
            if(vl.isEmpty() || !vl.containsKey(temp_token.getId())){
                //加入变量表
                vl.put(temp_token.getId(), -1);
                register reg = new register();
                this.reg_seq++;
                reg.setSeq(this.reg_seq);
                reg.setValueOfReg(-1);
                reg.setCreatedWhenOp(0);
                if(is_const) reg.setIsConst(true);
                else reg.setIsConst(false);
                //为不同块中的同名变量分配不同的寄存器
                this.reglist.put(temp_token.getId() + this.listnum, reg);
                this.answer.append("    %").append(this.reg_seq).append(" = alloca i32\n");
                //检查下一个
                if(this.tokenList.peek() instanceof operator){
                    operator temp_op = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_op.getOperator(), ",")){
                        this.tokenList.poll();
                        flag = isIndentifyNew(is_const, vl);
                    }
                    else if(Objects.equals(temp_op.getOperator(), ";")){
                        this.tokenList.poll();
                        flag = true;
                        return flag;
                    }
                    else if(Objects.equals(temp_op.getOperator(), "=")){
                        this.tokenList.poll();
                        dealWithFuncInExp(this.listnum, vl, 0);
                        flag = isGiveValueOld(temp_token, this.listnum);
                        if(this.tokenList.peek() instanceof operator && flag){
                            operator temp_op_2 = (operator) this.tokenList.peek();
                            if(Objects.equals(temp_op_2.getOperator(), ",")){
                                this.tokenList.poll();
                                flag = isIndentifyNew(is_const, vl);
                            }
                            else if(Objects.equals(temp_op_2.getOperator(), ";")){
                                this.tokenList.poll();
                                flag = true;
                                return flag;
                            }
                            else flag = false;
                        }
                        else flag = false;
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else flag = false;
        }
        //数组型变量
        else if(this.tokenList.peek() instanceof ident && isArray > 0){
            ident temp_token = (ident) this.tokenList.poll();
            int x = getdemension(isArray, 1, vl), y = getdemension(isArray, 2, vl);
            if(x <= 0 || (isArray == 2 && y <= 0)) {
                System.exit(8);
            }
            if(vl.isEmpty() || !vl.containsKey(temp_token.getId())) {
                //加入变量表
                vl.put(temp_token.getId(), -1);
                this.reg_seq++;
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setIsArray(true);
                reg.setX_d(x);
                reg.setY_d(y);
                if(y == 0) reg.setDemension(1);
                else reg.setDemension(2);
                //设置是否是常量数组
                if(is_const) reg.setIsConst(true);
                else reg.setIsConst(false);
                this.reglist.put(temp_token.getId() + this.listnum, reg);
                this.answer.append(reg.initArray());
                this.answer.append("    %" + (++this.reg_seq) + " = " + reg.getArray_firstaddr(this.reg_seq));
                if(reg.getDemension() == 2) this.reg_seq++;
                this.answer.append(reg.getArray_useaddr(this.reg_seq));
                //数组只要一经定义，现自动给各元素赋值为0
                this.answer.append(reg.memsetStep());
                if(this.tokenList.peek() instanceof operator){
                    operator temp_op = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_op.getOperator(), ",")){
                        this.tokenList.poll();
                        flag = isIndentifyNew(is_const, vl);
                    }
                    else if(Objects.equals(temp_op.getOperator(), ";")){
                        this.tokenList.poll();
                        flag = true;
                        return flag;
                    }
                    else if(Objects.equals(temp_op.getOperator(), "=")){
                        this.tokenList.poll();
                        dealWithFuncInExp(this.listnum, vl, 0);
                        //token f = this.tokenList.peek();
                        flag = giveArrayValue(temp_token, this.listnum, 0, 0, this.reglist.get(temp_token.getId()+forJudgeNum(temp_token)).getDemension(), 0);
                        if(this.tokenList.peek() instanceof operator && flag){
                            operator temp_op_2 = (operator) this.tokenList.peek();
                            if(Objects.equals(temp_op_2.getOperator(), ",")){
                                this.tokenList.poll();
                                flag = isIndentifyNew(is_const, vl);
                            }
                            else if(Objects.equals(temp_op_2.getOperator(), ";")){
                                this.tokenList.poll();
                                flag = true;
                                return flag;
                            }
                            else flag = false;
                        }
                        else flag = false;
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean get_IsEndBlock(int is_one){
        int i;
        boolean flag = false;
        if(is_one == 1){
            for(i = 0; i < this.tokenList.size(); i++){
                if(this.tokenList.get(i) instanceof operator op && Objects.equals(op.getOperator(), "}")){
                    if(this.tokenList.get(i + 1) instanceof operator opp && Objects.equals(opp.getOperator(), "}")){
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
    }
    //mark_isElseIf = 1表示是elseif
    //mark_isElseIf = 0表示是if
    public boolean isCondExp(int mark_isElseIf, boolean hasFollowingElse, HashMap<String, Integer> vl, int is_one){
        boolean flag = true;
        token check = null;
        this.expList.clear();
        while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident || this.tokenList.peek() instanceof function)){
            check = this.tokenList.peek();
            if(check instanceof ident id && this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray()){
                this.tokenList.poll();
                int x, y;
                int Array = isArray();
                if(Array != this.reglist.get(id.getId() + forJudgeNum(id)).getDemension()){
                    //System.out.println(this.reglist.get(id.getId() + forJudgeNum(id)).getDemension());
                    System.out.println(this.content);
                    //System.out.println(this.answer);
                    System.exit(8);
                }
                x = getdemension(Array, 1, vl);
                //使用全局数据的准备工作
                if(Array == 2){
                    int temp_n_x = -1, temp_reg_x = -1;
                    int temp_n_y = -1, temp_reg_y = -1;
                    if(t_judge instanceof number n)
                        temp_n_x = n.getValue();
                    else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                        temp_reg_x = ++this.reg_seq;
                        if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                        else
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                    }
                    else temp_reg_x = this.reg_seq;
                    y = getdemension(Array, 2, vl);
                    if(t_judge instanceof number n)
                        temp_n_y = n.getValue();
                    else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                        temp_reg_y = ++this.reg_seq;
                        if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                        else
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                    }
                    else temp_reg_y = this.reg_seq;
                    String str_x = temp_n_x == -1 ? ("%" + temp_reg_x) : String.valueOf(temp_n_x);
                    String str_y = temp_n_y == -1 ? ("%" + temp_reg_y) : String.valueOf(temp_n_y);
                    this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(id.getId() + forJudgeNum(id)).getArray_DD());
                    this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(id.getId() + forJudgeNum(id)).getArray_ONE_in_DD(this.reg_seq - 1)).append("i32 " + str_x + ", i32 " +  str_y +"\n");
                }
                else{
                    if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                        dealWithGlobalArray(id);
                    }
                    int old_seq = this.reg_seq;
                    this.reg_seq++;
                    if(t_judge instanceof number n){
                        this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 " + n.getValue() + "\n");
                    }
                    //本来是一个已经被定义过的变量，而不是计算时产生的变量
                    else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                        if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                        else
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                        this.answer.append("    %" + (++this.reg_seq) + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + (this.reg_seq - 1) + "\n");
                    }
                    else{
                        this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + old_seq + "\n");
                    }
                }
                ident idd = new ident(getArrayName(), "Ident", 9, 1, 1);
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setIsConst(false);
                reg.setHasValue();
                //数组型变量在表达式中使用，需要被load出来
                reg.setCreatedWhenOp(0);
                this.reglist.put(idd.getId() + this.listnum, reg);
                this.expList.offer(idd);
            }
            else if(check instanceof function fc && Objects.equals(fc.getFuncName(), "getarray")){
                this.tokenList.poll();
                deal_getarray(true);
                ident temp = new ident("func" + this.func_var, "Ident", 11, 1, 1);
                this.func_var++;
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setValueOfReg(-1);
                reg.setCreatedWhenOp(1);
                reg.setIsConst(false);
                reg.setHasValue();
                this.reglist.put(temp.getId() + this.listnum,  reg);
            }
            else{
                this.expList.offer(this.tokenList.poll());
                if(check instanceof operator){
                    if(((operator) check).getOperator().charAt(0) == '{') break;
                }
            }
        }
        if(check instanceof operator && (Objects.equals(((operator) check).getOperator(), "{"))){
            exper.getExp(expList);
            exper.setFinal_layer(this.listnum);
            exper.getMap(vl, reglist, reg_seq, answer);
            flag = exper.dealExp(((operator) check).getOperator().charAt(0), expList, true);
            if(flag){
                this.reg_seq = exper.passRegSeq();
                this.answer = exper.getAns();
                this.t_judge = exper.forJudge();
                //将'{'加回去
                this.tokenList.addFirst(check);
                if(mark_isElseIf == 0){
                    //if后面有一个else statement
                    if(hasFollowingElse){
                        addLabel(1); String temp1 = label;
                        addLabel(1); String temp2 = label;
                        addLabel(1); String temp3 = label;
                        dstANDstr temp = new dstANDstr(temp1, temp2, temp3);
                        this.three.push(temp);
                        this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(temp1).append(", label %").append(temp2);
                    }
                    //只有一个if statement
                    else{
                        if(get_IsEndBlock(is_one)){
                            addLabel(1); String temp1 = label;
                            dstANDstr temp = new dstANDstr(temp1, this.three.peek().getDst(), this.three.peek().getDst());
                            this.three.push(temp);
                            this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(temp1).append(", label %").append(this.three.peek().getDst());
                        }
                        else{
                            addLabel(1); String temp1 = label;
                            addLabel(1); String temp2 = label;
                            dstANDstr temp = new dstANDstr(temp1, "x0", temp2);
                            this.three.push(temp);
                            this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(temp1).append(", label %").append(temp2);
                        }
                    }
                }
                //是elseif statement
                else{
                    String a3 = this.three.peek().getDst();
                    this.three.pop();
                    addLabel(1); String temp1 = label;
                    addLabel(1); String temp2 = label;
                    dstANDstr temp_new = new dstANDstr(temp1, temp2, a3);
                    this.three.push(temp_new);
                    if(hasFollowingElse){
                        this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(temp1).append(", label %").append(temp2);
                    }
                    else{
                        this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(temp1).append(", label %").append(a3);
                    }
                    //++this.reg_seq;
                }
                exper.clearExp();
            }
        }
        else{
            flag = false;
        }
        return flag;
    }
    public boolean isCondExp_while(circulation cir, HashMap<String, Integer> vl){
        boolean flag = true;
        token check = null;
        this.expList.clear();
        while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident || this.tokenList.peek() instanceof  function)){
            check = this.tokenList.peek();
            if(check instanceof ident id && this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray()){
                this.tokenList.poll();
                int x, y;
                int Array = isArray();
                if(Array != this.reglist.get(id.getId() + forJudgeNum(id)).getDemension()){
                    System.out.println(this.content);
                    //System.out.println(this.answer);
                    System.exit(8);
                }
                x = getdemension(Array, 1, vl);
                //使用全局数据的准备工作
                if(Array == 2){
                    int temp_n_x = -1, temp_reg_x = -1;
                    int temp_n_y = -1, temp_reg_y = -1;
                    if(t_judge instanceof number n)
                        temp_n_x = n.getValue();
                    else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                        temp_reg_x = ++this.reg_seq;
                        if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                        else
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                    }
                    else temp_reg_x = this.reg_seq;
                    y = getdemension(Array, 2, vl);
                    if(t_judge instanceof number n)
                        temp_n_y = n.getValue();
                    else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                        temp_reg_y = ++this.reg_seq;
                        if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                        else
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                    }
                    else temp_reg_y = this.reg_seq;
                    String str_x = temp_n_x == -1 ? ("%" + temp_reg_x) : String.valueOf(temp_n_x);
                    String str_y = temp_n_y == -1 ? ("%" + temp_reg_y) : String.valueOf(temp_n_y);
                    this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(id.getId() + forJudgeNum(id)).getArray_DD());
                    this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(id.getId() + forJudgeNum(id)).getArray_ONE_in_DD(this.reg_seq - 1)).append("i32 " + str_x + ", i32 " +  str_y +"\n");
                }
                else{
                    if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                        dealWithGlobalArray(id);
                    }
                    int old_seq = this.reg_seq;
                    this.reg_seq++;
                    if(t_judge instanceof number n){
                        this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 " + n.getValue() + "\n");
                    }
                    //本来是一个已经被定义过的变量，而不是计算时产生的变量
                    else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                        if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                        else
                            this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                        this.answer.append("    %" + (++this.reg_seq) + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + (this.reg_seq - 1) + "\n");
                    }
                    else{
                        this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr() +  ", i32 %" + old_seq + "\n");
                    }
                }
                ident idd = new ident(getArrayName(), "Ident", 9, 1, 1);
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setIsConst(false);
                reg.setHasValue();
                //数组型变量在表达式中使用，需要被load出来
                reg.setCreatedWhenOp(0);
                this.reglist.put(idd.getId() + this.listnum, reg);
                this.expList.offer(idd);
            }
            else if(check instanceof function fc && Objects.equals(fc.getFuncName(), "getarray")){
                this.tokenList.poll();
                deal_getarray(true);
                ident temp = new ident("func" + this.func_var, "Ident", 11, 1, 1);
                this.func_var++;
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setValueOfReg(-1);
                reg.setCreatedWhenOp(1);
                reg.setIsConst(false);
                reg.setHasValue();
                this.reglist.put(temp.getId() + this.listnum,  reg);
            }
            else{
                this.expList.offer(this.tokenList.poll());
                if(check instanceof operator){
                    if(((operator) check).getOperator().charAt(0) == '{') break;
                }
            }
        }
        if(check instanceof operator && (Objects.equals(((operator) check).getOperator(), "{"))){
            //System.out.println("HIHI");
            exper.getExp(expList);
            exper.setFinal_layer(this.listnum);
            exper.getMap(vl, reglist, reg_seq, answer);
            flag = exper.dealExp(((operator) check).getOperator().charAt(0), expList, true);
            if(flag){
                this.reg_seq = exper.passRegSeq();
                this.answer = exper.getAns();
                this.t_judge = exper.forJudge();
                //将'{'加回去
                this.tokenList.addFirst(check);
                String temp1, temp2;
                addLabel(2); temp1 = this.label_while;
                addLabel(2); temp2 = this.label_while;
                cir.setPrefer_target(temp1);
                cir.setAnti_cond_target(temp2);
                while_info.push(cir);
                this.answer.append("    br i1 %").append(this.reg_seq).append(", label %").append(temp1).append(", label %").append(temp2).append("\n");
                exper.clearExp();
            }
        }
        else flag = false;
        return flag;
    }
    public boolean checkFollowingElse(int tab){
        int i, numofif = 1, numofelse = 0;
        for(i = 0; i < this.tokenList.toArray().length; i++){
            token temp = this.tokenList.get(i);
            if(temp instanceof cond){
                if((Objects.equals(((cond) temp).getCondid(), "if")&& ((cond) temp).getTabnums() == tab)) break;
                if((Objects.equals(((cond) temp).getCondid(), "else") || Objects.equals(((cond) temp).getCondid(), "else if"))
                        && ((cond) temp).getTabnums() == tab){
                    numofelse++;
                }
                //else numofif++;
            }
            if(numofelse == numofif) return true;
        }
        return false;
    }
    public boolean isIf(int mark_isElseIf, int tab, HashMap<String, Integer> vl, int is_one){
        boolean flag = true;
        //if中表达式处理，包括两个跳转地址压栈、一个块跳转地址压栈
        //设置three
        boolean hasFollowingElse = checkFollowingElse(tab);
        flag = isCondExp(mark_isElseIf, hasFollowingElse, vl, is_one);
        if(flag){
            this.strblockeach.push(this.three.peek().getIf_seq());
            flag = isLbrace(1, 0, mark_isElseIf, hasFollowingElse, false);
        }
        return flag;
    }
    public boolean isElseIf(int mark_isElseIf){
        boolean flag = true;
        this.strblockeach.push(this.three.peek().getElse_seq());
        flag = isStmt(1, 0, mark_isElseIf, false, "var"+this.listnum, false);
        return flag;
    }
    public boolean isElse(int mark_isElseIf){
        boolean flag = true;
        //进入新的块
        this.strblockeach.push(this.three.peek().getElse_seq());
        flag = isLbrace(2, 0, mark_isElseIf, false, false);
        return flag;
    }
    public boolean isWhile(circulation cir, HashMap<String, Integer> vl){
        boolean flag = true;
        addLabel(2);
        String temp1 = this.label_while;
        this.answer.append("    br label %").append(temp1).append("\n");
        this.answer.append(temp1).append(":\n");
        cir.setCond_target(temp1);
        //进入条件判断语句
        flag = isCondExp_while(cir, vl);
        if(flag){
            flag = isLbrace(4, 0,0, false, false);
        }
        return flag;
    }
    public void dealWithFuncInExp(int ln, HashMap<String, Integer> vl, int is_funcInfunc){
        int i, j;
        Stack<token> token_home = new Stack<>();
        for(i = 0; i < this.tokenList.toArray().length; i++){
            if(this.tokenList.get(i) instanceof operator op){
                if(Objects.equals(op.getOperator(), ";") || Objects.equals(op.getOperator(), ","))
                    break;
            }
            else if(this.tokenList.get(i) instanceof function func && !Objects.equals(func.getFuncName(), "getarray")){
                ident temp = new ident("func" + this.func_var, "Ident", 11, 1, 1);
                this.func_var++;
                register reg = new register();
                reg.setSeq(this.reg_seq);
                reg.setValueOfReg(-1);
                reg.setCreatedWhenOp(1);
                reg.setIsConst(false);
                reg.setHasValue();
                if(Objects.equals(func.getFuncName(), "getint")){
                    //System.out.println("youxui");
                    this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @getint()\n");
                    reg.setSeq(this.reg_seq);
                    this.reglist.put(temp.getId() + ln,  reg);
                    //去除两个括号;
                    //forBug(this.tokenList);
                    this.tokenList.remove(i);
                    this.tokenList.remove(i);
                    this.tokenList.remove(i);
                    this.tokenList.add(i, temp);
                }
                else if(Objects.equals(func.getFuncName(), "getch")){
                    this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @getch()\n");
                    reg.setSeq(this.reg_seq);
                    this.reglist.put(temp.getId() + ln,  reg);
                    //去除两个括号;
                    this.tokenList.remove(i);
                    this.tokenList.remove(i);
                    this.tokenList.remove(i);
                    this.tokenList.add(i, temp);
                }
                //对普通函数进行处理
                else{
                    //先将函数前面的东西都拿出来
                    //处理完函数再放回去
                    for(j = 0; j < i; j++){
                        token_home.push(this.tokenList.poll());
                    }
                    //弹出函数名
                    this.tokenList.poll();
                    func = this.ff.get(func.getFuncName());
                    if (Objects.equals(func.getTypeOfRetValue(), "void")) {
                        //函数不需要传参
                        if(func.getParams_num() == 0){
                            this.answer.append("    call void @").append(func.getFuncName()).append("()\n");
                        }
                        else{
                            deal_withfunc(func, vl, is_funcInfunc);
                            this.answer.append("    %").append(++this.reg_seq).append(" = call void @").append(func.getFuncName()).append("(").append(this.funcstr.toString()).append(")\n");
                        }
                    }
                    //返回值为int类型
                    else{
                        if(func.getParams_num() == 0){
                            this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @").append(func.getFuncName()).append("()\n");
                        }
                        else{
                            deal_withfunc(func, vl, is_funcInfunc);
                            this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @").append(func.getFuncName()).append("(").append(this.funcstr.toString()).append(")\n");
                        }
                    }
                    funcstr.delete(funcstr.length() - 2, funcstr.length());
                    reg.setSeq(this.reg_seq);
                    this.reglist.put(temp.getId() + ln,  reg);
                    this.tokenList.addFirst(temp);
                    while(!token_home.isEmpty()){
                        this.tokenList.addFirst(token_home.pop());
                    }
                }
            }
        }
    }
    public void dealWithGlobalArray(ident temp_ident){
        int xd = this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getX_d();
        int yd = this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getY_d();
        if(this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getDemension() == 2){
            this.answer.append("    %" + (++this.reg_seq) + " = getelementptr [" + xd + " x [" + yd + " x i32]], [" + xd + " x [" + yd + " x i32]]* " + this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getGlobalname() + ", i32 0, i32 0\n");
            this.answer.append("    %" + (++this.reg_seq) + " = getelementptr [" + yd + " x i32], [" + yd +" x i32]* %" + (this.reg_seq - 1)+ ", i32 0, i32 0\n");
            this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).setUseaddr(this.reg_seq);
        }
        else{
            this.answer.append("    %" + (++this.reg_seq) + " = getelementptr [" + xd + " x i32], [" + xd +" x i32]* " +this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getGlobalname() + ", i32 0, i32 0\n");
            this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).setUseaddr(this.reg_seq);
        }
    }
    //不进Exp处理了,因为括号内表达式只有一个数组元素
    public boolean deal_getarray(boolean is_inexp){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), "(")){
            this.tokenList.poll();
            //判断是否是数组类型
            if(this.tokenList.peek() instanceof ident id && this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray()){
                //如果数组是一维，则只有数组名自己作为参数才是合法的情况
                if(this.reglist.get(id.getId() + forJudgeNum(id)).getDemension() == 1){
                    if(this.tokenList.get(1) instanceof operator opp && Objects.equals(opp.getOperator(), ")")){
                        //非全局数组
                        if(!this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal())
                            this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @getarray(i32* %").append(this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr()).append(")\n");
                        else{
                            dealWithGlobalArray(id);
                            this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @getarray(i32* %").append(this.reglist.get(id.getId() + forJudgeNum(id)).getUseaddr()).append(")\n");
                        }
                        //对获得的返回值进行存储, 当is_inexp为true
                        this.tokenList.poll();
                        this.tokenList.poll();
                    }
                    else flag = false;
                }
                //如果数组是二维
                else{
                    this.tokenList.poll();
                    getdemension(1, 1, new HashMap<String, Integer>());
                    if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal())
                        dealWithGlobalArray(id);
                    if(t_judge instanceof number n){
                        this.answer.append("    %").append(++this.reg_seq).append(this.reglist.get(id.getId() + forJudgeNum(id)).deal_getarray_addr_two_d(n.getValue(), 1));
                        this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @getarray(i32* %").append(this.reg_seq - 1).append(")\n");
                    }
                    else if(t_judge instanceof ident idd && this.reglist.containsKey(idd.getId() + forJudgeNum(idd))){
                        this.answer.append("    %").append(++this.reg_seq).append(" = load i32, i32* %").append(this.reglist.get(idd.getId() + forJudgeNum(idd)).getSeq()).append("\n");
                        this.answer.append("    %").append(++this.reg_seq).append(this.reglist.get(id.getId() + forJudgeNum(id)).deal_getarray_addr_two_d(this.reg_seq - 1, 2));
                        this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @getarray(i32* %").append(this.reg_seq - 1).append(")\n");
                    }
                    else{
                        this.answer.append("    %").append(++this.reg_seq).append(this.reglist.get(id.getId() + forJudgeNum(id)).deal_getarray_addr_two_d(this.reg_seq - 1, 2));
                        this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @getarray(i32* %").append(this.reg_seq - 1).append(")\n");
                    }
                    //弹出")"
                    this.tokenList.poll();
                }
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isStmt(int is_one, int mark_return, int mark_isElseIf, boolean hasFollowingElse, String key_sub_varlist, boolean isfuncdecl){
        int mr = mark_return;
        boolean has_jump = false;
        boolean flag = true;
        //查找变量若无，则可搜索外层表
        //查找变量若有，则一切变量的属性按照内层表来，包括值、是否赋值、是否是常量等
        HashMap<String, Integer> vl = this.blocklist.get(key_sub_varlist);
        if(!this.strblockeach.isEmpty()){
            this.answer.append(this.strblockeach.pop()).append(":").append("\n");
        }
        //进入while循环体内
        if(is_one == 4 && !this.while_info.isEmpty()){
            this.answer.append(this.while_info.peek().getPrefer_target()).append(":\n");
        }
        while(flag){
            if(this.tokenList.peek() instanceof ident){
                ident temp_ident = (ident)this.tokenList.peek();
                //int
                //块内定义一个变量
                if(Objects.equals(temp_ident.getId(), "int")){
                    this.tokenList.poll();
                    if(this.tokenList.peek() instanceof ident) flag = isIndentifyNew(false, vl);
                    else flag = false;
                }
                //const int
                //块内定义一个常量
                else if(Objects.equals(temp_ident.getId(), "const")){
                    this.tokenList.poll();
                    if(this.tokenList.peek() instanceof ident){
                        if(Objects.equals(((ident) this.tokenList.peek()).getId(), "int")){
                            this.tokenList.poll();
                            if(this.tokenList.peek() instanceof ident) flag = isIndentifyNew(true, vl);
                            else flag = false;
                        }
                        else flag= false;
                    }
                    else flag = false;
                }
                else if(Objects.equals(temp_ident.getId(), "return")){
                    //也需要传参
                    //对应着isExp()要加参数
                    flag = isReturn(vl);
                    if(flag) mr = 1;
                }
                else if(Objects.equals(temp_ident.getId(), "continue")){
                    this.tokenList.poll();
                    this.answer.append("    br label %").append(while_info.peek().getCond_target()).append("\n");
                    if(this.tokenList.peek() instanceof operator op){
                        if(Objects.equals(op.getOperator(), ";")){
                            this.tokenList.poll();
                            has_jump= true;
                        }
                        else
                            flag = false;
                    }
                    else flag = false;
                }
                else if(Objects.equals(temp_ident.getId(), "break")){
                    this.tokenList.poll();
                    this.answer.append("    br label %").append(while_info.peek().getAnti_cond_target()).append("\n");
                    if(this.tokenList.peek() instanceof operator op){
                        if(Objects.equals(op.getOperator(), ";")){
                            this.tokenList.poll();
                            has_jump = true;
                        }
                        else
                            flag = false;
                    }
                    else
                        flag = false;
                }
                //赋值或函数语句
                else{
                    int loc_list;
                    //本块内就存在这个变量
                    if(vl.containsKey(temp_ident.getId())){
                        //非数组型变量
                        if(!this.reglist.get(temp_ident.getId() + this.listnum).getIsArray()){
                            token back = this.tokenList.poll();
                            if(this.tokenList.peek() instanceof operator){
                                if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "=")){
                                    this.tokenList.poll();
                                    dealWithFuncInExp(this.listnum, vl, 0);
                                    flag = isGiveValueOld(temp_ident, this.listnum);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                                else{
                                    this.tokenList.addFirst(back);
                                    flag = isExp(vl, 0, 0, 0, 0, 0);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                            }
                            else flag = false;
                        }
                        //数组型变量
                        else{
                            int x, y;
                            //先获取变量
                            this.tokenList.poll();
                            //处理角标
                            int Array = isArray();
                            if(Array != this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getDemension()) {
                                System.out.println(this.content);
                                //System.out.println(this.answer);
                                System.exit(8);
                            }
                            x = getdemension(Array, 1, vl);
                            //如果是二维
                            if(Array == 2){
                                int temp_n_x = -1, temp_reg_x = -1;
                                int temp_n_y = -1, temp_reg_y = -1;
                                if(t_judge instanceof number n)
                                    temp_n_x = n.getValue();
                                else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                    temp_reg_x = ++this.reg_seq;
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                }
                                else temp_reg_x = this.reg_seq;
                                y = getdemension(Array, 2, vl);
                                if(t_judge instanceof number n)
                                    temp_n_y = n.getValue();
                                else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                    temp_reg_y = ++this.reg_seq;
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                }
                                else temp_reg_y = this.reg_seq;
                                String str_x = temp_n_x == -1 ? ("%" + temp_reg_x) : String.valueOf(temp_n_x);
                                String str_y = temp_n_y == -1 ? ("%" + temp_reg_y) : String.valueOf(temp_n_y);
                                this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getArray_DD());
                                this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getArray_ONE_in_DD(this.reg_seq - 1)).append("i32 " + str_x + ", i32 " +  str_y +"\n");
                                this.reglist.get(temp_ident.getId() + this.listnum).setPresent_use(this.reg_seq);
                            }
                            else{
                                if(this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getIsGlobal()){
                                    dealWithGlobalArray(temp_ident);
                                }
                                int old_seq = this.reg_seq;
                                this.reg_seq++;
                                if(t_judge instanceof number n){
                                    this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getUseaddr() +  ", i32 " + n.getValue() + "\n");
                                }
                                //本来是一个已经被定义过的变量，而不是计算时产生的变量
                                else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                    this.answer.append("    %" + (++this.reg_seq) + " = getelementptr i32, i32* %" + this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getUseaddr() +  ", i32 %" + (this.reg_seq - 1) + "\n");
                                }
                                else{
                                    this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getUseaddr() +  ", i32 %" + old_seq + "\n");
                                }
                                this.reglist.get(temp_ident.getId() + this.listnum).setPresent_use(this.reg_seq);
                            }
                            if(this.tokenList.peek() instanceof operator op){
                                if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "=")){
                                    this.tokenList.poll();
                                    dealWithFuncInExp(this.listnum, vl, 0);
                                    flag = isGiveValueOld(temp_ident, this.listnum);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                                else{
                                    //第一个数组元素其实是表达式的一部分
                                    //将已经计算出的数组元素值，作为一个OP时创造变量，存进去，用于表达式计算
                                    ident id = new ident(getArrayName(), "Ident", 9, 1, 1);
                                    register reg = new register();
                                    //数组型变量在表达式中使用，需要被load出来
                                    reg.setSeq(this.reg_seq);
                                    reg.setIsConst(false);
                                    reg.setHasValue();
                                    reg.setCreatedWhenOp(0);
                                    this.reglist.put(id.getId() + this.listnum, reg);
                                    this.tokenList.addFirst(id);
                                    flag = isExp(vl, 0, 0, 0, 0, 0);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                            }
                            else flag = false;
                        }
                    }
                    //本块内不存在这个变量，查找外层块
                    else if((loc_list = forJudgeNum(temp_ident)) != -1){
                        //非数组型变量
                        System.out.println("Here" + loc_list);
                        if(!this.reglist.get(temp_ident.getId() + loc_list).getIsArray()){
                            token back = this.tokenList.poll();
                            if(this.tokenList.peek() instanceof operator){
                                if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "=")){
                                    this.tokenList.poll();
                                    dealWithFuncInExp(this.listnum, vl, 0);
                                    flag = isGiveValueOld(temp_ident, loc_list);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                                else{
                                    this.tokenList.addFirst(back);
                                    flag = isExp(vl, 0, 0, 0, 0, 0);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                            }
                            else flag = false;
                        }
                        //数组型变量
                        else{
                            int x, y;
                            //先获取变量
                            this.tokenList.poll();
                            //处理角标
                            int Array = isArray();
                            if(Array != this.reglist.get(temp_ident.getId() + loc_list).getDemension()) {
                                System.out.println(this.content);
                                //System.out.println(this.answer);
                                System.exit(8);
                            }
                            x = getdemension(Array, 1, vl);
                            if(Array == 2){
                                int temp_n_x = -1, temp_reg_x = -1;
                                int temp_n_y = -1, temp_reg_y = -1;
                                if(t_judge instanceof number n)
                                    temp_n_x = n.getValue();
                                else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                    temp_reg_x = ++this.reg_seq;
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                }
                                else temp_reg_x = this.reg_seq;
                                y = getdemension(Array, 2, vl);
                                if(t_judge instanceof number n)
                                    temp_n_y = n.getValue();
                                else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                    temp_reg_y = ++this.reg_seq;
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                }
                                else temp_reg_y = this.reg_seq;
                                String str_x = temp_n_x == -1 ? ("%" + temp_reg_x) : String.valueOf(temp_n_x);
                                String str_y = temp_n_y == -1 ? ("%" + temp_reg_y) : String.valueOf(temp_n_y);
                                this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getArray_DD());
                                this.answer.append("    %").append(++this.reg_seq).append(" = ").append(this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getArray_ONE_in_DD(this.reg_seq - 1)).append("i32 " + str_x + ", i32 " +  str_y +"\n");
                                this.reglist.get(temp_ident.getId() + loc_list).setPresent_use(this.reg_seq);
                            }
                            else{
                                if(this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getIsGlobal()){
                                    dealWithGlobalArray(temp_ident);
                                }
                                int old_seq = this.reg_seq;
                                this.reg_seq++;
                                if(t_judge instanceof number n){
                                    this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getUseaddr() +  ", i32 " + n.getValue() + "\n");
                                }
                                //本来是一个已经被定义过的变量，而不是计算时产生的变量
                                else if(t_judge instanceof ident d && this.reglist.containsKey(d.getId()+forJudgeNum(d))){
                                    if(!this.reglist.get(d.getId()+forJudgeNum(d)).getIsGlobal())
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + this.reglist.get(d.getId() + forJudgeNum(d)).getSeq() + "\n");
                                    else
                                        this.answer.append("    %" + this.reg_seq + " = load i32, i32* " + this.reglist.get(d.getId() + forJudgeNum(d)).getGlobalname() + "\n");
                                    this.answer.append("    %" + (++this.reg_seq) + " = getelementptr i32, i32* %" + this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getUseaddr() +  ", i32 %" + (this.reg_seq - 1) + "\n");
                                }
                                else{
                                    this.answer.append("    %" + this.reg_seq + " = getelementptr i32, i32* %" + this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getUseaddr() +  ", i32 %" + old_seq + "\n");
                                }
                                this.reglist.get(temp_ident.getId() + loc_list).setPresent_use(this.reg_seq);
                            }
                            if(this.tokenList.peek() instanceof operator op){
                                if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "=")){
                                    this.tokenList.poll();
                                    dealWithFuncInExp(this.listnum, vl, 0);
                                    flag = isGiveValueOld(temp_ident, loc_list);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                                else{
                                    ident id = new ident(getArrayName(), "Ident", 9, 1, 1);
                                    register reg = new register();
                                    //数组型变量在表达式中使用，需要被load出来
                                    reg.setSeq(this.reg_seq);
                                    reg.setIsConst(false);
                                    reg.setHasValue();
                                    reg.setCreatedWhenOp(0);
                                    this.reglist.put(id.getId() + this.listnum, reg);
                                    this.tokenList.addFirst(id);
                                    flag = isExp(vl, 0, 0, 0, 0, 0);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                            }
                            else flag = false;
                        }
                    }
                    else flag = false;
                }
            }
            //putint、putch
            //part13中加入putarray 和 getarray
            else if(this.tokenList.peek() instanceof function){
                function f = (function)this.tokenList.poll();
               //System.out.println("YYYYYYYY" + f.getFuncName());
                if(this.funclist.containsKey(f.getFuncName())){
                    //System.out.println("houhou");
                    String func_name = f.getFuncName();
                    //如果函数是getarray
                    //且解决的是getarray单独出现的情况
                    //getarray出现在表达式中时，也可以调用下面的方法
                    if(Objects.equals(func_name, "getarray")){
                        flag = deal_getarray(false);
                        if(flag)
                            if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), ";"))
                                this.tokenList.poll();
                            else flag = false;
                    }
                    //处理putint和putch

                    else if(Objects.equals(func_name, "putint") || Objects.equals(func_name, "putch")){
                        dealWithFuncInExp(this.listnum, vl, 1);
                        flag = isExp(vl, 0, 0, 0, 0, 0);
                        if(flag && this.tokenList.peek() instanceof operator){
                            operator temp_opp = (operator) this.tokenList.peek();
                            if(Objects.equals(temp_opp.getOperator(), ";")){
                                if(t_judge instanceof number){
                                    if(Objects.equals(func_name, "putint")){
                                        this.answer.append("    call void @putint(i32 " + ans + ")\n");
                                    }
                                    else{
                                        this.answer.append("    call void @putch(i32 " + ans + ")\n");
                                    }
                                }
                                else if(t_judge instanceof ident id && this.reglist.containsKey(id.getId() + forJudgeNum(id)) && this.reglist.get(id.getId() +forJudgeNum(id)).getCreatedWhenOp() == 0){
                                    if(!this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                                        int s = this.reglist.get(id.getId() + forJudgeNum(id)).getSeq();
                                        this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* %" + s + "\n");
                                    }
                                    else{
                                        String s = this.reglist.get(id.getId() + forJudgeNum(id)).getGlobalname();
                                        this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* " + s + "\n");
                                    }
                                    if(Objects.equals(func_name, "putint")){
                                        this.answer.append("    call void @putint(i32 %" + this.reg_seq + ")\n");
                                    }
                                    else{
                                        this.answer.append("    call void @putch(i32 %" + this.reg_seq + ")\n");
                                    }
                                }
                                else{
                                    if(Objects.equals(func_name, "putint")){
                                        this.answer.append("    call void @putint(i32 %" + this.reg_seq + ")\n");
                                    }
                                    else{
                                        this.answer.append("    call void @putch(i32 %" + this.reg_seq + ")\n");
                                    }
                                }
                                this.tokenList.poll();
                            }
                            else flag = false;
                        }
                        else flag = false;
                    }


                    //处理一般函数,包括putarray
                    //加上对putint和putch的支持
                    else{
                        //返回值为void类型的函数
                        //不会加入表达式
                        System.out.println(f.getFuncName());
                        f = this.ff.get(f.getFuncName());
                        if (Objects.equals(f.getTypeOfRetValue(), "void")) {
                            //System.out.println("LALA");
                            //函数不需要传参
                            if(f.getParams_num() == 0){
                                this.answer.append("    call void @").append(f.getFuncName()).append("()\n");
                            }
                            else{
                                //System.out.println("LALA");
                                flag = deal_withfunc(f, vl, 0);
                                this.answer.append("    call void @").append(f.getFuncName()).append("(").append(this.funcstr.toString()).append(")\n");
                            }
                        }
                        //返回值为int类型
                        else{
                            if(f.getParams_num() == 0){
                                this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @").append(f.getFuncName()).append("()\n");
                            }
                            else{
                                flag = deal_withfunc(f, vl, 0);
                                this.answer.append("    %").append(++this.reg_seq).append(" = call i32 @").append(f.getFuncName()).append("(").append(this.funcstr.toString()).append(")\n");
                            }
                        }
                    }
                }
                else flag= false;
            }
            //if else statement
            //new block begin
            else if(this.tokenList.peek() instanceof cond){
                cond temp = (cond)this.tokenList.poll();
                if(Objects.equals(temp.getCondid(), "if")){
                    flag = isIf(mark_isElseIf, temp.getTabnums(), vl, is_one);
                    if(mark_isElseIf == 1) return flag;
                }
                else if(Objects.equals(temp.getCondid(), "else if")){
                    flag = isElseIf(1);
                }
                else{
                    flag = isElse(0);
                }
            }
            else if(this.tokenList.peek() instanceof circulation){
                circulation cir = (circulation) this.tokenList.poll();
                flag = isWhile(cir, vl);
            }
            //一行只有一个表达式
            else if(this.tokenList.peek() instanceof number){
                flag = isExp(vl, 0, 0, 0, 0, 0);
                if(flag && this.tokenList.peek() instanceof operator){
                    operator temp_opp = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_opp.getOperator(), ";")){
                        this.tokenList.poll();
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else if(this.tokenList.peek() instanceof operator){
                operator op = (operator) this.tokenList.peek();
                //part10 ;
                if(Objects.equals(op.getOperator(), ";"))
                    this.tokenList.poll();
                else if(Objects.equals(op.getOperator(), "+") || Objects.equals(op.getOperator(), "-") || Objects.equals(op.getOperator(), "(")){
                    flag = isExp(vl, 0, 0, 0, 0, 0);
                    if(flag && this.tokenList.peek() instanceof operator){
                        operator temp_opp = (operator) this.tokenList.peek();
                        if(Objects.equals(temp_opp.getOperator(), ";")){
                            this.tokenList.poll();
                        }
                        else flag = false;
                    }
                    else flag = false;
                }
                //标志着一个块的开始
                //if else elsif块除外
                else if(Objects.equals(op.getOperator(), "{")){
                    isLbrace(3, 0,0, false, false);
                }
                //标志着一个块儿的结束
                else if(Objects.equals(op.getOperator(), "}")){
                    this.tokenList.poll();
                    //main块结束
                    if(is_one == 0){
                        this.answer.append("}");
                        return isEnd();
                    }
                    //if块结束
                    else if(is_one == 1){
                        if(hasFollowingElse){
                            if(mr == 1){
                                return flag;
                            }
                            else{
                                if(!has_jump)
                                    this.answer.append("    br label %").append(this.three.peek().getDst() + "\n");
                            }
                        }
                        else{
                            if(mr == 1){
                                if(this.three.size() <= 2){
                                    this.answer.append(this.three.pop().getDst()).append(":").append("\n");
                                    return flag;
                                }
                                else{
                                    this.three.pop();
                                }
                            }
                            else{
                                if(this.three.size() <= 2){
                                    if(!has_jump)
                                        this.answer.append("    br label %").append(this.three.peek().getDst() + "\n");
                                    this.answer.append(this.three.pop().getDst()).append(":").append("\n");
                                }
                                else{
                                    this.three.pop();
                                }
                            }
                        }
                        this.deletelist(key_sub_varlist);
                    }
                    //else 块结束
                    else if(is_one == 2){
                        if(mr == 1){
                            this.answer.append(this.three.pop().getDst()).append(":").append("\n");
                        }
                        else{
                            if(!has_jump)
                                this.answer.append("    br label %").append(this.three.peek().getDst() + "\n");
                            this.answer.append(this.three.pop().getDst()).append(":").append("\n");
                        }
                        this.deletelist(key_sub_varlist);
                    }
                    //普通块结束
                    else if(is_one == 3){
                        if(mr == 1){
                            this.answer.append("}");
                            System.exit(0);
                        }
                        else{
                            this.deletelist(key_sub_varlist);
                        }
                    }
                    //while块结束
                    else if(is_one == 4){
                        //while调回条件判断
                        this.answer.append("    br label %").append(while_info.peek().getCond_target()).append("\n");
                        //结束while块，开始剩余部分
                        //同时该while实例弹出
                        this.answer.append(while_info.pop().getAnti_cond_target()).append(":\n");
                        this.deletelist(key_sub_varlist);
                    }
                    //函数定义块结束
                    else if(is_one == 5 && isfuncdecl){
                        if(Objects.equals(this.func_now.getTypeOfRetValue(), "void"))
                            this.answer.append("    ret void\n");
                        this.answer.append("}\n");
                        //this.answer_decl.append(this.answer.toString().substring(func_sign));
                        //this.answer.delete(func_sign, this.answer.length());
                        //func_sign = 0;
                        this.deletelist(key_sub_varlist);
                        this.deletelist_inparams();
                        this.reg_seq = 0;
                    }
                    return flag;
                }
                else flag = false;
            }
            else flag = false;
        }
        return flag;
    }
    //处理函数调用
    public boolean deal_withfunc(function f, HashMap<String, Integer> vl, int is_funcInfunc){
        boolean flag = true;
        int cnt = 0, j = 0, n = f.getParams_num();
        //System.out.println("number: " + n);
        LinkedList<String> temp = f.getFuncVar();
        this.funcstr.delete(0, funcstr.length());
        if(this.tokenList.peek() instanceof operator op && Objects.equals(op.getOperator(), "(")){
            //弹出"(“
            //System.out.println("kokokoko");
            this.tokenList.poll();
            forBug(this.tokenList);
            while(cnt < n && flag){
                if(cnt > 0)
                    this.tokenList.poll();
                dealWithFuncInExp(this.listnum, vl, 1);
                flag = isExp(vl, 0, 0, cnt == n - 1 ? 1 : 0, is_funcInfunc, 1);
                //System.out.println(this.answer);
                //forBug();
                if(flag){
                    if(t_judge instanceof number num){
                        if(Objects.equals(temp.get(j), "one_var")){
                            cnt++;
                            j++;
                            this.funcstr.append("i32 ").append(num.getValue()).append(", ");
                        }
                        else flag= false;
                    }
                    else if(t_judge instanceof ident id && this.reglist.containsKey(id.getId() + forJudgeNum(id))){
                        //System.out.println(temp.get(j));
                        if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray() && this.reglist.get(id.getId() + forJudgeNum(id)).getDemension() == 1 && Objects.equals(temp.get(j), "one_Array")){
                            cnt++;
                            j++;
                            this.funcstr.append("i32* %").append(this.reg_seq).append(", ");
                        }
                        else if(this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray() && this.reglist.get(id.getId() + forJudgeNum(id)).getDemension() == 2 && Objects.equals(temp.get(j), "two_Array")){
                            cnt++;
                            j++;
                            this.funcstr.append("i32* %").append(this.reg_seq).append(", ");
                        }
                        else if(!this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray() && Objects.equals(temp.get(j), "one_var")){
                            cnt++;
                            j++;
                            if(!this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                                this.answer.append("    %").append(++this.reg_seq).append(" = load i32, i32* %").append(this.reglist.get(id.getId() + forJudgeNum(id)).getSeq()).append("\n");
                                this.funcstr.append("i32 %").append(this.reg_seq).append(", ");
                            }
                            else{
                                String s = this.reglist.get(id.getId() + forJudgeNum(id)).getGlobalname();
                                this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* " + s + "\n");
                                this.funcstr.append("i32 %").append(this.reg_seq).append(", ");
                            }
                        }
                        else{
                            flag= false;
                        }
                    }
                    else{
                        if(Objects.equals(temp.get(j), "one_var")){
                            cnt++;
                            j++;
                            this.funcstr.append("i32 %").append(this.reg_seq).append(", ");
                        }
                    }
                }
            }
        }
        else flag = false;
        funcstr.delete(funcstr.length() - 2, funcstr.length());
        return flag;
    }
    //以逗号分隔
    /*
    public boolean judge_isParamsNumLegal(int n){
        int cnt = 0, i = 0;
        //先判断没有参数的情况
        if(this.tokenList.get(0) instanceof operator op && Objects.equals(op.getOperator(), "(")
                && this.tokenList.get(0) instanceof operator opp && Objects.equals(opp.getOperator(), ")")){
            cnt = 0;
        }
        else{
            //v1 -> 逗号分隔，)结束
            while(true){
                if(this.tokenList.get(i) instanceof operator op && Objects.equals(op.getOperator(), ")"))
                {
                    if(this.tokenList.get(i - 1) instanceof operator opp && !Objects.equals(opp.getOperator(), "(")){
                        break;
                    }
                }
                else if(this.tokenList.get(i) instanceof operator opp && Objects.equals(opp.getOperator(), ","))
                    cnt++;
                i++;
            }
            cnt += 1;
        }
        return cnt == n;
    }*/
    public boolean isReturn(HashMap<String, Integer> sub_var_list){
        boolean flag = true;
        if(this.tokenList.peek() instanceof ident){
            if(Objects.equals(((ident) this.tokenList.peek()).getId(), "return")){
                this.tokenList.poll();
                int old_value = this.reg_seq;
                dealWithFuncInExp(this.listnum, sub_var_list, 0);
                flag = isExp(sub_var_list, 0, 0, 0, 0, 0);
                if(this.tokenList.peek() instanceof operator && flag){
                    operator temp_op_2 = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_op_2.getOperator(), ";")){
                        this.tokenList.poll();
                        if(t_judge instanceof number){
                            this.answer.append("    ret i32 ");
                            this.answer.append(ans).append("\n");
                        }
                        else if(t_judge instanceof ident id && this.reglist.containsKey(id.getId() + forJudgeNum(id)) && this.reglist.get(id.getId() + forJudgeNum(id)).getCreatedWhenOp() == 0){
                            if(!this.reglist.get(id.getId() + forJudgeNum(id)).getIsArray()){
                                if(!this.reglist.get(id.getId() + forJudgeNum(id)).getIsGlobal()){
                                    int s = this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getSeq();
                                    this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* %" + s + "\n");
                                    this.answer.append("    ret i32 %" + this.reg_seq).append("\n");
                                }
                                else{
                                    String s = this.reglist.get(id.getId() + forJudgeNum(id)).getGlobalname();
                                    this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* " + s + "\n");
                                    this.answer.append("    ret i32 %" + this.reg_seq).append("\n");
                                }
                            }
                            else{
                                int s = this.reglist.get(id.getId() + forJudgeNum(id)).getPresent_use();
                                this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* %" + s + "\n");
                                this.answer.append("    ret i32 %" + this.reg_seq).append("\n");

                            }
                        }
                        else{
                            this.answer.append("    ret i32 %" + this.reg_seq).append("\n");
                        }
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isEnd(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "#")){
                return true;
            }
        }
        return false;
    }
    public StringBuilder getAnswer(){return this.answer;}
    public void setContent(String c){this.content = c;}
    public void forBug(LinkedList<token> l){
        int i;
        System.out.println("Yeah");
        for(i = 0; i < l.size(); i++){
            if(l.get(i) instanceof operator op){
                System.out.print(op.getOperator());
            }
            else if(l.get(i) instanceof ident id){
                System.out.print(id.getId());
            }
            else if(l.get(i) instanceof number nu){
                System.out.print(nu.getValue());
            }
            else if(l.get(i) instanceof function func){
                System.out.print(func.getFuncName());
            }
        }
        System.out.println();
    }
}
