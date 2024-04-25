#!/usr/bin/bash

VCDFILE="remote_wave.vcd"
MOUNT_POINT="/home/han/Disk/GZIC_Server"


if [ ! "$(ls -A $1)" ]; then
	echo "Mount Server File System"
	sshfs $GZIC_SERVER_USER@$GIZC_SERVER_IP:/ ${MOUNT_POINT}
fi


