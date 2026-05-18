dbset db pg
dbset bm TPC-C

diset connection pg_host postgres
diset connection pg_port 5432

diset tpcc pg_superuser postgres
diset tpcc pg_superuserpass postgres
diset tpcc pg_defaultdbase postgres
diset tpcc pg_user tpcc
diset tpcc pg_pass tpcc
diset tpcc pg_dbase tpcc
diset tpcc pg_count_ware 10
diset tpcc pg_raiseerror false
diset tpcc pg_keyandthink false
diset tpcc pg_allwarehouse true
diset tpcc pg_timeprofile false

loadscript
vuset vu 4
vucreate
vurun
vuwait 3600
vudestroy
puts "TPC-C run cycle complete"
