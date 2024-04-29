#!/usr/bin/bash

#反汇编ELF文件
#$1 .bin文件，这是因为npc运行时会传入bin文件，而不是elf文件
#S2 反汇编后的文件存放目录

ELF="${1%.bin}.elf"
FILE_NAME=$(basename "${1}")
DISASM="${2}/${FILE_NAME}.disasm"
##echo "$ELF"
##echo "$DISASM"
riscv64-linux-gnu-objdump -d --no-show-raw-insn -M numeric,no-aliases "${ELF}" \
        | awk '/^[[:xdigit:]]+:/ {printf "%s\t", substr($1, 1, length($1)-1); \
        for (i=2; i<=NF; i++) { if ($i ~ /<|#/) break; printf "%s ", $i} printf "\n";}' > "${DISASM}";

