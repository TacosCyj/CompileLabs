import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Analysis {
    public static void main(String[] args) throws IOException {
        StringBuilder input_file = new StringBuilder();
        StringBuilder output_file = new StringBuilder();
        LinkedList<token> tokenlist;
        input_file.append(args[0]);
        output_file.append(args[1]);
        Lexer lexer = Lexer.getLexerInstance();
        lexer.setFile(input_file);
        lexer.getContent();
        if(lexer.lexerAnalysis()){
            tokenlist = lexer.getTokenList();
            operator o = new operator("#", "Op", 28);
            tokenlist.offer(o);
            Grammar grammar = Grammar.getInstance();
            expression exp = expression.getInstance();
            grammar.setTokenList(tokenlist);
            grammar.setExper(exp);
            grammar.checkForFunc();
            boolean flag = grammar.isInt();
            if(flag){
                System.out.println(lexer.getcon());
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
                System.exit(3);
            }
        }
        else{
            System.exit(2);
        }
    }
}
