import subprocess
import threading

def handle_process(process, pred_method, bits, table_size, process_id):
    try:
        for line in process.stdout:
            # print(line)
            if(line.strip()):
                print(f"{pred_method}, {bits}, {table_size}, {bits*table_size}, {line.strip().split(' ')[1]}")
        process.wait()
    except Exception as e:
        print(f"Error in process {process_id}: {e}")

def runJobs(njobs,pred_method, bits, table_size, trace_file):
    num_processes = njobs
    processes = []
    threads = []

    for i in range(num_processes):
        args = [
            "--address-size", str(4),
            "--prediction-method", pred_method[i],
            "--bits", str(bits[i]),
            "--table-size", str(table_size[i]),
            "-f", trace_file,
            
        ]

        process = subprocess.Popen(
            ['python', '-u', 'branchsim.py'] + args,  # '-u' for unbuffered
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,  # Merge stderr into stdout
            text=True
        )
        processes.append(process)

        thread = threading.Thread(target=handle_process, args=(
            process, 
            pred_method[i], 
            bits[i], 
            table_size[i],
            i + 1
        ))
        thread.start()
        threads.append(thread)

        process.stdin.close()

    # Wait for all threads to complete
    for thread in threads:
        thread.join()

def main():
    branch_trace = "btrace_test.bin.gz"
    maxJobs = 7

    bits_list = [1, 2, 3, 4, 5, 6, 7, 8]
    table_size_list = [1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024]
    pred_methods = ["saturating", "local_history", "global_history", "gshare"]


    print("predict_method, nbits, table_size, total_bits, accuracy")
    arg_bits = []
    arg_pred_method = []
    arg_table_size = []
    for bits in bits_list:
        for pred_method in pred_methods:
            for table_size in table_size_list:
                    if(bits*table_size > 1024):
                        continue
                    arg_bits.append(bits)
                    arg_pred_method.append(pred_method)
                    arg_table_size.append(table_size)
                    if len(arg_bits) == maxJobs:
                        runJobs(maxJobs, arg_pred_method, arg_bits, arg_table_size, branch_trace)
                        arg_bits = []
                        arg_pred_method = []
    if len(arg_bits) > 0:
        runJobs(len(arg_bits),arg_pred_method, arg_bits,arg_table_size, branch_trace)

if __name__ == "__main__":
    main()
