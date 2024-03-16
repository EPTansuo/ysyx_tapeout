/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>

enum {
  TK_NOTYPE = 256, TK_EQ,

  /* TODO: Add more token types */
  //TODO FINISHED
  TK_NUM,   //数字 
  TK_LP = '(',   //左括号
  TK_RP = ')',   //右括号
  TK_PLUS ='+',  //加
  TK_MINUS = '-',//减
  TK_MUL = '*',  //乘
  TK_DIV = '/',  //除
};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */
  //TODO FINISHED
  {" +", TK_NOTYPE},    // spaces
  {"\\+", '+'},         // plus
  {"==", TK_EQ},        // equal

  {"\\-", '-'},         // 减
  {"\\*", '*'},         // 乘
  {"\\/", '/'},         // 除
  {"\\(", TK_LP},         //左括号
  {"\\)", TK_RP},         //右括号
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[32] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;



bool check_parentheses(int p, int q){
  //如果最左边和最右边不是完整的括号，返回false
  if(tokens[p].type != TK_LP || tokens[q].type != TK_RP)
    return false;

  //但是也有可能出现(1+2)(3+4)的情况，所以再做判断
  int LP_unpaired = 0; 
  for(int i = p+1; i<q; i++){
    switch(tokens[i].type){
      case TK_LP: LP_unpaired++; break;
      case TK_RP: LP_unpaired--; break;
      default: break;
    }
    if(LP_unpaired == -1) return false;
  }
  if(LP_unpaired != 0)
    return false;  
  
  return true;
}



static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      //匹配成功 && 匹配的字符串正好是当前开始的位置
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */ //TODO FINISHED 
        switch (rules[i].token_type) {
          case TK_NOTYPE: tokens[nr_token++].type = TK_NOTYPE; break;
          case TK_EQ: tokens[nr_token++].type = TK_EQ; break;
          case TK_LP: tokens[nr_token++].type = TK_LP; break;
          case TK_RP: tokens[nr_token++].type = TK_RP; break;
          case TK_PLUS: tokens[nr_token++].type = TK_PLUS; break;
          case TK_MINUS: tokens[nr_token++].type = TK_MINUS; break;
          case TK_MUL: tokens[nr_token++].type = TK_MUL; break;
          case TK_DIV: tokens[nr_token++].type = TK_DIV; break;
          case TK_NUM: 
            tokens[nr_token].type = TK_NUM; 
            //把匹配到的数字字符串复制到到str中
            strncpy(tokens[nr_token].str, &e[position-substr_len], substr_len);
            nr_token++; 
            break;
          default: printf("Un recognized rules: %d", rules[i].token_type);
        }

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}

//运算符优先级到整数的映射，优先级越高，映射得到的数值越大
int order_map(int type)
{
  switch (type)
  {
  case TK_MINUS:
  case TK_PLUS:
    return 1; 
  case TK_MUL:
  case TK_DIV:
    return 2;
  default:
    return 0;
  }
}

//type1的运算符优先级是否高于type2
bool higher_order(int type1, int type2)
{
  return order_map(type1) - order_map(type2) > 0;
}

//函数的主体从文档中复制过来
uint32_t eval(int p, int q) {
  if (p > q) {
    /* Bad expression */
    printf("Bad Expression!\n");
    //assert(0);
    return 0;
  }
  else if (p == q) {
    /* Single token.
     * For now this token should be a number.
     * Return the value of the number.
     */
    return atoi(tokens[p].str);
  }
  else if (check_parentheses(p, q) == true) {
    /* The expression is surrounded by a matched pair of parentheses.
     * If that is the case, just throw away the parentheses.
     */
    return eval(p + 1, q - 1);
  }
  else {

    int op = -1; //op = the position of 主运算符 in the token expression;

    for(int i=p; i<=q; i++)
    {
      switch (tokens[i].type)
      {
        case TK_LP: while(tokens[++i].type != TK_RP);  //不要break
        
        case TK_NUM:  break;

        case TK_PLUS:
        case TK_MINUS: 
          if(op < 0){
            op = i;
            break;
          }
          if(higher_order(tokens[op].type,tokens[i].type))
            op = i; 
          break;
        
        case TK_MUL:
        case TK_DIV: 
          if(op < 0){
            op = i;
            break;
          }
          if(higher_order(tokens[op].type,tokens[i].type))
            op = i; 
          break;
      default:
        break;
      }
    }

    uint32_t val1 = eval(p, op - 1);
    uint32_t val2 = eval(op + 1, q);

    assert(op>0 && op<32);

    switch (tokens[op].type) {
      case '+': return val1 + val2;
      case '-': return val1 - val2;
      case '*': return val1 * val2;
      case '/': return (val2 != 0 ? (val1 / val2) : 0);
      default: assert(0);
    }
  }
}



word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
  return  eval(0,nr_token-1);

  return 0;
}
