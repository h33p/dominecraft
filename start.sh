#!/bin/bash

cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"/mcserver

export EULA=TRUE
export TYPE=VANILLA
export VERSION=1.14.4
export CONSOLE=false
export OVERRIDE_SERVER_PROPERTIES=true
export ENABLE_RCON=true
export ONLINE_MODE=FALSE
export MOTD="3 builders Many tools"
export MAX_TICK_TIME=60000
export OPS=Robert_Ford,ManoVardasRimas
export WHITELIST=Robert_Ford,ManoVardasRimas,dapkarlas
export OVERRIDE_SERVER_PROPERTIES=true

echo EULA $EULA
echo TYPE $TYPE

/start
