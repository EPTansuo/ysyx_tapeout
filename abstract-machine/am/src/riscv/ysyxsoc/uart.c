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





//#define UART_BASE 0x10000000
#define UART_LCR (*(volatile uint8_t *)(UART_BASE + 3))
#define UART_THR (*(volatile uint8_t *)(UART_BASE + 0))
#define UART_RBR (*(volatile uint8_t *)(UART_BASE + 0))
#define UART_FCR (*(volatile uint8_t *)(UART_BASE + 2))
#define UART_LSR (*(volatile uint8_t *)(UART_BASE + 5))

#define UART_LSB (*(volatile uint8_t *)(UART_BASE + 0))
#define UART_MSB (*(volatile uint8_t *)(UART_BASE + 1))

// #define DIVISOR (uint16_t)(12000000 / (16 * 9600))
#define DIVISOR 1

void __am_uart_init() {
    UART_LCR |= 0x80;
    UART_LSB = DIVISOR & 0xff;
    UART_MSB = (DIVISOR >> 8) & 0xff;
    UART_LCR &= ~0x80;
    UART_LCR |= 0x3;
    UART_FCR = 0x7;
}

void __am_uart_config(AM_INPUT_CONFIG_T *cfg) {
    cfg->present = true;
}

inline int __uart_tx_ready() {
    return UART_LSR & (1 << 5);
}

void __am_uart_tx(AM_UART_TX_T *tx) {
    //while (!__uart_tx_ready());
    UART_THR = tx->data;
}

inline int __uart_rx_ready() {
    return UART_LSR & (1 << 0);
}

void __am_uart_rx(AM_UART_RX_T *rx) {
    if (__uart_rx_ready()) {
        rx->data = UART_RBR;
    } else {
        rx->data = 0xff;
    }
}
/*
void UART_init(){
    __am_uart_init();
}*/


void UART_send(uint8_t c) {

   io_write(AM_UART_TX, c);

}
