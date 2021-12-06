import java.util.concurrent.SynchronousQueue;

public class register {
    //当储存的变量类型为数组时，这个值是数组的首地址
    private int seq = 0;
    private int valueofreg;
    private int CreatedWhenOp = 0;
    private String globalname;

    private Boolean hasValue = false;
    private Boolean isConst;
    private Boolean isGlobal = false;
    private Boolean isArray = false;
    private int x_d = 0;
    private int y_d = 0;
    private int size = 0;
    private int firstaddr = 0;
    private int useaddr = 0;
    private int demension = 0;

    //储存数组元素赋值时，在计算表达式前，确定的目标元素的角标
    private int present_use = 0;

    public void setSeq(int s){this.seq = s;}
    public void setHasValue(){this.hasValue = true;}
    public Boolean getHasValue(){return this.hasValue;}
    public void setValueOfReg(int v){this.valueofreg = v;}
    public int getSeq(){return this.seq;}
    public void setIsConst(Boolean c){this.isConst = c;}
    public Boolean getIsConst(){return this.isConst;}
    public int getValueofreg(){return this.valueofreg;}
    public void setCreatedWhenOp(int c){this.CreatedWhenOp = c;}
    public int getCreatedWhenOp(){return this.CreatedWhenOp;}
    public void setIsGlobal(Boolean f){this.isGlobal = f;}
    public Boolean getIsGlobal(){return this.isGlobal;}
    public void setGlobalname(String g){this.globalname = g;}
    public String getGlobalname(){return this.globalname;}
    public void setIsArray(boolean t){this.isArray = t;}
    public boolean getIsArray(){return this.isArray;}
    public void setX_d(int x){this.x_d = x;}
    public void setY_d(int y){this.y_d = y;}
    public int getX_d() {return x_d;}
    //如果是一维数组，则y_d设置为0
    public int getY_d() {return y_d;}
    private void setFirstaddr(int fa){this.firstaddr = fa;}
    public int getFirstaddr(){return this.firstaddr;}
    public void setDemension(int d){this.demension = d;}
    public int getDemension(){return this.demension;}
    public int getUseaddr(){return this.useaddr;}
    public void setUseaddr(int ua){this.useaddr = ua;}
    public void setPresent_use(int pu){this.present_use = pu;}
    public int getPresent_use(){return this.present_use;}
    //初始化一个数组
    public String initArray(){
        if(this.demension == 1){
            if(!isGlobal){
                return  "    %" + this.seq + " = alloca [" + x_d + " x i32]\n";
            }
            else{
                return " = dso_local " + (isConst ? "constant" : "global") + " [" + x_d + " x i32] ";
            }
        }
        else{
            if(!isGlobal){
                return  "    %" + this.seq + " = alloca [" + x_d +" x [" + y_d + " x i32]]\n";
            }
            else{
                return " = dso_local " + (isConst ? "constant" : "global") + " [" + x_d +" x [" + y_d + " x i32]] ";
            }
        }

    }
    //
    public String getArray_firstaddr(int p){
        this.firstaddr = p;
        if(this.isArray){
            //二维数组
            if(this.demension == 2)
                return "getelementptr [" + x_d + " x [" + y_d + " x i32]], [" + x_d + " x [" + y_d + " x i32]]* %" + seq +", i32 0, i32 0\n";
            //一维数组
            else
                return "getelementptr [" + x_d + " x i32], [" + x_d +" x i32]* %" + seq +", i32 0, i32 0\n";
        }
        else return "";
    }
    public String getArray_DD(){
        return "getelementptr [" + x_d + " x [" + y_d + " x i32]], [" + x_d + " x [" + y_d + " x i32]]* " + (isGlobal ? globalname : "%" + seq) +", i32 0, i32 0\n";
    }
    public String getArray_ONE_in_DD(int p){
        return "getelementptr [" + y_d + " x i32], [" + y_d +" x i32]* %" + p + ", ";
    }

    public String getArray_useaddr(int p){
        if(this.demension == 1){
            this.useaddr = this.firstaddr;
            return "";
        }
        else{
            this.useaddr = p;
            return "    %" + this.useaddr + " = getelementptr [" + y_d +" x i32], [" +  y_d +" x i32]* %" + this.firstaddr +", i32 0, i32 0\n";
        }
    }
    //传入的x,y是角标
    public String getArray_certainaddr(int x, int y){
        //二维数组
        if(this.demension == 2){
            if(x < 0 || y < 0 || x >= x_d || y >= y_d) System.exit(9);
            return " getelementptr i32, i32* %" +  this.useaddr +", i32 " + (x * y_d + y) + "\n";
        }
        else{
            if(y < 0 || y >= x_d) System.exit(9);
            return " getelementptr i32, i32* %" +  this.useaddr +", i32 " + y + "\n";
        }
    }
    public String memsetStep(){
        return "    call void @memset(i32* %" +  this.useaddr +", i32 0, i32 " + (x_d * y_d * 4) +")\n";
    }
}
