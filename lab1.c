#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<ctype.h>
#define LEN 1024

char input_file[LEN];
char output_file[LEN];
char buffer[LEN];
char content[LEN];
char result[LEN];
char token[20];
FILE* fp_r, *fp_w;

int symbol;
char* pmove;

void error(){
    exit(1);
}

void init(){
    pmove = content;
}


/*消除单行注释功能*/
void getsym(){
    if(pmove == NULL) return;
    int loc = 0;
    symbol = -1;
    memset(token, 0, sizeof(token));
    while(*pmove == ' ' || *pmove == '\t' || *pmove == '\n') pmove++;
    if(*pmove == '/'){
        int flag = 0;
        char* pp = pmove + 1;
        if(*pp == '/'){
            while(*pp != '\n'){
                pp++;
                if(pp == NULL) break;
            }
            pmove = pp;
            getsym();
        }
        else if(*pp == '*'){
            char* pmove_temp = pmove;
            while(*pp != '/' || *pmove_temp != '*'){
                pp++;
                pmove_temp++;
                if(*pp == '#'){
                    flag = 1;
                    break;
                }
            }
            if(flag == 1) error();
            pmove = pp + 1;
            getsym();
        }
        else error();
    }
    else if(isdigit(*pmove)){
        while(isdigit(*pmove) || (loc == 1 && (*pmove == 'x' || *pmove == 'X')) || (loc > 1 && isalpha(*pmove))){
            token[loc++] = *pmove;
            pmove++;
            if(pmove == NULL) break;
        }
        symbol = 10;
    }
    else if(isalpha(*pmove)){
        while(isalpha(*pmove)){
            token[loc++] = *pmove;
            pmove++;
            if(pmove == NULL) break;
        }
        if(strcmp(token, "int") == 0) symbol = 11;
        else if(strcmp(token, "main") == 0) symbol = 12;
        else if(strcmp(token, "return") == 0) symbol = 13;
        else{
            error();
        }
    }
    else{
        switch(*pmove){
            case '(': token[loc] = '('; symbol = 14; break;
            case ')': token[loc] = ')'; symbol = 15; break;
            case '{': token[loc] = '{'; symbol = 16; break;
            case '}': token[loc] = '}'; symbol = 17; break;
            case ';': token[loc] = ';'; symbol = 18; break;
            default: symbol = -1; break;
        }
        if(symbol == -1) error();
        pmove++;
    }
}

/*检查main*/
void FuncTypeAnalysis(){
    strcat(result, "define dso_local i32 ");
    getsym();
    if(symbol == 12) IdentAnalysis();
    else error();
}

/*检查(*/
void IdentAnalysis(){
    strcat(result, "@main");
    getsym();
    if(symbol == 14) LBarAnalysis();
    else error();
}

/*检查)*/
void LBarAnalysis(){
    strcat(result, "(");
    getsym();
    if(symbol == 15) RBarAnalysis();
    else error();
}

void RBarAnalysis(){
    strcat(result, ")");
    getsym();
    if(symbol == 16) LBraceAnalysis();
    else error();
}

void LBraceAnalysis(){
    strcat(result, "{\n");
    getsym();
    if(symbol == 13) StmtAnalysis();
    else error();
}

void StmtAnalysis(){
    strcat(result, "    ret ");
    getsym();
    if(symbol == 10){
        strcat(result, "i32 ");
        strcat(result, token);
        getsym();
        if(symbol != 18) error();
        else{
            strcat(result, "\n");
            getsym();
            if(symbol == 17) strcat(result, "}");
            else error();
        }
    }
    else error();
}

void RBraceAnalysis(){
    strcat(result, "}");
    getsym();
}

void AnalysisBegin(){
    FuncTypeAnalysis();
}

int main(int argv, char* argc[]){
    int i;
    if(argv == 3){
        strcpy(input_file, argc[1]);
        strcpy(output_file, argc[2]);
        fp_r = fopen(input_file, "r");
        fp_w = fopen(output_file, "w");
        while(fgets(buffer, LEN, fp_r)){
            strcat(content, buffer);
        }
        strcat(content, "#");
        init();
        getsym();
        if(symbol != 11) error();
        else AnalysisBegin();
        fwrite(result, strlen(result), 1, fp_w);
        fclose(fp_r);
        fclose(fp_w);
    }
    else{
        printf("There are not enough parameters!");
    }
    return 0;
}