import javax.sound.midi.SysexMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Analysis {
    public static void main(String[] args) throws IOException {
        StringBuilder input_file = new StringBuilder();
        StringBuilder output_file = new StringBuilder();
        LinkedList<token> tokenlist;
        input_file.append("E:\\编译原理\\lab3\\testfile\\c.txt");
        output_file.append("E:\\编译原理\\lab3\\testfile\\b.txt");
        Lexer lexer = Lexer.getLexerInstance();
        lexer.setFile(input_file);
        lexer.getContent();
        if(lexer.lexerAnalysis()){
            tokenlist = lexer.getTokenList();
            operator o = new operator('#', "Op", 28);
            tokenlist.offer(o);
            Grammar grammar = Grammar.getInstance();
            expression exp = expression.getInstance();
            grammar.setTokenList(tokenlist);
            grammar.setExper(exp);
            grammar.checkForFunc();
            boolean flag = grammar.isInt();
            if(flag){
                try{
                    FileWriter writer = new FileWriter(String.valueOf(output_file));
                    writer.write(grammar.getAnswer().toString());
                    writer.flush();
                    writer.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                System.exit(0);
            }
            else{
                for(int i = 0; i < tokenlist.toArray().length; i++){
                    token t = tokenlist.get(i);
                    if(t instanceof ident) System.out.println(((ident) t).getId());
                    else if(t instanceof  function) System.out.println(((function) t).getFuncName());
                    else if(t instanceof number) System.out.println(((number) t).getValue());
                    else if(t instanceof operator) System.out.println(((operator) t).getOperator());
                }
                System.exit(3);
            }
        }
        else{
            System.exit(2);
        }
    }
}
