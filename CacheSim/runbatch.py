import subprocess
import threading

def handle_process(process, nways, nsets, bs, replace, process_id):
    try:
        for line in process.stdout:
            print(f"{nways}, {nsets}, {bs}, {replace}, {line.strip()}")
        process.wait()
    except Exception as e:
        print(f"Error in process {process_id}: {e}")

def runJobs(njobs, nways, nsets, bs, replace, trace_file):
    num_processes = njobs
    processes = []
    threads = []

    for i in range(num_processes):
        args = [
            "--nways", str(nways[i]),
            "--nsets", str(nsets[i]),
            "--bs", str(bs[i]),
            "--replace", replace[i],
            "-f", trace_file
        ]

        process = subprocess.Popen(
            ['python', '-u', 'cachesim.py'] + args,  # '-u' for unbuffered
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,  # Merge stderr into stdout
            text=True
        )
        processes.append(process)

        thread = threading.Thread(target=handle_process, args=(
            process, 
            nways[i], 
            nsets[i], 
            bs[i], 
            replace[i], 
            i + 1
        ))
        thread.start()
        threads.append(thread)

        process.stdin.close()

    # Wait for all threads to complete
    for thread in threads:
        thread.join()

def main():
    maxJobs = 8
    ways = [1, 2, 4]
    sets = [2, 4, 8, 16]
    bs = [4, 8, 12, 16, 20, 24, 28, 32]
    replaces = ["RANDOM", "LRU", "FIFO"]
    print("ways, sets, bs, replace, hit_rate")
    arg_ways = []
    arg_sets = []
    arg_bs = []
    arg_replace = []
    for nways in ways:
        for nsets in sets:
            for nbs in bs:
                for replace in replaces:  # Corrected loop variable
                    arg_ways.append(nways)
                    arg_sets.append(nsets)
                    arg_bs.append(nbs)
                    arg_replace.append(replace)
                    if len(arg_ways) == maxJobs:
                        runJobs(maxJobs, arg_ways, arg_sets, arg_bs, arg_replace, "pc_trace.bin.gz")
                        arg_ways = []
                        arg_sets = []
                        arg_bs = []
                        arg_replace = []
    if len(arg_ways) > 0:
        runJobs(len(arg_ways), arg_ways, arg_sets, arg_bs, arg_replace, "pc_trace.bin.gz")

if __name__ == "__main__":
    main()
