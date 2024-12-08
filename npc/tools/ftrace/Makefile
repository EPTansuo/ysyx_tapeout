BIN=ftrace

CFLAGS = -Wall -O1
CSRCS = ftrace.c main.c elfread.c


$(BIN): $(CSRCS)
	gcc $(CFLAGS) $(CSRCS) -o $(BIN)

run: $(BIN)
	./$(BIN)

clean:
	rm $(BIN)



