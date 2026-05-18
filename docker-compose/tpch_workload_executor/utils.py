import multiprocessing
import pandas.io.sql as pdsql
import datetime as dt
import time
import os
import csv
import psycopg2

def params_inn_to_str(params_inn):
	output = ''
	for key, value in params_inn.items():
		if 'conn' not in key and 'file' not in key:
			output += '_'+str(value)
	print(output)
	return output

def get_file_safe_to_write(filename, operation_type):
	i = 0
	while i < 60:
		i += 1
		try:
			file = open(filename, operation_type, encoding="utf8")
			return file
		except Exception as e:
			time.sleep(1)
			print(str(e))
		print(filename + ' open try '+str(i))
	print('File error: '+filename)
	return None


def read_ddl_file(filename, lower = False):
	ouput = ''
	with open(filename, 'r') as f:
		lines = f.readlines()
	for line in lines:
		if len(line.rstrip()) > 0 and '--' not in line:
			ouput += line
	return [x.rstrip() for x in ouput.split(';') if len(x.rstrip()) > 0]

def write_list_file(data, filename):
	line = ''
	for value in data:
		if line:
			line += ','
		line += str(value)
	with get_file_safe_to_write(filename, 'a+') as f:
		f.write(line+'\n')

def write_log_file(data, filename):
	with get_file_safe_to_write(filename, 'a+') as f:
		f.write(data+'\n')

def get_thread_number():
	return multiprocessing.cpu_count() - 1

