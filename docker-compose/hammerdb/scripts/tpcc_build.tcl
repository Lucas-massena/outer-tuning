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
diset tpcc pg_num_vu 4
diset tpcc pg_allwarehouse true
diset tpcc pg_timeprofile false
diset tpcc pg_storedprocs false

print dict
buildschema
waittocomplete
puts "TPC-C build complete"
