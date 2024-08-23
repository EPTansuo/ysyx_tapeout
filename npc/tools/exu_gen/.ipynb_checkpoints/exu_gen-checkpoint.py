#!/usr/bin/env python
# coding: utf-8

# In[2]:


import os
import pandas as pd
import argparse


# In[49]:


def gen_assign(table, col):
    code_assign = "assign " + table.columns.values[col] + " = " 
    spaces = len(code_assign) * ' '
    code = code_assign
    for row in range(table.shape[0]): # number of rows(insts)
        if(row != 0):
            code += spaces
        
        if(row != table.shape[0] - 1):
            code += f"inst_type == {table.iloc[row,0]} ? {table.iloc[row,col]}" +" : \n"
        else:
            code += f"{table.iloc[row,col]};\n\n"

    return code

# In[54]:


def gen_all_assign(table):
    all_code = ""
    for col in range(1, table.shape[1]): #number of cols-1 (output signals)
        all_code += gen_assign(table,col)
    return all_code


# In[4]:


if __name__ == "__main__":

    parser = argparse.ArgumentParser(
        prog='exu_gen',
        description='Generate exu.v automatically',
        epilog='By EPTansuo (Bingjin Han)'
    )
    NPC_HOME = os.environ.get('KEY_THAT_MIGHT_EXIST', ".")
    
    parser.add_argument('-i','--input', default = f'{NPC_HOME}/tools/exu_gen/exu_table.xlsx',help='Input a table file(csv, xlsx...).') # input table file
    parser.add_argument('-b','--begin', default = f'{NPC_HOME}/tools/exu_gen/code_head.v', help = 'Begining of the output file.');
    parser.add_argument('-e','--end', default = f'{NPC_HOME}/tools/exu_gen/code_tail.v', help = 'Ending of the output file.');
    parser.add_argument('-o','--output', default = f'', help = 'Output Verilog file.');

    
    # head_v = "code_head.v"
    # tail_v = "code_tail.v"
    # idu_table = "exu_table.xlsx"
    # out_v = "exu.v"

    args = parser.parse_args()
    exu_table = args.input
    out_v = args.output
    tail_v = args.end
    head_v = args.begin
    
    
    with open(head_v, 'r') as file:
        code_head = file.read()
        file.close()
    
    with open(tail_v, 'r') as file:
        code_tail = file.read()
        file.close()
    
    table = pd.read_excel(exu_table)
    code_assign = gen_all_assign(table)
    code = code_head + code_assign + code_tail

    if(out_v == ""):
        print(code)
    else:
        with open(out_v, 'w') as file:
            file.write(code)
            file.close()


# In[ ]:




