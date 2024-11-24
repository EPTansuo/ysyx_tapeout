#include <stdio.h>
#include <stdlib.h>

#define BUFFER_SIZE 16

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage: %s <input_file> [output_file]\n", argv[0]);
        return 1;
    }

    FILE *input_file = fopen(argv[1], "rb");
    if (!input_file) {
        perror("Failed to open input file");
        return 1;
    }

    FILE *output_file = stdout; // 默认输出到终端
    if (argc >= 3) {
        output_file = fopen(argv[2], "w");
        if (!output_file) {
            perror("Failed to open output file");
            fclose(input_file);
            return 1;
        }
    }

    unsigned char buffer[BUFFER_SIZE];
    size_t bytes_read;

    while ((bytes_read = fread(buffer, 1, BUFFER_SIZE, input_file)) > 0) {
        for (size_t i = 0; i < bytes_read; i++) {
            fprintf(output_file, "%02X\n", buffer[i]);
        }
    }

    fclose(input_file);
    if (output_file != stdout) {
        fclose(output_file);
    }

    return 0;
}
