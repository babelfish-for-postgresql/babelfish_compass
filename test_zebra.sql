CREATE TABLE test_1 (col_char char(10) default 'abc')
go

INSERT INTO test_1 DEFAULT VALUES;
go

select * from test_1
go

-- declare statement
DECLARE @table_var TABLE (col_char CHAR(10) default 'abc')
INSERT INTO @table_var  DEFAULT VALUES;
go
select * from @table_var
go

-- first result goes in table, not really the usecase?
DECLARE @table_var TABLE (col_char CHAR(10));
INSERT INTO @table_var SELECT col_char FROM test_1;
go

SELECT * from  @table_var;
go

-- TEST 2
-- DEFAULT VALUES OUTPUT 
CREATE SCHEMA ADT;
GO

CREATE TABLE ADT.PatientSilo
(
    PatientSiloKey UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    CreatedDate DATETIME DEFAULT GETDATE(),
    IsActive BIT DEFAULT 1,
    PatientName NVARCHAR(100) NULL,
    Status NVARCHAR(50) DEFAULT 'Pending'
);
GO

DECLARE @TempGUIDs TABLE (PatientSiloKey UNIQUEIDENTIFIER);

INSERT INTO ADT.PatientSilo
    OUTPUT INSERTED.PatientSiloKey INTO @TempGUIDs 
    DEFAULT VALUES;
GO

SELECT * FROM @TempGUIDs;
GO