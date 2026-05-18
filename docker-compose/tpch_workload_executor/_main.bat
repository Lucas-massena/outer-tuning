@echo off
set database=pg_tpch_10gb
set dbms=postgresql
set explain=explain
set mv=1
set repetition=0.25
:loop
set /p database="Database (%database%):"
set /p dbms="DBMS postgresql, sqlserver, oracle, mysql (%dbms%):"
set /p explain="Explain (explain / explain analyse / empty ) (%explain%):"
set /p repetition="Repetition (0.00 / 0.05 / 0.10 / 0.25 / 0.50) (%repetition%):"
set /p mv="MV (1/0 ) (%mv%):"
python main.py %database% %dbms% "%explain%" %mv% %repetition%
pause
goto loop