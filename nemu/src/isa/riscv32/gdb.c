#include <gdb.h>
#include <isa.h>
#include <common.h>
#include <cpu/cpu.h>
#include <memory/vaddr.h>
#include <stdio.h>
bool gdbstub_init(gdbstub_t *gdbstub, struct target_ops *ops, arch_info_t arch, char *s);

static int nemu_read_reg(void *args, int regno, size_t *reg_value)
{       
        *reg_value = isa_reg_read(regno);
        return 0;
}

static int nemu_write_reg(void *args, int regno, size_t data)
{
        isa_reg_write(regno, data);
        return 0;
}

static int nemu_read_mem(void *args, size_t addr, size_t len, void *val)
{
        (*((word_t*)val)) = vaddr_read(addr, len);
        return 0;
}

static int nemu_write_mem(void *args, size_t addr, size_t len, void *val)
{
        vaddr_write(addr, len, *(word_t*)val);
        return 0;
}

static gdb_action_t nemu_cont(void *args)
{
        cpu_exec(-1);

        return ACT_NONE;
}

static gdb_action_t nemu_stepi(void *args)
{
        cpu_exec(1);

        return ACT_NONE;
}

static bool nemu_set_bp(void *args, size_t addr, bp_type_t type)
{
        (void)args;
        (void)addr;
        (void)type;

        return false;
}

static bool nemu_del_bp(void *args, size_t addr, bp_type_t type)
{
        (void)args;
        (void)addr;
        (void)type;

        return false;
}

static void nemu_on_interrupt(void *args)
{
        (void)args;
}

struct target_ops nemu_ops = {
    .read_reg = nemu_read_reg,
    .write_reg = nemu_write_reg,
    .read_mem = nemu_read_mem,
    .write_mem = nemu_write_mem,
    .cont = nemu_cont,
    .stepi = nemu_stepi,
    .set_bp = nemu_set_bp,
    .del_bp = nemu_del_bp,
    .on_interrupt = nemu_on_interrupt,
};

bool init_gdbstub()
{
        
        struct target_ops ops;
        arch_info_t arch = {
            .reg_byte = 4,
            .reg_num = 32,
#ifdef CONFIG_RV64
            .target_desc = TARGET_RV32,
#else
            .target_desc = TARGET_RV64,
#endif
        };
        char ip_port[32];
        sprintf(ip_port, "%s", "127.0.0.1:9012");
        return gdbstub_init(&gdbstub, &ops, arch, ip_port);

        gdbstub_run(&gdbstub, NULL);
}