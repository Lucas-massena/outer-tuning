FROM python:3.7
ADD *.py /
RUN mkdir -p queries
ADD queries/ /queries
ADD requirements.txt /
ADD create_table_experiment_mysql.sql /

ADD mysql_docker.config /
RUN mv mysql_docker.config mysql.config
RUN mkdir -p output
RUN pip install -r requirements.txt

CMD [ "python", "./main.py", "mysql", "mysql", "EXPLAIN", "0.25" ]