#include <gdb.h>
#include <isa.h>
#include <common.h>
#include <cpu/cpu.h>
#include <memory/vaddr.h>
#include <stdio.h>
#include <utils.h>
#include <fmt-def.h>


void execute(uint64_t n);

//bool gdbstub_init(gdbstub_t *gdbstub, struct target_ops *ops, arch_info_t arch, char *s);

static int nemu_read_reg(void *args, int regno, size_t *value)
{       
        printf("read_reg: %d\n", regno);

        if(regno > 31 || regno < 0){
                printf("ERROR: read_reg: %d\n", regno);
                return -1;
        }
        
       // *value = (size_t)isa_reg_read(regno);
        return 0;
}

static int nemu_write_reg(void *args, int regno, size_t data)
{
        printf("write_reg: %d %zx\n", regno, data);
        isa_reg_write(regno, data);
        return 0;
}

static int nemu_read_mem(void *args, size_t addr, size_t len, void *val)
{
        printf("read_mem = "FMT_WORD_HEX" %zx\n", (word_t)addr, len);
        (*((word_t*)val)) = vaddr_read(addr, len);
        return 0;
}

static int nemu_write_mem(void *args, size_t addr, size_t len, void *val)
{
        printf("write_mem = " FMT_WORD_HEX " %zx %x\n", (word_t)addr, len, *(word_t*)val);
        vaddr_write(addr, len, *(word_t*)val);
        return 0;
}

gdb_action_t nemu_cont(void *args)
{
        //cpu_exec(-1);
        printf("cont\n");
        while(nemu_state.state == NEMU_RUNNING){
               execute(1);
        }
        return ACT_RESUME;
}

static gdb_action_t nemu_stepi(void *args)
{
        //cpu_exec(1);
        printf("stepi\n");
        if(nemu_state.state == NEMU_RUNNING)
                execute(1);

        return ACT_RESUME;
}

static bool nemu_set_bp(void *args, size_t addr, bp_type_t type)
{
        printf("set breakpoints = %x %zx %x\n", type, addr, type);
        // (void)args;
        // (void)addr;
        // (void)type;

        return false;
}

static bool nemu_del_bp(void *args, size_t addr, bp_type_t type)
{
        printf("remove breakpoints = %x %zx %x\n", type, addr, type);
        // (void)args;
        // (void)addr;
        // (void)type;

        return false;
}

static void nemu_on_interrupt(void *args)
{
        printf("interrupt\n");
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

void print_target_ops(const struct target_ops *ops) {
    printf("Function pointers: \n");
    printf("  continue: %p\n", (void*)ops->cont);
    printf("  stepi: %p\n", (void*)ops->stepi);
    // 添加其它函数指针的打印
}

void print_arch_info(const arch_info_t *arch) {
    printf("Arch info: \n");
    printf("  Description: %s\n", arch->target_desc);
    printf("  Number of registers: %d\n", arch->reg_num);
    printf("  Register size (bytes): %zu\n", arch->reg_byte);
}

void print_gdbstub(const gdbstub_t *gdbstub) {
    printf("GDB Stub Information:\n");
    print_target_ops(gdbstub->ops);
    print_arch_info(&gdbstub->arch);
    //print_gdbstub_private(gdbstub->priv);
}

bool init_gdbstub()
{
        
        struct target_ops ops;
        arch_info_t arch = {
            .reg_byte = 4,
            .reg_num = 32,
#ifdef CONFIG_RV64
            .target_desc = TARGET_RV64,
#else
            .target_desc = TARGET_RV32,
#endif
        };
        
        char ip_port[32];
        sprintf(ip_port, "%s", "127.0.0.1:9012");
        
        if(!gdbstub_init(&gdbstub, &ops, arch, ip_port)){
                fprintf(stderr, "Fail to create socket.\n");
                return false;
        }
        
        print_gdbstub(&gdbstub);
        size_t a;
        nemu_read_reg(NULL, 0, &a);

        if(!gdbstub_run(&gdbstub, NULL)){
                fprintf(stderr, "Fail to run in debug mode.\n");
                return false;
        }
        
        return true;
}