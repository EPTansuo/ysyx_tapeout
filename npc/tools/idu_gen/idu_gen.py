#!/usr/bin/env python
# coding: utf-8

# In[1]:


import os
import sys


# In[2]:


def log_error(log_info):
    error_color = "\033[01;31m"
    clear_color = "\033[01m"
    sys.stderr.write(f"{error_color}{log_info}{clear_color}\n")
def log_warning(log_info):
    warn_color = "\033[01;33m"
    clear_color = "\033[01m"
    sys.stderr.write(f"{warn_color}{log_info}{clear_color}\n")


# In[ ]:





# In[3]:


class Inst:
    inst_pat = ""
    inst_name = ""
    inst_type = ""
    funct3 = ""
    funct7 = ""
    opcode = ""
    inst_h6 = "" # highest 6 bits of a inst
    def __init__(self,line):
        self.inst_pat = line.split(',')[0].split('\"')[1].replace(" ","")
        self.inst_name = line.split(',')[1].replace(" ","")
        self.inst_type = line.split(',')[2].replace(" ","")
        self.opcode = self.inst_pat[-7:]
        self.funct3 = self.inst_pat[-15:-12]
        self.funct7 = self.inst_pat[:7]
        self.inst_h6 = self.inst_pat[:6]

    def print_info(self):
        print("Instruction Pattern:", self.inst_pat)
        print("Instruction Name:", self.inst_name)
        print("Instruction Type:", self.inst_type)
        print("Function 3:", self.funct3)
        print("Function 7:", self.funct7)
        print("Opcode:", self.opcode)
        print("Highest 6 bits of Instruction:", self.inst_h6)
        
        


# In[4]:


def read_insts(filename):
    insts = []
    lines = []
    with open(filename, 'r') as file:
        while True:
            line = file.readline()
            if not line:
                break
            if line.strip():
                inst = Inst(line)
                insts.append(inst)
    
    return insts


# In[5]:
#
#
# insts = read_insts("idu.c")
# len(insts)
# insts[1].print_info()
#

# In[6]:


def group_by(array, key=lambda x: x):
    grouped = {}
    for item in array:
        group_key = key(item)
        if group_key not in grouped:
            grouped[group_key] = []
        grouped[group_key].append(item)
    return grouped


# In[ ]:





# In[ ]:





# In[ ]:





# In[7]:

#
# str_ = "100001?"


# In[8]:


# len(insts)


# In[9]:


def gen_funct7_signal(insts):
    wire_code_list = [];
    code_list = []
    for inst in insts:
        if inst.funct7 == "???????":
            continue
        elif inst.funct7[-1] == "?":
            wire_code = f"wire funct7_6_{inst.funct7[0:-1]};\n"
            code = f"assign funct7_6_{inst.funct7[0:-1]} = (funct7[6:1] == 6'b{inst.funct7[0:-1]});\n"
        elif "?" in inst.funct7:
            log_error("Error while processing funct7 signal:")
            inst.print_info()
            sys.exit(-1)
        else:
            wire_code = f"wire funct7_{inst.funct7};\n"
            code = f"assign funct7_{inst.funct7} = (funct7 == 7'b{inst.funct7});\n"
        if code not in code_list:
            wire_code_list.append(wire_code)
            code_list.append(code)
            
    return (wire_code_list,code_list)


# In[10]:


# gen_funct7_signal(insts)


# In[11]:


def gen_funct3_signal(insts):
    wire_code_list = []
    code_list = []
    for inst in insts:
        if inst.funct3 == "???":
            continue
        elif "?" in inst.funct3:
            log_error("Error while processing funct3 signal:")
            inst.print_info()
            sys.exit(-1)
        else:
            wire_code = f"wire funct3_{inst.funct3};\n"
            code = f"assign funct3_{inst.funct3} = (funct3 == 3'b{inst.funct3});\n"
        if code not in code_list:
            wire_code_list.append(wire_code)
            code_list.append(code)
    return (wire_code_list,code_list)


