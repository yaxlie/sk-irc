#include "stack.h"  /* Include the header (not strictly necessary here) */

#include <stdio.h>          

void initStack(Stack s)
{
    s.top = -1;
    s.MAXSIZE = 100;
    int i;
    for(i=0; i<100; i++)
    {
        push(s, i);
    }
}

int isempty(Stack s) {

   if(s.top == -1)
      return 1;
   else
      return 0;
}
   
int isfull(Stack s) {

   if(s.top == s.MAXSIZE)
      return 1;
   else
      return 0;
}

int peek(Stack s) {
   return s.stack[s.top];
}

int pop(Stack s) {
   int data;
	
   if(!isempty()) {
      data = s.stack[s.top];
      s.top = s.top - 1;   
      return data;
   } else {
      printf("Could not retrieve data, Stack is empty.\n");
   }
}

int push(Stack s, int data) {

   if(!isfull()) {
      s.top = s.top + 1;   
      s.stack[s.top] = data;
   } else {
      printf("Could not insert data, Stack is full.\n");
   }
}