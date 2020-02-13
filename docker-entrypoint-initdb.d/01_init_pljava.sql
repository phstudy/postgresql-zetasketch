SET pljava.libjvm_location TO '/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/amd64/server/libjvm.so';
CREATE SCHEMA hll_count;
ALTER DATABASE postgres SET pljava.libjvm_location FROM CURRENT;
ALTER USER postgres SET SEARCH_PATH TO public,sqlj,hll_count;
CREATE EXTENSION pljava;
SELECT sqlj.install_jar('file:///postgresql-zetasketch/target/postgresql-zetasketch-1.0.jar', 'zetasketch', true);
SHOW search_path;
SELECT sqlj.get_classpath('hll_count');
SELECT sqlj.set_classpath('hll_count', 'zetasketch');
SELECT sqlj.get_classpath('hll_count');

-- BYTES HLL_COUNT.INIT(INT64   input [, precision])
-- BYTES HLL_COUNT.INIT(NUMERIC input [, precision])
-- BYTES HLL_COUNT.INIT(STRING  input [, precision])
-- BYTES HLL_COUNT.INIT(BYTES   input [, precision])
-- https://cloud.google.com/bigquery/docs/reference/standard-sql/hll_functions#hll_countinit
SELECT HLL_COUNT.MERGE_PARTIAL(respondents_hll) AS num_respondents, flavor
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

-- INT64 HLL_COUNT.MERGE(BYTES sketch)
-- https://cloud.google.com/bigquery/docs/reference/standard-sql/hll_functions#hll_countmerge
SELECT HLL_COUNT.MERGE(respondents_hll) AS num_respondents, flavor
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

-- BYTES HLL_COUNT.MERGE_PARTIAL(BYTES sketch)
-- https://cloud.google.com/bigquery/docs/reference/standard-sql/hll_functions#hll_countmerge_partial
SELECT HLL_COUNT.MERGE_PARTIAL(respondents_hll) AS num_respondents, flavor
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

-- INT64 HLL_COUNT.EXTRACT(BYTES sketch)
-- https://cloud.google.com/bigquery/docs/reference/standard-sql/hll_functions#hll_countextract
SELECT
  flavor,
  country,
  HLL_COUNT.EXTRACT(respondents_hll) AS num_respondents
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
  GROUP BY flavor, country) as bar;
