#!/bin/bash
echo '##### USER: (root) ##### PASS: ('$MYSQL_ROOT_PASSWORD') ##### DB: ('$MYSQL_DATABASE')';

mysql -uroot -p$MYSQL_ROOT_PASSWORD --execute="SET GLOBAL log_output = 'TABLE'"
mysql -uroot -p$MYSQL_ROOT_PASSWORD --execute="SET GLOBAL general_log = 'ON'"
mysql -uroot -p$MYSQL_ROOT_PASSWORD --execute="SET GLOBAL event_scheduler = ON;"

mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS NATION";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE NATION  (   N_NATIONKEY  INTEGER NOT NULL,
                                    N_NAME       CHAR(25) NOT NULL,
                                    N_REGIONKEY  INTEGER NOT NULL,
                                    N_COMMENT    VARCHAR(152)   )";
mysql -uroot -p$MYSQL_ROOT_PASSWORD mysql --execute="LOAD DATA INFILE '/opt/nation.tbl' INTO TABLE NATION FIELDS TERMINATED BY '|'";


mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS REGION";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE REGION  (   R_REGIONKEY  INTEGER NOT NULL,
                                    R_NAME       CHAR(25) NOT NULL,
                                    R_COMMENT    VARCHAR(152)   )";

mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="LOAD DATA INFILE '/opt/region.tbl' INTO TABLE REGION FIELDS TERMINATED BY '|'";


mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS PART";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE PART  ( P_PARTKEY     INTEGER NOT NULL,
                                P_NAME        VARCHAR(55) NOT NULL,
                                P_MFGR        CHAR(25) NOT NULL,
                                P_BRAND       CHAR(10) NOT NULL,
                                P_TYPE        VARCHAR(25) NOT NULL,
                                P_SIZE        INTEGER NOT NULL,
                                P_CONTAINER   CHAR(10) NOT NULL,
                                P_RETAILPRICE DECIMAL(15,2) NOT NULL,
                                P_COMMENT     VARCHAR(23) NOT NULL  )";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="LOAD DATA INFILE '/opt/part.tbl' INTO TABLE PART FIELDS TERMINATED BY '|'";


mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS SUPPLIER";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE SUPPLIER (  S_SUPPKEY     INTEGER NOT NULL,
                                    S_NAME        CHAR(25) NOT NULL,
                                    S_ADDRESS     VARCHAR(40) NOT NULL,
                                    S_NATIONKEY   INTEGER NOT NULL,
                                    S_PHONE       CHAR(15) NOT NULL,
                                    S_ACCTBAL     DECIMAL(15,2) NOT NULL,
                                    S_COMMENT     VARCHAR(101) NOT NULL )";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="LOAD DATA INFILE '/opt/supplier.tbl' INTO TABLE SUPPLIER FIELDS TERMINATED BY '|'";


mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS PARTSUPP";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE PARTSUPP (  PS_PARTKEY     INTEGER NOT NULL,
                                    PS_SUPPKEY     INTEGER NOT NULL,
                                    PS_AVAILQTY    INTEGER NOT NULL,
                                    PS_SUPPLYCOST  DECIMAL(15,2)  NOT NULL,
                                    PS_COMMENT     VARCHAR(199) NOT NULL )";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="LOAD DATA INFILE '/opt/partsupp.tbl' INTO TABLE PARTSUPP FIELDS TERMINATED BY '|'";


mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS CUSTOMER";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE CUSTOMER (  C_CUSTKEY     INTEGER NOT NULL,
                                    C_NAME        VARCHAR(25) NOT NULL,
                                    C_ADDRESS     VARCHAR(40) NOT NULL,
                                    C_NATIONKEY   INTEGER NOT NULL,
                                    C_PHONE       CHAR(15) NOT NULL,
                                    C_ACCTBAL     DECIMAL(15,2)   NOT NULL,
                                    C_MKTSEGMENT  CHAR(10) NOT NULL,
                                    C_COMMENT     VARCHAR(117) NOT NULL )";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="LOAD DATA INFILE '/opt/customer.tbl' INTO TABLE CUSTOMER FIELDS TERMINATED BY '|'";


mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS ORDERS";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE ORDERS  (   O_ORDERKEY       INTEGER NOT NULL,
                                    O_CUSTKEY        INTEGER NOT NULL,
                                    O_ORDERSTATUS    CHAR(1) NOT NULL,
                                    O_TOTALPRICE     DECIMAL(15,2) NOT NULL,
                                    O_ORDERDATE      DATE NOT NULL,
                                    O_ORDERPRIORITY  CHAR(15) NOT NULL,  
                                    O_CLERK          CHAR(15) NOT NULL, 
                                    O_SHIPPRIORITY   INTEGER NOT NULL,
                                    O_COMMENT        VARCHAR(79) NOT NULL   )";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="LOAD DATA INFILE '/opt/orders.tbl' INTO TABLE ORDERS FIELDS TERMINATED BY '|'";


mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS LINEITEM";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE LINEITEM (  L_ORDERKEY    INTEGER NOT NULL,
                                    L_PARTKEY     INTEGER NOT NULL,
                                    L_SUPPKEY     INTEGER NOT NULL,
                                    L_LINENUMBER  INTEGER NOT NULL,
                                    L_QUANTITY    DECIMAL(15,2) NOT NULL,
                                    L_EXTENDEDPRICE  DECIMAL(15,2) NOT NULL,
                                    L_DISCOUNT    DECIMAL(15,2) NOT NULL,
                                    L_TAX         DECIMAL(15,2) NOT NULL,
                                    L_RETURNFLAG  CHAR(1) NOT NULL,
                                    L_LINESTATUS  CHAR(1) NOT NULL,
                                    L_SHIPDATE    DATE NOT NULL,
                                    L_COMMITDATE  DATE NOT NULL,
                                    L_RECEIPTDATE DATE NOT NULL,
                                    L_SHIPINSTRUCT CHAR(25) NOT NULL,
                                    L_SHIPMODE     CHAR(10) NOT NULL,
                                    L_COMMENT      VARCHAR(44) NOT NULL )";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="LOAD DATA INFILE '/opt/lineitem.tbl' INTO TABLE LINEITEM FIELDS TERMINATED BY '|'";

mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP TABLE IF EXISTS matviews";
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="CREATE TABLE matviews (
                        mv varchar(64) NOT NULL PRIMARY KEY ,
                        view varchar(64) NOT NULL ,
                        last_refresh TIMESTAMP  ,
                        refresh_time INTEGER )";

mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP procedure IF EXISTS create_matview;"
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="DELIMITER //

CREATE procedure create_matview(matview varchar(64), view_name varchar(64)) NOT DETERMINISTIC
BEGIN
    DECLARE EXIT HANDLER FOR SQLSTATE '42000'
        SELECT 'ERROR! a matview or table with that name exists. will not proceed' as ERR;
    DECLARE EXIT HANDLER FOR SQLSTATE '23000'
        SELECT 'ERROR! a matview with that already name exists. will not proceed' as ERR;
    IF (select count(*) from information_schema.tables where table_schema in (select database()) and table_name = matview) THEN
        call raise_error;
    END IF;
    SET @time_start = CURRENT_TIMESTAMP();
    SET @crtbl = CONCAT('CREATE TABLE ', matview, ' as select * from ', view_name);
    prepare stmt from @crtbl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SET @time_stop = CURRENT_TIMESTAMP();
    SELECT @time_start as Refresh_start, @time_stop as Refresh_end;
    INSERT INTO matviews (mv, view, last_refresh, refresh_time)
    VALUES (matview, view_name, @time_start, UNIX_TIMESTAMP(@time_stop) - UNIX_TIMESTAMP(@time_start));
END //

DELIMITER ;"

mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP procedure IF EXISTS refresh_matview;"
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="DELIMITER //

CREATE procedure refresh_matview(matview varchar(64)) NOT DETERMINISTIC
BEGIN
    DECLARE EXIT HANDLER FOR SQLSTATE '42000'
        SELECT 'ERROR! a matview with that name does not exist, will not proceed' as ERR;
    IF (select count(*) != 1 from matviews where mv = matview) THEN
        call raise_error;
    END IF;
    START TRANSACTION;
    SET @time_start = CURRENT_TIMESTAMP();
    SET @v_name = (SELECT view from matviews where mv = matview limit 1);
    SET @crtbl = CONCAT('DELETE FROM ', matview);
    prepare stmt from @crtbl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SET @crtbl = CONCAT('INSERT INTO ', matview, ' SELECT * FROM ', @v_name);
    prepare stmt from @crtbl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SET @time_stop = CURRENT_TIMESTAMP();
    SELECT @time_start as Refresh_start, @time_stop as Refresh_end;
    update matviews
    set last_refresh = @time_start,
        refresh_time=UNIX_TIMESTAMP(@time_stop) - UNIX_TIMESTAMP(@time_start)
    where mv = matview;
    COMMIT;
END //

DELIMITER ;"

mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP procedure IF EXISTS drop_matview;"
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="DELIMITER //

CREATE procedure drop_matview(matview varchar(64)) NOT DETERMINISTIC
BEGIN
    DECLARE EXIT HANDLER FOR SQLSTATE '42000'
        SELECT 'ERROR! a matview with that name does not exist, will not proceed' as ERR;
    SET AUTOCOMMIT = 0;
    IF (select count(*) != 1 from matviews where mv = matview) THEN
        call raise_error;
    END IF;
    START TRANSACTION;
    SET @crtbl = CONCAT('drop table ', matview);
    prepare stmt from @crtbl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    delete from matviews where mv = matview;
    COMMIT;
END //

DELIMITER ;"

mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE --execute="DROP EVENT IF EXISTS matview_event;"
mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE \
--execute="DELIMITER //
CREATE EVENT matview_event
ON SCHEDULE EVERY 5 MINUTE
DO
BEGIN
    DECLARE finished INTEGER DEFAULT 0;
    DECLARE mvName VARCHAR(200);

    DECLARE curMV CURSOR FOR (select mv from matviews);

    DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET finished = 1;

    OPEN curMV;
    getMVName: LOOP
        FETCH  curMV into mvName;
        IF finished = 1 THEN
            LEAVE getMVName;
        end if;
        SET @mv = CONCAT(mvName);
        call refresh_matview(@mv);
    end loop;
    CLOSE curMV;
END //

DELIMITER ;"