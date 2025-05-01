#!/bin/sh
echo "⏳ Esperando a que el config-server esté activo..."
sleep 15
exec java -jar app.jar
