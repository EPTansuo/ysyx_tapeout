

KCONFIG = $(NPC_HOME)/Kconfig
CONFIG = $(NPC_HOME)/.config
AUTOCONF_H = $(NPC_HOME)/csrc/include/autoconf.h


menuconfig:
	@python3 $(NPC_HOME)/csrc/scripts/menuconfig.py $(KCONFIG)
	@python3 $(NPC_HOME)/csrc/scripts/genconfig.py $(KCONFIG) --header-path $(AUTOCONF_H)
	@echo "Configuration updated and saved to NPC_HOME/.config and NPC_HOME/csrc/include/autoconf.h"

include $(CONFIG)

# 判断优化级别
ifeq ($(CONFIG_CC_O2),y)
CFLAGS += $(CONFIG_CC_OPT)
endif


