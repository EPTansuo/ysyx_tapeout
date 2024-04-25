#!/usr/bin/bash

VCDFILE="remote_wave.vcd"
MOUNT_POINT="/home/han/Disk/GZIC_Server"

if [ -L "${VCDFILE}" ]; then
        rm "${VCDFILE}"
fi

echo "ln -s ${MOUNT_POINT}${GZIC_SERVER_NPC_HOME}/rtl_sim/wave.vcd ${VCDFILE}"
ln -s ${MOUNT_POINT}${GZIC_SERVER_NPC_HOME}/rtl_sim/wave.vcd ${VCDFILE}

