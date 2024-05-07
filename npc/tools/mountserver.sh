#!/usr/bin/bash

VCDFILE="remote_wave.vcd"
MOUNT_POINT="/home/han/Disk/GZIC_Server"


if [ ! "$(ls -A $1)" ]; then
	echo "Mount Server File System"
	mount.gzic.server	
fi


