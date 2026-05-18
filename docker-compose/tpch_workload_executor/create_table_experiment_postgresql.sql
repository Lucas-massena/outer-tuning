CREATE SCHEMA agent;

-- DROP TABLE agent.experiment;

CREATE TABLE agent.experiment
(
  query_name character varying(100),
  mean_cost double precision,
  experiment_number integer,
  id serial NOT NULL,
  date_experiment timestamp without time zone,
  test_name character varying(200),
  parameters text,
  CONSTRAINT experiment_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);