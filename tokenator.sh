#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

if [ "$#" -lt 1 ]; then
	echo "Enter your DigitalOcean token:"
	read token
else
	token=$1
fi

if [ "$#" -lt 2 ]; then
	echo "Enter a password for xor:"
	read key
else
	key=$2
fi

tlen=$(expr length $token)
klen=$(expr length $key)

fkey=""

let tlendiv=$tlen/2

for i in $(seq 1 $tlendiv); do
	let "idx = ($i - 1) % klen"
	fkey=$fkey${key:$idx:1}
done

fkeyhex=$(echo -n $fkey | xxd -ps -c 64)

ftoken=""

for i in $(seq 0 3); do
	let "subpos = 16 * i"
	ctoken=${token:$subpos:16}
	cfkeyhex=${fkeyhex:$subpos:16}
	let "cftoken = 0x$ctoken ^ 0x$cfkeyhex"
	dftoken=$(printf "%016x" $cftoken)
	ftoken=$ftoken$dftoken
done

echo $ftoken
if [ "$#" -lt 3 ]; then
	read -p "Would you like to write it to the resources file? [y/N] " ans
else
	ans=$3
fi

if [[ $ans = y ]]; then
	echo Saving the token
	echo $ftoken > $DIR/client/src/main/resources/token.txt
fi
