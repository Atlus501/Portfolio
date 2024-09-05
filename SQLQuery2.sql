--USE children;

/*
CREATE TABLE children_info(
id int NOT NULL IDENTITY(1,1),
name VARCHAR(50),
age int,
PRIMARY KEY (id)
)
;
*/
/*
ALTER TABLE [dbo].[children_info]
ADD PRIMARY KEY (id)

ALTER TABLE [dbo].[children_info]
 ADD dog_person bit;
 
INSERT INTO dbo.children_info(name, age)
 VALUES ('Bob', 10),
  ('Nate', 9),
  ('Daisy', 11),
  ('March', 14),
  ('Eve', 6),
  ('Sammy', 8)
  ;
  */

  /*
 UPDATE dbo.children_info
   SET dog_person = 1 WHERE id % 2 = 0;

UPDATE dbo.children_info
   SET dog_person = 0 
   WHERE id % 2 != 0;
   */

   /*
ALTER TABLE dbo.children_info
ADD parent_id int;
*/

/*
UPDATE dbo.children_info 
SET parent_id = 2 WHERE id BETWEEN 4 AND 5;
*/

--SET parent_id = 4 WHERE id <= 2;

--CREATE TABLE parent(id int NOT NULL identity(1,1), name VARCHAR(50), PRIMARY KEY (id));

/*
INSERT INTO dbo.parent(name)
VALUES('Jessica'),
('Lilith'),
('Max'),
('Long'),
('Elsie')
;
*/

/*
 ALTER TABLE dbo.children_info
  ADD FOREIGN KEY (parent_id) REFERENCES dbo.parent(id);
  */

 --DELETE FROM dbo.parent WHERE id > 5;

--DELETE FROM dbo.children_info WHERE id > 6

SELECT * FROM dbo.children_info AS a LEFT JOIN dbo.parent AS b ON a.parent_id = b.id;

--Calculates the average age of dog people vs not dog people
--SELECT dog_person AS 'Is Dog Person', AVG(age) AS 'Average Age' FROM dbo.children_info GROUP BY dog_person;


