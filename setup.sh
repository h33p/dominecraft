#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR

echo $AUTHKEY > /tmp/authkey

mkdir /opt
mkdir /opt/bin

dd if=/dev/zero of=/swapfile1 bs=2048 count=524288
chown root:root /swapfile1
chmod 000 /swapfile1
mkswap /swapfile1
swapon /swapfile1

groupadd -g 1000 minecraft
useradd -g minecraft -r -s /bin/false -u 1000 minecraft

curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-`uname -s`-`uname -m` > /opt/bin/docker-compose

chmod +x /opt/bin/docker-compose

/opt/bin/docker-compose up -d

cp chron.sh /usr/local/bin/chron.sh
cp destroy.sh /usr/local/bin/destroy.sh
cp autoshutdown.service /etc/systemd/system/
systemctl start autoshutdown.service
