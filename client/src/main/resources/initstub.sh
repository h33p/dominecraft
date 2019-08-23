#!/bin/bash

VOLUMEDASH=%s
AUTHKEY=%s

VOLUME=$(echo $VOLUMEDASH | sed -r 's/-/_/g')

sudo mkdir -p /mnt/$VOLUME

cat <<EOF | sudo tee /etc/systemd/system/mnt-$VOLUME.mount
[Unit]
Description = Mount DO Volume

[Mount]
What=/dev/disk/by-id/scsi-0DO_Volume_$VOLUMEDASH
Where=/mnt/$VOLUME
Options=defaults,discard,noatime
Type=ext4

[Install]
WantedBy = multi-user.target
EOF

# Enable the mount
sudo systemctl enable --now mnt-$VOLUME.mount

if [ ! -d /mnt/$VOLUME/doserver ]; then
	git clone https://github.com/Heep042/dominecraft /mnt/$VOLUME/dominecraft
else
	cd /mnt/$VOLUME/dominecraft
	git pull
fi

sudo AUTHKEY=$AUTHKEY /mnt/$VOLUME/dominecraft/setup.sh
