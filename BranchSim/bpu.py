from collections import defaultdict

class BPU:
    def __init__(self, address_size=4, prediction_method='saturating', bits=2, table_size=1024):
        self.address_size = address_size
        self.prediction_method = prediction_method
        self.bits = bits  # bit-width of the prediction counter
        self.table_size = table_size  # 分支预测表的大小
        self.btrace = []

        # 创建一个大小固定的列表来存储计数器，每个元素都是 (2^bits)//2
        # 表示计数器的初始值为“中间值”
        self.history_table = [(2**self.bits) // 2 for _ in range(self.table_size)]

        # 对于 global_history 和 gshare，还需要全局历史寄存器
        if self.prediction_method in ('global_history', 'gshare'):
            self.global_history = 0

    def _index_saturating(self, address):
        """ 对地址进行映射，以获取在 history_table 中的索引 """
        return address % self.table_size

    def _index_local_history(self, address):
        """ Local History 也可以用地址做索引。 """
        return address % self.table_size

    def _index_global_history(self):
        """ Global History 的表索引，就直接用 self.global_history 或其掩码。 """
        return self.global_history % self.table_size

    def _index_gshare(self, address):
        """ Gshare = (global_history XOR address)，然后再取模 table_size。 """
        return (self.global_history ^ address) % self.table_size

    def predict(self, address):
        """
        根据预测方法对给定地址的分支进行预测。
        返回值: 0=不采取, 1=采取
        """
        if self.prediction_method == 'saturating':
            idx = self._index_saturating(address)
            counter = self.history_table[idx]
            return 1 if counter >= (2**self.bits)//2 else 0

        elif self.prediction_method == 'local_history':
            idx = self._index_local_history(address)
            counter = self.history_table[idx]
            return 1 if counter >= (2**self.bits)//2 else 0

        elif self.prediction_method == 'global_history':
            idx = self._index_global_history()
            counter = self.history_table[idx]
            return 1 if counter >= (2**self.bits)//2 else 0

        elif self.prediction_method == 'gshare':
            idx = self._index_gshare(address)
            counter = self.history_table[idx]
            return 1 if counter >= (2**self.bits)//2 else 0

        else:
            raise ValueError("Unsupported prediction method.")

    def update(self, address, taken):
        """
        根据分支的实际结果更新历史表。
        """
        if self.prediction_method == 'saturating':
            idx = self._index_saturating(address)
            counter = self.history_table[idx]
            if taken:
                self.history_table[idx] = min(counter + 1, 2**self.bits - 1)
            else:
                self.history_table[idx] = max(counter - 1, 0)

        elif self.prediction_method == 'local_history':
            idx = self._index_local_history(address)
            counter = self.history_table[idx]
            if taken:
                self.history_table[idx] = min(counter + 1, 2**self.bits - 1)
            else:
                self.history_table[idx] = max(counter - 1, 0)

        elif self.prediction_method == 'global_history':
            # 先更新表
            idx = self._index_global_history()
            counter = self.history_table[idx]
            if taken:
                self.history_table[idx] = min(counter + 1, 2**self.bits - 1)
            else:
                self.history_table[idx] = max(counter - 1, 0)

            # 再更新全局历史寄存器
            # 如果你想让global_history也支持任意位，可以做一个掩码
            self.global_history = ((self.global_history << 1) | taken) & ((1 << self.bits) - 1)

        elif self.prediction_method == 'gshare':
            # 先更新表
            idx = self._index_gshare(address)
            counter = self.history_table[idx]
            if taken:
                self.history_table[idx] = min(counter + 1, 2**self.bits - 1)
            else:
                self.history_table[idx] = max(counter - 1, 0)

            # 再更新全局历史寄存器
            self.global_history = ((self.global_history << 1) | taken) & ((1 << self.bits) - 1)

        else:
            raise ValueError("Unsupported prediction method.")
