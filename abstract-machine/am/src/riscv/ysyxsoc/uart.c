#include <am.h>
#include <klib-macros.h>
#include <riscv/riscv.h>

#define UART_BASE 0x10000000L
#define UART_RX   0   // R   Receiver FIFO output
#define UART_TX   0   // W   Transmit FIFO input
#define UART_IE   1   // RW  Interrupt Enable
#define UART_II   2   // R   Interrupt Identification
#define UART_FC   2   // W   FIFO control
#define UART_LC   3   // RW  Line Control
#define UART_MC   4   // W   Modem Control
#define UART_LS   5   // R   Line Status
#define UART_MS   6   // W   Modem Status


// The registers can be accessed when the 7th (DLAB) bit
// of the Line Control Register is set to ‘1’
#define UART_DL1  0   // RW  Divisor Latch 1(LSB)
#define UART_DL2  1   // RW  Divisor Latch 2(MSB)


/*
void UART_init(){
    outb(UART_BASE + UART_LC, 0xf0); // Set Divisor Lath regs to 0
    outb(UART_BASE + UART_DL2, 0x0);
    outb(UART_BASE + UART_DL1, 0x0);
    outb(UART_BASE + UART_LC, 0x03); // 8-bit, no parity, 1 stop bit
    outb(UART_BASE + UART_FC, 0xc0); // Clear all FIFOs
    outb(UART_BASE + UART_IE, 0x0);  // Disable all Interrupts
}*/

void UART_send(uint8_t c) {
    uint8_t line_status = inb(UART_BASE + UART_LS);
    if(line_status & 0x02) {
        outb(UART_BASE + UART_TX,'s');    // send
        outb(UART_BASE + UART_FC, 0x06); // Clear all FIFOs
    }
    outb(UART_BASE + UART_TX, c);    // send
}
