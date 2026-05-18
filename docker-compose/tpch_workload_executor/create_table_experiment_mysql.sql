CREATE TABLE experiment(
   id MEDIUMINT NOT NULL AUTO_INCREMENT,
   query_name varchar(100),
   mean_cost DECIMAL(10,2),
   experiment_number integer,
   date_experiment DATE,
   test_name varchar(200),
   parameters varchar(2000),
   PRIMARY KEY (id)
);