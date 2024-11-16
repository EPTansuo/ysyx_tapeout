#!/usr/bin/env python
import os
import pandas as pd
import argparse
import sys

def check_table(table):
    red_color = "\033[01;31m"
    clear_color = "\033[01m"
    if(table.iloc[table.shape[0]-1,1]!="default"):
        sys.stderr.write(f"{red_color}The last row of table must be default values!{clear_color}\n")
        return False


def gen_assign(table, col):
    table_isnull = table.isnull()
    first_line = True
    code = "assign " + table.columns.values[col] + " = "
    spaces = len(code) * ' '

    for row in range(table.shape[0]): # number of rows(insts)
        if (table_isnull.iloc[row,0] == False):
            #continue       # line commont
            if (table.iloc[row,0].startswith('//')): # line commont
                continue
        if(first_line == False):
            code += spaces
        first_line = False
        if(row != table.shape[0] - 1):
            if(table_isnull.iloc[row,col]):   # set to default value, the last row is default
                code += f"inst_type == {table.iloc[row,1]} ? {table.iloc[table.shape[0]-1,col]}" +" : \n"
            else:
                code += f"inst_type == {table.iloc[row,1]} ? {table.iloc[row,col]}" +" : \n"
        else:
            if(table_isnull.iloc[row,col]):   # set to default value
                code += f"{table.iloc[table.shape[0]-1,col]};\n\n\n"
            else:
                code += f"{table.iloc[row,col]};\n\n\n"
    return code


def gen_all_assign(table):
    all_code = ""
    for col in range(2, table.shape[1]): #(output signals)
        all_code += gen_assign(table,col)
    return all_code

def gen_invalid_inst(table):
    table_isnull = table.isnull()
    code = "`ifndef STA\n"
    code_assign = "assign " + "exu_invalid_inst" + " = "
    code += code_assign
    spaces = len(code_assign) * ' '
    code += "rst == `RstEnable ? 0 : \n"
    code += spaces + "inst_type == `Inst_ebreak ? 0 : \n"

    for row in range(table.shape[0]): # number of rows(insts)
        if (table_isnull.iloc[row,0] == False):
            #continue       # line commont
            if (table.iloc[row,0].startswith('//')): # line commont
                continue
        code += spaces
        if(row != table.shape[0] - 1):
            code += f"inst_type == {table.iloc[row,1]} ? 0 : \n"
        else:
            code += "1;\n`endif\n\n\n"
    return code


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

    
    args = parser.parse_args()
    exu_table = args.input
    out_v = args.output
    tail_v = args.end
    head_v = args.begin
    
    table = pd.read_excel(exu_table)
    if(check_table(table) == False):
        sys.exit(-1)

    with open(head_v, 'r') as file:
        code_head = file.read()
        file.close()
    
    with open(tail_v, 'r') as file:
        code_tail = file.read()
        file.close()
    

    code_invalid_inst = gen_invalid_inst(table)
    code_assign = gen_all_assign(table)
    code = code_head+code_invalid_inst+code_assign+code_tail

    if(out_v == ""):
        print(code)
    else:
        with open(out_v, 'w') as file:
            file.write(code)
            file.close()






