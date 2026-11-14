#!/bin/sh
# O Render (e a maioria dos PaaS) injeta a porta em $PORT.
# O Tomcat 11 escuta 8080 por padrao; se $PORT existir, reescrevemos o server.xml.
set -e

if [ -n "$PORT" ]; then
    sed -i "s/port=\"8080\"/port=\"$PORT\"/g" /usr/local/tomcat/conf/server.xml
    echo "[smartpet] Tomcat escutando na porta $PORT"
else
    echo "[smartpet] PORT nao definida, usando 8080"
fi

# Garante que as variaveis de banco chegaram
if [ -z "$SMARTPET_DB_URL" ]; then
    echo "[smartpet] AVISO: SMARTPET_DB_URL nao definida. O banco vai falhar." >&2
fi

exec catalina.sh run
