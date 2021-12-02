public class circulation extends token{
    private String cond_target;
    private String prefer_target;
    private String anti_target;
    public circulation(String t, int s){
        super(t, s);
    }

    public void setCond_target(String cond_target) {
        this.cond_target = cond_target;
    }
    public void setAnti_cond_target(String anti_cond_target) {
        this.anti_target = anti_cond_target;
    }
    public String getCond_target(){return this.cond_target;}
    public String getAnti_cond_target(){return this.anti_target;}
    public void setPrefer_target(String pt){this.prefer_target = pt;}
    public String getPrefer_target(){return this.prefer_target;}
}
