import math
import gzip
import struct
import random

class CacheLine:
    def __init__(self):
        self.valid = False
        self.tag = None
        self.data = None
        self.lru_counter = 0  # 用于LRU替换策略
        self.insert_time = 0  # 用于FIFO替换策略

    def __str__(self):
        return f"Valid: {self.valid}, Tag: {self.tag}, LRU: {self.lru_counter}, Insert Time: {self.insert_time}"

class CacheSet:
    def __init__(self, associativity, replacement_policy='LRU'):
        self.associativity = associativity
        self.replacement_policy = replacement_policy
        self.lines = [CacheLine() for _ in range(associativity)]
        self.current_time = 0  # 用于FIFO

    def find_line(self, tag):
        for line in self.lines:
            if line.valid and line.tag == tag:
                return line
        return None

    def get_replacement_line(self):
        if self.replacement_policy == 'LRU':
            return min(self.lines, key=lambda line: line.lru_counter)
        elif self.replacement_policy == 'RANDOM':
            return random.choice(self.lines)
        elif self.replacement_policy == 'FIFO':
            return min(self.lines, key=lambda line: line.insert_time)
        else:
            raise ValueError("Unknown replacement policy")

    def replace_line(self, tag):
        replacement_line = self.get_replacement_line()
        replacement_line.valid = True
        replacement_line.tag = tag
        replacement_line.lru_counter = 0
        self.current_time += 1
        replacement_line.insert_time = self.current_time
        return replacement_line

    def update_on_hit(self, line):
        if self.replacement_policy == 'LRU':
            # 更新其他行的LRU计数器
            for l in self.lines:
                if l != line and l.valid:
                    l.lru_counter += 1
            line.lru_counter = 0
        elif self.replacement_policy == 'FIFO':
            # FIFO不需要在命中时更新
            pass
        elif self.replacement_policy == 'RANDOM':
            # RANDOM不需要在命中时更新
            pass

    def access(self, tag):
        line = self.find_line(tag)
        if line:
            # Cache Hit
            self.update_on_hit(line)
            return True
        else:
            # Cache Miss
            replaced_line = self.replace_line(tag)
            return False

    def __str__(self):
        return '\n'.join([str(line) for line in self.lines])

class Cache:
    def __init__(self, cache_size, block_size, associativity, replacement_policy='LRU'):
        self.cache_size = cache_size  # 总缓存大小，字节
        self.block_size = block_size  # 块大小，字节
        self.associativity = associativity  # 关联度
        self.replacement_policy = replacement_policy.upper()

        self.num_blocks = self.cache_size // self.block_size
        self.num_sets = self.num_blocks // self.associativity

        self.sets = [CacheSet(associativity, self.replacement_policy) for _ in range(self.num_sets)]

        # 计算地址划分
        self.block_offset_bits = int(math.log2(self.block_size))
        self.index_bits = int(math.log2(self.num_sets))
        self.tag_bits = 32 - self.index_bits - self.block_offset_bits  # 假设32位地址

        # 统计数据
        self.accesses = 0
        self.hits = 0
        self.misses = 0

    def access(self, address):
        self.accesses += 1

        # 提取地址的各部分
        block_offset = address & (self.block_size - 1)
        index = (address >> self.block_offset_bits) & (self.num_sets - 1)
        tag = address >> (self.block_offset_bits + self.index_bits)

        cache_set = self.sets[index]
        hit = cache_set.access(tag)

        if hit:
            self.hits += 1
        else:
            self.misses += 1

        return hit

    def get_stats(self):
        hit_rate = self.hits / self.accesses if self.accesses else 0
        miss_rate = self.misses / self.accesses if self.accesses else 0
        return {
            'Accesses': self.accesses,
            'Hits': self.hits,
            'Misses': self.misses,
            'Hit Rate': hit_rate,
            'Miss Rate': miss_rate
        }

    def __str__(self):
        stats = self.get_stats()
        return (f"Cache Stats:\n"
                f"Total Accesses: {stats['Accesses']}\n"
                f"Hits: {stats['Hits']}\n"
                f"Misses: {stats['Misses']}\n"
                f"Hit Rate: {stats['Hit Rate'] * 100:.2f}%\n"
                f"Miss Rate: {stats['Miss Rate'] * 100:.2f}%")

class CacheSimulator:
    def __init__(self, cache, address_size=4, debug=False):
        self.cache = cache
        self.address_size = address_size  # 每个地址的字节数，4或8
        self.debug = debug

    def run(self, trace_file):
        with gzip.open(trace_file, 'rb') as f:
            access_num = 0
            while True:
                address_bytes = f.read(self.address_size)
                if not address_bytes or len(address_bytes) < self.address_size:
                    break
                # 根据地址大小解码
                if self.address_size == 4:
                    address = struct.unpack('<I', address_bytes)[0]
                elif self.address_size == 8:
                    address = struct.unpack('<Q', address_bytes)[0]
                else:
                    raise ValueError("Unsupported address size. Use 4 or 8 bytes.")
                hit = self.cache.access(address)
                access_num +=1
                if self.debug and access_num <= 10:
                    print(f"Access {access_num}: Address=0x{address:08X}, Hit={'Yes' if hit else 'No'}")

    def report(self):
        print(self.cache)

if __name__ == "__main__":


    # 配置缓存参数
    cache_size = 32 * 1024  # 32 KB
    block_size = 64  # 64 bytes
    associativity = 4  # 4-way set associative
    replacement_policy = 'LRU'  # 可选: 'LRU', 'RANDOM', 'FIFO'

    # 创建缓存对象
    cache = Cache(cache_size, block_size, associativity, replacement_policy=replacement_policy)

    # 创建模拟器，指定地址大小为4字节（32位），开启调试模式
    simulator = CacheSimulator(cache, address_size=4, debug=True)

    # 运行模拟器
    simulator.run('pc_trace.bin_test.gz')

    # 打印统计结果
    simulator.report()
