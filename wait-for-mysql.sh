#!/bin/sh
set -e

host="$1"
port="$2"

echo "Esperando que $host:$port esté disponible..."

while ! nc -z "$host" "$port"; do
  sleep 2
done

echo "$host:$port disponible, arrancando aplicación..."

exec java -jar app.jar
