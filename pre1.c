#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<ctype.h>
#define LEN 1024

char input_file[LEN];
char read_in[LEN];
char ans[LEN];
char* reserved[6] = {"If", "Else", "While", "Break", "Continue", "Return"};
FILE* fp_r;

int judgeIsReserve(char s[]){
	int len, i, j;
	char temp[15] = {0}, temp_reserve[15] = {0};
	len = strlen(s);
	for(i = 6, j = 0; i < len; i++, j++){
		temp[j] = s[i];
	}
	for(i = 0; i < 6; i++){
		strcpy(temp_reserve, reserved[i]);
		temp_reserve[0] = 'a' + (temp_reserve[0] - 'A');
		if(strcmp(temp_reserve, temp) == 0){
			return i;
		}
		memset(temp_reserve, 0, sizeof(temp_reserve));
	}
	return 6;
}


int lexicalAnalysis(int i){
	char *pmove, title[LEN], temp_judge[LEN];
	char *pp, temp_c;
	int l, tp_l, len = 0, flag;
	int ret = 0;
	pmove = read_in;
	while(len < i){
		if(*pmove == ' ' || *pmove == '\t'){
			pmove++;
			len++;
		}
		else if(*pmove == '\n') break;
		else if(isdigit(*pmove)){
			strcpy(title, "Number");
			strcat(title, "(");
			l = strlen(title);
			while(isdigit(*pmove)){
				title[l++] = *pmove;
				pmove++;
				len++;
			}
			if(*pmove != ' ' && *pmove != '\n' && *pmove != '\t'){
				pp = pmove;
				if(*pp != ')' && *pp != ';'){
					printf("Err");
					ret = 1;
					break;
				}
				else if(*pp == ';' && *(pp + 1) != '\n'){
					printf("Err");
					ret = 1;
					break;
				}
			}
			strcat(title, ")");
			tp_l = strlen(title);
			title[tp_l] = '\n';
			strcat(ans, title);
			memset(title, 0, sizeof(title));
		}
		else if(isalpha(*pmove) || *pmove == '_'){
			strcpy(title, "Ident");
			strcat(title, "(");
			l = strlen(title);
			while(isalpha(*pmove) || *pmove == '_' || isdigit(*pmove)){
				title[l++] = *pmove;
				pmove++;
				len++;
			}
			if(*pmove != ' ' && *pmove != '\n' && *pmove != '\t'){
				pp = pmove;
				if(*pp != ')' && *pp != ';'){
					printf("Err");
					ret = 1;
					break;
				}
				else if(*pp == ';' && *(pp + 1) != '\n'){
					printf("Err");
					ret = 1;
					break;
				}	
			}
			flag = judgeIsReserve(title);
			if(flag == 6){
				strcat(title, ")");
				tp_l = strlen(title);
				title[tp_l] = '\n';
				strcat(ans, title);
			}
			else{
				strcpy(temp_judge, reserved[flag]);
				temp_judge[strlen(temp_judge)] = '\n';
				strcat(ans, temp_judge);
			}	
			memset(title, 0, sizeof(title));
			memset(temp_judge, 0, sizeof(temp_judge));
		}
		else{
			if(*pmove == '='){
				char* pback = pmove;
				int n = 0;
				while(*pmove == '='){
					pback = pmove;
					pmove++;
					n++;
				}
				pmove = pback;
				if(n % 2 == 0){
					n /= 2;
					while(n--){
						strcat(title, "Eq");
					}
				}
				else{
					n -= 1;
					n /= 2;
					while(n--){
						strcat(title, "Eq");
					}
					strcat(title, "Assign");
				}
			}
			else{
				switch(*pmove){
					case ';': strcpy(title, "Semicolon"); break;
					case '(': strcpy(title, "LPar"); break;
					case ')': strcpy(title, "RPar"); break;
					case '{': strcpy(title, "LBrace"); break;
					case '}': strcpy(title, "RBrace"); break;
					case '+': strcpy(title, "Plus"); break;
					case '*': strcpy(title, "Mult"); break;
					case '/': strcpy(title, "div"); break;
					case '<': strcpy(title, "Lt"); break;
					case '>': strcpy(title, "Gt"); break;
					default: ret = 1; break;
				}
			}
			temp_c = *(pmove + 1);
			if(ret == 1 || (strcmp(title, "LPar") != 0 && temp_c != ' ' && temp_c != '\n' && temp_c != '\t')){
				ret = 1;
				printf("Err");
				break;
			}
			tp_l = strlen(title);
			title[tp_l] = '\n';
			strcat(ans, title);
			memset(title, 0, sizeof(title));
			pmove++;
			len++;
		}
		
	}
	return ret;
}

int main(int argc, char* argv[]){
	int i, j;
	if(argc > 1){
		strcpy(input_file, argv[1]);
	}
	else{
		scanf("%s", input_file);
	}
	fp_r = fopen(input_file, "r");
	while(fgets(read_in, LEN, fp_r)){
		i = strlen(read_in);
		j = lexicalAnalysis(i);
		if(j == 1){
			break;
		}
		printf("%s", ans);
		memset(ans, 0, sizeof(ans));
	}
	return 0;
} 