# In[12]:


# gen_funct3_signal(insts)
#

# In[13]:


def gen_opcode_signal(insts):
    wire_code_list = []
    code_list = []
    for inst in insts:
        if "?" in inst.opcode:
            log_error("Error while processing opcode signal:")
            inst.print_info()
            sys.exit(-1)
        else:
            wire_code = f"wire opcode_{inst.opcode};\n"
            code = f"assign opcode_{inst.opcode} = (opcode == 7'b{inst.opcode});\n"
        if code not in code_list:
            wire_code_list.append(wire_code)
            code_list.append(code)
    return (wire_code_list,code_list)


# In[14]:


# gen_opcode_signal(insts)


# In[15]:


# level:
#         1: opcode   2: funct3   3: funct7
def _gen_pat_signal(inst, level:int):
    if(level < 1 or level > 3):
        log_error(f"Error for level = {level} in function `gen_pat_signal`");
        sys.exit(-1)
    wire_code = f"wire inst_pat_{inst.inst_name};\n"
    code = f"assign inst_pat_{inst.inst_name} = "
    spaces = ' ' * len(code)
    for lev in range(level):
        if lev == 1  -1:   # for opcode signal 
            code += f"opcode_{inst.opcode}"
        elif lev == 2  -1:
            code += " &\n" + spaces
            code += f"funct3_{inst.funct3}"
        elif lev == 3   -1:
            code += " &\n" + spaces
            if "?" in inst.funct7:
                code += f"funct7_6_{inst.funct7[0:-1]}"
            else:
                code += f"funct7_{inst.funct7}"
    code += ";\n"
    return (wire_code,code);
        
    


# In[16]:

#
# insts[40].print_info()
# print(_gen_pat_signal(insts[40],3)[0])
# print(_gen_pat_signal(insts[40],3)[1])


# In[17]:


def gen_inst_pat_code(insts):
    wire_code_list = []
    code_list = []
    insts_opcode_g = group_by(insts, key=lambda x: x.opcode)  # opcode
    for key_opcode in insts_opcode_g.keys():
        if(len(insts_opcode_g[key_opcode]) == 1):
            # do something
            wire_code,code = _gen_pat_signal(insts_opcode_g[key_opcode][0],1)
            wire_code_list.append(wire_code)
            code_list.append(code)
            continue
        else:
            insts_funct3_g = group_by(insts_opcode_g[key_opcode], key=lambda x: x.funct3) # funct3
            for key_funct3 in insts_funct3_g.keys():
                if(len(insts_funct3_g[key_funct3]) == 1):
                   # do something
                    wire_code,code = _gen_pat_signal(insts_funct3_g[key_funct3][0],2)
                    wire_code_list.append(wire_code)
                    code_list.append(code)
                    continue
                else:
                    insts_funct7_g = group_by(insts_funct3_g[key_funct3], key=lambda x: x.funct7) # funct7
                    for key_funct7 in insts_funct7_g.keys():
                        if(len(insts_funct7_g[key_funct7]) == 1):
                            # do something
                            wire_code,code = _gen_pat_signal(insts_funct7_g[key_funct7][0],3)
                            wire_code_list.append(wire_code)
                            code_list.append(code)
                            continue
                        else:
                            log_error("ERROR while parsing!")
                    
    return (wire_code_list, code_list)


# In[ ]:





# In[18]:
#
#
# gen_inst_pat_code(insts)


# In[19]:


def gen_inst_type_signal(insts):
    code = "assign inst_type = "
    spaces = " " * len(code)
    code += "inst == `EBREAK ? `Inst_ebreak : \n"
    for inst in insts:
        code += spaces
        code += f"inst_pat_{inst.inst_name} ? `Inst_{inst.inst_name} : \n"
    code += spaces + "`Inst_inv;\n"
    return code


# In[20]:

#
# print(gen_inst_type_signal(insts))


