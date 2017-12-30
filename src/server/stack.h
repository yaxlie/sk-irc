#ifndef STACK_H_   /* Include guard */
#define STACK_H_

struct Stack
{
    int MAXSIZE;       
    int stack[100];     
    int top;  
};

void initStack(struct Stack s);
int isempty(struct Stack s);
int isfull(struct Stack s);
int peek(struct Stack s);
int pop(struct Stack s);
int push(struct Stack s,int data);

#endif // STACK_H_