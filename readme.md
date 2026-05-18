# Article

https://sol.sbc.org.br/journals/index.php/jidm/article/view/1984

# Problema com docker

```
docker system prune -a
```

# Executando o projeto

### Para buildar o projeto

```
make all
```

### Para rodar os projetos

```
make deploy
```

### Parar os projetos

```
make stop
```

### Acessar o projeto

```
http://localhost/
```

# Mysql

O docker-compose ja esta sendo criando e populando as tabelas:

| Tables_in_mysql |
| --------------- |
| customer |
| lineitem |
| nation |
| orders |
| part |
| partsupp |
| region |
| supplier |

### Docker-compose

```yml
services:

  mysql:
    image: mysql:8.0.19
    command: --default-authentication-plugin=mysql_native_password --secure-file-priv=/opt --lower-case-table-names=1
    restart: always
    ports:
      - 3306:3306
      - 33060:33060
    volumes:
      - ./tpch_load_data_test:/opt
      - ./tpch_load_data_test/load.sh:/docker-entrypoint-initdb.d/load.sh
    environment:
      MYSQL_ROOT_PASSWORD: example
      MYSQL_DATABASE: mysql
    security_opt:
      - seccomp:unconfined

  workload:
    image: workload-executor
    restart: on-failure
    build:
      context: ./tpch_workload_executor
      dockerfile: ./Dockerfile
    depends_on:
      - mysql
    links:
      - mysql

  outertuning:
    image: outertuning
    restart: always
    depends_on:
      - mysql
    links:
      - mysql
    ports:
      - 80:8080
```

`running: docker-compose -f docker-compose.yml up`

### Login banco

`mysql -uroot -pexample mysql`
