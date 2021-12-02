import javax.sound.midi.SysexMessage;
import java.util.*;

public class Grammar {
    private LinkedList<token> tokenList;
    private static Grammar grammar;
    private StringBuilder answer = new StringBuilder();

    //主函数块表
    private HashMap<String, Integer> funclist = new HashMap<>();
    private HashMap<String, register> reglist = new HashMap<>();
    private Stack<String> strblockeach = new Stack<>();
    private Stack<dstANDstr> three = new Stack<>();
    private expression exper;
    public LinkedList<token> expList = new LinkedList<>();
    //part8,Hashmap储存不同块符号表的键值
    private HashMap<String, HashMap<String, Integer>> blocklist = new HashMap<>();
    //pary10 储存每个while的信息栈
    private Stack<circulation> while_info = new Stack<>();
    //不同块符号表名称(键值)
    //同时也标记main函数的
    private int listnum = 0;
    private int reg_seq = 0;
    private int ans = 0;
    private String label = "x";
    private String label_while = "y";
    private int labelseq = 1;
    private int labelseq_while = 1;
    private token t_judge;
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
    public void checkForFunc(){
        int i;
        for(i = 0; i < this.tokenList.toArray().length; i++){
            token temp = this.tokenList.get(i);
            if(temp instanceof function){
                if(Objects.equals(((function) temp).getFuncName(), "getint") && !this.funclist.containsKey("getint")){
                    this.answer.append("declare i32 @getint()\n");
                    this.funclist.put("getint", 1);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "getch") && !this.funclist.containsKey("getch")){
                    this.answer.append("declare i32 @getch()\n");
                    this.funclist.put("getch", 2);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "putint") && !this.funclist.containsKey("putint")){
                    this.answer.append("declare void @putint(i32)\n");
                    this.funclist.put("putint", 3);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "putch") && !this.funclist.containsKey("putch")){
                    this.answer.append("declare void @putch(i32)\n");
                    this.funclist.put("putch", 4);
                }
            }
        }
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
                }
                else{
                    this.tokenList.addFirst(temp);
                    addRbrace();

                    flag = 2;
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
        return setNewVarlist_global();
    }
    public boolean isGivenOldValue_Global(ident obj, int target) {
        boolean flag = true;
        HashMap<String, Integer> vl = this.blocklist.get("var" + target);
        String name = obj.getId() + target;
        //变量赋值
        if (!this.reglist.get(name).getIsConst() && isExp(vl, 1)) {
            register reg = this.reglist.get(obj.getId() + target);
            reg.setValueOfReg(ans);
            reg.setHasValue();
            reg.setGlobalname("@" + obj.getId());
        }
        //未赋值的常量赋值
        else if (this.reglist.get(name).getIsConst() && !this.reglist.get(name).getHasValue() && isExp(vl, 1)) {
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
        if(vl.isEmpty() || !vl.containsKey(temp_token.getId())){
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
            }
            else if(this.tokenList.peek() instanceof operator){
                operator temp_op = (operator) this.tokenList.poll();
                //全局变量去结束，进入函数区
                if(Objects.equals(temp_op.getOperator(), "}")){
                    //this.blocklist.remove(key0);
                    checkForFunc();
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
                flag = isLbrace(0, 0, 0, false);
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    //块中出现return，则置mark_return 为1
    //part8, {为块开始的标志，初始化一个符号表varlist;
    //会出现多次递归调用Lbrace
    public boolean isLbrace(int is_one, int mark_return, int mark_isElseIf, boolean hasFollowingElse){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "{")){
                if(is_one == 0){
                    this.answer.append("{\n");
                }
                else if(is_one == 1){
                    this.answer.append("\n");
                }
                this.tokenList.poll();
                //块起始
                //获得一个新的变量表
                String key1 = setNewVarlist();
                //进入块中
                flag = isStmt(is_one, mark_return, mark_isElseIf, hasFollowingElse, key1);
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isExp(HashMap<String, Integer> vl, int is_global){
        boolean flag = true;
        token check = null;
        this.expList.clear();
        if(is_global == 0){
            while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident || this.tokenList.peek() instanceof function)){
                check = this.tokenList.peek();
                this.expList.offer(this.tokenList.poll());
                if(check instanceof operator){
                    if(((operator) check).getOperator().charAt(0) == ';'|| ((operator) check).getOperator().charAt(0) == ',') break;
                }
            }
        }
        else{
            while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || (this.tokenList.peek() instanceof ident id1 && this.reglist.get(id1.getId() + 0).getIsConst() && this.reglist.get(id1.getId() + 0).getHasValue()))){
                check = this.tokenList.peek();
                this.expList.offer(this.tokenList.poll());
                if(check instanceof operator){
                    if(((operator) check).getOperator().charAt(0) == ';'|| ((operator) check).getOperator().charAt(0) == ',') break;
                }
            }
        }
        if(check instanceof operator && (Objects.equals(((operator) check).getOperator(), ";") || Objects.equals(((operator) check).getOperator(), ","))){
            String temp_ans = this.answer.toString();
            exper.getExp(expList);
            exper.setFinal_layer(this.listnum);
            exper.getMap(vl, reglist, reg_seq, answer);
            flag = exper.dealExp(((operator) check).getOperator().charAt(0), expList, false);
            if(flag){
                this.ans = exper.passAns();
                this.reg_seq = exper.passRegSeq();
                this.t_judge = exper.forJudge();
                this.tokenList.addFirst(check);
                if(is_global == 0) this.answer = exper.getAns();
                else{
                    this.answer.delete(0, this.answer.length());
                    this.answer.insert(0, temp_ans);
                }
                exper.clearExp();
            }
        }
        else flag = false;
        return flag;
    }
    public boolean isGiveValueOld(ident obj, int target){
        boolean flag = true;
        int old_value = this.reg_seq;
        HashMap<String, Integer> vl = this.blocklist.get("var" + target);
        String name = obj.getId() + target;
        //变量赋值
        if(!this.reglist.get(name).getIsConst() && isExp(vl, 0)){
            register reg = this.reglist.get(obj.getId() + target);
            reg.setValueOfReg(ans);
            reg.setHasValue();
            this.reglist.get(name).setHasValue();
            if(this.reg_seq == old_value){
                if(t_judge instanceof number){
                    if(!this.reglist.get((obj).getId() + forJudgeNum(obj)).getIsGlobal())
                        this.answer.append("    store i32 ").append(ans).append(", i32* %").append(reg.getSeq()).append("\n");
                    else
                        this.answer.append("    store i32 ").append(ans).append(", i32* ").append(this.reglist.get(obj.getId() + forJudgeNum(obj)).getGlobalname()).append("\n");
                }
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
                if(!this.reglist.get((obj).getId() + forJudgeNum(obj)).getIsGlobal())
                    this.answer.append("    store i32 %").append(this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                else
                    this.answer.append("    store i32 %").append(this.reg_seq).append(", i32* ").append(this.reglist.get((obj).getId() + forJudgeNum(obj)).getGlobalname()).append("\n");
            }
        }
        //未赋值的常量赋值
        else if(this.reglist.get(name).getIsConst() && !this.reglist.get(name).getHasValue() && isExp(vl, 0)){
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
    public boolean isIndentifyNew(boolean is_const, HashMap<String, Integer> vl){
        boolean flag = true;
        if(this.tokenList.peek() instanceof ident){
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
                        token f = this.tokenList.peek();
                        if(f instanceof function){
                            if(Objects.equals(((function) f).getFuncName(), "getint")){
                                this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getint()\n");
                                this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(this.reglist.get(temp_token.getId() + this.listnum).getSeq()).append("\n");
                                this.reglist.get(temp_token.getId() + this.listnum).setHasValue();
                            }
                            else if(Objects.equals(((function) f).getFuncName(), "getch")){
                                this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getch()\n");
                                this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(this.reglist.get(temp_token.getId() + this.listnum).getSeq()).append("\n");
                                this.reglist.get(temp_token.getId() + this.listnum).setHasValue();
                            }
                            else{
                                flag = false;
                            }
                            this.tokenList.poll();
                            this.tokenList.poll();
                            this.tokenList.poll();
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
                        else{
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
    //mark_isElseIf = 1表示是elseif
    //mark_isElseIf = 0表示是if
    public boolean isCondExp(int mark_isElseIf, boolean hasFollowingElse, HashMap<String, Integer> vl){
        boolean flag = true;
        token check = null;
        this.expList.clear();
        while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident)){
            check = this.tokenList.peek();
            this.expList.offer(this.tokenList.poll());
            if(check instanceof operator){
                if(((operator) check).getOperator().charAt(0) == '{') break;
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
                        if(this.three.isEmpty()){
                            addLabel(1); String temp1 = label;
                            addLabel(1); String temp2 = label;
                            dstANDstr temp = new dstANDstr(temp1, "x0", temp2);
                            this.three.push(temp);
                            this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(temp1).append(", label %").append(temp2);
                        }
                        else{
                            addLabel(1); String temp1 = label;
                            dstANDstr temp = new dstANDstr(temp1, this.three.peek().getDst(), this.three.peek().getDst());
                            this.three.push(temp);
                            this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(temp1).append(", label %").append(this.three.peek().getDst());
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
        else flag = false;
        return flag;
    }
    public boolean isCondExp_while(circulation cir, HashMap<String, Integer> vl){
        boolean flag = true;
        token check = null;
        this.expList.clear();
        while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident)){
            check = this.tokenList.peek();
            this.expList.offer(this.tokenList.poll());
            if(check instanceof operator){
                if(((operator) check).getOperator().charAt(0) == '{') break;
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
    public boolean isIf(int mark_isElseIf, int tab, HashMap<String, Integer> vl){
        boolean flag = true;
        //if中表达式处理，包括两个跳转地址压栈、一个块跳转地址压栈
        //设置three
        boolean hasFollowingElse = checkFollowingElse(tab);
        flag = isCondExp(mark_isElseIf, hasFollowingElse, vl);
        if(flag){
            this.strblockeach.push(this.three.peek().getIf_seq());
            flag = isLbrace(1, 0, mark_isElseIf, hasFollowingElse);
        }
        return flag;
    }
    public boolean isElseIf(int mark_isElseIf){
        boolean flag = true;
        this.strblockeach.push(this.three.peek().getElse_seq());
        //boolean hasFollowingElse = checkFollowingElse();
        flag = isStmt(1, 0, mark_isElseIf, false, "var"+this.listnum);
        return flag;
    }
    public boolean isElse(int mark_isElseIf){
        boolean flag = true;
        //进入新的块
        this.strblockeach.push(this.three.peek().getElse_seq());
        flag = isLbrace(2, 0, mark_isElseIf, false);
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
            flag = isLbrace(4, 0,0, false);
        }
        return flag;
    }
    public boolean isStmt(int is_one, int mark_return, int mark_isElseIf, boolean hasFollowingElse, String key_sub_varlist){
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
                        token back = this.tokenList.poll();
                        if(this.tokenList.peek() instanceof operator){
                            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "=")){
                                this.tokenList.poll();
                                token f = this.tokenList.peek();
                                if(f instanceof function){
                                    if(Objects.equals(((function) f).getFuncName(), "getint")){
                                        this.reglist.get(temp_ident.getId() + this.listnum).setHasValue();
                                        this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getint()\n");
                                        this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_ident.getId() + this.listnum).getSeq() + "\n");
                                        this.reglist.get(temp_ident.getId() + this.listnum).setHasValue();
                                    }
                                    else if(Objects.equals(((function) f).getFuncName(), "getch")){
                                        this.reglist.get(temp_ident.getId() + this.listnum).setHasValue();
                                        this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getch()\n");
                                        this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_ident.getId() + this.listnum).getSeq() + "\n");
                                        this.reglist.get(temp_ident.getId() + this.listnum).setHasValue();
                                    }
                                    else{
                                        flag = false;
                                    }
                                    this.tokenList.poll();
                                    this.tokenList.poll();
                                    this.tokenList.poll();
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
                            }
                            else{
                                this.tokenList.addFirst(back);
                                flag = isExp(vl, 0);
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
                    //本块内不存在这个变量，查找外层块
                    else if((loc_list = forJudgeNum(temp_ident)) != -1){
                        token back = this.tokenList.poll();
                        if(this.tokenList.peek() instanceof operator){
                            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "=")){
                                this.tokenList.poll();
                                token f = this.tokenList.peek();
                                if(f instanceof function){
                                    if(Objects.equals(((function) f).getFuncName(), "getint")){
                                        this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getint()\n");
                                        if(!this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getIsGlobal())
                                            this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_ident.getId() + loc_list).getSeq() + "\n");
                                        else
                                            this.answer.append("    store i32 %" + this.reg_seq + ", i32* " + this.reglist.get(temp_ident.getId() + 0).getGlobalname() + "\n");
                                        this.reglist.get(temp_ident.getId() + loc_list).setHasValue();
                                    }
                                    else if(Objects.equals(((function) f).getFuncName(), "getch")){
                                        this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getch()\n");
                                        if(!this.reglist.get(temp_ident.getId() + forJudgeNum(temp_ident)).getIsGlobal())
                                            this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_ident.getId() + loc_list).getSeq() + "\n");
                                        else
                                            this.answer.append("    store i32 %" + this.reg_seq + ", i32* " + this.reglist.get(temp_ident.getId() + 0).getGlobalname() + "\n");
                                        this.reglist.get(temp_ident.getId() + loc_list).setHasValue();
                                    }
                                    else{
                                        flag = false;
                                    }
                                    this.tokenList.poll();
                                    this.tokenList.poll();
                                    this.tokenList.poll();
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
                            }
                            else{
                                this.tokenList.addFirst(back);
                                flag = isExp(vl, 0);
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
                    else flag = false;
                }
            }
            //putint、putch
            else if(this.tokenList.peek() instanceof function){
                function f = (function)this.tokenList.poll();
                String func_name = f.getFuncName();
                int old_value = this.reg_seq;
                flag = isExp(vl, 0);
                if(flag && this.tokenList.peek() instanceof operator){
                    operator temp_opp = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_opp.getOperator(), ";")){
                        if(this.reg_seq == old_value){
                            if(t_judge instanceof number){
                                if(Objects.equals(func_name, "putint")){
                                    this.answer.append("    call void @putint(i32 " + ans + ")\n");
                                }
                                else{
                                    this.answer.append("    call void @putch(i32 " + ans + ")\n");
                                }
                            }
                            else if(t_judge instanceof ident){
                                if(!this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getIsGlobal()){
                                    int s = this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getSeq();
                                    this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* %" + s + "\n");
                                }
                                else{
                                    String s = this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getGlobalname();
                                    this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* " + s + "\n");
                                }
                                if(Objects.equals(func_name, "putint")){
                                    this.answer.append("    call void @putint(i32 %" + this.reg_seq + ")\n");
                                }
                                else{
                                    this.answer.append("    call void @putch(i32 %" + this.reg_seq + ")\n");
                                }
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
            //if else statement
            //new block begin
            else if(this.tokenList.peek() instanceof cond){
                cond temp = (cond)this.tokenList.poll();
                if(Objects.equals(temp.getCondid(), "if")){
                    flag = isIf(mark_isElseIf, temp.getTabnums(), vl);
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
                flag = isExp(vl, 0);
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
                    flag = isExp(vl, 0);
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
                    isLbrace(3, 0,0, false);
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
                                if(this.three.size() < 2){
                                    return flag;
                                }
                                else{
                                    this.three.pop();
                                }
                            }
                            else{
                                if(this.three.size() < 2){
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
                    return flag;
                }
                else flag = false;
            }
            else flag = false;
        }
        return flag;
    }
    public boolean isReturn(HashMap<String, Integer> sub_var_list){
        boolean flag = true;
        if(this.tokenList.peek() instanceof ident){
            if(Objects.equals(((ident) this.tokenList.peek()).getId(), "return")){
                this.tokenList.poll();
                int old_value = this.reg_seq;
                flag = isExp(sub_var_list, 0);
                if(this.tokenList.peek() instanceof operator && flag){
                    operator temp_op_2 = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_op_2.getOperator(), ";")){
                        this.tokenList.poll();
                        if(this.reg_seq == old_value){
                            if(t_judge instanceof number){
                                this.answer.append("    ret i32 ");
                                this.answer.append(ans).append("\n");
                            }
                            else if(t_judge instanceof ident){
                                int s = this.reglist.get(((ident) t_judge).getId() + forJudgeNum((ident)t_judge)).getSeq();
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
}
