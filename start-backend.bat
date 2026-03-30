@echo off
setlocal enabledelayedexpansion

echo Loading .env...
for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0.env") do (
    set "line=%%a"
    if not "!line:~0,1!"=="#" if not "%%a"=="" (
        set "%%a=%%b"
    )
)

echo Starting Quarkus backend...
cd /d "%~dp0backend\parcel-sorter"
mvn quarkus:dev -DOPENROUTER_API_KEY="%OPENROUTER_API_KEY%" -DOPENROUTER_MODEL="%OPENROUTER_MODEL%" -DSUPABASE_DB_URL="%SUPABASE_DB_URL%" -DSUPABASE_DB_USER="%SUPABASE_DB_USER%" -DSUPABASE_DB_PASSWORD="%SUPABASE_DB_PASSWORD%" -DKAFKA_BOOTSTRAP_SERVERS="%KAFKA_BOOTSTRAP_SERVERS%"
