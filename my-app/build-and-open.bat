@echo off
cd /d "%~dp0"
call npm install
call npm run build
start "" http://localhost:4173/
npm run preview
