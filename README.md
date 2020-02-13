# PostgreSQL-ZetaSketch
PostgreSQL-ZetaSketch brings [Google BigQuery HLL++ compatible functions](https://cloud.google.com/bigquery/docs/reference/standard-sql/hll_functions) to PostgreSQL.

## Run in Docker
```bash
docker build . -t phstudy/postgresql-zetasketch
docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d phstudy/postgresql-zetasketch
```



## Test HLL++ functions in Docker

### Example
```bash
docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d phstudy/postgresql-zetasketch

docker exec -it some-postgres psql -U postgres

postgres=# SELECT HLL_COUNT.MERGE(respondents_hll) AS num_respondents, flavor
FROM (
  SELECT
    HLL_COUNT.INIT(respondent) AS respondents_hll,
    flavor,
    country
  FROM (
    SELECT * FROM (
      VALUES (1, 'Vanilla', 'CH'),
             (1, 'Chocolate', 'CH'),
             (2, 'Chocolate', 'US'),
             (2, 'Strawberry', 'US')) AS t(respondent, flavor, country)) as foo
  GROUP BY flavor, country) as bar
GROUP BY flavor;
```


### Result

```
 num_respondents |   flavor   
-----------------+------------
               1 | Strawberry
               1 | Vanilla
               2 | Chocolate
(3 rows)
```



## How to build

### Install dependencies
```bash
$ git clone https://github.com/tada/pljava.git
$ cd pljava
$ git checkout tags/V1_5_5
$ mvn -Pwnosign clean install
$ cd ..
```

### Build postgresql-zetasketch jar
```
$ git clone https://github.com/phstudy/postgresql-zetasketch.git
$ mvn -f postgresql-zetasketch/pom.xml clean package
```