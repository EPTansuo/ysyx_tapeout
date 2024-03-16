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

#include "sdb.h"

#define NR_WP 32



typedef struct watchpoint {
  int NO;
  struct watchpoint *next;

  /* TODO: Add more members if necessary */
  char expr[256];
  word_t value;
  word_t value_old;
} WP;

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

WP* new_wp();
void free_wp(WP *wp);


void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */
WP* new_wp()
{
  if (free_ == NULL){
    printf("Ther is no free watchpoint!\n");
    return NULL;
  }

  //static int number = 0;

  WP* tmp = free_;
  free_ = free_->next;  //从空闲链表中取出
  tmp->next = head; 
  head = tmp;           //将添加的监视点添加到在使用的链表中
  
  head->expr[0] = '\0';
  head->value = 0;
  head->value_old = 0;
  //head->NO = number++;

  return head;
}
void free_wp(WP *wp)
{
  if(wp == NULL){
    printf("Ther is no used watchpoint!\n");
    return;
  }

  WP* tmp = NULL;

  if(head == wp){
    tmp =  free_;
    free_ = head;
    free_->next = tmp;
    head = head->next;
  } else {
    for(WP* p = head->next; p!=NULL; tmp = p, p = p->next){
      if(p == wp){
        tmp->next = p->next;
        p->next = free_;
        free_ = p;
        return;
      }
    }
  }
}
