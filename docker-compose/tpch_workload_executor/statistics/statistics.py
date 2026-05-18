import os
import re
from pprint import pprint
import pandas as pd
import sys
from random import shuffle
from pprint import pprint
from os import listdir
from os.path import isfile, join
import re
import numpy as np
import time
from decimal import Decimal
import json
import pandas.io.sql as pdsql
import re
import datetime
import pymssql
import psycopg2
import pandas as pd
import matplotlib.pyplot as plt
import csv
import cx_Oracle


def get_file_safe_to_write(filename, operation_type):
	i = 0
	while i < 60:
		i += 1
		try:
			file = open(filename, operation_type, encoding="utf8")
			return file
		except Exception as e:
			time.sleep(1)
			print('Error <get_file_safe_to_write>: '+str(e))
		print(filename + ' open try '+str(i))
	print('File error: '+filename)
	return None

def get_db_config(database):
	file = get_file_safe_to_write(database+".config", 'r')
	data = json.load(file)
	file.close()
	return data


def postgresql_connection(config):
	try:
		conn = psycopg2.connect("dbname='"+config['database']+"' user='"+config['user']+"' host='"+config['host']+"' password='"+config['password']+"'")
		conn.autocommit = True
		print('Connected database '+config['database']+': '+str(config))
		return conn
	except Exception as e:
		print('Error <postgresql_connection>: ', config)
		print('Error <postgresql_connection>: '+str(e))
		exit()

def sqlserver_connection(config):
	try:
		conn = pymssql.connect(config['host'], config['user'], config['password'], config['database'])
		print('Connected database '+config['database']+': '+str(config))
		return conn
	except Exception as e:
		print('Error <postgresql_connection>: ', config)
		print('Error <postgresql_connection>: '+str(e))
		exit()

def oracle_connection(config):
	try:
		conn = cx_Oracle.connect(config['user'],config['password'], config['host']+'/'+config['tna'])
		print('Connected database '+config['database']+': '+str(config))
		return conn
	except Exception as e:
		print('Error <oracle_connection>: ', config)
		print('Error <oracle_connection>: '+str(e))
		exit()

def extract_tna_names(ddl):
	tna_list = re.findall(r'\S+_tap_\S+', str(ddl.lower()))
	if len(tna_list) == 0:
		tna_list = re.findall(r'\S+_dexter_\S+', str(ddl.lower()))
	if len(tna_list) == 0:
		tna_list = re.findall(r'\S+_powa_\S+', str(ddl.lower()))

	for i in range(0, len(tna_list)):
		tna_list[i] = tna_list[i].split('.')[0]
	output = []
	for tna in tna_list:
		tna = tna.replace('[', '').replace(']', '').replace("'", '')
		output.append(tna)
	return output

def get_distinct_prefix(tna_list, prefix):
	output = []
	print(prefix)
	print(len(tna_list))
	for tna in tna_list:
		tna = tna.replace('[', '').replace("'", '')
		if tna.lower()[:len(prefix)] in prefix.lower():
			output.append(tna)
	return output

def get_match(tna_list, tna_target):
	output = []
	for tna in tna_list:
		if tna_target.lower() == tna:
			output.append(tna)
	return output


def get_shared_by_family(tna_list, queries, prefix):
	tnas = set(get_distinct_prefix(tna_list, prefix))
	shared_count = 0
	for tna in tnas:
		count = 0
		for items in queries:
			hits = get_match(items, tna)
			if len(hits) > 1:
				count = count + 1
		if count > 1:
			shared_count = shared_count + 1
	return shared_count

def get_shared_by_query(all_tna, prefix):
	tnas = set(get_distinct_prefix(all_tna, prefix))
	count = 0
	for tna in tnas:
		hits = get_match(all_tna, tna)
		if len(hits) > 1:
			count = count + 1
	print(count)
	return count

def db_connection(database):
	config = get_db_config(database)
	if config['dbms'] in 'oracle':
		return oracle_connection(config)
	if config['dbms'] in 'postgresql':
		return postgresql_connection(config)
	if config['dbms'] in 'sqlserver':
		return sqlserver_connection(config)


def get_cost(tna):
	cost = 0
	conn = db_connection(params_inn['database'])
	if conn:
		cursor = conn.cursor()
		query = "select tna_profit from agent.tb_tuning_action where tna_ddl_execute ilike '%"+tna+"%'"
		print(query)
		cursor.execute(query)
		for row in cursor:
			if cost == 0:
				cost = row[0]
		cursor.close()
	return cost


def process_cost_by_query(tna_by_query):
	sum_costs = {}
	for key, tnas in tna_by_query.items():
		sum_costs[key] = 0
		for item in tnas:
#			if 'id_' in item:
			if '_tap_' in item:
				sum_costs[key] = sum_costs[key]+get_cost(item)
	stats = []
	keys = []
	for key, value in sum_costs.items():
		keys.append(key)
		stats.append(value)
	sum_costs = pd.DataFrame([stats], columns=keys)
	sum_costs.to_csv('sum_costs.csv')


params_inn = {}
params_inn['database'] = 'pg_tpch_10gb'


path = '.'
files = [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f)) and '.txt' in f]
queries = []
tna_by_query = {}
all_tna = []
for filename in files:
	with open(filename, 'r') as f:
		lines = f.read()
	query = filename.replace('tna_', '').replace('.txt', '')
	tnas = extract_tna_names(lines)
	tna_by_query[query] = tnas
	queries.append(tnas)
	all_tna = all_tna + tnas
process_cost_by_query(tna_by_query)

stats = []
stats.append(len(set(get_distinct_prefix(all_tna, 'mv_'))))
stats.append(len(set(get_distinct_prefix(all_tna, 'id_'))))
stats.append(len(set(get_distinct_prefix(all_tna, 'pid_'))))

stats.append(len(get_distinct_prefix(all_tna, 'mv_')))
stats.append(len(get_distinct_prefix(all_tna, 'id_')))
stats.append(len(get_distinct_prefix(all_tna, 'pid_')))

shared_mv_by_family = get_shared_by_family(all_tna, queries, 'mv_')
shared_id_by_family = get_shared_by_family(all_tna, queries, 'id_')
shared_pid_by_family = get_shared_by_family(all_tna, queries, 'pid_')

stats.append(shared_mv_by_family)
stats.append(shared_id_by_family)
stats.append(shared_pid_by_family)


shared_mv_by_query = get_shared_by_query(all_tna, 'mv_')
shared_id_by_query = get_shared_by_query(all_tna, 'id_')
shared_pid_by_query = get_shared_by_query(all_tna, 'pid_')

stats.append(shared_mv_by_query)
stats.append(shared_id_by_query)
stats.append(shared_pid_by_query)


title = ['mv_count', 'id_count', 'pid_count', 'mv_hits', 'id_hits', 'pid_hits', 'shared_mv_by_family', 'shared_id_by_family', 'shared_pid_by_family', 'shared_mv_by_query', 'shared_id_by_query', 'shared_pid_by_query']
data = pd.DataFrame([stats], columns=title)
data.to_csv('total_tna_hits_count.csv')
print(title)
print(stats)
