#!/bin/bash

let SLEEP=30
let ITIME=600
let MCNT=$ITIME/$SLEEP
let CNT=0
let TCNT=0

echo $SLEEP $ITIME $MCNT

while [[ $CNT -lt $MCNT ]]; do
	sleep $SLEEP
	DLINE=$(docker ps | grep mc)
	DLINE2=$(echo $DLINE | grep "\(healthy\)") 

	if [[ -z "$DLINE" ]] || [[ -z "$DLINE2" ]]; then
		if [[ $TCNT -eq 0 ]]; then
			continue
		else
			break
		fi
	fi

	let NUMP=$(docker exec -i mc rcon-cli list | grep -o -E '[0-9]+' | head -1)
	if [[ $NUMP -eq 0 ]]; then
		let CNT++
		let EMPTY=$CNT*$SLEEP
		echo Server empty for $EMPTY seconds.
	else
		let CNT=0
	fi

	let TCNT++
done

echo "Shutting down the server..."
docker exec -i mc rcon-cli stop
sleep 5
sudo sync
destroy.sh