# In[35]:


def gen_imm_signal(insts):
    code = "assign imm = "
    isfirst = True
    spaces = " " * len(code)
    insts_type_g = group_by(insts, lambda x: x.inst_type)
    for key in insts_type_g.keys():
        if key != 'R' and key != 'N':
            opcodes = []
            for inst in insts_type_g[key]:
                if inst.opcode not in opcodes:
                    opcodes.append(inst.opcode)
                    #inst.print_info()
            for opcode in opcodes:
                if(isfirst):
                    isfirst = False
                else:
                    code += spaces    
                code += f"(opcode == 7'b{opcode}) ? imm{key} : \n"

    code += spaces + "0;\n"
    return code;


# In[36]:


# print(gen_imm_signal(insts))


# In[37]:


def gen_all_code(insts):
    wire_code = ""
    assign_code = ""
    

    wire_code_list, assign_code_list = gen_funct3_signal(insts)
    for wire_code_ in wire_code_list:
        wire_code += wire_code_ 
    wire_code += "\n\n"
    for assign_code_ in assign_code_list:
        assign_code += assign_code_
    assign_code += "\n\n"


    wire_code_list, assign_code_list = gen_funct7_signal(insts)
    for wire_code_ in wire_code_list:
        wire_code += wire_code_
    wire_code += "\n\n"
    for assign_code_ in assign_code_list:
        assign_code += assign_code_ 
    assign_code += "\n\n"
    

    wire_code_list, assign_code_list = gen_opcode_signal(insts)
    for wire_code_ in wire_code_list:
        wire_code += wire_code_
    wire_code += "\n\n"
    for assign_code_ in assign_code_list:
        assign_code += assign_code_ 
    assign_code += "\n\n"


    wire_code_list, assign_code_list = gen_inst_pat_code(insts)
    for wire_code_ in wire_code_list:
        wire_code += wire_code_ 
    wire_code += "\n\n"
    for assign_code_ in assign_code_list:
        assign_code += assign_code_ + "\n" 
    assign_code += "\n\n"

    inst_type_code = gen_inst_type_signal(insts) + "\n\n"

    imm_code = gen_imm_signal(insts)
    
    return wire_code + assign_code + inst_type_code + imm_code
    


# In[38]:


def _gen_one_inst_def(inst_name, spaces, cnt):
    str_tmp = f"`define Inst_{inst_name}"
    code = str_tmp + (spaces - len(str_tmp))*" " +f"8'd{cnt}\n"
    return code


# In[39]:


def gen_inst_def(insts):
    inst_def = "`define EBREAK              "
    space_len = len(inst_def)
    inst_def += "32'h00100073\n\n"
    cnt = 0;
    inst_def += _gen_one_inst_def("inv", space_len, cnt) #invalid inst
    cnt += 1;
    inst_def += _gen_one_inst_def("ebreak", space_len, cnt) #invalid inst
    
    for inst in insts:
        cnt+=1
        inst_def += _gen_one_inst_def(inst.inst_name, space_len, cnt)
    return inst_def


# In[40]:


if __name__ == "__main__":

    head_v = "code_head.v"
    tail_v = "code_tail.v"
    idu_c = "idu.c"
    out_v = "idu.v"
    out_def = "inst_def.v"
    def_head_v = "def_head.v"
    
    with open(head_v, 'r') as file:
        code_head = file.read()
        file.close()
    
    with open(tail_v, 'r') as file:
        code_tail = file.read()
        file.close()
    
    insts = read_insts(idu_c)
    code = gen_all_code(insts)

    with open(out_v, 'w') as file:
        file.write(code_head)
        file.write(code)
        file.write(code_tail)
        file.close()

    with open(def_head_v, 'r') as file:
        def_head = file.read()
        file.close()
    with open(out_def, "w") as file:
        file.write(def_head)
        file.write(gen_inst_def(insts))
        file.close()
    


# In[41]:


print(code)


# In[ ]:




